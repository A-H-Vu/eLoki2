package script.action.impl;

import org.openqa.selenium.interactions.Actions;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionImpl;
import script.action.ActionTick;

public class MouseUp extends ActionImpl implements Action {
	private int button;
	private String css;
	public MouseUp(String raw) {
		super(raw);
		String[] args = raw.split(" ");
		button = Integer.parseInt(args[1]);
		if(args.length>2) {
			css = args[2];
		}
	}

	public MouseUp(Action original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}
	
	//TODO logic to determine if it's a click vs press and hold or drag
	//TODO also code in right clicks
	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			Actions action = new Actions(sClient.getWebDriver());
			action.release();
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
	public String getRaw() {
		return "mouseUp "+button;
	}
	@Override
	public Action clone() {
		return new MouseUp(this);
	}
	
	public int getButton() {
		return button;
	}
	
	
	//used by MouseDown to determine if its a click or a click and hold
	//can only emulate a click with javascript
	//currently unused
	String getCSS() {
		return css;
	}

}
