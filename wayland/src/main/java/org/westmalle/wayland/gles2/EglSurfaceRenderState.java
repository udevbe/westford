package org.westmalle.wayland.gles2;

import com.google.auto.value.AutoValue;
import org.freedesktop.jaccall.Pointer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Optional;

@AutoValue
public abstract class EglSurfaceRenderState implements SurfaceRenderState {

    public static EglSurfaceRenderState create(@Nonnegative final int pitch,
                                               @Nonnegative final int height,
                                               final int target,
                                               final int shaderProgram,
                                               final boolean yInverted,
                                               final int[] textures,
                                               final long[] eglImages) {
        return new AutoValue_EglSurfaceRenderState(pitch,
                                                   height,
                                                   target,
                                                   shaderProgram,
                                                   yInverted,
                                                   textures,
                                                   eglImages);
    }

    @Nonnegative
    public abstract int getPitch();

    @Nonnegative
    public abstract int getHeight();

    public abstract int getTarget();

    public abstract int getShaderProgram();

    public abstract boolean getYInverted();

    public abstract int[] getTextures();

    public abstract long[] getEglImages();

    @Override
    public Optional<SurfaceRenderState> accept(final SurfaceRenderStateVisitor surfaceRenderStateVisitor) {
        return surfaceRenderStateVisitor.visit(this);
    }
}
