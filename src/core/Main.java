package core;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;
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
import ext.extensions.ExtensionLoader;
import ext.extensions.ExtensionsList;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentChoice;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import scraper.SeleniumScraper;
import script.Script;
import script.ScriptController;
import script.action.*;
import script.action.impl.*;

public class Main {
	//Version string, should be same as the version string in the pom.xml file
	private static String version = "0.3.1";
	//Class that runs scripts, for now only create one as default, in the future one will be used on each thread
	public static ScriptController defaultController = new ScriptController();
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		//Argument parser section sets up the parameters for argparse4j
		//Main parser which parses the main arguments
		ArgumentParser parser = ArgumentParsers.newFor("eloki2").build()
				.description("A tool to generate, record and replay browser sessions");
		parser.addArgument("--client")
			.choices(new IgnoreChoice("SeleniumChrome","SeleniumFirefox"))
			.dest("client")
			.help("sets the browser client to use");
		parser.addArgument("--driver").dest("driver").help("Sets the driver used by selenium");
		parser.addArgument("--full-browser")
			.dest("fullBrowser")
			.help("Use the full browser instead of a headless browser(if possible)")
			.action(Arguments.storeFalse());
		parser.addArgument("--headless")
			.dest("headless")
			.help("Hide the browser from view with Selenium Clients")
			.action(Arguments.storeTrue());
		parser.addArgument("--proxy")
			.dest("proxy")
			.metavar("address:port")
			.help("Set the [address:port] of the SOCKS5 proxy to use");
		parser.addArgument("--useragent")
			.dest("useragent")
			.help("Sets the user agent for the browser");
		parser.version(version);
		parser.addArgument("--version").action(Arguments.version());
		
		//Run sub-module, used to run scripts
		Subparsers subparsers = parser.addSubparsers().help("sub-command help");
		Subparser runscript = subparsers.addParser("run")
				.help("Run a script")
				.setDefault("runscript", true);
		runscript.addArgument("--randomMove")
			.help("Randomize movement of mouse slightly")
			.action(Arguments.storeTrue())
			.dest("randomize");
		runscript.addArgument("script")
			.nargs("+")
			.help("Script to run");
		
		//Scraper sub-module, used to scrape websites
		Subparser scraper = subparsers.addParser("scrape")
				.help("Scrape a website using Selenium")
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
		
