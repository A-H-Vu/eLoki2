package clients;

import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;

public class SeleniumFirefox extends SeleniumClient {

	public SeleniumFirefox() {
		super("Selenium-Firefox");
		FirefoxOptions options = new FirefoxOptions();
		options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		super.webdriver = new FirefoxDriver(options);
	}
	
	
	

}
