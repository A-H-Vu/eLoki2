package clients;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;

public class SeleniumFirefox extends SeleniumClient {
	private FirefoxOptions options;
	
	public SeleniumFirefox() {
		super("Selenium-Firefox");
		options = new FirefoxOptions();
		options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
	}
	
	public void setHeadless(boolean headless) {
		options.setHeadless(headless);
	}
	public void setProxy(Proxy proxy) {
		options.setProxy(proxy);
	}
	
	public void init() {
		super.webdriver = new FirefoxDriver(options);
	}
	
	
	

}
