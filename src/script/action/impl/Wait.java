package script.action.impl;

import clients.Client;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionTick;

public class Wait extends Action {
	int delay = 0;
	public Wait(String raw) {
		super(raw);
		String[] args = raw.split(" ");
		delay = Integer.parseInt(args[1]);
	}
	public Wait(Action original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}
	@Override
	public Action execute(Client client) {
		try {
			Thread.sleep(delay);
			//stop sleep on interruption
		} catch (InterruptedException e) {}
		return super.next;
	}

	@Override
	public ActionCompatibility checkComptability(Client client) {
		return ActionCompatibility.Ok;
	}
	
	@Override
	public ActionTick.Response actionTickResponse() {
		return ActionTick.Response.ResetEpoch;
	}
	@Override
	public Action clone() {
		return new Wait(this);
	}

}
