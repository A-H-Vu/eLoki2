// To test, you can upload this file on your webserver and comment out the line below
// export { init };
// Then, you can run this file by executing the following in your console:
// import('http(s)://<domain></path>/mouseCapture.js').then(m => m.init(<height>, <width>));
// ex. import('http://www.eloki.tk/mouseCapture.js').then(m => m.init());

//Default recorder for eLoki
//Records session by embedding the webpage in an iframe
//This makes it easier to record the session as the script only has to be injected once, any url changes
//can be easily checked by checking the iframe contentwindow href
//Additionally with this method the recorded session window can be larger than the browser window/display
//as the iframe can be sized independantly from the browser


//Temporary thing for clicking an element by its css tag
//attempts to bubble up the tree till it finds something clickable
//testing tbd
function clickByCSS(css){
    var ele = document.querySelector(css)
    while(typeof(ele.click)==='undefined'){
        ele = ele.parentNode
    }
    ele.click()
}

//function that is called when the iframe's url changes
//executes the callback function once the url has changed 
function iframeURLChange(iframe, callback) {
    var lastDispatched = null;

    var dispatchChange = function () {
        var newHref = null;
        if (iframe.contentWindow !== null)
            newHref = iframe.contentWindow.location.href;

        if (newHref !== lastDispatched) {
            callback(newHref);
            lastDispatched = newHref;
        }
    };

    var unloadHandler = function () {
        // Timeout needed because the URL changes immediately after
        // the `unload` event is dispatched.
        setTimeout(dispatchChange, 0);
    };

    function attachUnload() {
        // Remove the unloadHandler in case it was already attached.
        // Otherwise, there will be two handlers, which is unnecessary.
        iframe.contentWindow.removeEventListener("unload", unloadHandler);
        iframe.contentWindow.addEventListener("unload", unloadHandler);
    }

    iframe.addEventListener("load", function () {
        attachUnload();

        // Just in case the change wasn't dispatched during the unload event...
        dispatchChange();
    });

    attachUnload();
}

//https://stackoverflow.com/a/12222317 gets css selector for element
var cssPath = function(el) {
    var path = [];
    while (el.nodeType === Node.ELEMENT_NODE) {
        var selector = el.nodeName.toLowerCase();
        if (el.id) {
            selector += '#' + el.id;
            path.unshift(selector);
            break;
        } else {
            var sib = el, nth = 1;
            while (sib = sib.previousElementSibling) {
                if (sib.nodeName.toLowerCase() == selector)
                   nth++;
            }
            if (nth != 1)
                selector += ":nth-of-type("+nth+")";
        }
        path.unshift(selector);
        el = el.parentNode;
    }
    return path.join(" > ");
 }


//stores the recording
var ticks = [];

