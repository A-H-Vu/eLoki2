#############
Script Module
#############

The Scripting module consists of all the classes used to interpret and run the custom script language that ``eLoki2`` uses. The main classes for the module are the scriptController class and the Action class. The scriptController class is the main class responsible for parsing and running the custom scripting language that ``eLoki2`` uses. The Action class is the main parent for all of the actions that the script is capable of executing. This section will cover an overview of how the scriptController class parses and runs the scripts as well as some details on the action class and how they work. As well as details on the script format and its general design.


FlowCharts
----------

.. image:: ../img/flow-script-parse.svg
    :width: 600

The flow chart for the parser portion of the scripting module is that of a fairly simple parser looping thorugh each line and parsing each independantly. There are two checks done by this module before it is used to construct the action class that is used for replay. The first check is a simple regex (``(?:(@[0-9]+) )?(.+)``)to ensure that the line is the expected format. The first part ``(?:(@[0-9]+) )?`` consists of the main capturing group which matches against an ``@`` followed by some numbers representing the timestamp of the action, the surrounding non-capturing group and ``?`` is to make this an optional part of the line format. The remaining part ``(.+)`` matches against everything else, the actual action name and any further arguments for the specific action. The regex is currenlty fairly permissive in what it allows and could be adjusted to be more specific i.e. only alphanumeric for action. The next check is done to ensure that the action actually exists before the action class is constructed and added to the list of actions to execute. The loop ends when there are no more lines/actions left to parse.


.. image:: ../img/flow-script-run.svg
    :width: 600

The script runner part of the module is also fairly simple with a core loop that executes each action in the script one by one. By default if there are any errors in executing a particular action then it is skipped, if there are more than ten errors in a row then it's likely due to some persistant error such as the browser being shutdown which leads to the quit state. The other aspect of the script runner is to calculate the delay between actions so that the replay is similar as possible to the original recording. 


Action
------

For eLoki2 an Action in the script is any arbitrary set of events that can be executed in a browser-like environment. The action class implments the code used to execute an action, for example moving the mouse or scrolling the window. Multiple actions can be combined into a single action class i.e. the mouseMoveScroll action. Which combines the windowScroll and mouseMove actions. Additionally as the action class is responsible for the exact format of the action in addition to its implementation i.e. for mouseMove, it follows the format of ``mouseMove x y`` where x and y are the co-ordinates to move to. 

Each action in a particular script has references to the previous and next actions in a script like a doubly linked list. There are methods such as ``chainNextAction`` or ``getNext`` that are used to populate and traverse this list. This aspect may change in the future i.e. moving the list aspect to an encapsulating class so that the individual action classes can be reused when recombining/rewriting scripts to generate custom sessions or creating multiple scripts with slight alterations. 


Script Parser
-------------

Reading lines
-------------

.. code-block:: java
    :lineno-start: 29

    //Various functions to parse a script file 
	public Action parseScript(List<String> lines) {
		initial = null;
		current = null;
		for (String line : lines) {
			parseLine(line);
		}
		return initial;
	}
	public Action parseScript(String[] lines) {
		initial = null;
		current = null;
		for(String line: lines) {
			parseLine(line);
		}
		return initial;
	}
	public Action parseScript(BufferedReader lines) {
		initial = null;
		current = null;
		try {
			while(lines.ready()) {
				parseLine(lines.readLine());
			}
		} catch (IOException e) {
			return null;
		}
		return initial;
	}

This is the main loop of the parser with different implementations reading lines from different sources. The current value holds the latest action parsed so that the next action can be easily added after it. The initial value holds the first action parsed.


Check regex
-----------

The following lines of code checks the if the line matches the regex and then extracts the action name from the line.

.. code-block:: java
    :lineno-start: 71

    parseMatcher.reset(line);
    if(!parseMatcher.matches()) {
        System.err.println("Error parsing format for "+line+", skipping");
        return;
    }
    
    String action = parseMatcher.group(2).split(" ")[0];

The regex checked is ``(?:(@[0-9]+) )?(.+)`` to ensure that the line is the expected format. The first part ``(?:(@[0-9]+) )?`` consists of the main capturing group which matches against an ``@`` followed by some numbers representing the timestamp of the action, the surrounding non-capturing group and ``?`` is to make this an optional part of the line format. The remaining part ``(.+)`` matches against everything else, the actual action name and any further arguments for the specific action. If the line does not match the regex then it is skipped. Line 77 extracts the action name from the line which is used in the action check next.


Check Action
------------

.. code-block:: java
    :lineno-start: 79

    if (actionMap.containsKey(action)) {
        ...
    } else {
        System.err.println("Unrecognized Action keyword " + action + ", skipping");
    }

This section uses the action name extracted in the previous section to check if it exists. The actionMap maps action classes to their names, this is populated in the :ref:`main_module` class. 


Create Action
-------------

