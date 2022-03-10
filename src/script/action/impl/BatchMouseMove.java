package script.action.impl;

import java.time.Duration;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionImpl;
import script.action.ActionTick;

public class BatchMouseMove extends ActionImpl implements Action {
	
	//array of points, x and y co-ordinates followed by duration in milliseconds
	String[] points;
	public BatchMouseMove(String raw) {
		super(raw);
		points = raw.substring(raw.indexOf(' ')+1).split(" ");
	}

	public BatchMouseMove(ActionImpl original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		if(client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient)client;
			Actions action = new Actions(sClient.getWebDriver());
			for(int i = 0; i<(points.length/3); i++) {
				action.tick(sClient.getPointerInput().createPointerMove(Duration.ofMillis(Integer.parseInt(points[i*3+2])),
						PointerInput.Origin.viewport(), Integer.parseInt(points[i*3]),Integer.parseInt(points[i*3+1])));
			}
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
		return new BatchMouseMove(this);
	}

}
