package script.mod;

import script.Script;
import script.action.Action;
import script.action.MousePositionAction;
import script.action.impl.DragAndDrop;
import script.action.impl.MouseDown;
import script.action.impl.MouseUp;

/**
 * A script modifier that converts mousedown followed by mouse movements and finally mousedown with a single click and drag action
 * 
 * Necessary with Selenium for clicking and dragging to work
 * @author Allen
 *
 */
public class CreateClickAndDrag implements ScriptMod {
	
	//empty constructor as currently no params
	public CreateClickAndDrag() {
	}

	@Override
	public Script modify(Script script) {
		//Search for the following pattern
		//MouseDown
		//numerous mouseMove type actions
		//MouseUp with the same button
		Action c = script.getFirstAction();
		MouseDown mDown = null;
		MouseUp mUp = null;
		int moveActions = 0;
		boolean cDrag = false;
		while(c!=null) {
			//going through clicking and drag portion
			if(cDrag) {
				//mouseUp, end of drag
				if(c instanceof MouseUp) {
					mUp = (MouseUp)c;
					if(mUp.getButton()!=mDown.getButton()) {
						//break as this class is unable to handle such cases
						//where multiple buttons pressed at once
						mDown = null;
						mUp = null;
						moveActions = 0;
						cDrag = false;
					}
				}
				else if(c instanceof MousePositionAction) {
					moveActions++;
				}
				else {
					//some other action ocurrs in between click start and end
					//break as this class is unable to handle such cases
					mDown = null;
					mUp = null;
					moveActions = 0;
					cDrag = false;
				}
			}
			//look for a mousedown action
			else {
				if(c instanceof MouseDown) {
					mDown = (MouseDown)c;
					cDrag = true;
				}
			}
			c = c.getNextAction();
			//Logic to collect actions after the iterator to avoid removing from the chain the action `c`
			if(mDown!=null&&mUp!=null&&cDrag) {
				System.out.println("moveActions "+moveActions);
				//use string buffer to build command string
				StringBuffer buf = new StringBuffer();
				buf.append("dragDrop");
				buf.append(" "+mDown.getButton());
				Action d = mDown.getNextAction();
				//loop through all the position actions in the mouse down->up
				while(d!=mUp) {
					MousePositionAction mpa = (MousePositionAction)d;
					buf.append(" "+mpa.getX()+" "+mpa.getY()+" "+mpa.getDurationFromPrev());
					d = d.getNextAction();
				}
				DragAndDrop dg = new DragAndDrop(buf.toString());
				dg.setTickVal(mDown.getTick().getValue());
				mDown.insertPreviousAction(dg);
				
				if(!script.removeRange(mDown, mUp)) {
					System.out.println("CreateClickAndDrag:error removing range");
				}
				//reset everything
				cDrag = false;
				mDown = null;
				mUp = null;
				moveActions = 0;
			}
		}
		
		return script;
	}

}
