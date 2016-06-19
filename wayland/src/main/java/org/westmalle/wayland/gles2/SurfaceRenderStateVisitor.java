package org.westmalle.wayland.gles2;


import java.util.Optional;

public interface SurfaceRenderStateVisitor {
    default Optional<SurfaceRenderState> visit(final ShmSurfaceRenderState shmSurfaceRenderState) { return Optional.of(shmSurfaceRenderState); }

    default Optional<SurfaceRenderState> visit(final EglSurfaceRenderState eglSurfaceRenderState) { return Optional.of(eglSurfaceRenderState); }
}
