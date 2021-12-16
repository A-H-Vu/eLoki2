package script.action.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionTick;

/**
 * Action that navigates the browser to a specific page
 * @author Allen
 *
 */
public class GetPage extends Action {
	String pageURL = "about;blank";
	
	public GetPage(String raw) throws ParseException {
		super(raw);
		String[] args = raw.split(" ");
		pageURL = args[1];
		//fix url stuff
		try{
			new URL(pageURL);
			
		}catch(MalformedURLException e){
			try{
				pageURL = new URL("http://"+pageURL).toString();
			}catch(MalformedURLException e1){
				throw new ParseException(raw+" Malformed URL given", args[0].length()+1);
			}
		}
	}
	public GetPage(Action original) throws ParseException {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			//Reference and query portion of url are stripped before comparison in case there are
			//any dynamically changing variables fed through the query while checking if it is on the right page
			if(!stripQuery(sClient.getWebDriver().getCurrentUrl()).equals(stripQuery(pageURL))){
				sClient.getWebDriver().get(pageURL);
			}
		}
		return super.next;
	}
	
	/**
	 * Strips the query and reference portion of the URL
	 * @param url
	 * @return
	 */
	private static String stripQuery(String url) {
		try {
			//A copy of URL.toString() without the query and reference parameters
			URL u = new URL(url);
			StringBuffer result = new StringBuffer();
			result.append(u.getProtocol());
			result.append(":");
			if (u.getAuthority() != null && u.getAuthority().length() > 0) {
				result.append("//");
				result.append(u.getAuthority());
			}
			if (u.getPath() != null) {
				result.append(u.getPath());
			}
			return result.toString();
		//Catch block should never be triggered as url is checked in the constructor
		//added just in case
		}catch(MalformedURLException e) {
			return url;
		}
	}

	@Override
	public ActionCompatibility checkComptability(Client client) {
		if (client instanceof SeleniumClient) {
			return ActionCompatibility.Ok;
		}
		return ActionCompatibility.Incompatible;
	}
	
	@Override
	protected ActionTick.Response actionTickResponse() {
		return ActionTick.Response.ResetEpochToEnd;
	}
	@Override
	public Action clone() {
		try {
			return new GetPage(this);
		} catch (ParseException e) {
			return null;
		}
	}

}
