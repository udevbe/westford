package org.westmalle.wayland.gles2;

import com.google.auto.value.AutoValue;
import org.freedesktop.jaccall.Pointer;

import javax.annotation.Nonnegative;

@AutoValue
public abstract class ShmSurfaceRenderState implements SurfaceRenderState {

    public ShmSurfaceRenderState create(final int pitch,
                                        final int height,
                                        final int target,
                                        final int glFormat,
                                        final int glPixelType,
                                        final int texture) {
        //TODO specify shader program to use?
        return new AutoValue_ShmSurfaceRenderState(pitch,
                                                   height,
                                                   target,
                                                   glFormat,
                                                   glPixelType,
                                                   texture);
    }

    @Nonnegative
    public abstract int getPitch();

    @Nonnegative
    public abstract int getHeight();

    public abstract int getTarget();

    public abstract int getGlFormat();

    public abstract int getGlPixelType();

    public abstract int getTexture();

    @Override
    public void accept(final SurfaceRenderStateVisitor surfaceRenderStateVisitor) {
        surfaceRenderStateVisitor.visit(this);
    }
}
