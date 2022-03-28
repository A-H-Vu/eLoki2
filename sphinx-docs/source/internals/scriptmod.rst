..
  Normally, there are no heading levels assigned to certain characters as the structure is
  determined from the succession of headings. However, this convention is used in Python’s
  Style Guide for documenting which you may follow:

  # with overline, for parts
  * for chapters
  = for sections
  - for subsections
  ^ for subsubsections
  " for paragraphs


####################
Script Modifications
####################

Script modifications are the main method to create more complex scripts by examining the loaded actions and modifying them to fix issues, apply changes and insert new actions. The basic interface for a modification is under ``script/mod/ScriptMod.java`` with one function ``public Script modify(Script script);``. The function takes a script and returns the same script, both the script returned and the script given as an argugment should be the same modified script.

The ``script/Script.java`` class is a wrapper around an action chain and contains some convenience  functions to make it easier to remove actions or to iterate through the actions. 

RandomMove
----------

The SimpleRandomMove modification randomly adjusts actions that move the mouse to a slightly different position. 

.. code-block:: java
    :lineno-start: 15

    if(a instanceof MousePositionAction) {
        MousePositionAction mv = (MousePositionAction)a;
        Random r = new Random();
        int xInit = mv.getX();
        int yInit = mv.getY();
        int i = 0;
        double epsilon = Math.PI/6;//15 deg angle in both directions

The first section initiates most of the variables. After checking to make sure the action has a mouse position, it sets the initial/original x and y position.

.. code-block:: java
    :lineno-start: 22

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
            //avoid adjusting points where there are two identical ones in a row
            ///to avoid odd jumping around
            return;
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
            //avoid adjusting points where there are two identical ones in a row
            ///to avoid odd jumping around
            return;
        }
    }

The next two blocks of code check if the previous/next action is also one with a mouse position value. If they are then their x and y values are set and the angle is calculated. 

.. code-block::
    :lineno-start: 55

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

The final block of code generates a random new point within the 11x11 grid and checks to make sure that the angles to the previous/next points are within the epsilon. 


Grouping movement
-----------------

Both the BatchMove, DragDrop and NaturalMove actions are created using a variation of this algorithmn to group a bunch of mouse movement together. The example below is from CreateBatchMoves.java

.. code-block::
    :lineno-start: 15

    Action c = script.getFirstAction();
    MousePositionAction firstMP = null;
    MousePositionAction lastMP = null;
    //previous scrollX/Y values
    int scrollX = Integer.MIN_VALUE;
    int scrollY = Integer.MIN_VALUE;
    boolean bMove = false;

The first bit initializes some variables. The main ones are the first/lastMP which records the first/last action in a grouping. The boolean records the state of whether the current action will be grouped or not convenience variable for clarity, it is equal to firstMP!=null. ScrollX/Y are to detect when the window scrolling has changed which breaks up the grouping as the composite actions cannot handle scrolling. 


.. code-block::
    :lineno-start: 22

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
            if(c.getNextAction()==null&&lastMP==null){
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

Within the ``if(bMove)`` block there are three checks done to see if the grouping should stop. The first if the next action is not one with a mouse position value. The next if checks if the scroll position has changed. The final if checks if the current action is the last one in the script.

Within the else block there is one main check done to see if the action has a mouse position value. The second if checks to make sure that the current action does not change the scroll position, as the composite actions cannot handle scrolling.


.. code-block::
    :lineno-start: 61

    //update scrollX/Y values
    if(c instanceof ScrollPositionAction) {
        ScrollPositionAction spa = (ScrollPositionAction)c;
        scrollX = spa.getScrollX();
        scrollY = spa.getScrollY();
    }
    c = c.getNextAction();

The next set of lines updates the previous scroll position and goes onto the next action. 

.. code-block::
    :lineno-start: 68

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

The final block is where the actions are grouped by going through each one, extracting the relevant values and creating the composite action. 