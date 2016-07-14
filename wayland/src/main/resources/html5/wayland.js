var socket = new WebSocket("wss://" + location.hostname);

socket.onopen = function () {
    //request output info
    socket.send('req-output-info');
};

socket.onerror = function (error) {
  //TODO show error on screen
  console.log('WebSocket Error ' + error);
};

socket.onclose = function () {
    alert("Connection with server closed; Maybe the server wasn't found, it shut down or you're behind a firewall/proxy.");
    //TODO should we attempt to reconnect?
};

socket.onmessage = function (e) {
    if(typeof e.data === "string"){
        //create canvas object based on output (json?) reply
        var canvas = document.createElement('canvas');

        //TODO map canvas geometry to reported server output
        canvas.id     = "output";
        canvas.width  = 1024;
        canvas.height = 768;
        canvas.style.position = "absolute";
        canvas.style.border   = "1px solid";

        //canvas left & right mouse click listeners
        //TODO add middle click? How to emulate other buttons?
        canvas.addEventListener('click',function(evt){
            //p(ointer):b(utton):0:t(ime):xxxxxxx
            socket.send("p:b:0:t:"+Date.now())
        },false);
        canvas.addEventListener('contextmenu', function(ev) {
            ev.preventDefault();
            //p(ointer):b(utton):1:t(ime):xxxxxxx
            socket.send("p:b:1:t:"+Date.now())
            return false;
        }, false);

        //TODO canvas key events

        document.body.appendChild(canvas);
    } else {
        //TODO read data as image & put it in the canvas
    }
};