package script.action;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import clients.Client;
import clients.SeleniumClient;

public class AwaitPageLoad extends Action {
	int timeout;

	public AwaitPageLoad(String raw) {
		super(raw);

	}

	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			WebDriverWait webDriverWait = new WebDriverWait(sClient.getWebDriver(), 180);
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

}
