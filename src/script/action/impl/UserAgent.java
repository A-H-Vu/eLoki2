package script.action.impl;

import java.util.Map;

import org.openqa.selenium.chromium.ChromiumDriver;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionTick;

public class UserAgent extends Action {
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
