package script.action;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that basically parses the string input for actions given a raw input
 * Only thing this really does is provide parsing for quoted inputs
 * @author Allen
 *
 */
public class ActionArgParser {
	//Name of the action, as it is assigned in the ScriptController and may not be constant
	//for all times the action is created
	private String actionName;
	//pattern that ignores space delim within '' or ""
	private String tokenRegex = "\"([^\"]*)\"|'([^']*)'|(\\S+)";
	//args
	private ArrayList<String> args = new ArrayList<String>();
	public ActionArgParser(String raw) {
		String[] v = raw.split(" ", 2);
		actionName = v[0];
		if(v.length>1) {
			//parse additional arguments
			Matcher m = Pattern.compile(tokenRegex).matcher(v[1]);
			while(m.find()) {
				if(m.group(1)!=null) {
					args.add(m.group(1));
				}
				else if(m.group(2)!=null){
					args.add(m.group(2));
				}
				else {
					args.add(m.group(3));
				}
			}
			
		}
	}
	
	public String getActionName() {
		return actionName;
	}
	public int argLength() {
		return args.size();
	}
	public String getArg(int index) {
		return args.get(index);
	}
	public int getArgAsInt(int index, int defaultNum) {
		try {
			return Integer.parseInt(args.get(index));
		}
		catch(NumberFormatException e) {
			return defaultNum;
		}
	}
	public Optional<String> getArgO(int index) {
		if(index>=argLength()||index<0) {
			return Optional.empty();
		}
		else {
			return Optional.ofNullable(args.get(index));
		}
	}
	public Optional<Integer> getArgAsIntO(int index){
		try {
			return Optional.of(Integer.parseInt(args.get(index)));
		}
		catch(NumberFormatException|IndexOutOfBoundsException e) {
			return Optional.empty();
		}
	}

}
