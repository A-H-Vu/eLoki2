package core;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.openqa.selenium.Proxy;

import capture.MouseCapture;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import clients.Client;
import clients.SeleniumChrome;
import clients.SeleniumClient;
import clients.SeleniumFirefox;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentChoice;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import scraper.SeleniumScraper;
import script.ScriptController;
import script.action.*;

public class Main {
	
	private static String version = "0.2.0";
	public static ScriptController defaultController = new ScriptController();
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		ArgumentParser parser = ArgumentParsers.newFor("eloki2").build()
				.description("A tool to generate, record and replay browser sessions");
		parser.addArgument("--client")
			.choices(new IgnoreChoice("SeleniumChrome","SeleniumFirefox"))
			.dest("client")
			.help("sets the browser client to use");
		parser.addArgument("--driver").dest("driver").help("Sets the driver used by selenium");
		parser.addArgument("--headless")
			.dest("headless")
			.help("Hide the browser from view with Selenium Clients")
			.action(Arguments.storeTrue());
		parser.addArgument("--force-browser")
			.dest("headless")
			.help("Force the browser to be shown with Selenium Clients")
			.action(Arguments.storeFalse());
		parser.addArgument("--proxy")
			.dest("proxy")
			.metavar("address:port")
			.help("Set the [address:port] of the SOCKS5 proxy to use");
		parser.version(version);
		parser.addArgument("--version").action(Arguments.version());
		
		Subparsers subparsers = parser.addSubparsers().help("sub-command help");
		Subparser runscript = subparsers.addParser("run")
				.help("Run a script")
				.setDefault("runscript", true);
		runscript.addArgument("script")
			.nargs("+")
			.help("Script to run");
		
		
		Subparser scraper = subparsers.addParser("scrape")
				.help("Scrape a website using JSoup")
				.setDefault("scrape", true);
		scraper.addArgument("url")
			.required(true)
			.help("url to scrape");
		scraper.addArgument("--max-depth")
			.setDefault(10)
			.metavar("DEPTH")
			.type(Integer.class)
			.help("Maximum depth from first url to scrape, 0 means scrape only the given url");
		scraper.addArgument("--timeout")
			.metavar("MILLIS")
			.setDefault(1000)
			.type(Integer.class)
			.help("The time to wait between get requests, in milliseconds");
		scraper.addArgument("--dest")
			.metavar("FILE")
			.setDefault("anchors")
			.help("File to save the scraped urls to.");
		scraper.addArgument("--add-prefix")
			.metavar("URLs")
			.dest("prefixes")
			.nargs("+")
			.help("Additional URL prefixes to scrape");
		
		Subparser capture = subparsers.addParser("capture")
				.help("Record a session using Selenium")
				.setDefault("capture", true);
		capture.addArgument("--passive")
			.dest("passive")
			.action(Arguments.storeTrue())
			.help("Use the passive session capture method, if iframe embedding is blocked by the site");
		
		//Add actions manually in main for now
		defaultController.addAction("waiting", AwaitPageLoad.class);
		defaultController.addAction("getPage", GetPage.class);
		defaultController.addAction("click", MouseClick.class);
		defaultController.addAction("right_click", MouseRightClick.class);
		defaultController.addAction("mouseMove", MouseMove.class);
		defaultController.addAction("mouseMoveScroll", MouseMoveScroll.class);
		defaultController.addAction("delay", Wait.class);
		defaultController.addAction("scrollWindow", ScrollWindow.class);
		defaultController.addAction("attachMouse", AttachMouse.class);
		defaultController.addAction("resize", ResizeWindow.class);
		
		
		try {
			Namespace res = parser.parseArgs(args);
			System.out.println("res"+res);
			//determine if to use headless browser or not
			boolean headless = false;
			if(res.getBoolean("headless")!=null) {
				headless = res.getBoolean("headless");
			}
			
			//resolve the client
			String clientName = res.getString("client");
			Client client = null;
			if(clientName.equalsIgnoreCase("SeleniumChrome")) {
				if(res.getString("driver")==null) {
					System.err.println("The ChromeDriver must be set using the --driver argument");
					System.exit(1);
				}
				System.setProperty("webdriver.chrome.driver", res.getString("driver"));
				client = new SeleniumChrome();

			}
			else if(clientName.equalsIgnoreCase("SeleniumFirefox")) {
				System.setProperty("webdriver.gecko.driver", res.getString("driver"));
				client = new SeleniumFirefox();
			}
			//set stuff specific to Selenium instances
			if(client instanceof SeleniumClient) {
				SeleniumClient sClient = (SeleniumClient) client;
				if(res.getString("proxy")!=null) {
					Proxy proxy = new Proxy();
					proxy.setSocksVersion(5);
					proxy.setSocksProxy(res.getString("proxy"));
					sClient.setProxy(proxy);
				}
			}
			if(res.getBoolean("runscript")!=null) {
				if(client==null) {
					//TODO change to more generic message
					System.err.println("The selenium client and driver must be set using --client and --driver");
					System.exit(1);
				}
				if(client instanceof SeleniumClient) {
					((SeleniumClient) client).setHeadless(headless);
				}
				client.init();
				for(Object s:res.getList("script")) {
					try {
						Action initial = defaultController.parseScript(Files.readAllLines(new File(s.toString()).toPath()));
						defaultController.runScript(initial, client);
					} catch (IOException e) {
						System.err.println("Error reading script "+res.getString("script"));
						System.err.println(e.getMessage());
					}
				}
				if(client instanceof Closeable) {
					((Closeable)client).close();
				}
			}
			else if(res.getBoolean("scrape")!=null) {
				if(client==null) {
					System.err.println("The selenium client and driver must be set using --client and --driver");
					System.exit(1);
				}
				if(!(client instanceof SeleniumClient)) {
					System.err.println("The client must be a Selenium Client, either Selenium-Firefox or Selenium-Chrome");
					System.exit(1);
				}
				else {
					SeleniumClient sClient = (SeleniumClient)client;
					if(res.get("headless")==null) {
						//default true
						headless = true;
					}
					sClient.setHeadless(headless);
					sClient.init();
					SeleniumScraper selScraper = new SeleniumScraper((SeleniumClient)client);
					selScraper.setDest(res.getString("dest"));
					if(res.get("prefixes")!=null) {
						selScraper.setPrefixes(res.getList("prefixes").stream().map(Object::toString).collect(Collectors.toList()).toArray(new String[] {}));
					}
					selScraper.setMaxDepth(res.getInt("max_depth"));
					selScraper.scrapeSite(res.getString("url"));
				}

			}
			else if(res.getBoolean("capture")!=null) {
				if(client==null) {
					System.err.println("The selenium client and driver must be set using --client and --driver");
					System.exit(1);
				}
				if(!(client instanceof SeleniumClient)) {
					System.err.println("The client must be a Selenium Client, either Selenium-Firefox or Selenium-Chrome");
					System.exit(1);
				}
				client.init();
				if(res.getBoolean("passive")) {
					MouseCapture.captureRecording2((SeleniumClient)client);
				}
				else {
					MouseCapture.captureRecording((SeleniumClient)client);
				}
				
			}

		} catch (ArgumentParserException e) {
			parser.handleError(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static class IgnoreChoice implements ArgumentChoice {
		private String options[];
		private IgnoreChoice(String... options) {
			this.options = options;
		}
		@Override
		public boolean contains(Object val) {
			for(String s:options) {
				if(s.equalsIgnoreCase(val.toString())) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String textualFormat() {
			return Arrays.stream(options).collect(Collectors.joining(","));
		}
		
	}
}
