package script.action;

/**
 * Interface defining an action that has the scrollX and scrollY
 * properties. These should reference the position of the window
 * according to the window.scrollTo(x,y) javascript function.
 * 
 * @author Allen
 *
 */

public interface ScrollPositionAction extends Action {
	public int getScrollX();
	public void setScrollX(int scrollX);
	public int getScrollY();
	public void setScrollY(int scrollY);
	public default ScrollPositionAction getPreviousScrollPosition() {
		Action prev = getPreviousAction();
		while(prev!=null&&!(prev instanceof ScrollPositionAction)) 
			prev = prev.getPreviousAction();
		if(prev!=null&&prev instanceof ScrollPositionAction) {
			return (ScrollPositionAction) prev;
		}
		else {
			return null;
		}
	}
	public default ScrollPositionAction getNextScrollPosition() {
		Action next = getNextAction();
		while(next!=null&&!(next instanceof ScrollPositionAction)) 
			next = next.getNextAction();
		if(next!=null&&next instanceof ScrollPositionAction) {
			return (ScrollPositionAction) next;
		}
		else {
			return null;
		}
	}
}
