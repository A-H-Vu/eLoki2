package scraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import clients.SeleniumClient;

public class SeleniumScraper {
	private SeleniumClient client;
	private File dest;
	private int maxDepth = Integer.MAX_VALUE;
	private int timeout = 1000;
	
	private Set<String> seen;
	private Set<String> visited;
	private Queue<QueueURL> urls = new ArrayDeque<QueueURL>();
	private String baseUrl;
	
	public SeleniumScraper(SeleniumClient client) {
		this.client = client;
	}
	
	/**
	 * Sets the file to write the scraped urls to.
	 * 
	 * @param dest File to write output
	 */
	public void setDest(String dest) {
		this.dest = new File(dest);
	}
	
	/**
	 * Sets the depth(the number of urls from the given page)
	 * in which the scraper will scrape the site.
	 * 
	 * A depth of 0 will only return the url itself, validating it.
	 * A depth of 1 means that it will only scrape the urls on the page given.
	 * 
	 * @param maxDepth maximum depth to scrape, negative numbers will set it to 0
	 */
	public void setMaxDepth(int maxDepth) {
		if(maxDepth<-1) {
			maxDepth = 0;
		}
		this.maxDepth = maxDepth;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public void scrapeSite(String url) throws IOException {
		BufferedWriter bWriter = Files.newBufferedWriter(dest.toPath(), 
				StandardOpenOption.CREATE, 
				StandardOpenOption.TRUNCATE_EXISTING, 
				StandardOpenOption.WRITE);
		PrintWriter out = new PrintWriter(bWriter);
		
		baseUrl = url;
		
		//Some checks to make sure that the given string is a proper url
		try{
			new URL(baseUrl);
			
		}catch(MalformedURLException e){
			try{
				//assume most sites will auto upgrade an http connection to https
				baseUrl = new URL("http://"+baseUrl).toString();
			}catch(MalformedURLException e1){
				System.err.println(e1.getMessage());
				//TODO better error handling
				System.exit(1);
			}
		}
		seen = new TreeSet<String>();
		visited = new TreeSet<String>();
		urls.offer(new QueueURL(baseUrl,0));
		
		baseUrl = validateURL(baseUrl);
		//Gets the base folder path of the url, the scraper will only look for links under that pattern
		int endSlash = baseUrl.lastIndexOf('/');
		if(endSlash>7){
			baseUrl = baseUrl.substring(0, endSlash);
		}
		System.out.println(baseUrl);
		QueueURL u;
		while((u=urls.poll())!=null){
			try{
				parseUrls(u);
			}
			catch(UnsupportedMimeTypeException e){
				System.err.println("Not an html page, skipping "+u);
			}
			catch(HttpStatusException e){
				System.err.println("Error fetching, status="+e.getStatusCode()+" skipping "+u);
			}
			catch(Exception e){
				e.printStackTrace();
				System.err.println("Error scraping "+u);
			}
		}
		
		for(String ul : visited){
			out.write(ul+"\n");
		}
		out.close();
		client.close();
		
	}
	public String validateURL(String URL) throws IOException{
		client.getWebDriver().get(URL);
		client.awaitPageLoad(10000);
		return client.getWebDriver().getCurrentUrl();
	}
	//A copy of URL.toString() without the query and reference parameters
	private String printURL(URL u){
		StringBuffer result = new StringBuffer();
		result.append(u.getProtocol());
		result.append(":");
		if (u.getAuthority() != null && u.getAuthority().length() > 0) {
			result.append("//");
			result.append(u.getAuthority());
		}
		if (u.getPath() != null) {
			result.append(u.getPath());
		}
		return result.toString();
	}
	private void parseUrls(QueueURL url) throws IOException{
		client.getWebDriver().get(url.url);
		try {
			//another sleep to try and wait through strange redirects not done using code 302 re:rbc.com
			Thread.sleep(1000);
		} catch (InterruptedException e1) {}
		client.awaitPageLoad(10000);
		String finalURL = printURL(new URL(client.getWebDriver().getCurrentUrl()));
		if(!finalURL.startsWith(baseUrl)) return;
		if(finalURL.equals(baseUrl)){
			finalURL = finalURL+"/";
		}
		System.out.println(finalURL);
		visited.add(finalURL);
		//skip scraping for more urls if it's at the max depth
		if(url.depth>=maxDepth) return;
		RemoteWebElement body = (RemoteWebElement)client.getJSExecutor().executeScript("return document.body; ");
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e1) {}
		if(body==null) {
			System.err.println("Null body on page "+finalURL);
			return;
		}
		for(WebElement e:body.findElementsByCssSelector("a[href]")){
			String u = e.getAttribute("href");
			try {
				new URL(u);
			}catch(MalformedURLException e1) {
				try {
					u = new URL(finalURL+"/"+u).toString();
				}catch(MalformedURLException e2) {
					continue;
				}
			}
			if(u.startsWith(baseUrl)&&!seen.contains(u)){
				seen.add(u);
				urls.offer(new QueueURL(u, url.depth+1));
			}
		}
	}
	private static class QueueURL{
		public String url;
		public int depth;
		public QueueURL(String url, int depth){
			this.url = url;
			this.depth = depth;
		}
		public String toString(){
			return url;
		}
	}

}
