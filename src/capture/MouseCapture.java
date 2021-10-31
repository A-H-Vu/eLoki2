package capture;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Random;

import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import clients.SeleniumClient;
/**
 * 
 * Main Class that handles the mouse capture module currently
 * The two different methods of capture are handled in the two megafunctions
 * captureRecording and captureRecording2
 * <br>
 * <br>
 * In general the mouse capture functions works as follows
 * <br>
 * <br>
 * 1. Use Selenium to launch the specified browser - either Firefox or Chrome currently
 * <br>
 * 2. Start main loop
 * <br>
 * 3. Inject the javascript used to show the main page
 * <br>
 * 4. Wait for a response from the javascript checking a variable or for the url to change, if page change is unexpected return to 2.
 * <br>
 * 5. Inject the javascript used to record the mouse movements
 * <br>
 * 6. Wait for a response from the javascript checking a varaible and returning to the main page (step 2), displaying the recording etc.
 * 
 * 
 * @author Allen
 *
 */
public class MouseCapture {
	/**
	 * Default Recording method, captures mouse movements by creating an iframe and re-embedding the website within it.
	 * This is the preferred method as a custom header can be used to control the recording and to show whether or not the
	 * program is recording or not.
	 * <br>
	 * Attempting to embed a site in an iframe where the domain does not match will likely trigger CORS errors. Certain sites
	 * also disallow themselves from being embedded in an iframe. (TODO look to see w/e or not it is possible to easily disable
	 * some of these protections to facilitate easy recording)
	 * @param browser The browser to record with, currently only Selenium-Firefox and Selenium-Chrome are supported
	 * 			it will be closed once the recording has completed
	 * @throws IOException if any sort of error occurs while recording the session, 
	 * 			typically from the browser being closed by the user
	 */
	public static void captureRecording(SeleniumClient browser) throws IOException{
		//Setup some variables that persist beyond loops
		WebDriver webdriver = browser.getWebDriver();
		JavascriptExecutor jsExec = browser.getJSExecutor();
		WebDriverWait wait = new WebDriverWait(webdriver, 100000);
		//Status string used to send error messages on the Main page 
		String status = "";
		//Load the javascript that will be injected, Main is for the main page and the other to record
		String js = readResource("/capture/res/mouseCapture.js");
		String mainPage = readResource("/capture/res/mouseCaptureMain.js");


		mainloop: while(true){
			System.out.println("displaying main landing page");
			webdriver.get("about:blank");
			jsExec.executeScript(mainPage, status);

			//The hotloop below does the following
			//If the current url changes from the user navigating off the page redisplay the main page
			//If it is a blob URL then it is downloading the recorded script
			//Check if the ready variable on the page is set to true, if yes break loop and continue to next part
			String currentURL = webdriver.getCurrentUrl();
			while(true){
				//Avoid switching if it is a blob: url as it is used to download the recording
				if(!webdriver.getCurrentUrl().equals(currentURL)&&!webdriver.getCurrentUrl().startsWith("blob:")){
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

			//Get the page that the user wants to record a session on
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

			//Wait for the page to fully load
			wait.until((ExpectedCondition<Boolean>) wd -> {
				return ((Boolean)((JavascriptExecutor)wd).executeScript("return document.readyState == 'complete';"));
			});

			//Inject the javascript which will clear the page and embed it in an iframe along with a control bar on the top
			System.out.println("injecting js");
			jsExec.executeScript(js);
			System.out.println("Waiting for state change");

			//The hotloop below does the following
			//Check the state variable on the page act depending on the value
			//"active" - do nothing, recordign script is still active
			//"reset" - continue loop in order to go back to the main page
			//"quit" - break loop to quit/close the browser
			//This is the point at which the user is recording the session, most of the session recording logic is in mouseCapture.js
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
	/**
	 * Secondary recording method, records sessions by injecting the recording javascript whenever the page changes
	 * Not as user friendly and is a bit janky in terms of the stopping mechanism as there is no good way in javascript to
	 * detect when the browser window loses focus. The recording stops once the body window loses focus, this is only easily
	 * triggered by clicking on the url bar or switching tabs, the browser itself losing focus does not trigger this.
	 * 
	 * 
	 * 
	 * @param browser The browser to record with, currently only Selenium-Firefox and Selenium-Chrome are supported
	 * 			it will be closed once the recording has completed
	 * @throws IOException if any sort of error occurs while recording the session, 
	 * 			typically from the browser being closed by the user
	 */
	public static void captureRecording2(SeleniumClient browser) throws IOException{
		//Setup some variables that persist beyond loops
		WebDriver webdriver = browser.getWebDriver();
		JavascriptExecutor jsExec = browser.getJSExecutor();
		StringBuilder sb = new StringBuilder();
		WebStorage webStorage = (WebStorage) new Augmenter().augment(webdriver);
		//Load the javascript that will be injected, Main is for the main page and the other to record
		String js = readResource("/capture/res/mouseCapture_session.js");
		String mainPage = readResource("/capture/res/mouseCapture_sessionMain.js");
		//Display the main page
		webdriver.get("about:blank");
		browser.awaitPageLoad(1000);
		jsExec.executeScript(mainPage);
		//Not entirely sure how this works properly, how the recording script being injected at the start of every loop not break things
		//Main loop injects the script whenever the page changes, reads data from previous page from the session storage
		mainloop: while(true){
			//Generate a random string to as the key store the recording for the page in indexdb
			//The javascript injected will automatically write everything recorded to the session storage when it is unloaded
			//by the page chaning
			String tempName = genRandom(10);
			tempName = (String)jsExec.executeScript(js, tempName);
			String currentURL = webdriver.getCurrentUrl();
			//Hot loop to check if the current url has changed
			while(true){
				if(!webdriver.getCurrentUrl().equals(currentURL)){
					//If the user is returning to the main page from stopping the recording, inject main page script
					//and save recording data to ticks key in the session storage
					if(webdriver.getCurrentUrl().equals("about:blank")) {
						browser.awaitPageLoad(1000);
						jsExec.executeScript(mainPage, sb.toString());
						sb = new StringBuilder();
						webStorage.getSessionStorage().setItem("ticks", sb.toString());
						break;
					}
					//Save the recorded data from the previous page
					System.out.println("Saving data from page "+currentURL);
					String data = webStorage.getSessionStorage().getItem(tempName);
					if(data!=null&&!data.equals("null")) {
						sb.append(data);
					}
					else {
						System.out.println("Null Data");
					}
					//remove the item from session storage to avoid takign up too much space
					webStorage.getSessionStorage().removeItem(tempName);
					continue mainloop;
				}
				if(webdriver.getCurrentUrl().equals("about:blank")) {
					if((Boolean)jsExec.executeScript("if(typeof quit !== 'undefined'){ return quit;} return false;")) {
						break mainloop;
					}
				}
				//Short sleep to avoid completely maxing out the cpu
				try {
					Thread.sleep(10);
				}catch(InterruptedException e1) {}
			}
		}
		System.out.println(sb);
		browser.close();

	}


	private static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	/**
	 * Generates a random alphanumeric string with characters A-Z,a-z,0-9
	 * @param length Length of the string to generate
	 * @return A random alphanumeric string of the specified length
	 */
	private static String genRandom(int length) {
		StringBuilder b = new StringBuilder(length);
		Random r = new Random();
		for(int i = 0; i<length; i++) {
			b.append(chars.toCharArray()[r.nextInt(chars.length())]);
		}
		return b.toString();

	}
	/**
	 * Function to read a resource file that may either be on the disk or in a jar file
	 * @param path Path of the resource with the src directory as root, paths are relative to the MouseCapture class
	 * @return The contents of the file as a string
	 * @throws IOException
	 */
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
