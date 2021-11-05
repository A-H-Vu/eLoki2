//Script used to write the main landing page for the recording capture

//arg 0 is the status message 
if(arguments.length > 0){
	var statusInput = arguments[0]
}
else{
	var statusInput = "";
}
//avoid securityexception on firefox from document.write, manually construct using js
function init(){

	//Write text on the page
	var title = document.createElement("title");
	title.innerText = "Mouse Capture";
	document.head.append(title);

	document.body.style.textAlign = "center";
	document.body.bgColor = "white";

	var heading = document.createElement("h2");
	heading.innerText = "Capture instructions";
	document.body.append(heading)

	var info = document.createElement("p");
	info.innerText = "After entering the base url, click start recording. Wait until the recording header loads. Press Ctrl to start and stop recording. The header will be red during recording";
	info.style.margin = "2em";
	document.body.append(info);


	//p for status message if there is one
	var statusInfo = document.createElement("p");
	statusInfo.innerText = statusInput;
	document.body.append(statusInfo);

	//Container for most of the input fields
	var cont = document.createElement("div");
	cont.style.paddingTop = "5em";
	document.body.append(cont);

	//Labal and input element for url box
	var urlLabel = document.createElement("label");
	urlLabel.for = "iURL";
	urlLabel.innerText = "URL";
	cont.append(urlLabel);

	var urlInput = document.createElement("input");
	urlInput.id = "iURL";
	urlInput.type = "text";
	urlInput.addEventListener("keyup", function(event) {
	  // Number 13 is the "Enter" key on the keyboard
	  if (event.keyCode === 13) {
	    // Cancel the default action, if needed
	    event.preventDefault();
	    // Trigger the button element with a click
	    document.getElementById("mainButton").click();
	  }
	});
	cont.append(urlInput);

	//Button to start the recording
	var urlButton = document.createElement("button");
	urlButton.setAttribute("onclick", "buttonClick()");
	urlButton.innerText = "Go";
	urlButton.id = "mainButton"
	cont.append(urlButton);

	//p for error messages
	var bOutput = document.createElement("p");
	bOutput.id = "bOutput"
	cont.append(bOutput)

	//change global ready var by inserting the function that changes it in the body
	//there are inconsistencies between what works in chrome vs firefox
	//whether anonymous functions can modify global variables
	//firefox appears to block all global modification from anonymous functions
	//this is to work around that restriction by doing the modification in a global script
	//
	//The script sets ready to true if the url is valid according to the js url class,
	//otherwise 
	var script = document.createElement("script");
	script.innerText = "var ready = false;function buttonClick(){u = document.getElementById('iURL').value;try{new URL(u); ready = true;}catch(_){document.getElementById('bOutput').innerText = u+' is not a valid URL'}}"
	document.body.append(script);
	
	urlInput.focus()
}

init();
