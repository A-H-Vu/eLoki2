package script;


import java.util.function.Consumer;
import java.util.function.Predicate;

import script.action.Action;

public class Script {
	private Action first;
	
	public Script(Action first) {
		this.first = first;
	}
	
	public Action getFirstAction() {
		return first;
	}
	
	public void removeActions(Predicate<Action> removePredicate) {
		Action c = first;
		while(c!=null) {
			if(removePredicate.test(c)) {
				Action d = c;
				c = c.getNextAction();
				d.remove();
			}
		}
	}
	public void forEach(Consumer<Action> consumer) {
		Action c = first;
		while(c!=null) {
			consumer.accept(c);
			c = c.getNextAction();
		}
	}
	
}
