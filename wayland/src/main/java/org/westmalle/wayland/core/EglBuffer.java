package org.westmalle.wayland.core;


import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.WlBufferResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoValue
public abstract class EglBuffer implements Buffer {

    @Nonnull
    public static EglBuffer create(@Nonnegative final int width,
                                   @Nonnegative final int height,
                                   @Nonnegative final WlBufferResource wlBufferResource,
                                   final int textureFormat) {
        return new AutoValue_EglBuffer(width,
                                       height,
                                       wlBufferResource,
                                       textureFormat);
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

    public abstract int getTextureFormat();
}
