package clients;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.HasExtensions;
import org.openqa.selenium.remote.CapabilityType;

public class SeleniumFirefox extends SeleniumClient {
	private FirefoxOptions options;
	private FirefoxProfile profile;
	private ArrayList<Path> extensionPaths = new ArrayList<Path>();
	
	public SeleniumFirefox() {
		super("Selenium-Firefox");
		//Allow alerts to be displayed to the user without needing them to be handled by Selenium
		//Used by the mouseCapture scripts
		options = new FirefoxOptions();
		options.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		profile = new FirefoxProfile();
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
	
	public void addExtension(File path) {
		extensionPaths.add(path.toPath());
		profile.addExtension(path);
	}
	
	public void init() {
		options.setProfile(profile);
		super.webdriver = new FirefoxDriver(options);
		//Installing via profile.addExtension does not work as 3.141.59 seems to have a bug
		//where it is unable to load the profile thus not loading the extension
		HasExtensions he = (HasExtensions)webdriver;
		for(Path p:extensionPaths) {
			he.installExtension(p);
		}
	}
	
	
	

}
