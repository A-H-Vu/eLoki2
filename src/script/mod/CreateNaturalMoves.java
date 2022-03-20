package script.mod;

import script.Script;
import script.action.Action;
import script.action.MousePositionAction;
import script.action.ScrollPositionAction;
import script.action.impl.NaturalMove;
import script.action.impl.ResizeWindow;

public class CreateNaturalMoves implements ScriptMod {

	public CreateNaturalMoves() {
	}

	@Override
	public Script modify(Script script) {
		Action c = script.getFirstAction();
		MousePositionAction firstMP = null;
		MousePositionAction lastMP = null;
		//previous scrollX/Y values
		int scrollX = Integer.MIN_VALUE;
		int scrollY = Integer.MIN_VALUE;
		int winW = 100;
		int winH = 100;
		boolean bMove = false;
		while(c!=null) {
			//during batchMove
			if(bMove) {
				//this is not a mousePositionAction
				if(!(c instanceof MousePositionAction)) {
					//mark last action as previous
					lastMP = (MousePositionAction)c.getPreviousAction();
				}
				//this action changes the scroll Position,
				else if(c instanceof ScrollPositionAction) {
					ScrollPositionAction spa = (ScrollPositionAction)c;
					if(spa.getScrollX()!=scrollX||spa.getScrollY()!=scrollY) {
						//mark last action as previous
						lastMP = (MousePositionAction)c.getPreviousAction();
					}
				}
				//end of chain
				else if(c.getNextAction()==null){
					lastMP = (MousePositionAction)c;
				}
				
			}
			else {
				if(c instanceof MousePositionAction) {
					//check if action changes scroll position
					if(c instanceof ScrollPositionAction) {
						ScrollPositionAction spa = (ScrollPositionAction)c;
						if(spa.getScrollX()==scrollX&&spa.getScrollY()==scrollY) {
							firstMP = (MousePositionAction)c;
							bMove = true;
						}
					}
					//normal move action
					else {
						firstMP = (MousePositionAction)c;
						bMove = true;
					}
				}
			}
			//update scrollX/Y values
			if(c instanceof ScrollPositionAction) {
				ScrollPositionAction spa = (ScrollPositionAction)c;
				scrollX = spa.getScrollX();
				scrollY = spa.getScrollY();
			}
			if(c instanceof ResizeWindow) {
				ResizeWindow rw = (ResizeWindow)c;
				winW = rw.getWidth();
				winH = rw.getHeight();
			}
			c = c.getNextAction();
			if(firstMP!=null&&lastMP!=null&&bMove) {
				//make sure it's more than one action
				if(firstMP!=lastMP) {
					StringBuffer buf = new StringBuffer();
					buf.append("naturalMove ");
					//screen size
					buf.append(winW+" "+winH+" ");
					//initial position
					buf.append(firstMP.getX()+" "+firstMP.getY()+" ");
					//destination position
					buf.append(lastMP.getX()+" "+lastMP.getY());
					NaturalMove nm = new NaturalMove(buf.toString());
					nm.setTickVal(lastMP.getTick().getValue());
					firstMP.insertPreviousAction(nm);
					if(!script.removeRange(firstMP, lastMP)) {
						System.out.println("CreateNaturalMove:error removing range");
					}
				}
				//reset
				firstMP = null;
				lastMP = null;
				bMove = false;
			}
		}
		return script;
	}

}
