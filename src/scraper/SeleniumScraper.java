package scraper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
	
	/**
	 * The set of all urls seen in their raw form
	 */
	private Set<String> seen;
	/**
	 * The set of all urls visited/found by the scraper, after normalizing by stripping the query and reference portions of the url
	 */
	private Set<String> visited;
	private Queue<QueueURL> urls = new ArrayDeque<QueueURL>();
	/**
	 * The baseUrl which is the original page given to scrape with only the directory portion i.e. www.xyz.com/index.html -> www.xyz.com/
	 */
	private String baseUrl;
	
	/**
	 * List of acceptable url prefixes, by default the only acceptable prefix baseUrl given
	 */
	private String[] prefixes;
	
	/**
	 * Initializes a new scraper with the given Selenium Client as the browser to use
	 * @param client
	 */
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
	
	/**
	 * Sets the time to wait between each page visit
	 * @param timeout time in milliseconds to wait between page loads
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * Sets the prefixes to filter acceptable urls to visit
	 * @param prefixes string or urls, must be valid according to java's {@link URL} class 
	 */
	public void setPrefixes(String[] prefixes) {
		this.prefixes = prefixes;
		for(int i = 0; i<prefixes.length; i++) {
			try {
				URL url = new URL(prefixes[i]);
				URI uri = new URI(url.getProtocol(), url.getUserInfo(), IDN.toASCII(url.getHost()), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
				this.prefixes[i] = uri.toASCIIString();
			} catch (MalformedURLException | URISyntaxException e) {}
		}
	}
	/**
	 * Main entry point to the scraper, actually scrapes the site.
	 * Use {@link #setDest(String)}, {@link #setPrefixes(String[])}, {@link #setTimeout(int)}m {@link #setMaxDepth(int)}
	 * to configure the scraper
	 * @param urlStr Base url to start scraping from
	 * @throws IOException If an error ocurrs while writing
	 */
	public void scrapeSite(String urlStr) throws IOException {
		BufferedWriter bWriter = Files.newBufferedWriter(dest.toPath(), 
				StandardOpenOption.CREATE, 
				StandardOpenOption.TRUNCATE_EXISTING, 
				StandardOpenOption.WRITE);
		PrintWriter out = new PrintWriter(bWriter);
		
		
		baseUrl = urlStr;
		
		//Some checks to make sure that the given string is a proper url
		//also some stuff to make sure the query portion is url encoded
		try{
			URL url = new URL(baseUrl);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), IDN.toASCII(url.getHost()), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			baseUrl = uri.toASCIIString();
		}catch(MalformedURLException | URISyntaxException e){
			try{
				//assume most sites will auto upgrade an http connection to https
				URL url = new URL("http://"+baseUrl);
				URI uri = new URI(url.getProtocol(), url.getUserInfo(), IDN.toASCII(url.getHost()), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
				baseUrl = uri.toASCIIString();
			}catch(MalformedURLException | URISyntaxException e1){
				System.err.println(e1.getMessage());
				//TODO better error handling
				System.exit(1);
			}
		}
		//initialize the sets
		seen = new TreeSet<String>();
		visited = new TreeSet<String>();
		//Start with the base url
		urls.offer(new QueueURL(baseUrl,0));
		
		//Get the actual url of the page, after any redirects etc
		baseUrl = validateURL(baseUrl);
		//Gets the base folder path of the url, the scraper will only look for links under that pattern
		int endSlash = baseUrl.lastIndexOf('/');
		if(endSlash>7){
			baseUrl = baseUrl.substring(0, endSlash);
		}
		System.out.println(baseUrl);
		QueueURL u;
		//Main loop which repeatedly pulls a new url from the queue and visits it
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
	/**
	 * Gets the final url of a given url after redirects
	 * @param URL url to check
	 * @return url after all redirects
	 * @throws IOException If there are any issues with the webdriver or getting the page
	 */
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
	/**
	 * Main function that visits a given page, scrapes all the urls and processes them
	 * @param url Url to visit
	 * @throws IOException
	 */
	private void parseUrls(QueueURL url) throws IOException{
		client.getWebDriver().get(url.url);
		try {
			//another sleep to try and wait through strange redirects not done using code 302 re:rbc.com
			Thread.sleep(1000);
		} catch (InterruptedException e1) {}
		//Wait for the page to fully load
		client.awaitPageLoad(10000);
		//Get the actual url w/o the reference or query portions
		String finalURL = printURL(new URL(client.getWebDriver().getCurrentUrl()));
		//skip page if it is not valid
		if(!checkURL(finalURL)) return;
		//skip page if was already visited/checked
		if(visited.contains(finalURL)) return;
		if(finalURL.equals(baseUrl)){
			finalURL = finalURL+"/";
		}
		System.out.println(finalURL+" "+urls.size()+" left");
		//Add to the visited set which tracks all the urls found by the scraper
		visited.add(finalURL);
		//skip scraping for more urls if it's at the max depth
		if(url.depth>=maxDepth) return;
		//Get the page body
		RemoteWebElement body = (RemoteWebElement)client.getJSExecutor().executeScript("return document.body; ");
		if(body==null) {
			System.err.println("Null body on page "+finalURL);
			return;
		}
		//Find all links on the page using a css selector
		for(WebElement e:body.findElementsByCssSelector("a[href]")){
			String u = e.getAttribute("href");
//			System.out.println("u="+u);
			//quick check to try and resolve relative paths
			try {
				new URL(u);
			}catch(MalformedURLException e1) {
				try {
					u = new URL(finalURL+"/"+u).toString();
				}catch(MalformedURLException e2) {
					continue;
				}
			}
			//Add the url to the queue if it was not seen before
			if(checkURL(u)&&!seen.contains(u)){
				seen.add(u);
				urls.offer(new QueueURL(u, url.depth+1));
			}
		}
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e1) {}
	}
	
	/**
	 * Check that a given url starts with etiher one of the prefixes or the base url
	 * @param url
	 * @return
	 */
	private boolean checkURL(String url) {
//		System.out.println("checking "+url);
		url = URLDecoder.decode(url, StandardCharsets.UTF_8);
		if(prefixes!=null) {
			for(String s:prefixes) {
//				System.out.println("url "+url+" "+s+" "+url.startsWith(s));
				//decode the url to normalize, fixes issues where for some reason parts of the path
				//may be url encoded instead of just the query portion of the url
				if(url.startsWith(URLDecoder.decode(s, StandardCharsets.UTF_8))) {
//					System.out.println(urls);
					return true;
				}
			}
		}
		if(url.startsWith(URLDecoder.decode(baseUrl,StandardCharsets.UTF_8))) return true;
		return false;
		
	}
	/**
	 * Class for the urls in the queue, with a depth parameter to track depth of urls crawled
	 * and stop once the max-depth is reached.
	 * @author Allen
	 *
	 */
	private static class QueueURL{
		public String url;
		//Tracks the depth of the url, or the minimum number of links you need to click to get to the page
		//from the base url page. 0 for the baseUrl
		public int depth;
		//public String prevUrl;
		public QueueURL(String url, int depth){
			this.url = url;
			this.depth = depth;
		}
		public String toString(){
			return url;
		}
	}

}
