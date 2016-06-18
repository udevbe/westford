package org.westmalle.wayland.gles2;

import java.util.Optional;

@FunctionalInterface
public interface SurfaceRenderState {
    Optional<SurfaceRenderState> accept(SurfaceRenderStateVisitor surfaceRenderStateVisitor);
}
