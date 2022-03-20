package script.action.impl;

import java.time.Duration;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;

import clients.Client;
import clients.SeleniumClient;
import script.action.ActionImpl;
import script.action.ActionCompatibility;
import script.action.Action;
import script.action.ActionArgParser;
import script.action.ActionTick;

/**
 * Action that executes a mouse right click at the current location
 * @author Allen
 *
 */
public class MouseRightClick extends ActionImpl {
	private int x;
	private int y;
	
	public MouseRightClick(String raw) {
		super(raw);
		ActionArgParser ap = new ActionArgParser(raw);
		x = ap.getArgAsIntO(0).orElse(-1);
		y = ap.getArgAsIntO(1).orElse(-1);
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
			if(x>=0&&y>=0) {
				action.tick(sClient.getPointerInput().createPointerMove(Duration.ofMillis(0),
				PointerInput.Origin.viewport(), x, y));
			}
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
