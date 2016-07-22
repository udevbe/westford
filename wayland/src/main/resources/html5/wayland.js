//use browser url to find socket to connect to
var parser = document.createElement('a');
parser.href = document.URL;

socket = new WebSocket("ws://" + parser.host + parser.pathname + parser.hash.substring(1));
window.onbeforeunload = function(e) {
    socket.close();
};

function createOutputCanvas(outputInfo){
    //make canvas & context global
    canvas = document.createElement('canvas');
    ctx = canvas.getContext('2d');

    img = new Image();
    img.onload = function() {
        ctx.drawImage(img, 0, 0, canvas.width, canvas.height, 0, 0, canvas.width, canvas.height);
        socket.send("ack-frame");
        URL.revokeObjectURL(img.src);
    };

    //TODO map canvas geometry to reported server output
    canvas.id     = "output";
    canvas.width  = 800;
    canvas.height = 600;
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
        var x = Math.round((ev.clientX-rect.left)/(rect.right-rect.left)*canvas.width);
        var y = Math.round((ev.clientY-rect.top)/(rect.bottom-rect.top)*canvas.height);
        //p(ointer):m(otion):x:xxxx:y:xxxx:t(ime):xxxxxxx
        socket.send("p:m:x:"+x+":y:"+y+":t:"+Date.now())
    }, false);
    canvas.addEventListener('keydown', function(ev) {
        //k(ey):d(own):c(ode):xxxx:t(ime):xxxxxx
        socket.send("k:d:c:"+ev.keyCode+":t:"+Date.now());
    }, false);
    canvas.addEventListener('keyup', function(ev) {
        //k(ey):u(p):c(ode):xxxx:t(ime):xxxxxx
        socket.send("k:u:c:"+ev.keyCode+":t:"+Date.now());
    }, false);

    document.body.appendChild(canvas);

    //replace text handler with blob handler
    socket.onmessage = function (e) {
        //read binary data as image & put it in the canvas
        img.src = URL.createObjectURL(e.data);
    };
    //notify server we have created the output canvas and are now ready to receive binary frames
    socket.send("ack-output-info");
}

socket.onopen = function () {
    //request output info so we can initialize our html5 canvas with the correct size
    socket.onmessage = function (e) {

        //TODO validate reply
        //create canvas object based on output info reply
        createOutputCanvas(e.data);
    }
    socket.send("req-output-info");
};

socket.onerror = function (error) {
  //TODO show error on screen
  console.log(error);
};

socket.onclose = function (event) {
    //TODO should we attempt to reconnect?
};

