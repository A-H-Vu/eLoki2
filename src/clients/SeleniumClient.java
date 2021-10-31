package clients;

import java.io.Closeable;
import java.io.IOException;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.PointerInput;

public abstract class SeleniumClient extends Client implements Closeable {

	protected WebDriver webdriver;
	
	/**
	 * Single pointerInput instance for the client to avoid reinitializing
	 * for every mouse action
	 */
	protected PointerInput pointerinput;

	protected SeleniumClient(String name) {
		super(name);
	}
	
	/**
	 * Sets headless mode, where the GUI of the browser is hidden
	 * Some sites may be able to detect a headless browser
	 */
	public abstract void setHeadless(boolean headless);
	/**
	 * Sets the SOCKS proxy
	 * 
	 * Use {@link Proxy#setSocksProxy} and {@link Proxy#setSocksVersion(Integer)} to set.
	 * 
	 * Optionally set usernamd and password if necessary
	 * @param proxy The {@link Proxy} object with the SOCKS proxy information
	 */
	public abstract void setProxy(Proxy proxy);
	
	/**
	 * Gets the Selenium Webdriver for the client
	 * Most functions in the SeleniumClient class wrap around the webdriver.
	 * 
	 * 
	 * @return Selenium Webdriver for the client
	 */
	public WebDriver getWebDriver() {
		return webdriver;
	}
	
	/**
	 * Gets the pointer input instance for this client
	 * @return
	 */
	public PointerInput getPointerInput() {
		if (pointerinput == null) {
			pointerinput = new PointerInput(PointerInput.Kind.MOUSE, "BrowserMouse");
		}
		return pointerinput;
	}

	/**
	 * Gets the Javascript Executor for the webdriver
	 * @return
	 */
	public JavascriptExecutor getJSExecutor() {
		return (JavascriptExecutor) webdriver;
	}

	public void awaitPageLoad(long timeout) {
		long start = System.currentTimeMillis();
		while(System.currentTimeMillis()<start+timeout) {
			if((Boolean)getJSExecutor().executeScript("return document.readyState == 'complete';")) {
				return;
			}
		}
	}

	/**
	 * Closes the client by calling webdriver.quit()
	 */
	public void close() throws IOException {
		webdriver.quit();
	}

	//This may be the cause of countless bugs with how java's GC works
//	public final void finalize() {
//		try {
//			//System.out.println("SeleniumClient finalized, GCed");
//			close();
//		} catch (IOException e) {
//		}
//	}

}
