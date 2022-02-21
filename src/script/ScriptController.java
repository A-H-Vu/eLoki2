package script;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import clients.Client;
import script.action.Action;
import script.action.ActionTick;

/**
 * 
 * Main class for running and parsing the scripts used and generated by the program
 * @author Allen
 *
 */
public class ScriptController {

	private Map<String, Class<? extends Action>> actionMap = new HashMap<String, Class<? extends Action>>();
	
	//Variables used in script parsing, initial action is the start of the script chain and
	//current is the latest action parsed
	private Action initial;
	private Action current;
	//Various functions to parse a script file 
	public Action parseScript(List<String> lines) {
		initial = null;
		current = null;
		for (String line : lines) {
			parseLine(line);
		}
		return initial;
	}
	public Action parseScript(String[] lines) {
		initial = null;
		current = null;
		for(String line: lines) {
			parseLine(line);
		}
		return initial;
	}
	public Action parseScript(BufferedReader lines) {
		initial = null;
		current = null;
		try {
			while(lines.ready()) {
				parseLine(lines.readLine());
			}
		} catch (IOException e) {
			return null;
		}
		return initial;
	}
	/**
	 * General pattern for each line
	 * an optional @###### which marks the timestamp of the line
	 * and then the action itself
	 */
	Pattern parsePattern = Pattern.compile("(?:(@[0-9]+) )?(.+)");
	Matcher parseMatcher = parsePattern.matcher("");
	
	/**
	 * Parsers a single line of a script file
	 * @param line
	 */
	private void parseLine(String line) {
		parseMatcher.reset(line);
		if(!parseMatcher.matches()) {
			System.err.println("Error parsing format for "+line+", skipping");
			return;
		}
		
		String action = parseMatcher.group(2).split(" ")[0].toLowerCase();
		//actionMap contains all possible actions
		if (actionMap.containsKey(action)) {
			try {
				//Create the action using the string constructor for it
				Action n = actionMap.get(action).getConstructor(String.class).newInstance(parseMatcher.group(2));
				//Add the action to the action chain, if initial set as such
				if (initial == null) {
					initial = n;
					current = n;
				} else {
					// TODO figure out how to deal with issues like stuff not chaining due to some
					// error
					current.chainNextAction(n);
					current = n;
				}
			} catch (Exception e) {
				//ignore errors in parsing, most likely due to bad user added lines etc
				//print errors so it can be debugged if necessary
				System.err.println("Error parsing action "+line+", skipping");
				e.printStackTrace();
			}
		} else {
			System.err.println("Unrecognized Action keyword " + action + ", skipping");
		}
		//sets the value of the timing tick parsed
		if(parseMatcher.group(1)!=null) {
			current.setTickVal(Long.parseLong(parseMatcher.group(1).substring(1)));
		}
	}
	
	/**
	 * Runs a script starting from the given action using the client given
	 * @param script The first action in the script
	 * @param client Client to use
	 */
	public void runScript(Script script, Client client) {
		runScript(script.getFirstAction(), client);
	}
	
	/**
	 * Runs a script starting from the given action using the client given
	 * @param script The first action in the script
	 * @param client Client to use
	 */
	public void runScript(Action script, Client client) {
		Action current = script;
		//Epoch and Epoch Rel are the main values used to control timing
		//The timing of a specific action is calculated as follows time to wait = (actionTickValue - epochRel) - (currentTime - epoch)
		//The epoch value is used to pin the timing to some unpredictable event i.e. waiting for page load
		//The actions afterwards are then executed relative to this value
		long epoch = System.currentTimeMillis();
		long epochRel = script.getTick().getValue();
		//Counter for errors, if > 10 occur with the same action then stop the script as something is likely going wrong
		int errors = 0;
		boolean skip = false;
		while(current != null) {
			System.out.println(current.getRaw());
			Action prev = current;
			if(!skip) {
				//Execute the action
				try {
					current = current.execute(client);
					errors = 0;
				}catch(Exception e) {
					//Error handing, stop execution if too many error ocurr with the same action
					System.err.println("Error executing "+current.getRaw()+",skipping");
					current = current.getNextAction();
					e.printStackTrace();
					errors++;
					if(errors>10) {
						break;
					}
				}
			}
			else {
				current = current.getNextAction();
				skip = false;
			}
			//Stuff to time actions to occur exactly as it was recorded
			//Reset epoch, if the previous action resets it to the end of its execution
			if(prev.getTick().getResponse()==ActionTick.Response.ResetEpochToEnd) {
				epoch = System.currentTimeMillis();
				epochRel = prev.getTick().getValue();
			}
			//end of script exit
			if(current == null)break;
			//Calculate the necessary delay to the next action allow ~ 2ms for webdriver communication etc.
			ActionTick.Response type = current.getTick().getResponse();
			long delay = 0;
			if(type == ActionTick.Response.Skippable||type == ActionTick.Response.UseTick) {
				delay = (current.getTick().getValue()-epochRel)-(System.currentTimeMillis()-epoch);
			}
			//Skip next action if delay is negative/lagging significantly
			//Only skip if it is not a significant action/can be skipped
			if(delay<-10&&type == ActionTick.Response.Skippable) {
				skip = true;
			}
			//Sleep for the specified delay
			if(delay > 2) {
				try {
					Thread.sleep(delay-2);
				} catch (InterruptedException e) {
					//presumably interrupted to stop script
					break;
				}
			}
			//Reset epoch if the next action is one that resets it to its start
			if(type == ActionTick.Response.ResetEpoch) {
				epoch = System.currentTimeMillis();
				epochRel = current.getTick().getValue();
			}
				
		}
	}

	/**
	 * Adds an action to be parsed
	 * 
	 * @param name        name of the action used when parsing
	 * @param actionClass class of the action
	 * @throws NoSuchMethodException if there are no constructors with the parameter
	 *                               (String)
	 * @throws SecurityException     if the class cannot be loaded due to a security
	 *                               manager
	 */
	public void addAction(String name, Class<? extends Action> actionClass)
			throws NoSuchMethodException, SecurityException {
		actionClass.getConstructor(String.class);
		actionMap.put(name.toLowerCase(), actionClass);
	}
}