		//Sub-module for used to capture recordings
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
		defaultController.addAction("keyStroke", KeyStroke.class);
		defaultController.addAction("keyDown", KeyDown.class);
		defaultController.addAction("keyUp", KeyUp.class);
		defaultController.addAction("userAgent", UserAgent.class);
		
		
		try {
			//Parse arguments from commandline
			Namespace res = parser.parseArgs(args);
			//printout as debug
			//System.out.println("res"+res);
			
			//determine if to use headless browser or not
			boolean headless = false;
			if(res.getBoolean("headless")!=null) {
				headless = res.getBoolean("headless");
			}
			
			//resolve the client
			String clientName = res.getString("client");
			Client client = null;
			if(clientName!=null) {
				if(clientName.equalsIgnoreCase("SeleniumChrome")) {
					if(res.getString("driver")==null) {
						System.err.println("The ChromeDriver must be set using the --driver argument");
						System.exit(1);
					}
					System.setProperty("webdriver.chrome.driver", res.getString("driver"));
					client = new SeleniumChrome();

				}
				else if(clientName.equalsIgnoreCase("SeleniumFirefox")) {
					if(res.getString("driver")==null) {
						System.err.println("The GeckoDriver must be set using the --driver argument");
						System.exit(1);
					}
					System.setProperty("webdriver.gecko.driver", res.getString("driver"));
					client = new SeleniumFirefox();
				}
			}
			
			//set stuff specific to Selenium instances
			if(client instanceof SeleniumClient) {
				SeleniumClient sClient = (SeleniumClient) client;
				//Set the proxy, SOCKS v5. Can be used to tunnel through tor
				if(res.getString("proxy")!=null) {
					Proxy proxy = new Proxy();
					proxy.setSocksVersion(5);
					proxy.setSocksProxy(res.getString("proxy"));
					sClient.setProxy(proxy);
				}
				if(res.getString("useragent")!=null) {
					sClient.setUserAgent(res.getString("useragent"));
				}
			}
			
			//Section of the code that handles the run sub-module
			if(res.getBoolean("runscript")!=null) {
				//Various checks related to the client
				if(client==null) {
					//TODO change to more generic message
					System.err.println("The selenium client and driver must be set using --client and --driver");
					System.exit(1);
				}
				if(client instanceof SeleniumClient) {
					((SeleniumClient) client).setHeadless(headless);
				}
				//Init client and iterate through the list of given scripts and execute them one after another
				client.init();
				for(Object s:res.getList("script")) {
					try {
						ActionImpl initial = defaultController.parseScript(Files.readAllLines(new File(s.toString()).toPath()));
						if(res.getBoolean("randomize")) {
							Script script = new Script(initial);
							script.forEach(a ->{
								if(a instanceof MousePositionAction) {
									MousePositionAction mv = (MousePositionAction)a;
									Random r = new Random();
									int xInit = mv.getX();
									int yInit = mv.getY();
									int i = 0;
									double epsilon = Math.PI/6;//15 deg angle in both directions
									//initial angle calculations, x,y are positions, t is theta, the angle
									//Stuff to calculate angle from previous position
									boolean prev = false;
									int xPrev = 0, yPrev = 0;
									double tPrev = 0;
									if(mv.getPreviousAction() instanceof MousePositionAction) {
										MousePositionAction preva = (MousePositionAction)mv.getPreviousAction();
										prev = true;
										xPrev = preva.getX();
										yPrev = preva.getY();
										tPrev = Math.atan2(yInit-yPrev, xInit-xPrev);
										if(xPrev==xInit&&yPrev==yInit) {
											prev = false;
										}
									}
									//stuff to calculate angle from next position
									boolean next = false;
									int xNext = 0, yNext = 0;
									double tNext = 0;
									if(mv.getNextAction() instanceof MousePositionAction) {
										MousePositionAction nexta = (MousePositionAction)mv.getNextAction();
										next = true;
										xNext = nexta.getX();
										yNext = nexta.getY();
										tNext = Math.atan2(yNext-yInit, xNext-xInit);
										if(xNext==xInit&&yNext==yInit) {
											next = false;
										}
									}
									while(true) {
										if(i>100) {
											mv.setX(xInit);
											mv.setY(yInit);
											break;
										}
										else {
											i++;
										}
										mv.setX(mv.getX()-5+r.nextInt(11));
										mv.setY(mv.getY()-5+r.nextInt(11));
										if(prev) {
											double trPrev = Math.atan2(mv.getY()-yPrev, mv.getX()-xPrev);
											double deltaPrev = Math.min(Math.abs(trPrev-tPrev), Math.abs(tPrev-trPrev));
											if(deltaPrev>epsilon) {
												continue;
											}
										}
										if(next) {
											double trNext = Math.atan2(yNext-mv.getY(), xNext-mv.getX());
											double deltaNext = Math.min(Math.abs(trNext-tNext), Math.abs(tNext-trNext));
											if(deltaNext>epsilon) {
												continue;
											}
										}
										break;
									}
								}
							});
							defaultController.runScript(script, client);
						}
						else {
							defaultController.runScript(initial, client);
						}
					} catch (IOException e) {
						System.err.println("Error reading script "+s);
						System.err.println(e.getMessage());
					}
				}
				//Close the client
				if(client instanceof Closeable) {
					((Closeable)client).close();
				}
			}
			
			//Section of the code that handles the scraping sub-module
			else if(res.getBoolean("scrape")!=null) {
				//Various checks related to the client
				if(client==null) {
					System.err.println("The selenium client and driver must be set using --client and --driver");
					System.exit(1);
				}
				if(!(client instanceof SeleniumClient)) {
					System.err.println("The client must be a Selenium Client, either Selenium-Firefox or Selenium-Chrome");
					System.exit(1);
				}
				else {
					//Initialize the client
					SeleniumClient sClient = (SeleniumClient)client;
					headless = res.getBoolean("fullBrowser");
					sClient.setHeadless(headless);
					sClient.init();
					
					//Initialize the scraper
					SeleniumScraper selScraper = new SeleniumScraper((SeleniumClient)client);
					//Dest is the destination file to write to
					selScraper.setDest(res.getString("dest"));
					//Prefixes are the list of url prefixes that the scraper will look for in addition to the baseurl
					if(res.get("prefixes")!=null) {
						selScraper.setPrefixes(res.getList("prefixes").stream().map(Object::toString).collect(Collectors.toList()).toArray(new String[] {}));
					}
					//max-depth is the maximum depth of links to follow, depth of a link is minimum # of clicks
					//needed to get to that link from the initial page
					selScraper.setMaxDepth(res.getInt("max_depth"));
					
					//Start the scaper
					selScraper.scrapeSite(res.getString("url"));
				}

			}
			
			//Section of the code that handles the capture sub-module
			else if(res.getBoolean("capture")!=null) {
				//Usual client checks
				if(client==null) {
					System.err.println("The selenium client and driver must be set using --client and --driver");
					System.exit(1);
				}
				if(!(client instanceof SeleniumClient)) {
					System.err.println("The client must be a Selenium Client, either Selenium-Firefox or Selenium-Chrome");
					System.exit(1);
				}
				ExtensionLoader.loadExtension(client, ExtensionsList.IgnoreXFrame);
				client.init();
				//Select capture method based on the --passive arg
				if(res.getBoolean("passive")) {
					MouseCapture.captureRecording2((SeleniumClient)client);
				}
				else {
					MouseCapture.captureRecording((SeleniumClient)client);
				}
				
			}
			
			//Handle some errors
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * A custom argumentChoice class for ArgParse4j to handle options where there are multiple choices
	 * while ignoring the case of the text such that both `FIREFOX` and `Firefox` match to the same thing
	 * @author Allen
	 *
	 */
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
