// To test, you can upload this file on your webserver and comment out the line below
// export { init };
// Then, you can run this file by executing the following in your console:
// import('http(s)://<domain></path>/mouseCapture.js').then(m => m.init(<height>, <width>));
// ex. import('http://www.eloki.tk/mouseCapture.js').then(m => m.init());

//var state = "active";

//currently not being called, anonymous function js is unloaded on page change
// function iframeURLChange(iframe, callback) {
//     var lastDispatched = null;

//     var dispatchChange = function () {
//         var newHref = null;
//         if (iframe.contentWindow !== null)
//             newHref = iframe.contentWindow.location.href;

//         if (newHref !== lastDispatched) {
//             callback(newHref);
//             lastDispatched = newHref;
//         }
//     };

//     var unloadHandler = function () {
//         // Timeout needed because the URL changes immediately after
//         // the `unload` event is dispatched.
//         setTimeout(dispatchChange, 0);
//     };

//     function attachUnload() {
//         // Remove the unloadHandler in case it was already attached.
//         // Otherwise, there will be two handlers, which is unnecessary.
//         iframe.contentWindow.removeEventListener("unload", unloadHandler);
//         iframe.contentWindow.addEventListener("unload", unloadHandler);
//     }

//     iframe.addEventListener("load", function () {
//         attachUnload();

//         // Just in case the change wasn't dispatched during the unload event...
//         dispatchChange();
//     });

//     attachUnload();
// }

//A check to see if the url change was done via js or something, page has not actually unloaded

if(document.head.getAttribute("elokiSessionName")==null){
    document.head.setAttribute("elokiSessionName", tickName);
}
else{
    return document.head.getAttribute("elokiSessionName");
}



var tickName = "ticks"
if(arguments.length>0){
    tickName = arguments[0];
}

function onUnload(){
    window.sessionStorage.setItem(tickName,get_ticks())
}

var ticks = [];

function init(ifHeight = 1200, ifWeight = 1920) {



    var capturing = false;
    var waiting = false;
    var mousePos;
    // var theInterval;

    function toggleCapturing(ifrmDoc) {
        if (capturing) {
            //body.style.backgroundColor = 'white';
            ifrmDoc.onmousemove = null;
            ifrmDoc.onclick = null;
            // clearInterval(theInterval);
            printButton.disabled = false;
            downloadButton.disabled = false;
            
            // ticks.push({
            //     content: `stopped`,
            //     t: new Date()
            // });
        } else {
            //body.style.backgroundColor = 'red';
            // ticks.push({
            //     content: `started`,
            //     t: new Date()
            // });
            console.log(ticks.length)
            if(ticks.length===0){
                ticks.push({
                    content: `getPage `+window.location,
                    t: new Date()
                })
            }
            // theInterval = setInterval(() => {
            //     if (!!mousePos)
            //         ticks.push(mousePos);
            // }, 1);
            //printButton.disabled = true;
            //downloadButton.disabled = true;
        }
        capturing = !capturing;
    }


    function loadCaptureScript(){
        var doc = document.body;
        // ticks.push({
        //     content: ifrmDoc.readyState,
        //     t: new Date()
        // });

        // if (waiting) {
        //     toggleCapturing(doc);
        //     waiting = false;
        // }
        ticks.push({
            content: `getPage `+window.location,
            t: new Date()
        })
        console.log(ticks);
        doc.onmousemove = event => {
            if(capturing) {
                ticks.push({
                    content: `mouseMoveScroll ${event.x} ${event.y} ${window.pageXOffset} ${window.pageYOffset}`,
                    t: new Date()
                });
            }
        };

        doc.onclick = event => {
            if (capturing) {
                ticks.push({
                    content: `click`,
                    t: new Date()
                });
            }
        };

        doc.oncontextmenu = event => {
            if (capturing) {
                ticks.push({
                    content: `right_click`,
                    t: new Date()
                });
            }
        };
        window.onbeforeunload = onUnload

        var checkFocus = () =>{
            if(document.hasFocus() == false){
                window.location = "about:blank";
            }
        }
        var visibilityChange;
        if (typeof document.hidden !== "undefined") {
            visibilityChange = "visibilitychange";
        } else if (typeof document.mozHidden !== "undefined") {
            visibilityChange = "mozvisibilitychange";
        } else if (typeof document.msHidden !== "undefined") {
            visibilityChange = "msvisibilitychange";
        } else if (typeof document.webkitHidden !== "undefined") {
            visibilityChange = "webkitvisibilitychange";
        }

        document.addEventListener(visibilityChange, checkFocus);
        window.addEventListener("focus", checkFocus);
        window.addEventListener("blur", checkFocus);

        // ifrmDoc.body.onkeydown = event => {
        //     if (event.ctrlKey) {
        //         toggleCapturing(ifrmDoc);
        //     }
        // }

        toggleCapturing(doc)
    }



    window.scrollTo(0, 0);
    //Selenium's UnexpectedAlertException is sticky, cannot wait for user input to close alert
    //alert('Make sure the mouse focus is on the iframe and hit Ctrl to start and stop recording.');

    // iframeURLChange(ifrm, newURL => {
    //     console.log("iFrame is on " + newURL);
    //     if (capturing) {
    //         toggleCapturing(ifrm.contentWindow.document);
    //         ticks.push({
    //             content: `waiting`,
    //             t: new Date()
    //         });
    //         waiting = true;
    //     }
    // });


    //TODO debug issues that may be caused by onload being called multiple times without changing urls
    if(!!document.body){
        //loadscript immediately if body is present and loaded
        //Fixes edge case where it appears that onload is not called when the page is already cached
        //If it was a page that was recently visited i.e. using browser back
        loadCaptureScript()
    }
    else{
        window.onload = () => {
            loadCaptureScript();
        };
    }

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
        result += '@' + tick.t.getTime() + ' '+ tick.content + '\n';
    // result += tick.content + ' @ ' + tick.t.getMinutes() + ":" + tick.t.getSeconds() + ":" + tick.t.getMilliseconds() + '\n';
    return result;
}

function print_ticks() {
    var body = document.querySelector('body');
    
    
    body.style.fontFamily = 'monospace';
    body.style.color = 'black';
    body.innerText = get_ticks();
}



init();

return tickName;
