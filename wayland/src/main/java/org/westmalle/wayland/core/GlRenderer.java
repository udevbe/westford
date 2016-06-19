package org.westmalle.wayland.core;

import javax.annotation.Nonnull;

public interface GlRenderer extends Renderer {

    long eglConfig(long eglDisplay,
                   @Nonnull String eglExtensions);
}
