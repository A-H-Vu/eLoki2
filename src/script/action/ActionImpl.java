package script.action;

import clients.Client;

public abstract class ActionImpl implements Action {
	
	/**
	 * Raw input for the action before any parsing or changes
	 */
	private final String raw;
	/**
	 * Time this action occurs when initially recorded, in milliseconds
	 */
	protected ActionTick tick;
	
	// action chain
	protected Action next;
	protected Action previous;
	
	/**
	 * Create a new instance of the action
	 * @param raw 
	 */
	protected ActionImpl(String raw) {
		this.raw = raw;
		//default for if tick val is not set.
		tick = new ActionTick(0, ActionTick.Response.Ignore);
	}
	
	protected ActionImpl(ActionImpl original) {
		this.raw = original.getRaw();
		tick = new ActionTick(original.tick.getValue(), original.tick.getResponse());
	}

	/**
	 * Get the original string representing the action
	 * 
	 * @return
	 */
	@Override
	public String getRaw() {
		return raw;
	}

	/**
	 * Execute the action using the specified client
	 * 
	 * @param client
	 * @return next action to execute
	 */
	@Override
	public abstract Action execute(Client client);
	
	/**
	 * Checks whether the action is compatible with the current client
	 * @param client Client to check compatability with
	 * @return If it is compatiable or not and whether it is expected to break the script or not
	 */
	@Override
	public abstract ActionCompatibility checkComptability(Client client);
	
	/**
	 * 
	 * See {@link ActionTick.Response} for details on what each one does 
	 * @return
	 */
	protected ActionTick.Response actionTickResponse() {
		return ActionTick.Response.Ignore;
	}
	
	/**
	 * Used to set the value of the action tick, if it is present in the script
	 * @param tick
	 */
	@Override
	public void setTickVal(long tick) {
		this.tick = new ActionTick(tick, actionTickResponse());
	}

	/**
	 * Time this action occurs from the start of the script in milliseconds Negative
	 * if the tick value is not set
	 * 
	 * @return
	 */
	@Override
	public ActionTick getTick() {
		return tick;
	}

	// Stuff to manage the action chain
	// Note: this was sort of designed so that control flow can potentially be
	// implemented
	//chain functions chain the action to form a doubly linked list
	//set functions sets the next/prev action values directly without chaining them
	//get functions gets the next/prev actions
	//Additionally getNext is used to get the next action if the action cannot be executed/is skipped for any reason
	@Override
	public void chainNextAction(Action next) {
		this.next = next;
		next.setPreviousAction(this);
	}

	@Override
	public void chainPreviousAction(Action previous) {
		this.previous = previous;
		previous.setNextAction(this);
	}

	@Override
	public void setNextAction(Action next) {
		this.next = next;
	}

	@Override
	public void setPreviousAction(Action previous) {
		this.previous = previous;
	}

	@Override
	public Action getNextAction() {
		return next;
	}

	@Override
	public Action getPreviousAction() {
		return previous;
	}

	/**
	 * Remove this action from the chain 
	 * @return the previous action
	 */
	@Override
	public Action remove() {
		this.previous.setNextAction(this.next);
		this.next.setPreviousAction(this.previous);
		return this.previous;
	}
	
	@Override
	public abstract Action clone();
}
