package org.westmalle.wayland.gbm;


import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory
public class GbmConnector implements Connector {

    GbmConnector() {
    }

    @Nonnull
    @Override
    public Optional<WlOutput> getWlOutput() {
        return null;
    }
}
