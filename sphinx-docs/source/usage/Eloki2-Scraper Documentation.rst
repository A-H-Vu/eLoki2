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

.. _usage/scraper:

#####################
Eloki2-Scraper Module
#####################

The scraper module of the program scrapes a site looking for all the pages that can be reached from the given page. By default the scraper will only return html pages whose urls are prefixed by the given url.

If the page www.foo.com/home/index.html links to three pages www.foo.com/res/a.html, www.foo.com/home/about.html, www.foo.com/home/a.png, the scraper will only return www.foo.com/home/about.html. As a.png is an image and not an html/text page, and the other page is is not prefixed by "www.foo.com/home/"

The scraper can  be paused by terminating the program and restarted by using the same command as long as the ``inprogressScrape.db`` file is in the working directory.

Usage
*****

.. code-block:: console 

    java -jar eLoki2.jar [--full-browser] [--driver DRIVERFILE] [--client CLIENT] [--proxy address:port] [--useragent UA] scrape \<url\> [--timeout TIMEOUT] [--max-depth DEPTH] [--dest FILE]

<url\> The url argument is the address to the page to scrape for example www.yorku.ca



Output
******

The urls found will be written to the file specified by the ``--dest`` argument. If the ``--dest`` argument is not used then it will be written to a file named ``anchors``

While scraping a site a sqlite database file ``inprogressScrape.db`` will be created to store the data about the ongoing scrape. For details on the tables in the database see :ref:`scrape_database_fmt`. 

.. note::
    The current implementation of the scraper uses Selenium in order to avoid issues with site using javascript, html meta tags, etc to load, render or redirect  pages. As such the options ``--driver`` and ``--client`` must be set.
    
    Additionally the browser will be displayed in the background as it is visiting and scraping the urls until eLoki2 is modified to allow hiding the browser.



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


-------
--proxy
-------

The proxy to use, by default no proxy is used. The type of proxy used is a socks5 proxy. The proxy is specified in the following format ``address:port`` where the address is either the ip address of the proxy or is domain name i.e. 127.0.0.1, localhost etc. The port is the port that the socks5 proxy is open on and must be a number from 0 to 65535. This flag can be used to connect to the tor network if the tor browser is open and connected as follows ``--proxy 127.0.0.1:9150``


-----------
--useragent
-----------

Changes the useragent string that the browser uses.

--------------
--full-browser
--------------

Uses the full browser instead of the headless browser if it is applicable for the client. By default the scraping module runs the client on headless mode if it is possible. Note: in on some sites i.e. yorku.ca where there are autoplaying media, the headless mode may crash as when it is unable to play. See [troubleshooting](common_issues.html) for more details 


------
--dest
------

The destination file path to write the output to. By default it is a file named "anchors".


-----------
--max-depth
-----------

The max-depth specifies how far the scraper will go from the given page before stopping. A page that can be reached in at minimum 3 clicks/links from the given url has a depth of 3. If the max-depth is set to 0 then only the given url is scraped. Any number less than 0 will be set to 0.

---------
--timeout
---------

The time in milliseconds to wait between get requests to the site. By default this is set to 1 second or 1000 milliseconds.


------------
--add-prefix
------------

Adds additional URL prefixes that the scraper will accept. By default the scraper module will only scrape pages that have a prefix that matches the original URL. This is useful for sites where there are links leading to various other subdomains that you want to have scraped. The prefix must include the protocol. Examples "https:www.google.ca/", "http://www.cse.yorku.ca", "https://www.eecs.yorku.ca/" etc.

