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
