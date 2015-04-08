package org.westmalle.wayland.platform.newt;


import com.google.auto.value.AutoValue;

import com.jogamp.newt.opengl.GLWindow;

import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;

@AutoValue
public abstract class GLWindowOutput {

    public static GLWindowOutput create(@Nonnull final GLWindow glWindow,
                                        @Nonnull final WlOutput wlOutput){
        return new AutoValue_GLWindowOutput(glWindow,wlOutput);
    }

    @Nonnull
    public abstract GLWindow getGlWindow();

    @Nonnull
    public abstract WlOutput getWlOutput();
}
