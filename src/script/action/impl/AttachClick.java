package script.action.impl;

import clients.Client;
import clients.SeleniumClient;
import script.action.Action;
import script.action.ActionCompatibility;
import script.action.ActionImpl;
import script.action.ActionTick;

public class AttachClick extends ActionImpl{

	public AttachClick(String raw) {
		super(raw);
	}
	public AttachClick(Action original) {
		this(original.getRaw());
		this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());
	}
	@Override
	public Action execute(Client client) {
		if (client instanceof SeleniumClient) {
			SeleniumClient sClient = (SeleniumClient) client;
			sClient.getJSExecutor().executeScript("function enableClickTarget() {\n"
					+ "  if(document.querySelector(\"#selenium_mouse_clicks\")!==null) return;"
					+ "  var seleniumFollowerImg = document.createElement(\"img\");\n"
					+ "  seleniumFollowerImg.setAttribute('src', 'data:image/png;base64,'\n"
					+ "    + 'iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAACXBIWXMAAC4jAAAuI'\n"
					+ "    + 'wF4pT92AAAAOklEQVQY02NkQAX//0MZjCgUAwMTA5GAaIUsDAwM//HIw+UY/+NXSL'\n"
					+ "    + 'rVjOhWo/kaxY2M2NzEQPPgAQDBVgkSqqOwzQAAAABJRU5ErkJggg==');\n"
					+ "  seleniumFollowerImg.setAttribute('id', 'selenium_mouse_clicks');\n"
					+ "  seleniumFollowerImg.setAttribute('style', 'position: absolute; z-index: 99999999999; pointer-events: none; left:0; top:0');\n"
					+ "  document.body.appendChild(seleniumFollowerImg);\n"
					+ "  document.addEventListener('mousedown',function (e) {\n"
					+ "    document.getElementById(\"selenium_mouse_clicks\").style.left = (e.pageX-5) + 'px';\n"
					+ "    document.getElementById(\"selenium_mouse_clicks\").style.top = (e.pageY-5) + 'px';\n"
					+ "  });\n"
					+ "};\n"
					+ "\n"
					+ "enableClickTarget();");
			//SVGs get overwritten too easily/it's very difficult get the svg to reliably display, relying on the same image trick
//			sClient.getJSExecutor().executeScript("function enableClick() {\n"
//					+ "  if(document.querySelector(\"#selenium_mouse_clicks\")!==null) return;\n"
//					+ "  var containerDiv = document.createElement('div');\n"
//					+ "  containerDiv.setAttribute('style', 'position:absolute; pointer-events: none; height:20px; width:20px; left:0; top:0');\n"
//					+ "  var seleniumClickSVG = document.createElement(\"svg\");\n"
//					+ "  seleniumClickSVG.setAttribute('id', 'selenium_mouse_clicks');\n"
//					+ "  seleniumClickSVG.setAttribute('style', 'position: absolute; z-index: 99999999999; pointer-events: none; height:20px; width:20px; left:0; top:0');\n"
//					+ "  seleniumClickSVG.setAttribute('Height', '20px');\n"
//					+ "  seleniumClickSVG.setAttribute('Width', '20px');\n"
//					+ "  seleniumClickSVG.setAttribute('viewBox', '-10 -10 20 20');\n"
//					+ "  seleniumSVGCircle = document.createElement('circle');\n"
//					+ "  seleniumSVGCircle.setAttribute('r','10');\n"
//					+ "  seleniumSVGCircle.setAttribute('stroke-width','2');\n"
//					+ "  seleniumSVGCircle.setAttribute('stroke','red');\n"
//					+ "  seleniumSVGCircle.setAttribute('fill-opacity','0');\n"
//					+ "  seleniumClickSVG.appendChild(seleniumSVGCircle);\n"
//					+ "  containerDiv.appendChild(seleniumClickSVG);\n"
//					+ "  document.body.appendChild(containerDiv);\n"
//					+ "  document.addEventListener('mousedown', function (e) {\n"
//					+ "    document.getElementById(\"selenium_mouse_clicks\").style.left = (e.pageX-10) + 'px';\n"
//					+ "    document.getElementById(\"selenium_mouse_clicks\").style.top = (e.pageY-10) + 'px';\n"
//					+ "  });\n"
//					+ "};\n"
//					+ "\n"
//					+ "enableClick();");
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
		return new AttachClick(this);
	}

}
