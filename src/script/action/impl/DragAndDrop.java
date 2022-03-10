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

//temporary action to test drag and drop
public class DragAndDrop extends ActionImpl implements Action {
	//the array of points
	//first integer is the button to click and hold
	//it is then follwed by n triplets where for each triplet a b c
	//a = x position
	//b = y position
	//c = duration for the action in miliseconds
	String[] points;
	public DragAndDrop(String raw) {
		super(raw);
		
		points = raw.substring(raw.indexOf(' ')+1).split(" ");
	}

	
	public DragAndDrop(ActionImpl original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		if(client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient)client;
			Actions action = new Actions(sClient.getWebDriver());
			action.tick(sClient.getPointerInput().createPointerDown(Integer.parseInt(points[0])));
			for(int i = 0; i<(points.length/3)-1; i++) {
				action.tick(sClient.getPointerInput().createPointerMove(Duration.ofMillis(Integer.parseInt(points[i*3+3])),
						PointerInput.Origin.viewport(), Integer.parseInt(points[i*3+1]),Integer.parseInt(points[i*3+2])));
				
			}
			action.tick(sClient.getPointerInput().createPointerUp(Integer.parseInt(points[0])));
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
		// TODO Auto-generated method stub
		return null;
	}

}
