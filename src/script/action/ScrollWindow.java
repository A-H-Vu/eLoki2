package script.action;

import clients.Client;
import clients.SeleniumClient;

public class ScrollWindow extends Action {
	private int scrollX;
	private int scrollY;

	public ScrollWindow(String raw) {
		super(raw);
		String[] args = raw.split(" ");
		scrollX = Integer.parseInt(args[1]);
		scrollY = Integer.parseInt(args[2]);
		
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

}
