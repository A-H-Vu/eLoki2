package script.action;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import clients.Client;
import clients.SeleniumClient;

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

	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			if(!sClient.getWebDriver().getCurrentUrl().equals(pageURL)){
				sClient.getWebDriver().get(pageURL);
			}
		}
		return super.next;
	}

	@Override
	public ActionCompatibility checkComptability(Client client) {
		if (client instanceof SeleniumClient) {
			return ActionCompatibility.Ok;
		}
		return ActionCompatibility.Incompatible;
	}

}
