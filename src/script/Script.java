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
				if(first.equals(d)) {
					first = c;
				}
				d.remove();
			}
		}
	}
	/**
	 * Removes all the actions from the first action given to the last action given.
	 * The actions given will both be removed.
	 * 
	 * 
	 * @param firstAction
	 * @param lastAction
	 * @return true if all actions were removed, false if either action does not exist in the script
	 * 	or the last action given ocurrs before the first action
	 */
	public boolean removeRange(Action firstAction, Action lastAction) {
		//check that both actions exist on the action chain first
		boolean existsF = false;
		boolean existsL = false;
		Action c = first;
		while(c!=null) {
			if(c.equals(firstAction)) {
				existsF = true;
			}
			if(c.equals(lastAction)) {
				//wrong order
				if(!existsF) return false;
				existsL = true;
				break;
			}
			c = c.getNextAction();
		}
		if(!existsF||!existsL) return false;
		Action prev = firstAction.getPreviousAction();
		Action next = lastAction.getNextAction();
		//handle various cases with either being the edge action
		if(prev==null&&next==null) {
			//entire script is deleted
			first = null;
		}
		if(prev==null) {
			//front of script is deleted
			first = next;
			//remove by chaining the actions before and after the range
			next.chainPreviousAction(prev);
		}
		else {
			prev.chainNextAction(next);
		}
		
		//sucessfully removed the range of actions
		return true;
	}
	public void forEach(Consumer<Action> consumer) {
		Action c = first;
		while(c!=null) {
			consumer.accept(c);
			c = c.getNextAction();
		}
	}
	
	public Script clone() {
		Script s = new Script(this.first.clone());
		Action a = first.getNextAction();
		Action sa = s.first;
		while(a!=null) {
			sa.chainNextAction(a.clone());
			a = a.getNextAction();
			sa = sa.getNextAction();
		}
		return s;
	}
	
}
