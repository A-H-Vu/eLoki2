package script.action;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;

import clients.Client;
import clients.SeleniumClient;

/**
 * Action that executes a mouse left click at the current location
 * @author Allen
 *
 */
public class MouseClick extends Action {

	public MouseClick(String raw) {
		super(raw);
	}

	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			Actions action = new Actions(sClient.getWebDriver());
			action.tick(sClient.getPointerInput().createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
			action.tick(sClient.getPointerInput().createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
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

}
