package clients;

import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

public class SeleniumChrome extends SeleniumClient {

	public SeleniumChrome() {
		super("Selenium-Chrome");
		// TODO add another contructor with more properties
		ChromeOptions options = new ChromeOptions();
		options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		super.webdriver = new ChromeDriver(options);
	}

}
