package org.westmalle.wayland.platform.newt;

import com.jogamp.nativewindow.util.DimensionImmutable;
import com.jogamp.newt.Display;
import com.jogamp.newt.MonitorMode;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

import org.westmalle.wayland.output.Output;
import org.westmalle.wayland.output.OutputGeometry;
import org.westmalle.wayland.output.OutputMode;
import org.westmalle.wayland.protocol.WlOutput;

import javax.inject.Inject;

public class GLWindowOutputFactory {

    private final WlOutputFactory wlOutputFactory;
    private final OutputFactory outputFactory;

    @Inject
    GLWindowOutputFactory(final WlOutputFactory wlOutputFactory,
                          final OutputFactory outputFactory) {
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
    }

    public GLWindowOutput create(final String xDisplay,
                                 final GLProfile profile,
                                 final int width,
                                 final int height){
        final GLWindow glWindow = createGLWindow(xDisplay,
                                                 profile,
                                                 width,
                                                 height);
        final WlOutput wlOutput = createWlOutput(glWindow);

        //TODO makes sure our wl output gets update if our monitor/gl window is updated.

        return GLWindowOutput.create(glWindow,
                                     wlOutput);
    }

    private GLWindow createGLWindow(final String xDisplay,
                                    final GLProfile profile,
                                    final int width,
                                    final int height){
        final Display display = NewtFactory.createDisplay(xDisplay);
        final Screen screen = NewtFactory.createScreen(display,
                                                       0);
        final GLWindow glWindow = GLWindow.create(screen,
                                                  new GLCapabilities(profile));
        glWindow.setSize(width,
                         height);
        glWindow.setVisible(true,
                            true);

        return glWindow;
    }

    private WlOutput createWlOutput(final GLWindow glWindow){
        final float[] pixelsPerMM = glWindow.getPixelsPerMM(new float[2]);

        final OutputGeometry outputGeometry = OutputGeometry.builder()
                .x(glWindow.getX())
                .y(glWindow.getY())
                .physicalWidth((int) (glWindow.getSurfaceWidth() / pixelsPerMM[0]))
                .physicalHeight((int) (glWindow.getSurfaceHeight() / pixelsPerMM[1]))
                .make("NEWT")
                .model("GLX Window")
                .subpixel(0)
                .transform(0)
                .build();

        final MonitorMode currentMode = glWindow.getMainMonitor()
                .getCurrentMode();
        final DimensionImmutable resolution = currentMode.getSurfaceSize()
                .getResolution();
        final OutputMode outputMode = OutputMode.builder()
                .flags(currentMode.getFlags())
                .refresh((int) currentMode.getRefreshRate())
                .width(resolution.getWidth())
                .height(resolution.getHeight())
                .build();

        return this.wlOutputFactory.create(this.outputFactory.create(outputGeometry,
                                                                     outputMode));
    }
}