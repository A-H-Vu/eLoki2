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
		//Allow alerts to be displayed to the user without needing them to be handled by Selenium
		//Used by the mouseCapture scripts
		options = new FirefoxOptions();
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
		String[] address = proxy.getSocksProxy().split(":");
		options.addPreference("network.proxy.socks", address[0]);
        options.addPreference("network.proxy.socks_port", address[1]);
        options.addPreference("network.proxy.type", 1);
	}
	
	public void init() {
		super.webdriver = new FirefoxDriver(options);
	}
	
	
	

}
