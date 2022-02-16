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
  
#######################
Eloki2-RunScript Module
#######################

The replay/script runner module is used to replay sessions captured/recorded by the capture module. Click [here](Eloki2-Capture Documentation.html) for the documentation on the capture module. When run, the 



Usage
*****

.. code-block:: console

    $ java -jar eLoki2.jar [--headless] [--driver DRIVERFILE] [--client CLIENT] [--proxy address:port] run <script>

\<script\> is the location of the script file(s) recorded by the capture module.



Options
*******

--------
--driver
--------

The path to the driver for Selenium, this is either the geckodriver file or the chromedriver file depending on the selenium client specified in the *--client* option.



The path must include any extensions the file may have (.exe on windows for example). Additionally for the chromedriver to work you may have to follow the additional instructions to allow it to find your chrome installation.


--------
--client
--------

The client to use, currently the only options are ``SeleniumChrome`` and ``SeleniumFirefox`` for Selenium using Chrome and Firefox respectively. Additionally the driver must be specified using the ``--driver`` option


----------
--headless
----------

Start the browser in headless mode hiding it. 


-------
--proxy
-------

The proxy to use, by default no proxy is used. The type of proxy used is a socks5 proxy. The proxy is specified in the following format ``address:port`` where the address is either the ip address of the proxy or is domain name i.e. 127.0.0.1, localhost etc. The port is the port that the socks5 proxy is open on and must be a number from 0 to 65535. This flag can be used to connect to the tor network if the tor browser is open and connected as follows ``--proxy 127.0.0.1:9150``

-----------
--useragent
-----------

Changes the useragent string that the browser uses.