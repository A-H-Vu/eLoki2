package script.action;

/**
 * 
 * Main class used to hold the timing information of a given action
 * and how it responds to timing delays
 * @author Allen
 *
 */
public class ActionTick {
	/**
	 * Timestamp of the action in milliseconds
	 */
	private long tick;
	/**
	 * What to do with the timing information
	 */
	private Response res;
	
	/**
	 * What the script Runner should do with the action 
	 * @author Allen
	 *
	 */
	public enum Response {
		Ignore, // no timing to the action ignore the tick value, execute immediately
		ResetEpoch, // reset epoch to the start of the action
		ResetEpochToEnd, // reset epoch to the end of the epoch
		UseTick,// Standard definition, just use the tick value keeping the epoch
		Skippable//Can skip this action if things are lagging behind, otherwise identical to UseTick
	}
	
	
	public ActionTick(long tick) {
		this(tick, Response.UseTick);
	}

	public ActionTick(long tick, Response response) {
		this.tick = tick;
		this.res = response;
	}

	public long getValue() {
		return tick;
	}

	public Response getResponse() {
		return res;
	}

}
