package script.action;

import java.time.Duration;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;

import clients.Client;
import clients.SeleniumClient;

/**
 * An action that moves the mouse to the given x, y location and scrolls the main
 * document location to the given scrollX and scrollY values.
 * @author Allen
 *
 */
//temporary class for compatibility with the current script generation
public class MouseMoveScroll extends Action {
	private int X;
	private int Y;
	private int scrollX;
	private int scrollY;
	
	public MouseMoveScroll(String raw) {
		super(raw);
		String[] args = raw.split(" ");
		X = Integer.parseInt(args[1]);
		Y = Integer.parseInt(args[2]);
		scrollX = Integer.parseInt(args[3]);
		scrollY = Integer.parseInt(args[4]);
	}

	@Override
	public Action execute(Client client) {

		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			Actions action = new Actions(sClient.getWebDriver());
			action.tick(sClient.getPointerInput().createPointerMove(Duration.ofMillis(1),
					PointerInput.Origin.viewport(), X, Y));
			action.perform();
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
