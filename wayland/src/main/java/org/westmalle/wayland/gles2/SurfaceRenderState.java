package org.westmalle.wayland.gles2;

@FunctionalInterface
public interface SurfaceRenderState {
    void accept(SurfaceRenderStateVisitor surfaceRenderStateVisitor);
}
