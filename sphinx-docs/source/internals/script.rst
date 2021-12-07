
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

Eloki2 Script
*************

Eloki2 uses a fairly simple interal script for potential interopability between browser and framework clients and for flexibility. The script is interpreted and parsed line by line and each line has the format as follows ``[@timestamp] action [arguments]``, the parameters in [] are optional. The arguments vary depending on the specific action.

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

    click [cssSelector]


The click action creates a mouse-leftclick event at the current cursor position if no arguments are specified. If a css selector is specified as the first argument then it attempts to click the first element found by the css selector. If that fails then it fallsback to executing a leftclick event at the current cursor position. 

MouseRightClick
---------------

.. code-block::

    right_click

The right_click action creates a mouse-rightclick event at the current cursor position.


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

