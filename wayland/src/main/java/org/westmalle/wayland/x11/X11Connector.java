package org.westmalle.wayland.x11;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.protocol.WlOutput;

import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "PrivateX11ConnectorFactory")
public class X11Connector implements Connector {

    private final int                xWindow;
    private final Optional<WlOutput> wlOutput;

    X11Connector(final int xWindow,
                 final Optional<WlOutput> wlOutput) {
        this.xWindow = xWindow;
        this.wlOutput = wlOutput;
    }

    @Override
    public Optional<WlOutput> getWlOutput() {
        return this.wlOutput;
    }

    public int getXWindow() {
        return this.xWindow;
    }
}
