package clients;

import java.io.Closeable;
import java.io.IOException;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.PointerInput;

public abstract class SeleniumClient extends Client implements Closeable {

	protected WebDriver webdriver;
	protected PointerInput pointerinput;

	protected SeleniumClient(String name) {
		super(name);
	}
	
	public abstract void setHeadless(boolean headless);
	
	public WebDriver getWebDriver() {
		return webdriver;
	}

	public PointerInput getPointerInput() {
		if (pointerinput == null) {
			pointerinput = new PointerInput(PointerInput.Kind.MOUSE, "BrowserMouse");
		}
		return pointerinput;
	}

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
