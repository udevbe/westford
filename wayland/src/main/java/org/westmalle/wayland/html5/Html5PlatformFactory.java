package org.westmalle.wayland.html5;


import org.westmalle.wayland.core.Platform;

import javax.inject.Inject;

public class Html5PlatformFactory {

    private final Platform                    platform;
    private final PrivateHtml5PlatformFactory privateHtml5PlatformFactory;

    @Inject
    Html5PlatformFactory(final Platform platform,
                         final PrivateHtml5PlatformFactory privateHtml5PlatformFactory) {
        this.platform = platform;
        this.privateHtml5PlatformFactory = privateHtml5PlatformFactory;
    }

    public Html5Platform create() {
        //TODO setup factory that inits an embedded jetty server with a websocket and pass it in the constructor

        return this.privateHtml5PlatformFactory.create(null);
    }
}
