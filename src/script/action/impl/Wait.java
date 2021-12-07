package script.action.impl;

import clients.Client;
import script.action.Action;
import script.action.ActionCompatibility;

public class Wait extends Action {
	int delay = 0;
	public Wait(String raw) {
		super(raw);
		String[] args = raw.split(" ");
		delay = Integer.parseInt(args[1]);
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

}
