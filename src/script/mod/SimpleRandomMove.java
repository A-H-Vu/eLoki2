package script.mod;

import java.util.Random;

import script.Script;
import script.action.MousePositionAction;

public class SimpleRandomMove implements ScriptMod {

	public SimpleRandomMove() {}

	@Override
	public Script modify(Script script) {
		script.forEach(a ->{
			if(a instanceof MousePositionAction) {
				MousePositionAction mv = (MousePositionAction)a;
				Random r = new Random();
				int xInit = mv.getX();
				int yInit = mv.getY();
				int i = 0;
				double epsilon = Math.PI/6;//15 deg angle in both directions
				//initial angle calculations, x,y are positions, t is theta, the angle
				//Stuff to calculate angle from previous position
				boolean prev = false;
				int xPrev = 0, yPrev = 0;
				double tPrev = 0;
				if(mv.getPreviousAction() instanceof MousePositionAction) {
					MousePositionAction preva = (MousePositionAction)mv.getPreviousAction();
					prev = true;
					xPrev = preva.getX();
					yPrev = preva.getY();
					tPrev = Math.atan2(yInit-yPrev, xInit-xPrev);
					if(xPrev==xInit&&yPrev==yInit) {
						prev = false;
					}
				}
				//stuff to calculate angle from next position
				boolean next = false;
				int xNext = 0, yNext = 0;
				double tNext = 0;
				if(mv.getNextAction() instanceof MousePositionAction) {
					MousePositionAction nexta = (MousePositionAction)mv.getNextAction();
					next = true;
					xNext = nexta.getX();
					yNext = nexta.getY();
					tNext = Math.atan2(yNext-yInit, xNext-xInit);
					if(xNext==xInit&&yNext==yInit) {
						next = false;
					}
				}
				while(true) {
					if(i>100) {
						mv.setX(xInit);
						mv.setY(yInit);
						break;
					}
					else {
						i++;
					}
					mv.setX(mv.getX()-5+r.nextInt(11));
					mv.setY(mv.getY()-5+r.nextInt(11));
					if(prev) {
						double trPrev = Math.atan2(mv.getY()-yPrev, mv.getX()-xPrev);
						double deltaPrev = Math.min(Math.abs(trPrev-tPrev), Math.abs(tPrev-trPrev));
						if(deltaPrev>epsilon) {
							continue;
						}
					}
					if(next) {
						double trNext = Math.atan2(yNext-mv.getY(), xNext-mv.getX());
						double deltaNext = Math.min(Math.abs(trNext-tNext), Math.abs(tNext-trNext));
						if(deltaNext>epsilon) {
							continue;
						}
					}
					break;
				}
			}
		});
		return script;
	}

}
