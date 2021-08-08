package script.action;

import java.time.Duration;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;

import clients.Client;
import clients.SeleniumClient;

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

}
