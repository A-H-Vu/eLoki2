package script.action.impl;

import clients.Client;
import clients.SeleniumClient;
import script.action.ActionImpl;
import script.action.ActionCompatibility;
import script.action.Action;
import script.action.ActionTick;
import script.action.ScrollPositionAction;

/**
 * Scrolls the main document window to the given scrollX and scrollY values
 * @author Allen
 *
 */
public class ScrollWindow extends ActionImpl implements ScrollPositionAction{
	private int scrollX;
	private int scrollY;

	public ScrollWindow(String raw) {
		super(raw);
		String[] args = raw.split(" ");
		scrollX = Integer.parseInt(args[1]);
		scrollY = Integer.parseInt(args[2]);
	}
	public ScrollWindow(Action original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			sClient.getJSExecutor().executeScript(String.format("window.scrollTo(%d, %d);", scrollX, scrollY));
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
		return ActionTick.Response.Skippable;
	}
	@Override
	public Action clone() {
		return new ScrollWindow(this);
	}
	public int getScrollX() {
		return scrollX;
	}
	public void setScrollX(int scrollX) {
		this.scrollX = scrollX;
	}
	public int getScrollY() {
		return scrollY;
	}
	public void setScrollY(int scrollY) {
		this.scrollY = scrollY;
	}

}
