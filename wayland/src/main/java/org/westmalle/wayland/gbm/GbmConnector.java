package org.westmalle.wayland.gbm;


import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "GbmConnectorFactory")
public class GbmConnector implements Connector {

    @Nonnull
    private final Optional<WlOutput> wlOutput;

    GbmConnector(@Nonnull final Optional<WlOutput> wlOutput,
                 final long drmConnector,
                 final int drmCrtc) {
        this.wlOutput = wlOutput;
    }

    @Nonnull
    @Override
    public Optional<WlOutput> getWlOutput() {
        return this.wlOutput;
    }
}
