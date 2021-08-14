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
	tickP.innerText = get_ticks();
	clearButton.hidden = false;

}
function clear_ticks() {
	var clearButton = document.getElementById('clearButton');
	var tickP = document.getElementById('tickP');
	tickP.innerText = ''
	clearButton.hidden = true;
	
}

function get_ticks() {
	return window.sessionStorage.getItem("ticks");
}

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

var printDiv = document.createElement('div');
div.style = "all: initial; * {all: unset;}"
document.body.append(printDiv);

var tickP = document.createElement('p');
tickP.id = 'tickP'
printDiv.appendChild(tickP);

var clearButton = document.createElement('button');
clearButton.onclick = clear_ticks;
clearButton.innerText = "Clear ticks";
clearButton.hidden = true;
clearButton.style = buttonStyle;
clearButton.id = 'clearButton'
printDiv.appendChild(clearButton);




