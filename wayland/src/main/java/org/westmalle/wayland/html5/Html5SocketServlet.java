package org.westmalle.wayland.html5;


import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class Html5SocketServlet extends WebSocketServlet {
    @Override
    public void configure(final WebSocketServletFactory factory) {
        factory.register(Html5Socket.class);
    }


}
