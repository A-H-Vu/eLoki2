package script.action.impl;

import java.time.Duration;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionTick;

/**
 * An action that moves the mouse to the given x, y location
 * @author Allen
 *
 */
public class MouseMove extends Action {

	// co-ordinates to move to
	private int x;
	private int y;

	public MouseMove(String raw) {
		super(raw);
		String[] args = raw.split(" ");
		x = Integer.parseInt(args[1]);
		y = Integer.parseInt(args[2]);
	}
	public MouseMove(Action original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}
	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			Actions action = new Actions(sClient.getWebDriver());
			action.tick(sClient.getPointerInput().createPointerMove(Duration.ofMillis(1),
					PointerInput.Origin.viewport(), x, y));
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
		return ActionTick.Response.Skippable;
	}
	@Override
	public Action clone() {
		return new MouseMove(this);
	}

}
