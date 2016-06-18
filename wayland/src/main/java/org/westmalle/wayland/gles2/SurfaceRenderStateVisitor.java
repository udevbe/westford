package org.westmalle.wayland.gles2;


import java.util.Optional;

public interface SurfaceRenderStateVisitor {
    default Optional<SurfaceRenderState> visit(ShmSurfaceRenderState shmSurfaceRenderState) { return Optional.of(shmSurfaceRenderState); }

    default Optional<SurfaceRenderState> visit(EglSurfaceRenderState eglSurfaceRenderState) { return Optional.of(eglSurfaceRenderState); }
}
