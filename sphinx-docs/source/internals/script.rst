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

.. _internals/eloki-script:

Eloki2 Script Documentation
***************************

Eloki2 uses a fairly simple interal script for potential interopability between browser and framework clients and for flexibility. The script is interpreted and parsed line by line and each line has the format as follows ``[@timestamp] action [arguments]``. The arguments vary depending on the specific action.

Below is a list of all the currently implemented actions and the current specifications for them.


awaitPageLoad
-------------

.. code-block::

    waiting

The awaitPageLoad action pauses execution until the condition ``document.readyState == complete`` is true.


GetPage
-------

.. code-block::
    
    getPage [pageUrl]


The getPage action navigates the browser to the page specified by the [pageUrl] argument. If the current url of the browser is the same as the [pageUrl] argument then nothing happens.

MouseClick
----------

.. code-block::

    click [x] [y] [cssSelector]


The click action creates a mouse-leftclick event at the current cursor position if no arguments are specified. If the x and y arguments are specified then it will move and click at that location. If a css selector is specified as the first argument then it attempts to click the first element found by the css selector. If that fails then it fallsback to executing a leftclick event at the current cursor position. 

MouseRightClick
---------------

.. code-block::

    right_click [x] [y]

The right_click action creates a mouse-rightclick event at the current cursor position. If the x and y positions are specified then it will move and rightclick at that location.

MouseDown
---------


.. code-block::

    mouseDown [button] [x] [y] [cssSelector]

Starts holding the specified mouse button down at the optinally specified co-ordinates. The button argument corresponds with the Javascript MouseEvent.button value, 0 is left click, 2 is right click. If the x and y are specified then it will start holding at that location. The css option exists if this needs to be transformed into a click/right_click action with a corresponding mouseUp event.

Note: There must be a corresponding mouseUp action which will then be grouped together into a dragDrop action when running the script for this to work. A single mouseDown action will not emulate a click properly, use the click or right_click action. This due to the implementation of these actions with Selenium requiring a single grouped action for the click and hold to work.


MouseUp
-------

.. code-block::

    mouseUp [button] [cssSelector]

Releases the specified mouse button. The button argument corresponds with the Javascript MouseEvent.button value, 0 is left click, 2 is right click. The css option exists if this needs to be transformed into a click/right_click action with a corresponding mouseDown event.

.. note::
    
    There must be a corresponding mouseDown action which will then be grouped together into a dragDrop action when running the script for this to work. A single mouseUp will do nothing. 

DragDrop
--------


.. code-block::

    dragDrop [button] ([x] [y] [duration])...

Holds the specified mouse button down and drags it along the path specified by the co-ordinates and duration. The button argument corresponds with the Javascript MouseEvent.button value, 0 is left click, 2 is right click. The ``([x] [y] [duration])...`` portion is repeated for every point the drag and drop action should move through. The duration is the miliseconds to wait after moving to the previous point. It is initally the time to wait after starting the click and hold. 

.. note::
    
    A mouseDown action followed by some timestamped movement actions and then a mouseUp action will automatically be converted into this action with the correct values for the co-ordinates and duration. 

MouseMove
---------

.. code-block::

    mouseMove [x] [y]

Moves the cursor to the specified x and y positions. The x and y values are relative to the browser window.


MouseMoveScroll
---------------

.. code-block::

    mouseMoveScroll [x] [y] [scrollX] [scrollY]

Moves the cursor to the specified x and y positions and also scrolls the page to the specified scrollX and scrollY positions. The x and y values are relative to the browser window. The scrollX and scrollY values are relative to the main window of the document.

NaturalMove
-----------


.. code-block::

    naturalMove [w] [h] [ix] [iy] [fx] [fy]

Uses the `NaturalMouseMotion <https://github.com/JoonasVali/NaturalMouseMotion>`_ to move the mouse in a somewhat natural manner from the starting point specified by ix and iy  to the end point specified by fx and fy. The w and h paramters are the width and height of the window viewport. 


ScrollWindow
------------

.. code-block::

    scrollWindow [scrollX] [scrollY]

Scrolls the page to the specified scrollX and scrollY positions. The scrollX and scrollY values are relative to the main window of the document.


ResizeWindow
------------

.. code-block::

    resize [width] [height]

Resizes the display window to the specified width and height. The width and height is measured as the size of the innerWindow using ``window.innerWidth/Height`` in javascript.

KeyStroke
---------


.. code-block::

    keyStroke [key]

Presses the key specified. The key value should be the value from KeyEvent.key in javascript, this is typically the character typed when the key is typed. See https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values for some of the additional non-character values.


KeyDown
-------

.. code-block::

    keyDown [key]

Starts pressing the key specified. The key value should be the value from KeyEvent.key in javascript, this is typically the character typed when the key is typed. See https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values for some of the additional non-character values.

.. warning::

    This action is untested and may not work properly.


KeyUp
-----


.. code-block::

    keyUp [key]

Stops pressing the key specified. The key value should be the value from KeyEvent.key in javascript, this is typically the character typed when the key is typed. See https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values for some of the additional non-character values.


.. warning::

    This action is untested and may not work properly
    

AttachMouse
-----------

.. code-block::

    attachMouse

Injects some javascript which creates an image of a mouse that follows the position of the mouse. Uses the mousemove event and absolute positioning to move the image to the current position of the mouse.


