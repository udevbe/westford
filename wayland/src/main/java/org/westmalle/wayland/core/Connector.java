package org.westmalle.wayland.core;


import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface Connector {
    @Nonnull
    Optional<WlOutput> getWlOutput();
}
