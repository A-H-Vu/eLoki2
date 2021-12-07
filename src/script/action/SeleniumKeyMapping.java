package script.action;

import org.openqa.selenium.Keys;

public enum SeleniumKeyMapping {
	//Reference for the key events from the js side
	//https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values
	Alt("Alt",Keys.ALT),
	CapsLock("CapsLock",Keys.NULL),
	Control("Control",Keys.CONTROL),
	F1("F1",Keys.F1),
	F2("F2",Keys.F2),
	F3("F3",Keys.F3),
	F4("F4",Keys.F4),
	F5("F5",Keys.F5),
	F6("F6",Keys.F6),
	F7("F7",Keys.F7),
	F8("F8",Keys.F8),
	F9("F9",Keys.F9),
	F10("F10",Keys.F10),
	F11("F11",Keys.F11),
	F12("F12",Keys.F12),
	Meta("Meta",Keys.META),
	Shift("Shift", Keys.SHIFT),
	Enter("Enter",Keys.ENTER),
	Tab("Tab",Keys.TAB),
	ArrowDown("ArrowDown",Keys.ARROW_DOWN),
	ArrowLeft("ArrowLeft",Keys.ARROW_LEFT),
	ArrowRight("ArrowRight",Keys.ARROW_RIGHT),
	ArrowUp("ArrowUp",Keys.ARROW_UP),
	End("End",Keys.END),
	Home("Home",Keys.HOME),
	PageDown("PageDown",Keys.PAGE_DOWN),
	PageUp("PageUp",Keys.PAGE_UP),
	Backspace("Backspace",Keys.BACK_SPACE),
	Clear("Clear",Keys.CLEAR),
	Delete("Delete",Keys.DELETE),
	Escape("Escape",Keys.ESCAPE),
	Decimal("Decimal",Keys.DECIMAL),
	Multiply("Multiply",Keys.MULTIPLY),
	Add("Add",Keys.ADD),
	Divide("Divide",Keys.DIVIDE),
	Subtract("Subtract",Keys.SUBTRACT),
	Separator("Separator",Keys.SEPARATOR);
	
	private String keys;
	private Keys SeleniumKey;
	
	SeleniumKeyMapping(String keys, Keys SeleniumKey){
		this.keys = keys;
		this.SeleniumKey = SeleniumKey;
	}
	public Keys SeleniumKey() {
		return SeleniumKey;
	}
	public String key() {
		return keys;
	}

}
