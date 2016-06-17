package org.westmalle.wayland.gles2;

import com.google.auto.value.AutoValue;
import org.freedesktop.jaccall.Pointer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoValue
public abstract class EglSurfaceRenderState implements SurfaceRenderState {

    public static EglSurfaceRenderState create(final int pitch,
                                               final int height,
                                               final int target,
                                               final boolean yInverted,
                                               final int[] textures,
                                               final long[] eglImages) {
        //TODO specify shader program to use?
        return new AutoValue_EglSurfaceRenderState(pitch,
                                                   height,
                                                   target,
                                                   yInverted,
                                                   textures,
                                                   eglImages);
    }

    @Nonnegative
    public abstract int getPitch();

    @Nonnegative
    public abstract int getHeight();

    public abstract int getTarget();

    public abstract boolean getYInverted();

    public abstract int[] getTextures();

    public abstract long[] getEglImages();

    @Override
    public void accept(final SurfaceRenderStateVisitor surfaceRenderStateVisitor) {
        surfaceRenderStateVisitor.visit(this);
    }
}
