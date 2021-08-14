package capture;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Random;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import clients.SeleniumClient;

public class MouseCapture {
	public static void captureRecording(SeleniumClient browser) throws IOException{
		WebDriver webdriver = browser.getWebDriver();
		JavascriptExecutor jsExec = browser.getJSExecutor();
		String js = readResource("/capture/res/mouseCapture.js");
		String mainPage = readResource("/capture/res/mouseCaptureMain.js");
		WebDriverWait wait = new WebDriverWait(webdriver, 100000);
		
		String status = "";
		mainloop: while(true){
			System.out.println("displaying main landing page");
			webdriver.get("about:blank");
			jsExec.executeScript(mainPage, status);

			String currentURL = webdriver.getCurrentUrl();
			while(true){
		          if(!webdriver.getCurrentUrl().equals(currentURL)){
		        	  continue mainloop;
		          }
		          try{
		        	  Boolean val = ((Boolean)((JavascriptExecutor)webdriver).executeScript("return ready;"));
		        	  if(val!=null&&val){
		        		  break;
		        	  }
		          }catch(JavascriptException e){
		        	  //likely due to ready being undefined as it is not on the about:blank page
		        	  continue mainloop;
		          }
		          try {
		        	  Thread.sleep(10);
		          }catch(InterruptedException e1) {}
			}
			String recordURL = ((String)jsExec.executeScript("return document.getElementById('iURL').value;"));
			try {
				webdriver.get(recordURL);
			}catch(WebDriverException e) {
				System.err.println(e.getMessage());
				System.err.println("Error visiting URL "+recordURL);
				status = "Error getting page "+recordURL;
				try {
					//sleep so the user can see the error page before going back to the main page. 
					Thread.sleep(5000);
				}catch(InterruptedException e1) {}
				continue;
			}
			wait.until((ExpectedCondition<Boolean>) wd -> {
		          return ((Boolean)((JavascriptExecutor)wd).executeScript("return document.readyState == 'complete';"));
		    });
			System.out.println("injecting js");
			jsExec.executeScript(js);
			System.out.println("Waiting for state change");
			//Maybe replace with a hot loop to do more things.
			currentURL = webdriver.getCurrentUrl();
			String state;
			while(true){
				state = ((String)((JavascriptExecutor)webdriver).executeScript("if(typeof state !== 'undefined'){console.log(state); return state;}return 'active'"));
				if(!webdriver.getCurrentUrl().equals(currentURL)){
					state = "reset";
					break;
				}
				if(!"active".equals(state)){
					break;
				}
				try {
		        	  Thread.sleep(10);
		        }catch(InterruptedException e1) {}
			}
			if("quit".equals(state)) break;
		}
		browser.close();
		
	}
	public static void captureRecording2(SeleniumClient browser) throws IOException{
		WebDriver webdriver = browser.getWebDriver();
		JavascriptExecutor jsExec = browser.getJSExecutor();
		StringBuilder sb = new StringBuilder();
		WebStorage webStorage = (WebStorage) new Augmenter().augment(webdriver);
		String js = readResource("/capture/res/mouseCapture_session.js");
		String mainPage = readResource("/capture/res/mouseCapture_sessionMain.js");
		webdriver.get("about:blank");
		browser.awaitPageLoad(1000);
		jsExec.executeScript(mainPage);
		mainloop: while(true){
			String tempName = genRandom(10);
			tempName = (String)jsExec.executeScript(js, tempName);
			String currentURL = webdriver.getCurrentUrl();
			while(true){
		          if(!webdriver.getCurrentUrl().equals(currentURL)){
		        	  if(webdriver.getCurrentUrl().equals("about:blank")) {
		        		  browser.awaitPageLoad(1000);
		        		  jsExec.executeScript(mainPage, sb.toString());
		        		  webStorage.getSessionStorage().setItem("ticks", sb.toString());
		        		  break;
		        	  }
		        	  System.out.println("Saving data from page "+currentURL);
		        	  String data = webStorage.getSessionStorage().getItem(tempName);
		        	  if(data!=null&&!data.equals("null")) {
		        		  sb.append(data);
		        	  }
		        	  else {
		        		  System.out.println("Null Data");
		        	  }
		        	  webStorage.getSessionStorage().removeItem(tempName);
		        	  continue mainloop;
		          }
		          if(webdriver.getCurrentUrl().equals("about:blank")) {
		        	  if((Boolean)jsExec.executeScript("if(typeof quit !== 'undefined'){ return quit;} return false;")) {
		        		  break mainloop;
		        	  }
		          }
		          try {
		        	  Thread.sleep(10);
		          }catch(InterruptedException e1) {}
			}
		}
		System.out.println(sb);
		browser.close();
		
	}
	private static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static String genRandom(int length) {
		StringBuilder b = new StringBuilder(length);
		Random r = new Random();
		for(int i = 0; i<length; i++) {
			b.append(chars.toCharArray()[r.nextInt(chars.length())]);
		}
		return b.toString();
		
	}
	private static String readResource(String path) throws IOException{
		StringBuilder sb = new StringBuilder();
		BufferedInputStream in = new BufferedInputStream(MouseCapture.class.getResourceAsStream(path));
		int r;
		while(in.available()>0){
			byte[] b = new byte[1024];
			r = in.read(b);
			sb.append(new String(b, 0, r));
		}
		return sb.toString();
	}
}
