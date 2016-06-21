package org.westmalle.wayland.core;


import org.westmalle.wayland.protocol.WlOutput;

import java.util.Optional;

public interface Connector {
    Optional<WlOutput> getWlOutput();
}
