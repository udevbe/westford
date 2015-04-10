package org.westmalle.wayland.output;

import com.jogamp.opengl.GLDrawable;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GLDrawables {

    @Nonnull
    private final Set<GLDrawable> glDrawables = new HashSet<>();

    @Inject
    GLDrawables() {
    }

    @Nonnull
    public Set<GLDrawable> get() {
        return this.glDrawables;
    }
}
