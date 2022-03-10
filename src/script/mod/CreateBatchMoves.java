package script.mod;

import script.Script;
import script.action.Action;
import script.action.MousePositionAction;
import script.action.ScrollPositionAction;
import script.action.impl.BatchMouseMove;

public class CreateBatchMoves implements ScriptMod {

	public CreateBatchMoves() {}

	@Override
	public Script modify(Script script) {
		Action c = script.getFirstAction();
		MousePositionAction firstMP = null;
		MousePositionAction lastMP = null;
		//previous scrollX/Y values
		int scrollX = Integer.MIN_VALUE;
		int scrollY = Integer.MIN_VALUE;
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
			c = c.getNextAction();
			if(firstMP!=null&&lastMP!=null&&bMove) {
				//make sure it's more than one action
				if(firstMP!=lastMP) {
					StringBuffer buf = new StringBuffer();
					buf.append("batchMove");
					//start with first movement action
					Action d = firstMP;
					while(true) {
						MousePositionAction mpa = (MousePositionAction)d;
						long duration = 1;
						if(d!=firstMP) {
							duration = mpa.getDurationFromPrev(); 
						}
						buf.append(" "+mpa.getX()+" "+mpa.getY()+" "+duration);
						//ensure that the last one is also recorded
						if(d==lastMP) {
							break;
						}
						d = d.getNextAction();
					}
					BatchMouseMove bm = new BatchMouseMove(buf.toString());
					bm.setTickVal(firstMP.getTick().getValue());
					firstMP.insertPreviousAction(bm);
					if(!script.removeRange(firstMP, lastMP)) {
						System.out.println("CreateClickAndDrag:error removing range");
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
