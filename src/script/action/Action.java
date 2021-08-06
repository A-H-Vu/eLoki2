package script.action;

import clients.Client;

public abstract class Action {

	private final String raw;
	/**
	 * Time this action occurs from the start of the script in milliseconds
	 */
	protected ActionTick tick;

	// action chain
	protected Action next;
	protected Action previous;

	protected Action(String raw) {
		this.raw = raw;
		//default for if tick val is not set.
		tick = new ActionTick(0, ActionTick.Response.Ignore);
	}

	/**
	 * Get the original string representing the action
	 * 
	 * @return
	 */
	public String getRaw() {
		return raw;
	}

	/**
	 * Execute the action using the specified client
	 * 
	 * @param client
	 * @return next action to execute
	 */
	public abstract Action execute(Client client);

	public abstract ActionCompatibility checkComptability(Client client);
	
	protected ActionTick.Response actionTickResponse() {
		return ActionTick.Response.Ignore;
	}
	
	public void setTickVal(long tick) {
		this.tick = new ActionTick(tick, actionTickResponse());
	}

	/**
	 * Time this action occurs from the start of the script in milliseconds Negative
	 * if the tick value is not set
	 * 
	 * @return
	 */
	public ActionTick getTick() {
		return tick;
	}

	// Stuff to manage the action chain
	// Note: this was sort of designed so that control flow can potentially be
	// implemented
	public void chainNextAction(Action next) {
		this.next = next;
		next.setPreviousAction(this);
	}

	public void chainPreviousAction(Action previous) {
		this.previous = previous;
		previous.setNextAction(this);
	}

	public void setNextAction(Action next) {
		this.next = next;
	}

	public void setPreviousAction(Action previous) {
		this.previous = previous;
	}

	public Action getNextAction() {
		return next;
	}

	public Action getPreviousAction() {
		return previous;
	}

	public Action remove() {
		this.previous.setNextAction(this.next);
		this.next.setPreviousAction(this.previous);
		return this.previous;
	}
}
