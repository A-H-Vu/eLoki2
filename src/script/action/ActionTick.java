package script.action;

public class ActionTick {
	private long tick;
	private Response res;

	public enum Response {
		Ignore, // no timing to the action ignore the tick value
		ResetEpoch, // reset epoch to the start of the action
		ResetEpochToEnd, // reset epoch to the end of the epoch
		UseTick// Standard definition, just use the tick value keeping the epoch
	}

	public ActionTick(long tick) {
		this(tick, Response.UseTick);
	}

	public ActionTick(long tick, Response response) {
		this.tick = tick;
		this.res = response;
	}

	public long getTick() {
		return tick;
	}

	public Response getResponse() {
		return res;
	}

}
