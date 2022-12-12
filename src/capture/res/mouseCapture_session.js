//Recording script for the eloki passive recorder
//The script's responsibility is once injected, register the listeners to the page
//that are used to capture the user's actions and when the page is unloaded, save the recorded script
//into the session storage
//
//Should be used in situations where the default recorder cannot be used when embedding the site in an
//iframe is blocked such as github.com
//

//Arguments
// 0 : alphanumeric string 

//Default name
//counter to know when to skip mouseMoveScroll events
var counter = 0;

var tickName = "ticks"
if(arguments.length>0){
    tickName = arguments[0];
}



//A check to see if the url change was done via js or something, page has not actually unloaded
//Function checks if an attribute named elokiSessionName is set, stores the random alphanumeric string
//which the script saves the reccordings to 
if(document.head.getAttribute("elokiSessionName")==null){
    document.head.setAttribute("elokiSessionName", tickName);
}
else{
    return document.head.getAttribute("elokiSessionName");
}


//Saves the recorded data when the page is unloaded
function onUnload(){
    window.sessionStorage.setItem(tickName,get_ticks())
}

//https://stackoverflow.com/a/12222317 gets css selector for element
var cssPath = function(el) {
    if (!(el instanceof Element)) 
        return;
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


//stores the recorded actions
var ticks = [];

//Init function, most of the logic is here 
function init() {



    var capturing = false;
    var waiting = false;
    var mousePos;


    //TODO test if this block is needed or not
    function toggleCapturing(ifrmDoc) {
        //stop capture
        if (capturing) {
            ifrmDoc.onmousemove = null;
            ifrmDoc.onclick = null;
            printButton.disabled = false;
            downloadButton.disabled = false;
            
        //start capture
        } else {
            //if new page/capture, record url
            if(ticks.length===0){
                ticks.push({
                    content: `getPage `+window.location,
                    t: new Date()
                })
                ticks.push({
                    content: `attachMouse`,
                    t: new Date()
                })
            }
        }
        capturing = !capturing;
    }

    //main function that initializes all the listeners
    //mostly self explanatory, each listener pushes the event
    //into the ticks array as json where content is the action for the event
    //and t is the timestamp for the event
    function loadCaptureScript(){
        var doc = document.body;
        ticks.push({
            content: `getPage `+window.location,
            t: new Date()
        })
        ticks.push({
            content: `attachMouse`,
            t: new Date()
        })
        //listeners for all events
        doc.addEventListener('mousemove', event => {
			//counter set to skip every tenth event
            if (capturing && counter < 10) {
				counter++;
                ticks.push({
                    content: `mouseMoveScroll ${event.x} ${event.y} ${Math.trunc(ifrm.contentWindow.pageXOffset)} ${Math.trunc(ifrm.contentWindow.pageYOffset)}`,
                    t: new Date()
                });
            } else {
				counter = 0;
			}
        });
        doc.addEventListener('click', event => {
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

        doc.addEventListener('contextmenu', event => {
            if (capturing) {
                ticks.push({
                    content: `right_click`,
                    t: new Date()
                });
            }
        });
        //second key listener this time on the iframe
        //Both will not be triggered at the same time
        doc.addEventListener('keydown', event => {
            console.log('keydown');
            if (event.ctrlKey) {
                toggleCapturing(doc);
            }
        });
        doc.addEventListener('scroll', event =>{
            if(capturing) {
                ticks.push({
                    content: `scrollWindow ${Math.trunc(ifrm.contentWindow.pageXOffset)} ${Math.trunc(ifrm.contentWindow.pageYOffset)}`,
                    t: new Date()
                })
            }
        });
        doc.addEventListener('keypress', event => {
            if(capturing){
                ticks.push({
                    content: `keyStroke ${event.key}`,
                    t: new Date()
                });
            }
        });

        window.onbeforeunload = onUnload

        //Stuff to check if the window has lost focus, redirects to about:blank
        //which causes the program to display the main page again 
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
        //start capturing once all listeners have been registered
        toggleCapturing(doc)


        //canvas to draw a red dot, to signal recording
        var canvas = document.createElement('canvas');
        canvas.height = 64;
        canvas.width = 64
        var ctx = canvas.getContext("2d");
        ctx.beginPath();
        ctx.arc(32, 32, 20, 0, 2 * Math.PI);
        ctx.fillStyle = 'red'
        ctx.fill();
        document.head.appendChild(canvas)


        //Change favicon to recording icon
        var link = document.querySelector("link[rel~='icon']");
        if (!link) {
            link = document.createElement('link');
            link.rel = 'icon';
            document.getElementsByTagName('head')[0].appendChild(link);
        }
        link.href = canvas.toDataURL();
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

//function to convert the ticks array of json into text of the form @<timestamp in mili from epoch> <content> in each line
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

//TODO check if this is needed
function print_ticks() {
    var body = document.querySelector('body');
    
    
    body.style.fontFamily = 'monospace';
    body.style.color = 'black';
    body.innerText = get_ticks();
}


//call init function
init();

//return tickName so the program knows the key the recording is stored as in the sessondb
return tickName;
