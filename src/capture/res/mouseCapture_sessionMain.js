function download_ticks() {
    var downloadButton = document.getElementById("downloadAnchor");
    var fileName = document.getElementById("downloadInput").value;
    var myFile = new Blob([get_ticks()], {type: 'text/plain' });
    window.URL = window.URL || window.webkitURL;
    downloadButton.setAttribute("download", fileName);
    downloadButton.setAttribute("href", window.URL.createObjectURL(myFile));
    downloadButton.click();
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

function get_ticks() {
	return window.sessionStorage.getItem("ticks");
}

buttonStyle = ""//"all: initial; align-content: center; align-items: center; align-self: center; background: #fff; border: 1px solid black; border-spacing : 1em; color: black; cursor: default; direction: rtl; display: block; filter: none; flex: 0 1 auto; float: none; font: 15px Arial, sans-serif; font-weight: normal; height: 1em; width: max-content; justify-content: center; letter-spacing: normal; left: auto; right: auto; top: auto; line-height: normal; letter-spacing: normal; margin: 1em; max-height: none; max-width: none; min-height: none; min-width: none; opacity: 1; outline: medium invert color; overflow: visible; padding: 0.5em; position: static; resize: none; tab-size: 8; text-align: left; text-align-last: left; text-decoration: none currentcolor solid; text-ident: 0; text-justify: auto; text-overflow: clip; text-shadow: none; text-transform: capitalize; visibility: visible; word-break: normal; word-spacing: normal; word-wrap: normal;"

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


var printButton = document.createElement('button');
printButton.onclick = print_ticks;
printButton.innerText = "Print Result";
printButton.style = buttonStyle;
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
downloadButton.onclick = download_ticks;
downloadButton.id = "downloadButton";
downloadButton.style = buttonStyle;
downloadButton.style.marginLeft = "1em";
div.appendChild(downloadButton);

var downloadAnchor = document.createElement('a');
downloadAnchor.hidden = true;
downloadAnchor.id = "downloadAnchor";
div.appendChild(downloadAnchor);

var downloadButton = document.createElement('button');
downloadButton.innerText = "Quit";
downloadButton.setAttribute("onclick", "quitCapture()");
downloadButton.style = buttonStyle;
downloadButton.style.marginLeft = "1em";
div.appendChild(downloadButton);

var printDiv = document.createElement('div');
//printDiv.style = "all: initial; * {all: unset;}"
document.body.append(printDiv);

var tickP = document.createElement('textarea');
tickP.id = 'tickP'
//lazy values for height/width
tickP.style.width = '50%';
tickP.style.height = '50%';
tickP.hidden = true;
printDiv.appendChild(tickP);

var br = document.createElement('br');
printDiv.append(br);

var clearButton = document.createElement('button');
clearButton.onclick = clear_ticks;
clearButton.innerText = "Clear ticks";
clearButton.hidden = true;
clearButton.style = buttonStyle;
clearButton.id = 'clearButton'
printDiv.appendChild(clearButton);

var script = document.createElement("script");
script.innerText = "var quit = false;function quitCapture(){quit = true;}"
document.body.append(script);




