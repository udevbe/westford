package org.westmalle.wayland.html5;


import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "Html5ConnectorFactory")
public class Html5Connector implements Connector {

    private final Connector connector;

    Html5Connector(final Connector connector) {
        this.connector = connector;
    }

    @Nonnull
    @Override
    public WlOutput getWlOutput() {
        return this.connector.getWlOutput();
    }

    //TODO add methods to send a buffer
}
