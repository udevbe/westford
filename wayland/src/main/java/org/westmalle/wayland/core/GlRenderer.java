package org.westmalle.wayland.core;

public interface GlRenderer extends Renderer {

    long eglConfig(long eglDisplay);
}
