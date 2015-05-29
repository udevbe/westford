package org.westmalle.wayland.x11;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import com.sun.jna.Pointer;

import org.westmalle.wayland.egl.EglOutput;
import org.westmalle.wayland.egl.HasEglOutput;

import javax.annotation.Nonnull;

@AutoFactory(className = "XOutputFactory")
public class XOutput implements HasEglOutput {

    @Nonnull
    private final XEglOutputFactory xEglOutputFactory;
    private final int window;
    @Nonnull
    private final Pointer display;

    private XEglOutput eglOutput;

    XOutput(@Provided @Nonnull XEglOutputFactory xEglOutputFactory,
            @Nonnull final Pointer display,
            final int window) {
        this.xEglOutputFactory = xEglOutputFactory;
        this.window = window;
        this.display = display;
    }

    @Override
    public EglOutput getEglOutput() {
        if (this.eglOutput == null) {
            this.eglOutput = this.xEglOutputFactory.create(this.display,
                                                           this.window);
        }
        return this.eglOutput;
    }
}
