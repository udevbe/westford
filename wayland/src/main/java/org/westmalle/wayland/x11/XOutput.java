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
    private final int               xWindow;
    @Nonnull
    private final Pointer           xDisplay;

    private XEglOutput eglOutput;

    XOutput(@Provided @Nonnull final XEglOutputFactory xEglOutputFactory,
            @Nonnull final Pointer xDisplay,
            final int xWindow) {
        this.xEglOutputFactory = xEglOutputFactory;
        this.xWindow = xWindow;
        this.xDisplay = xDisplay;
    }

    @Override
    public EglOutput getEglOutput() {
        if (this.eglOutput == null) {
            this.eglOutput = this.xEglOutputFactory.create(this.xDisplay,
                                                           this.xWindow);
        }
        return this.eglOutput;
    }
}
