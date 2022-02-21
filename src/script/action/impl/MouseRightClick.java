package script.action.impl;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;

import clients.Client;
import clients.SeleniumClient;
import script.action.ActionImpl;
import script.action.ActionCompatibility;
import script.action.Action;
import script.action.ActionTick;

/**
 * Action that executes a mouse right click at the current location
 * @author Allen
 *
 */
public class MouseRightClick extends ActionImpl {

	public MouseRightClick(String raw) {
		super(raw);
	}
	public MouseRightClick(Action original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}
	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			Actions action = new Actions(sClient.getWebDriver());
			action.tick(sClient.getPointerInput().createPointerDown(PointerInput.MouseButton.RIGHT.asArg()));
			action.tick(sClient.getPointerInput().createPointerUp(PointerInput.MouseButton.RIGHT.asArg()));
			action.perform();
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
		return ActionTick.Response.UseTick;
	}
	@Override
	public Action clone() {
		return new MouseRightClick(this);
	}

}
