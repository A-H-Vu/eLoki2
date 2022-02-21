package script.action;

public interface MousePositionAction extends Action{
	
	public int getX();
	public void setX(int x);
	public int getY();
	public void setY(int y);
	public default MousePositionAction getPreviousMousePosition() {
		Action prev = getPreviousAction();
		while(prev!=null&&!(prev instanceof MousePositionAction)) 
			prev = prev.getPreviousAction();
		if(prev!=null&&prev instanceof MousePositionAction) {
			return (MousePositionAction) prev;
		}
		else {
			return null;
		}
	}
	public default MousePositionAction getNextMousePosition() {
		Action next = getNextAction();
		while(next!=null&&!(next instanceof MousePositionAction)) 
			next = next.getNextAction();
		if(next!=null&&next instanceof MousePositionAction) {
			return (MousePositionAction) next;
		}
		else {
			return null;
		}
	}
}
