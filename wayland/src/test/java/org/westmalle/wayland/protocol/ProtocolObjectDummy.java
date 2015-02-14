package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;

public class ProtocolObjectDummy implements ProtocolObject<Resource<?>> {

    private final Set<Resource<?>> resources = new HashSet<>();

    @Nonnull
    @Override
    public Set<Resource<?>> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public Resource<?> create(@Nonnull final Client client,
                              @Nonnegative final int version,
                              final int id) {
        return mock(Resource.class);
    }
}
