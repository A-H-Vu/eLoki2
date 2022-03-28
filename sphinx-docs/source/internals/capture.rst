##############
Capture Module
##############


The capture module is primarily responsible for recording the sessions that can be replayed by the replay module. There are two main modes for the capture module, the default iframe mode and an alternative passive mode. The iframe mode records a session be embedding the page to record in an iframe, this is generally the more user friendly way as there are controls on the screen to see what is going on and thus the default. The passive method records constantly while the user goes through the session injecting the javascript when the page changes. This method is generally not preferred currently as there is less control and less testing done with this method.


The javascript used may be documented later.

FlowCharts
----------

Iframe method

.. image:: ../img/flow-capture-iframe.svg
    :width: 500

The flow of the iframe method follows a generally linear flow starting with injecting the javascript used to generate the main landing page. Once the user enters a url to record it then transitions to the ready state and injects the actual recording script after navigating to the page. The program then waits for user input to either return to the main page or to quit enitrely. If there are any errors at any point then it loops back to the main page.

------

Passive method

.. image:: ../img/flow-capture-passive.svg
    :width: 600



The flow chart for the passive method is similar to the iframe method with a few differences. The first step is again to inject the script to display the main page after launching the client. However unlike the ifrae method where the quit button is on the recording page it is on the main page as there are no user interfaces other than the main page. Once the user visits the page they want to record and the url changes then the program will automatically enter the recording loop. The recording loop consists of injecting the recording script, waiting for the url to change and then saving what has been recorded on the page, if the url changes to about:blank from the user clicking off the page then it will return to the main page where the recorded script can be downloaded/displayed.

.. warning::

    The passive method has currently very buggy and difficult to get working properly. The issue with sites blocking iframes has been largely resolved by loading extensions that block 


Iframe method
-------------

Inject Main Page
----------------

.. code-block:: java
    :lineno-start: 70

    mainloop: while(true){
        System.out.println("displaying main landing page");
        webdriver.get("about:blank");
        jsExec.executeScript(mainPage, status);
        ...
    }

The following bit is code just injects the main page and navigates to the about:blank page at the start of the main loop.

Check Status
------------

.. code-block:: java
    :lineno-start: 79

    String currentURL = webdriver.getCurrentUrl();
    while(true){
        //Avoid switching if it is a blob: url as it is used to download the recording
        if(!webdriver.getCurrentUrl().equals(currentURL)&&!webdriver.getCurrentUrl().startsWith("blob:")){
            continue mainloop;
        }
        try{
            Boolean val = ((Boolean)((JavascriptExecutor)webdriver).executeScript("return ready;"));
            if(val!=null&&val){
                break;
            }
        }catch(JavascriptException e){
            //likely due to ready being undefined as it is not on the about:blank page
            continue mainloop;
        }
        try {
            Thread.sleep(10);
        }catch(InterruptedException e1) {}
    }

This block of code is a hot loop executed every 10ms that does a couple of checks. Lines 82-84 checks if the url has changed unexpectedly and returns to the main landing page if it is the case(may not be necessary could potentially just set that as the url instead of needing the user to input to the form). Lines 85-93 checks the a variable on the page which indicates that the user has entered a valid url into the form and breaks the hot loop moving onto the next section.


Inject recording Script
-----------------------

.. code-block:: java
    :lineno-start: 100

    String recordURL = ((String)jsExec.executeScript("return document.getElementById('iURL').value;"));
    try {
        webdriver.get(recordURL);
    }catch(WebDriverException e) {
        System.err.println(e.getMessage());
        System.err.println("Error visiting URL "+recordURL);
        status = "Error getting page "+recordURL;
        try {
            //sleep so the user can see the error page before going back to the main page. 
            Thread.sleep(5000);
        }catch(InterruptedException e1) {}
        continue;
    }

    //Wait for the page to fully load
    wait.until((ExpectedCondition<Boolean>) wd -> {
        return ((Boolean)((JavascriptExecutor)wd).executeScript("return document.readyState == 'complete';"));
    });

    //Inject the javascript which will clear the page and embed it in an iframe along with a control bar on the top
    System.out.println("injecting js");
    jsExec.executeScript(js);
    System.out.println("Waiting for state change");


Lines 100-112 gets the url from the form element and then attempst to navigate the browser to the page. If there are any errors then it loops back to the main page setting a status variable which is used to display messages on the main page. 
Lines 114-117 waits for the page to load before injecting the javascript on line 121.


Check Status
------------

.. code-block:: java
    :lineno-start: 124

    //The hotloop below does the following
    //Check the state variable on the page act depending on the value
    //"active" - do nothing, recordign script is still active
    //"reset" - continue loop in order to go back to the main page
    //"quit" - break loop to quit/close the browser
    //This is the point at which the user is recording the session, most of the session recording logic is in mouseCapture.js
    currentURL = webdriver.getCurrentUrl();
    String state;
    while(true){
        state = ((String)((JavascriptExecutor)webdriver).executeScript("if(typeof state !== 'undefined'){console.log(state); return state;}return 'active'"));
        if(!webdriver.getCurrentUrl().equals(currentURL)){
            state = "reset";
            break;
        }
        if(!"active".equals(state)){
            break;
        }
        try {
            Thread.sleep(10);
        }catch(InterruptedException e1) {}
    }
    if("quit".equals(state)) break;

This section just checks for state changes and responds accordingly. Line 133 retrieves the current value of the state variable. Lines 134-137 checks if the url has changed unexpectedly and will change the state to reset moving back to the main page. Lines 138-140 breaks the loop if the state changes of which it can be either quit or reset. Reset continues the loop while quit breaks the loop on line 145.


Injected Javascript
-------------------

The javascript used to record the session files(mouseCapture.js) largely works by listening on various event handlers on the iframe and recording the actions into an internal list. Lines 1-90 contain mostly utility functions as well as some comments describing the code. The init function is called one the script is fully loaded and initalizes the header and the various listeners. Lines 99-213 overwrites the current page and sets up the recording header. Lines 218-294 sets up variables used while recording and sets up some listeners used to handle recording. The iframeURLChange is used to toggle off capturing when the iframe url changes and wait till the new pages loads before continuing recording. Lines 297-458 contains the bulk of the event handlers that are used to record a session. Lines 462-505 are various utility functions used to print the recorded sessions. 
