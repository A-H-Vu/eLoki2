package script.action.impl;

import java.util.Map;

import org.openqa.selenium.chromium.ChromiumDriver;

import clients.Client;
import clients.SeleniumClient;
import script.action.ActionImpl;
import script.action.ActionCompatibility;
import script.action.Action;
import script.action.ActionTick;

public class UserAgent extends ActionImpl {
	private String UA;
	public UserAgent(String raw) {
		super(raw);
		int space = raw.indexOf(' ');
		if(space>0) {
			UA = raw.substring(space+1);
		}
	}

	public UserAgent(Action original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			if(UA!=null) {
				if(sClient.getWebDriver() instanceof ChromiumDriver) {
					ChromiumDriver cDriver = (ChromiumDriver)sClient.getWebDriver();
					cDriver.executeCdpCommand("Network.setUserAgentOverride", Map.of("userAgent",UA));
				}
				//To get firefox working will need to pull in devtools package and use the proper interface
				//instead of directly executing the cdp command which is only possible with Chrome based browsers
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

	@Override
	public Action clone() {
		return new UserAgent(this);
	}

}
