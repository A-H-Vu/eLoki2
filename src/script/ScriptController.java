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

public class ScriptController {

	private Map<String, Class<? extends Action>> actionMap = new HashMap<String, Class<? extends Action>>();
	
	private Action initial;
	private Action current;
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
	Pattern parsePattern = Pattern.compile("(?:(@[0-9]+) )?(.*)");
	Matcher parseMatcher = parsePattern.matcher("");
	
	private void parseLine(String line) {
		parseMatcher.reset(line);
		if(!parseMatcher.matches()) {
			System.err.println("Error parsing format for "+line+", skipping");
		}
		
		String action = parseMatcher.group(2).split(" ")[0];
		if (actionMap.containsKey(action)) {
			try {
				Action n = actionMap.get(action).getConstructor(String.class).newInstance(parseMatcher.group(2));
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
				System.err.println("Error parsing action "+line+", skipping");
				e.printStackTrace();
				// TODO proper error handling
			}
		} else {
			System.err.println("Unrecognized Action keyword " + action + ", skipping");
		}
		if(parseMatcher.group(1)!=null) {
			current.setTickVal(Long.parseLong(parseMatcher.group(1).substring(1)));
		}
	}
	
	public void runScript(Action script, Client client) {
		long epoch = System.currentTimeMillis();
		Action current = script;
		long epochRel = script.getTick().getValue();
		int errors = 0;
		boolean skip = false;
		while(current != null) {
			System.out.println(current.getRaw());
			Action prev = current;
			if(!skip) {
				try {
					current = current.execute(client);
					errors = 0;
				}catch(Exception e) {
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
				skip = false;
			}
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
			//Skip if delay is negative/lagging significantly
			if(delay<-10&&type == ActionTick.Response.Skippable) {
				skip = true;
			}
			
			if(delay > 2) {
				try {
					Thread.sleep(delay-2);
				} catch (InterruptedException e) {
					//presumably interrupted to stop script
					break;
				}
			}
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
		actionMap.put(name, actionClass);
	}
}
