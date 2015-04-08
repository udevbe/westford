package org.westmalle.wayland.platform.newt;


import com.google.auto.value.AutoValue;

import com.jogamp.newt.opengl.GLWindow;

import org.westmalle.wayland.protocol.WlOutput;

@AutoValue
public abstract class GLWindowOutput {

    public static GLWindowOutput create(final GLWindow glWindow,
                                        final WlOutput wlOutput){
        return new AutoValue_GLWindowOutput(glWindow,wlOutput);
    }

    public abstract GLWindow getGlWindow();

    public abstract WlOutput getWlOutput();
}
