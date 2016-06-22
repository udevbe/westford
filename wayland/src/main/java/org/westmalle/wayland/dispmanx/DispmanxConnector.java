package org.westmalle.wayland.dispmanx;


import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "DispmanxConnectorFactory")
public class DispmanxConnector implements Connector {

    private final Optional<WlOutput> wlOutput;
    private final int                dispmanxElement;


    DispmanxConnector(final Optional<WlOutput> wlOutput,
                      final int dispmanxElement) {
        this.wlOutput = wlOutput;
        this.dispmanxElement = dispmanxElement;
    }

    @Nonnull
    @Override
    public Optional<WlOutput> getWlOutput() {
        return this.wlOutput;
    }


    public int getDispmanxElement() {
        return this.dispmanxElement;
    }
}
