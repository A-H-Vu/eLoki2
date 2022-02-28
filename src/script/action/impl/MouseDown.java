package script.action.impl;

import org.openqa.selenium.interactions.Actions;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionImpl;
import script.action.ActionTick;

public class MouseDown extends ActionImpl implements Action {
	private int button;
	private String css;
	
	public MouseDown(String raw) {
		super(raw);
		String[] args = raw.split(" ");
		button = Integer.parseInt(args[1]);
		if(args.length>2) {
			css = args[2];
		}
	}

	public MouseDown(ActionImpl original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			Actions action = new Actions(sClient.getWebDriver());
			action.tick(sClient.getPointerInput().createPointerUp(button));
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
	public Action clone() {
		return new MouseDown(this);
	}
	
	//used by MouseUp to determine if its a click or a click and hold
	//can only emulate a click with javascript
	String getCSS() {
		return css;
	}

}
