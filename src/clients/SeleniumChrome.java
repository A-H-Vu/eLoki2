package clients;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

public class SeleniumChrome extends SeleniumClient {
	private ChromeOptions options;
	public SeleniumChrome() {
		super("Selenium-Chrome");
		options = new ChromeOptions();
		options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
	}
	
	public void setHeadless(boolean headless) {
		options.setHeadless(headless);
	}
	
	public void setProxy(Proxy proxy) {
		options.setProxy(proxy);
	}
	
	public void init() {
		super.webdriver = new ChromeDriver(options);
	}

}
