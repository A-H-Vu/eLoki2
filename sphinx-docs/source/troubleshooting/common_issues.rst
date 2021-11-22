
#############################
Troubleshooting common issues
#############################




java.lang.IllegalStateException: The driver executable does not exist:
======================================================================

The path specified after the ``--driver`` argument does not point to a valid file. 

Fixes
-----

If running from the commandline, dragging and dropping the gecko/chromedriver onto the terminal while the cursor is after the driver argument will paste the full path to the file. 



If the path is a relative path, try typing out the full path to the file i.e. ``C:/Users/user/Desktop/geckodriver.exe``,  instead of ``geckodriver.exe``



org.openqa.selenium.SessionNotCreatedException: Unable to find a matching set of capabilities
=============================================================================================

The file specified by the ``--driver`` argument is not a valid chrome/gecko driver, or the client specified by the ``--client`` option does not match the 



org.openqa.selenium.WebDriverException: Failed to decode response from marionette
=================================================================================

If the client was SeleniumFirefox and the following lines appear in the crash log

.. code-block:: text

    [Child 7906, MediaDecoderStateMachine #1] WARNING: 12de61200 Could not set cubeb stream name.: 
    file /builds/worker/checkouts/gecko/dom/media/AudioStream.cpp:367
    [GFX1-]: Receive IPC close with reason=AbnormalShutdown
    Exiting due to channel error.
    [GFX1-]: Receive IPC close wEithxi trienags odnu=e Atbon ocrmhaannlSheult deorwron
    [GFX1-]: Receive IPC close with reason=AbnormalShutdown
    r.
    Exiting due to channel error.


The error was likely caused by the failure to load autoplaying media on the page. To fix this run with the ``--force-browser`` flag.