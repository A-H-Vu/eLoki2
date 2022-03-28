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


################
Extending Eloki2
################


The eLoki2 program was designed to be fairly easy to extend with additional functionality in various areas. This page lists the process to add additional components and some general checks.

Adding a new client
-------------------

A new client can be added by extending the clients/Client class. The client specific functionality will have to be implemented individually in each of the action class' execute function. The checkCompatibility function should also be modified to reflect the changes. Additionally the new client would have to be initialized in the Main class with all the appropriate flags if possible.

If the client has/is a new extension-compatible browser, modify the ext/extensions class to load a similar compatible extension if possible. 


Adding a new action
-------------------


A new action can be added by extending the ActionImpl class. In order to facilitate deep cloning the following must also be copied/changed.

- The clone function must be implemented and consist of returing a new instance of the class using the (this) constructor.
- The (this) constructor must be modified to call the (String) constructor using ``this(original.getRaw())``
- In the (this) constructor this line must be added ``this.tick = new ActionTick(original.getTick().getValue(), original.getTick().getResponse());`` in order to copy the tick value from the original action

Some additional things to note:

- The getRaw function should be overwritten if the Action has any modifiable values, and should reflect the current state of the action. This function is used to create a clone of the action.
- The actionTickResponse function should be overwritten from the default ignore to properly use the timings in the script. 


Adding a new submodule
----------------------

The Main class should be the only place that needs to be modified. To add a new submodule, add a new subparser using the other subparsers as an example. Add a new brench to the main if statement determining the module and add the code to use the module there.

Adding a new BrowserExtension
-----------------------------

To add a new browserextension, add the extension to the ExtensionsListEnum. Load the extension when/where it is necesssary using the existing example in the capture submodule in the main class. 


