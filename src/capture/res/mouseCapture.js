// To test, you can upload this file on your webserver and comment out the line below
// export { init };
// Then, you can run this file by executing the following in your console:
// import('http(s)://<domain></path>/mouseCapture.js').then(m => m.init(<height>, <width>));
// ex. import('http://www.eloki.tk/mouseCapture.js').then(m => m.init());

//var state = "active";

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

var ticks = [];

function init(ifHeight = 1200, ifWeight = 1920) {
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
    restartButton.innerText = "Record another Site";
    restartButton.style = buttonStyle;
    div.appendChild(restartButton);
    
    var quitButton = document.createElement('button');
    quitButton.setAttribute("onclick", "bq()");
    quitButton.innerText = "Quit";
    quitButton.style = buttonStyle;
    div.appendChild(quitButton);
    

    body.appendChild(div);

    var ifrm = document.createElement('iframe');
    ifrm.setAttribute('src', window.location.origin);
    body.appendChild(ifrm);
    ifrm.style.height = ifHeight + 'px';
    ifrm.style.width = ifWeight + 'px';
    ifrm.style.padding = '0';
    ifrm.style.margin = '0';
    ifrm.style.border = 'none';
    html.appendChild(body);


    var capturing = false;
    var waiting = false;
    var mousePos;
    var theInterval;

    function toggleCapturing(ifrmDoc) {
        console.log("toggle");
        if (capturing) {
            body.style.backgroundColor = 'white';
            ifrmDoc.onmousemove = null;
            ifrmDoc.onclick = null;
            clearInterval(theInterval);
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
            body.style.backgroundColor = 'red';
            // ticks.push({
            //     content: `started`,
            //     t: new Date()
            // });

            // console.log(ticks.length)
            // if(ticks.length===0){
                ticks.push({
                    content: `getPage `+ifrm.contentWindow.location,
                    t: new Date()
                })
            // }
            d = new Date()
            theInterval = setInterval(() => {
                //For some reason this appears to start running prior to toggle capturing being pressed
                //Prevents mouse pos recordings from before the start of the current run from being recorded
                if (!!mousePos&&mousePos.t>d)
                    ticks.push(mousePos);
            }, 1);
            printButton.disabled = true;
            //downloadButton.disabled = true;
        }
        capturing = !capturing;
    }



    window.scrollTo(0, 0);
    //Selenium's UnexpectedAlertException is sticky, cannot wait for user input to close alert
    //alert('Make sure the mouse focus is on the iframe and hit Ctrl to start and stop recording.');

    iframeURLChange(ifrm, newURL => {
        console.log("iFrame is on " + newURL);
        if (capturing) {
            toggleCapturing(ifrm.contentWindow.document);
            ticks.push({
                content: `waiting`,
                t: new Date()
            });
            waiting = true;
        }
    });

    document.body.onkeydown = event => {
        if (event.ctrlKey) {
            toggleCapturing(ifrm.contentWindow.document);
        }
    }

    ifrm.onload = () => {
        var ifrmDoc = ifrm.contentWindow.document;
        // ticks.push({
        //     content: ifrmDoc.readyState,
        //     t: new Date()
        // });

        if (waiting) {
            toggleCapturing(ifrmDoc);
            waiting = false;
        }

        ifrmDoc.onmousemove = event => {
            mousePos = {
                content: `mouseMoveScroll ${event.x} ${event.y} ${ifrm.contentWindow.pageXOffset} ${ifrm.contentWindow.pageYOffset}`,
                t: new Date()
            };
        };

        ifrmDoc.onclick = event => {
            if (capturing) {
                ticks.push({
                    content: `click`,
                    t: new Date()
                });
            }
        };

        ifrmDoc.oncontextmenu = event => {
            if (capturing) {
                ticks.push({
                    content: `right_click`,
                    t: new Date()
                });
            }
        };
        ifrmDoc.body.onkeydown = event => {
            if (event.ctrlKey) {
                toggleCapturing(ifrmDoc);
            }
        }
        
        
        ifrmDoc.body.focus()

    };

}

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
    var body = document.querySelector('body');
    body.style.fontFamily = 'monospace';
    body.style.color = 'black';
    body.innerText = get_ticks();
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




init();

