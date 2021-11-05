//Script used to write the main landing page for the recording capture

//functions for displaying the recording
function download_ticks() {
    //Downloads the recording by creating a blob wiht the text and then
    //creating an anchor that links to it and automatically cliking on it to download
    var downloadButton = document.getElementById("downloadAnchor");
    var fileName = document.getElementById("downloadInput").value;
    var myFile = new Blob([get_ticks()], {type: 'text/plain' });
    window.URL = window.URL || window.webkitURL;
    downloadButton.setAttribute("download", fileName);
    downloadButton.setAttribute("href", window.URL.createObjectURL(myFile));
    downloadButton.click();
}

//displays the recorded script in a textbox defined in the document
function print_ticks() {
    var clearButton = document.getElementById('clearButton');
    var tickP = document.getElementById('tickP');
    var tn = document.createTextNode(get_ticks());
    tickP.appendChild(tn);
    clearButton.hidden = false;
    tickP.hidden = false;

}

//Clears the text box created above
function clear_ticks() {
    var clearButton = document.getElementById('clearButton');
    var tickP = document.getElementById('tickP');
    tickP.innerText = ''
    clearButton.hidden = true;
    tickP.hidden = true;
    
}

//function to get the recorded script
function get_ticks() {
	return window.sessionStorage.getItem("ticks");
}

//comment is old button style that attempts to override every possible thing
buttonStyle = ""//"all: initial; align-content: center; align-items: center; align-self: center; background: #fff; border: 1px solid black; border-spacing : 1em; color: black; cursor: default; direction: rtl; display: block; filter: none; flex: 0 1 auto; float: none; font: 15px Arial, sans-serif; font-weight: normal; height: 1em; width: max-content; justify-content: center; letter-spacing: normal; left: auto; right: auto; top: auto; line-height: normal; letter-spacing: normal; margin: 1em; max-height: none; max-width: none; min-height: none; min-width: none; opacity: 1; outline: medium invert color; overflow: visible; padding: 0.5em; position: static; resize: none; tab-size: 8; text-align: left; text-align-last: left; text-decoration: none currentcolor solid; text-ident: 0; text-justify: auto; text-overflow: clip; text-shadow: none; text-transform: capitalize; visibility: visible; word-break: normal; word-spacing: normal; word-wrap: normal;"

//Create and append the various DOM elements
var title = document.createElement("title");
title.innerText = "Mouse Capture";
document.head.append(title);

document.body.style.textAlign = "center";
document.body.bgColor = "white";

var heading = document.createElement("h2");
heading.innerText = "Capture instructions";
document.body.append(heading)

var info = document.createElement("p");
info.innerText = "Enter the url of the site you want to record into the url bar of the browser. When the page loads the recording will automatically start. To stop the recording click on the URL bar again.";
info.style.margin = "2em";
document.body.append(info);


var div = document.createElement('div');
div.style = "all: initial; * {all: unset;}"
//div.style.display = '';
document.body.append(div);


//Create all the buttons

//displays the recording in a text box
var printButton = document.createElement('button');
printButton.onclick = print_ticks;
printButton.innerText = "Print Result";
printButton.style = buttonStyle;
div.appendChild(printButton);

//label for input box
var downloadLabel = document.createElement('label');
downloadLabel.for = "downloadButton"
downloadLabel.innerText = "File name"
downloadLabel.style = buttonStyle;
downloadLabel.style.marginLeft = "1em"
downloadLabel.style.border = "none";
div.appendChild(downloadLabel);

//input box for filename
var downloadInput = document.createElement('input');
downloadInput.value = "tick.txt";
downloadInput.id = "downloadInput";
downloadInput.style = buttonStyle;
downloadInput.style.cursor = "text";
div.appendChild(downloadInput);

//download the recording as text file
var downloadButton = document.createElement('button');
downloadButton.innerText = "Download Result";
downloadButton.onclick = download_ticks;
downloadButton.id = "downloadButton";
downloadButton.style = buttonStyle;
downloadButton.style.marginLeft = "1em";
div.appendChild(downloadButton);

//hidden anchor for the download function
var downloadAnchor = document.createElement('a');
downloadAnchor.hidden = true;
downloadAnchor.id = "downloadAnchor";
div.appendChild(downloadAnchor);

//Quits the button by setting the quit variable to true in the small embedded script at the bottom
var quitButton = document.createElement('button');
quitButton.innerText = "Quit";
quitButton.setAttribute("onclick", "quitCapture()");
quitButton.style = buttonStyle;
quitButton.style.marginLeft = "1em";
div.appendChild(quitButton);

//Stuff to do with the textbox used to display the recorded script
var printDiv = document.createElement('div');
//printDiv.style = "all: initial; * {all: unset;}"
document.body.append(printDiv);

//textbox is this, text gets reinitialized with the script as the text
var tickP = document.createElement('textarea');
tickP.id = 'tickP'
//lazy values for height/width
tickP.style.width = '50%';
tickP.style.height = '50%';
tickP.hidden = true;
printDiv.appendChild(tickP);

//newline/under textarea
var br = document.createElement('br');
printDiv.append(br);

//clear button to rehide the textarea and this button
var clearButton = document.createElement('button');
clearButton.onclick = clear_ticks;
clearButton.innerText = "Clear ticks";
clearButton.hidden = true;
clearButton.style = buttonStyle;
clearButton.id = 'clearButton'
printDiv.appendChild(clearButton);

//Global variable in a script tag on the page so that it can be accessed by other snippets of injected scripts
//This is primarily to solve a minor difference between chrome and firefox and the way they handle global variables in injected scripts
var script = document.createElement("script");
script.innerText = "var quit = false;function quitCapture(){quit = true;}"
document.body.append(script);




