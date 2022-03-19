package script.action.impl;

import java.time.Duration;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionArgParser;
import script.action.ActionCompatibility;
import script.action.ActionImpl;
import script.action.ActionTick;

public class MouseDown extends ActionImpl implements Action {
	private int button;
	private int x;
	private int y;
	
	private String css;
	
	public MouseDown(String raw) {
		super(raw);
		ActionArgParser ap = new ActionArgParser(raw);
		//left mouse down default
		this.button = ap.getArgAsIntO(0).orElse(0);
		this.x = ap.getArgAsIntO(1).orElse(-1);
		this.y = ap.getArgAsIntO(2).orElse(-1);
		this.css = ap.getArgO(3).orElse("");
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
			if(x>=0&&y>=0) {
				action.tick(sClient.getPointerInput().createPointerMove(Duration.ofMillis(0),
				PointerInput.Origin.viewport(), x, y));
			}
			action.clickAndHold();
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
		return "mouseDown "+button;
	}
	
	@Override
	public Action clone() {
		return new MouseDown(this);
	}

	public int getButton() {
		return button;
	}
	//used by MouseUp to determine if its a click or a click and hold
	//can only emulate a click with javascript
	String getCSS() {
		return css;
	}

}