.. code-block:: java
    :lineno-start: 80

    try {
        //Create the action using the string constructor for it
        Action n = actionMap.get(action).getConstructor(String.class).newInstance(parseMatcher.group(2));
        //Add the action to the action chain, if initial set as such
        if (initial == null) {
            initial = n;
            current = n;
        } else {
            // TODO figure out how to deal with issues like stuff not chaining due to some
            // error
            current.chainNextAction(n);
            current = n;
        }
    } catch (Exception e) {
        System.err.println("Error parsing action "+line+", skipping");
        e.printStackTrace();
        // TODO proper error handling
    }

This section of code creates the actual action class by calling the string constructer with the given line excluding the timestamp portion on line 82. The remaining lines add the action after the previous action as well as some error handling if there are any errors parsing the line in the action class. In general a line is skipped if there are any errors parsing it.


Script Runner
-------------

Script Runner Loop
------------------

.. code-block:: java

    while(current != null) {
        ..
    }

The main loop of the script runner is just a while loop checking if the current action being executed is null. There are some additional break points within the loop to deal with some edge cases.

Execute Action
--------------

The following lines executes the action and also handles any errors

.. code-block:: java
    :lineno-start: 126

    if(!skip) {
        //Execute the action
        try {
            current = current.execute(client);
            errors = 0;
        }catch(Exception e) {
            //Error handing, stop execution if too many error occur with the same action
            System.err.println("Error executing "+current.getRaw()+",skipping");
            current = current.getNextAction();
            e.printStackTrace();
            errors++;
            if(errors>10) {
                break;
            }
        }
    }
    else {
        current = current.getNextAction();
        skip = false;
    }

The entire execution block in lines 127-140 is wrapped in an if block that skips the execution of the action if the script replay is lagging behind. The details of when it is determined the script is lagging is explained in the next section.

The actual execution of the actioin is fairly simple calling the execute function with the given browser client on line 129. Line 130 resets the error counter so it only increments to 10 or more when there are multiple errors in a row. The remaining lines from 132-138 handle the error, skip the current action, increments the counter and breaks the main loop if there have been too many errors in a row.


Calculate Delay
---------------

.. code-block:: java
    :lineno-start: 146

    //Stuff to time actions to occur exactly as it was recorded
    //Reset epoch, if the previous action resets it to the end of its execution
    if(prev.getTick().getResponse()==ActionTick.Response.ResetEpochToEnd) {
        epoch = System.currentTimeMillis();
        epochRel = prev.getTick().getValue();
    }
    //end of script exit
    if(current == null)break;
    //Calculate the necessary delay to the next action allow ~ 2ms for webdriver communication etc.
    ActionTick.Response type = current.getTick().getResponse();
    long delay = 0;
    if(type == ActionTick.Response.Skippable||type == ActionTick.Response.UseTick) {
        delay = (current.getTick().getValue()-epochRel)-(System.currentTimeMillis()-epoch);
    }
    //Skip next action if delay is negative/lagging significantly
    //Only skip if it is not a significant action/can be skipped
    if(delay<-10&&type == ActionTick.Response.Skippable) {
        skip = true;
    }
    //Sleep for the specified delay
    if(delay > 2) {
        try {
            Thread.sleep(delay-2);
        } catch (InterruptedException e) {
            //presumably interrupted to stop script
            break;
        }
    }
    //Reset epoch if the next action is one that resets it to its start
    if(type == ActionTick.Response.ResetEpoch) {
        epoch = System.currentTimeMillis();
        epochRel = current.getTick().getValue();
    }

.. This part probably isn't explained that well, may need to go back and reword this in the future.

The delay between actions is calculated relative to certain actions on line 158 with the formula (nextAction Timestamp - epoch)-(currentTimestamp-epoch). The epochRel and epoch value in this case is the timestamp of a certain variable length action at the time it was recorded and when it was replayed. Some actions that may take varying amounts of time to execute include waiting for the page to load. Lines 148-151 and lines 175-178 reset the epoch values to either the end or beginning of these actions. 

.. image:: ../img/script-timeline.svg
    :width: 500

The image above gives an example of how the calculation occurs. In this example the next action occurs 50ms after the last action that reset the epoch (epochRel value). In the current replay it is 35ms after replaying that same action(epoch value). Thus the script runner will wait 15ms before executing the next action so that it's as close as possible to when it was originally executed. 


Lines 162-164 skips the next action if the program is currently lagging behind by more than 10ms in order to catch up. In general the actions that can be skipped are ones that should not affect the page state such as moving the mouse, or scrolling the window. Actions such as clicking on a link, navigating to a new page etc, cannot be skipped. 

Lines 165-174 executes the delay, by sleeping the thread, a slight correction of 2ms is done as it is assumed that the execution of the main loop takes ~2ms on average to avoid the relative accuracy of the replayed actions swinging around. 
