package org.westmalle.wayland.gles2;


public interface SurfaceRenderStateVisitor {
    void visit(ShmSurfaceRenderState shmSurfaceRenderState);

    void visit(EglSurfaceRenderState eglSurfaceRenderState);
}
