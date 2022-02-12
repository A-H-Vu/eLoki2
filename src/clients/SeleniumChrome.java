package clients;

import java.io.File;
import java.util.Collections;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

/**
 * Class representing the Selenium Client for the chrome browser
 * @author Allen
 *
 */
public class SeleniumChrome extends SeleniumClient {
	private ChromeOptions options;
	public SeleniumChrome() {
		super("Selenium-Chrome");
		//Allow alerts to be displayed to the user without needing them to be handled by Selenium
		//Used by the mouseCapture scripts
		options = new ChromeOptions();
		//Disable CORS as the injected scripts tend to be communicating from different sources
		options.addArguments("--disable-web-security");
		options.addArguments("--disable-blink-features=AutomationControlled");
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("useAutomationExtension", false);
		options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
	}
	
	/**
	 * Sets headless mode, where the GUI of the browser is hidden
	 * Some sites may be able to detect a headless browser
	 */
	public void setHeadless(boolean headless) {
		options.setHeadless(headless);
	}
	
	/**
	 * Sets the SOCKS proxy
	 * 
	 * Use {@link Proxy#setSocksProxy} and {@link Proxy#setSocksVersion(Integer)} to set.
	 * 
	 * Optionally set usernamd and password if necessary
	 * @param proxy The {@link Proxy} object with the SOCKS proxy information
	 */
	public void setProxy(Proxy proxy) {
		options.addArguments("--proxy-server=socks5://"+proxy.getSocksProxy());
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUserAgent(String userAgent) {
		options.addArguments("user-agent="+userAgent);
	}

	
	public void addExtension(File path) {
		options.addExtensions(path);
	}
	
	public void init() {
		super.webdriver = new ChromeDriver(options);
	}
}
