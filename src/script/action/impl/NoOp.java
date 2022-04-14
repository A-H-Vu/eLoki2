package script.action.impl;

import clients.Client;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionImpl;
import script.action.ActionTick;

public class NoOp extends ActionImpl {

	public NoOp(String raw) {
		super(raw);
	}

	public NoOp(ActionImpl original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		return super.next;
	}

	@Override
	public ActionCompatibility checkComptability(Client client) {
		return ActionCompatibility.Ok;
	}

	@Override
	public Action clone() {
		return new NoOp(this);
	}

}
