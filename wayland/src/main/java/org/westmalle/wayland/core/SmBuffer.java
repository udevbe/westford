package org.westmalle.wayland.core;


import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoValue
public abstract class SmBuffer implements Buffer {

    @Nonnull
    public static SmBuffer create(@Nonnegative final int width,
                                  @Nonnegative final int height,
                                  @Nonnull final WlBufferResource wlBufferResource,
                                  @Nonnull final ShmBuffer shmBuffer) {
        return new AutoValue_SmBuffer(width,
                                      height,
                                      wlBufferResource,
                                      shmBuffer);
    }

    @Override
    public void accept(@Nonnull final BufferVisitor bufferVisitor) {
        bufferVisitor.visit(this);
    }

    @Override
    @Nonnegative
    public abstract int getWidth();

    @Override
    @Nonnegative
    public abstract int getHeight();

    @Override
    @Nonnull
    public abstract WlBufferResource getWlBufferResource();

    @Nonnull
    public abstract ShmBuffer getShmBuffer();
}
