package script.action;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import clients.Client;
import clients.SeleniumClient;

public class AttachMouse extends Action{

	public AttachMouse(String raw) {
		super(raw);
	}
	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			sClient.getJSExecutor().executeScript("function enableCursor() {\n"
					+ "  var seleniumFollowerImg = document.createElement(\"img\");\n"
					+ "  seleniumFollowerImg.setAttribute('src', 'data:image/png;base64,'\n"
					+ "    + 'iVBORw0KGgoAAAANSUhEUgAAABQAAAAeCAQAAACGG/bgAAAAAmJLR0QA/4ePzL8AAAAJcEhZcwAA'\n"
					+ "    + 'HsYAAB7GAZEt8iwAAAAHdElNRQfgAwgMIwdxU/i7AAABZklEQVQ4y43TsU4UURSH8W+XmYwkS2I0'\n"
					+ "    + '9CRKpKGhsvIJjG9giQmliHFZlkUIGnEF7KTiCagpsYHWhoTQaiUUxLixYZb5KAAZZhbunu7O/PKf'\n"
					+ "    + 'e+fcA+/pqwb4DuximEqXhT4iI8dMpBWEsWsuGYdpZFttiLSSgTvhZ1W/SvfO1CvYdV1kPghV68a3'\n"
					+ "    + '0zzUWZH5pBqEui7dnqlFmLoq0gxC1XfGZdoLal2kea8ahLoqKXNAJQBT2yJzwUTVt0bS6ANqy1ga'\n"
					+ "    + 'VCEq/oVTtjji4hQVhhnlYBH4WIJV9vlkXLm+10R8oJb79Jl1j9UdazJRGpkrmNkSF9SOz2T71s7M'\n"
					+ "    + 'SIfD2lmmfjGSRz3hK8l4w1P+bah/HJLN0sys2JSMZQB+jKo6KSc8vLlLn5ikzF4268Wg2+pPOWW6'\n"
					+ "    + 'ONcpr3PrXy9VfS473M/D7H+TLmrqsXtOGctvxvMv2oVNP+Av0uHbzbxyJaywyUjx8TlnPY2YxqkD'\n"
					+ "    + 'dAAAAABJRU5ErkJggg==');\n"
					+ "  seleniumFollowerImg.setAttribute('id', 'selenium_mouse_follower');\n"
					+ "  seleniumFollowerImg.setAttribute('style', 'position: absolute; z-index: 99999999999; pointer-events: none; left:0; top:0');\n"
					+ "  document.body.appendChild(seleniumFollowerImg);\n"
					+ "  document.onmousemove = function (e) {\n"
					+ "    document.getElementById(\"selenium_mouse_follower\").style.left = e.pageX + 'px';\n"
					+ "    document.getElementById(\"selenium_mouse_follower\").style.top = e.pageY + 'px';\n"
					+ "  };\n"
					+ "};\n"
					+ "\n"
					+ "enableCursor();");
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

}
