package org.westmalle.wayland.gles2;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnegative;
import java.util.Optional;

@AutoValue
public abstract class ShmSurfaceRenderState implements SurfaceRenderState {

    public static ShmSurfaceRenderState create(@Nonnegative final int pitch,
                                               @Nonnegative final int height,
                                               final int target,
                                               final int shaderProgram,
                                               final int glFormat,
                                               final int glPixelType,
                                               final int texture) {
        return new AutoValue_ShmSurfaceRenderState(pitch,
                                                   height,
                                                   target,
                                                   shaderProgram,
                                                   glFormat,
                                                   glPixelType,
                                                   texture);
    }

    @Nonnegative
    public abstract int getPitch();

    @Nonnegative
    public abstract int getHeight();

    public abstract int getTarget();

    public abstract int getShaderProgram();

    public abstract int getGlFormat();

    public abstract int getGlPixelType();

    public abstract int getTexture();


    @Override
    public Optional<SurfaceRenderState> accept(final SurfaceRenderStateVisitor surfaceRenderStateVisitor) {
        return surfaceRenderStateVisitor.visit(this);
    }
}
