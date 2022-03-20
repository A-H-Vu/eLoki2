package script.action.impl;

import java.awt.Dimension;
import java.awt.Point;
import java.time.Duration;

import javax.swing.plaf.DimensionUIResource;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;

import com.github.joonasvali.naturalmouse.api.MouseInfoAccessor;
import com.github.joonasvali.naturalmouse.api.MouseMotion;
import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.api.SystemCalls;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionArgParser;
import script.action.ActionCompatibility;
import script.action.ActionImpl;
import script.action.ActionTick;

public class NaturalMove extends ActionImpl implements Action {
	//screen size
	private int w;
	private int h;
	//initial position
	private int ix = -1;
	private int iy = -1;
	//final position
	private int fx = -1;
	private int fy = -1;
	public NaturalMove(String raw) {
		super(raw);
		ActionArgParser ap = new ActionArgParser(raw);
		if(ap.argLength()==6) {
			w = ap.getArgAsIntO(0).orElseThrow();
			h = ap.getArgAsIntO(1).orElseThrow();
			ix = ap.getArgAsIntO(2).orElseThrow();
			iy = ap.getArgAsIntO(3).orElseThrow();
			fx = ap.getArgAsIntO(4).orElseThrow();
			fy = ap.getArgAsIntO(5).orElseThrow();
		}
		else {
			throw new IllegalArgumentException("Incorrect number of parameters for NaturalMove");
		}
	}

	public NaturalMove(ActionImpl original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		if(client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient)client;
			MouseMotionFactory factory = new MouseMotionFactory();
			SeleniumMouseMotionProvider mmp = new SeleniumMouseMotionProvider(w,h,ix,iy,sClient);
			factory.setMouseInfo(mmp);
			factory.setSystemCalls(mmp);
			MouseMotion mm = factory.build(fx, fy);
			try {
				mm.move();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
		return new NaturalMove(this);
	}
	private static class SeleniumMouseMotionProvider implements SystemCalls, MouseInfoAccessor{
		private int x;
		private int y;
		private int w;
		private int h;
		private SeleniumClient client;
		private SeleniumMouseMotionProvider(int w, int h, int x, int y, SeleniumClient sClient) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.client = sClient;
		}
		
		@Override
		public Point getMousePosition() {
			return new Point(x,y);
		}

		@Override
		public long currentTimeMillis() {
			return System.currentTimeMillis();
		}

		@Override
		public void sleep(long time) throws InterruptedException {
			Thread.sleep(time);
		}

		@Override
		public Dimension getScreenSize() {
			return new DimensionUIResource(w, h);
		}

		@Override
		public void setMousePosition(int x, int y) {
			//currently just update position with the desired position
			this.x = x;
			this.y = y;
			Actions action = new Actions(client.getWebDriver());
			action.tick(client.getPointerInput().createPointerMove(Duration.ofMillis(1),
					PointerInput.Origin.viewport(), x, y));
			action.perform();
		}
		
	}

}
