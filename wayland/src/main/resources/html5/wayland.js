//use browser url to find socket to connect to
var socket = new WebSocket("wss://" + location.hostname);
//initialize socket using text for communication, used to initialize our output
socket.binaryType = "text";

function createOutputCanvas(outputInfo){
    //make canvas global
    canvas = document.createElement('canvas');

    //TODO map canvas geometry to reported server output
    canvas.id     = "output";
    canvas.width  = 1024;
    canvas.height = 768;
    canvas.style.position = "absolute";
    canvas.style.border   = "1px solid";

    //add canvas input listeners
    canvas.addEventListener('mousedown',function(ev){
        //p(ointer):d(own):b(utton):xxxx:t(ime):xxxxxxx
        socket.send("p:d:b:"+ev.button+":t:"+Date.now())
    },false);
    canvas.addEventListener('mouseup',function(ev){
        //p(ointer):u(p):b(utton):0:t(ime):xxxxxxx
        socket.send("p:u:b:"+ev.button+":t:"+Date.now())
    },false);
    canvas.addEventListener('contextmenu', function(ev) {
        ev.preventDefault();
        return false;
    }, false);
    canvas.addEventListener('mousemove', function(ev) {
        var rect = canvas.getBoundingClientRect();
        var x = Math.round((evt.clientX-rect.left)/(rect.right-rect.left)*canvas.width);
        var y = Math.round((evt.clientY-rect.top)/(rect.bottom-rect.top)*canvas.height);
        //p(ointer):m(otion):x:xxxx:y:xxxx:t(ime):xxxxxxx
        socket.send("p:m:x:"+x+":y:"+y+":t:"+Date.now())
    }, false);
    canvas.addEventListener('keydown', function(ev) {
        //k(ey):d(own):c(ode):xxxx:t(ime):xxxxxx
        socket.send("k:d:c"+ev.keyCode+":t:"+Date.now());
    }, false);
    canvas.addEventListener('keyup', function(ev) {
        //k(ey):u(p):c(ode):xxxx:t(ime):xxxxxx
        socket.send("k:d:c"+ev.keyCode+":t:"+Date.now());
    }, false);

    document.body.appendChild(canvas);
    //output canvas created, switch to binary mode so we can receive output frames
    socket.binaryType = "blob";
    //notify server we have created the output canvas and are now ready to receive binary frames
    socket.send("ack-output-info");
}

socket.onopen = function () {
    //request output info so we can initialize our html5 canvas with the correct size
    socket.send("req-output-info");
};

socket.onerror = function (error) {
  //TODO show error on screen
  console.log("WebSocket Error " + error);
};

socket.onclose = function () {
    alert("Connection with server closed; Maybe the server wasn't found, it shut down or you're behind a firewall/proxy.");
    //TODO should we attempt to reconnect?
};

socket.onmessage = function (e) {
    if(typeof e.data === "string"){
        //TODO validate reply
        //create canvas object based on output info reply
        createOutputCanvas(e.data);
    } else {
        //read binary data as image & put it in the canvas
        var ctx = canvas.getContext('2d');
        var img = new Image;
        img.onload = function() {
            ctx.drawImage(img, canvas.width, canvas.height);
        }
        img.src = URL.createObjectURL(e.data);
    }
};