//ifHeight/ifWidth sets the iframe size
function init(ifHeight = 1200, ifWidth = 1920) {
    //This section creates the header
    var html = document.querySelector('html');
    for (let element of html.children)
        if (element.tagName !== 'HEAD')
            html.removeChild(element);

    //Attempt to set all the different css styles to override any global css stylesheets
    buttonStyle = "all: initial; align-content: center; align-items: center; align-self: center; background: #fff; border: 1px solid black; border-spacing : 1em; color: black; cursor: default; direction: rtl; display: block; filter: none; flex: 0 1 auto; float: none; font: 15px Arial, sans-serif; font-weight: normal; height: 1em; width: max-content; justify-content: center; letter-spacing: normal; left: auto; right: auto; top: auto; line-height: normal; letter-spacing: normal; margin: 1em; max-height: none; max-width: none; min-height: none; min-width: none; opacity: 1; outline: medium invert color; overflow: visible; padding: 0.5em; position: static; resize: none; tab-size: 8; text-align: left; text-align-last: left; text-decoration: none currentcolor solid; text-ident: 0; text-justify: auto; text-overflow: clip; text-shadow: none; text-transform: capitalize; visibility: visible; word-break: normal; word-spacing: normal; word-wrap: normal;"

    var body = document.createElement('body');
    body.style.height = '100vh';
    body.style.width = '100wh';
    body.style.padding = '0';
    body.style.margin = '0 auto';
    body.style.backgroundColor = 'white';
    body.style.textAlign = 'center';

    var script = document.createElement('script');
    script.innerText = "var state = 'active';function br(){state='reset'};function bq(){state='quit'}";
    body.appendChild(script);

    var div = document.createElement('div');
    div.style = "all: initial; * {all: unset;}"
    div.style.display = 'flex';

    var stopResumeButton = document.createElement('button');
    stopResumeButton.onclick = ()=>{
        toggleCapturing()
    };
    stopResumeButton.innerText = "Start";
    stopResumeButton.style = buttonStyle;
    div.appendChild(stopResumeButton);
    
    var printButton = document.createElement('button');
    printButton.onclick = print_ticks;
    printButton.innerText = "Print Result";
    printButton.style = buttonStyle;
    printButton.disabled = true;
    div.appendChild(printButton);
    
    var downloadLabel = document.createElement('label');
    downloadLabel.for = "downloadButton"
    downloadLabel.innerText = "File name"
    downloadLabel.style = buttonStyle;
    downloadLabel.style.marginLeft = "1em"
    downloadLabel.style.border = "none";
    div.appendChild(downloadLabel);

    var downloadInput = document.createElement('input');
    downloadInput.value = "tick.txt";
    downloadInput.id = "downloadInput";
    downloadInput.style = buttonStyle;
    downloadInput.style.cursor = "text";
    div.appendChild(downloadInput);

    var downloadButton = document.createElement('button');
    downloadButton.innerText = "Download Result";
    downloadButton.disabled = true;
    downloadButton.onclick = download_ticks;
    downloadButton.id = "downloadButton";
    downloadButton.style = buttonStyle;
    downloadButton.style.marginLeft = "1em";
    div.appendChild(downloadButton);

    var downloadAnchor = document.createElement('a');
    downloadAnchor.hidden = true;
    downloadAnchor.id = "downloadAnchor";
    div.appendChild(downloadAnchor);

    var restartButton = document.createElement('button');
    restartButton.setAttribute("onclick", "br()");
    restartButton.innerText = "New Recording";
    restartButton.style = buttonStyle;
    div.appendChild(restartButton);
    
    var quitButton = document.createElement('button');
    quitButton.setAttribute("onclick", "bq()");
    quitButton.innerText = "Quit";
    quitButton.style = buttonStyle;
    div.appendChild(quitButton);
    body.appendChild(div);

    var printDiv = document.createElement('div');
    printDiv.style = "all: initial; * {all: unset;}"
    printDiv.style.display = 'flex';
    body.append(printDiv);

    var tickP = document.createElement('textarea');
    tickP.id = 'tickP'
    //lazy values for height/width
    tickP.style.width = '50%';
    tickP.style.height = '500px';
    tickP.hidden = true;
    printDiv.appendChild(tickP);

    var br = document.createElement('br');
    printDiv.append(br);

    var clearButton = document.createElement('button');
    clearButton.onclick = clear_ticks;
    clearButton.innerText = "Clear ticks";
    //clearButton.style = buttonStyle;
    clearButton.hidden = true;
    clearButton.id = 'clearButton'
    printDiv.appendChild(clearButton);
    

    var ifrm = document.createElement('iframe');
    ifrm.setAttribute('src', window.location.href);
    body.appendChild(ifrm);
    ifrm.style.height = ifHeight + 'px';
    ifrm.style.width = ifWidth + 'px';
    ifrm.style.padding = '0';
    ifrm.style.margin = '0';
    ifrm.style.border = 'none';
    html.appendChild(body);


    //Most of the recording logic starts here

    var capturing = false;
    var waiting = false;
    var mousePos;
    //var theInterval;
    //for initial load, record window size
    ticks.push({
        content: `resize ${ifWidth} ${ifHeight}`,
        t: new Date()
    })

    //Turns the recording on/off, capturing = true = recording
    function toggleCapturing() {
        console.log("toggle");
        if (capturing) {
            //update background
            body.style.backgroundColor = 'white';
            stopResumeButton.innerText = "Resume"
            //TODO remove new event listeners if they work
            //clearInterval(theInterval);
            //renable buttons
            printButton.disabled = false;
            downloadButton.disabled = false;
            ticks.push({
                content: `--------`,
                t: new Date()
            })
            // ticks.push({
            //     content: `stopped`,
            //     t: new Date()
            // });
        } else {
            //update background
            body.style.backgroundColor = 'red';
            stopResumeButton.innerText = "Pause";
            // ticks.push({
            //     content: `started`,
            //     t: new Date()
            // });

            // console.log(ticks.length)
            // if(ticks.length===0){
                //record new pageurl
                ticks.push({
                    content: `getPage `+ifrm.contentWindow.location,
                    t: new Date()
                })
                ticks.push({
                    content: `attachMouse`,
                    t: new Date()
                })
            // }
            d = new Date()
            // theInterval = setInterval(() => {
            //     //For some reason this appears to start running prior to toggle capturing being pressed
            //     //Prevents mouse pos recordings from before the start of the current run from being recorded
            //     if (!!mousePos&&mousePos.t>d)
            //         ticks.push(mousePos);
            // }, 1);
            printButton.disabled = true;
            downloadButton.disabled = true;
        }
        capturing = !capturing;
    }



    window.scrollTo(0, 0);
    //Selenium's UnexpectedAlertException is sticky, cannot wait for user input to close alert
    //alert('Make sure the mouse focus is on the iframe and hit Ctrl to start and stop recording.');

    //adds listeners that will execute function when iframe url changes
    iframeURLChange(ifrm, newURL => {
        console.log("iFrame is on " + newURL);
        if (capturing) {
            //push wait for page load event
            toggleCapturing();
            ticks.push({
                content: `waiting`,
                t: new Date()
            });
            waiting = true;
        }
    });

    //Key listener on the main document 
    document.body.addEventListener('keydown', event => {
        if (event.ctrlKey) {
            toggleCapturing();
        }
    });

    //Main function that registers all the listeners
    ifrm.addEventListener('load', () => {
        var ifrmDoc = ifrm.contentWindow.document;
        // ticks.push({
        //     content: ifrmDoc.readyState,
        //     t: new Date()
        // });
        //if url has changed, re-enable recording as page has loaded
        if (waiting) {
            toggleCapturing();
            waiting = false;
        }

        //listeners for all events
        ifrmDoc.addEventListener('mousemove', event => {
            if (capturing) {
                ticks.push({
                    content: `mouseMoveScroll ${event.x} ${event.y} ${Math.trunc(ifrm.contentWindow.pageXOffset)} ${Math.trunc(ifrm.contentWindow.pageYOffset)}`,
                    t: new Date()
                });
            }
        });
        ifrmDoc.addEventListener('click', event => {
            if (capturing) {
                event = event || window.event;
                var target = event.target || event.srcElement
                cssp = cssPath(target);
                if(typeof(cssp)==='undefined')
                    cssp = "";
                ticks.push({
                    content: `click ${cssp}`,
                    t: new Date()
                });
            }
        });

        ifrmDoc.addEventListener('contextmenu', event => {
            if (capturing) {
                ticks.push({
                    content: `right_click`,
                    t: new Date()
                });
            }
        });
        //second key listener this time on the iframe
        //Both will not be triggered at the same time
        ifrmDoc.body.addEventListener('keydown', event => {
            console.log('keydown');
            if (event.ctrlKey) {
                toggleCapturing(ifrmDoc);
            }
            else if(capturing){
                ticks.push({
                    content: `keyStroke ${event.key}`,
                    t: new Date()
                })
            }
        });
        ifrmDoc.body.addEventListener('scroll', event =>{
            if(capturing) {
                ticks.push({
                    content: `scrollWindow ${Math.trunc(ifrm.contentWindow.pageXOffset)} ${Math.trunc(ifrm.contentWindow.pageYOffset)}`,
                    t: new Date()
                })
            }
        });
        
        
        ifrmDoc.body.focus()

    });

}

