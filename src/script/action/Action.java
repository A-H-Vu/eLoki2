package script.action;

import clients.Client;

public interface Action {

	/**
	 * Get the original string representing the action
	 * 
	 * @return
	 */
	public String getRaw();

	/**
	 * Execute the action using the specified client
	 * 
	 * @param client
	 * @return next action to execute
	 */
	public Action execute(Client client);

	/**
	 * Checks whether the action is compatible with the current client
	 * @param client Client to check compatability with
	 * @return If it is compatiable or not and whether it is expected to break the script or not
	 */
	public ActionCompatibility checkComptability(Client client);

	/**
	 * Used to set the value of the action tick, if it is present in the script
	 * @param tick
	 */
	public void setTickVal(long tick);

	/**
	 * Time this action occurs from the start of the script in milliseconds Negative
	 * if the tick value is not set
	 * 
	 * @return
	 */
	public ActionTick getTick();

	// Stuff to manage the action chain
	// Note: this was sort of designed so that control flow can potentially be
	// implemented
	//chain functions chain the action to form a doubly linked list
	//set functions sets the next/prev action values directly without chaining them
	//get functions gets the next/prev actions
	//Additionally getNext is used to get the next action if the action cannot be executed/is skipped for any reason
	public void chainNextAction(Action next);

	public void chainPreviousAction(Action previous);

	public void setNextAction(Action next);

	public void setPreviousAction(Action previous);

	public Action getNextAction();

	public Action getPreviousAction();
	
	default long getDurationFromPrev() {
		ActionTick selfTick = getTick();
		ActionTick prevTick = getPreviousAction().getTick();
		if(selfTick.getResponse()==ActionTick.Response.Ignore) {
			return 1;
		}
		if(prevTick.getResponse()==ActionTick.Response.Ignore) {
			return 1;
		}
		return getTick().getValue()-getPreviousAction().getTick().getValue();
	}
	
	default long getDruationFromNext() {
		ActionTick selfTick = getTick();
		ActionTick nextTick = getNextAction().getTick();
		if(selfTick.getResponse()==ActionTick.Response.Ignore) {
			return 1;
		}
		if(nextTick.getResponse()==ActionTick.Response.Ignore) {
			return 1;
		}
		return getTick().getValue()-getNextAction().getTick().getValue();
	}

	/**
	 * Remove this action from the chain 
	 * @return the previous action
	 */
	public Action remove();
	
	public Action clone();

	public void insertNextAction(Action next);

	public void insertPreviousAction(Action prev);
}