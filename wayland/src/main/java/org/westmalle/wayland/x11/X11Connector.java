package org.westmalle.wayland.x11;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "X11ConnectorFactory")
public class X11Connector implements Connector {

    private final int                xWindow;
    @Nonnull
    private final Optional<WlOutput> wlOutput;

    X11Connector(final int xWindow,
                 @Nonnull final Optional<WlOutput> wlOutput) {
        this.xWindow = xWindow;
        this.wlOutput = wlOutput;
    }

    @Nonnull
    @Override
    public Optional<WlOutput> getWlOutput() {
        return this.wlOutput;
    }

    public int getXWindow() {
        return this.xWindow;
    }
}
