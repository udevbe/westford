package org.westmalle.wayland.core;

import javax.annotation.Nonnull;

public interface BufferVisitor {

    default void visit(@Nonnull final Buffer buffer) {}

    default void visit(@Nonnull final EglBuffer eglBuffer) {}

    default void visit(@Nonnull final SmBuffer smBuffer) {}
}
