package org.westmalle.wayland.core;


import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlBufferResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoValue
public abstract class UnsupportedBuffer implements Buffer {

    public static UnsupportedBuffer create(@Nonnull WlBufferResource wlBufferResource) {
        return new AutoValue_UnsupportedBuffer(wlBufferResource);
    }

    @Nonnegative
    @Override
    public int getWidth() {
        return 0;
    }

    @Nonnegative
    @Override
    public int getHeight() {
        return 0;
    }

    @Nonnull
    @Override
    public abstract WlBufferResource getWlBufferResource();

    @Override
    public void accept(@Nonnegative final BufferVisitor bufferVisitor) {
        bufferVisitor.visit(this);
    }
}