//Various functions to get the recording
function get_ticks() {
    var result = "";
    //result += "keys="+window.location.pathname
    ticks.sort((a, b) => {
        if (a.t < b.t)
            return -1;
        if (a.t > b.t)
            return 1;
        return 0;
    });
    for (let tick of ticks)
        result += '@' + tick.t.getTime() + ' ' + tick.content + '\n';
    // result += tick.content + ' @ ' + tick.t.getMinutes() + ":" + tick.t.getSeconds() + ":" + tick.t.getMilliseconds() + '\n';
    return result;
}

function print_ticks() {
    var clearButton = document.getElementById('clearButton');
    var tickP = document.getElementById('tickP');
    var tn = document.createTextNode(get_ticks());
    tickP.appendChild(tn);
    clearButton.hidden = false;
    tickP.hidden = false;

}
function clear_ticks() {
    var clearButton = document.getElementById('clearButton');
    var tickP = document.getElementById('tickP');
    tickP.innerText = ''
    clearButton.hidden = true;
    tickP.hidden = true;
    
}

function download_ticks() {
    var downloadButton = document.getElementById("downloadAnchor");
    var fileName = document.getElementById("downloadInput").value;
    var myFile = new Blob([get_ticks()], {type: 'text/plain' });
    window.URL = window.URL || window.webkitURL;
    downloadButton.setAttribute("download", fileName);
    downloadButton.setAttribute("href", window.URL.createObjectURL(myFile));
    downloadButton.click();
}

//tick information, url information etc


// if(arguments.length>2){
//     init(arguments[0], arguments[1]);
// }
// else{
    init(window.innerHeight, window.innerWidth);
// }

