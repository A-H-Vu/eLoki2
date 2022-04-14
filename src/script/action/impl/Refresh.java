package script.action.impl;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionImpl;
import script.action.ActionTick;

public class Refresh extends ActionImpl {

	public Refresh(String raw) {
		super(raw);
	}

	public Refresh(ActionImpl original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			sClient.getWebDriver().navigate().refresh();
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
	protected ActionTick.Response actionTickResponse() {
		return ActionTick.Response.ResetEpochToEnd;
	}
	@Override
	public Action clone() {
		return new Refresh(this);
	}

}
