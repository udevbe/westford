package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.WlBufferResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface Buffer {

    @Nonnegative
    int getWidth();

    @Nonnegative
    int getHeight();

    @Nonnull
    WlBufferResource getWlBufferResource();

    void accept(@Nonnegative BufferVisitor bufferVisitor);
}
