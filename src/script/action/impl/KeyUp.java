package script.action.impl;

import org.openqa.selenium.interactions.Actions;

import clients.Client;
import clients.SeleniumClient;
import script.action.ActionImpl;
import script.action.ActionCompatibility;
import script.action.Action;
import script.action.ActionTick;
import script.action.SeleniumKeyMapping;

public class KeyUp extends ActionImpl {
	private String keys;
	public KeyUp(String raw) {
		super(raw);
		int space = raw.indexOf(' ');
		if(space>0) {
			keys = raw.substring(space+1);
		}
		
	}
	public KeyUp(Action original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			//test if the key is something in the mapping and apply it
			try {
				if(SeleniumKeyMapping.valueOf(keys)!=null) {
					keys = SeleniumKeyMapping.valueOf(keys).SeleniumKey().toString();
				}
			}catch(IllegalArgumentException e) {}
			SeleniumClient sClient = (SeleniumClient) client;
			Actions action = new Actions(sClient.getWebDriver());
			action.keyUp(keys);
			action.build().perform();
		}
		return super.getNextAction();
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
		return ActionTick.Response.UseTick;
	}
	@Override
	public Action clone() {
		return new KeyUp(this);
	}

}
