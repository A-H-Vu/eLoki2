package script.action.impl;

import java.time.Duration;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionTick;

/**
 * Action that waits for the page to load checking for document.readyState === "complete
 * @author Allen
 *
 */
public class AwaitPageLoad extends Action {
	int timeout;

	public AwaitPageLoad(String raw) {
		super(raw);
	}
	
	public AwaitPageLoad(Action original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}

	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			WebDriverWait webDriverWait = new WebDriverWait(sClient.getWebDriver(), Duration.ofMinutes(3));
			webDriverWait.until((ExpectedCondition<Boolean>) wd -> {
				assert wd != null;
				return ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete");
			});
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
		return ActionTick.Response.ResetEpochToEnd;
	}

	@Override
	public Action clone() {
		return new AwaitPageLoad(this);
	}

}
