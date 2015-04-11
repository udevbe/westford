package org.westmalle.wayland.platform.newt;

import com.jogamp.nativewindow.util.DimensionImmutable;
import com.jogamp.newt.Display;
import com.jogamp.newt.MonitorMode;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import org.freedesktop.wayland.server.WlOutputResource;
import org.westmalle.wayland.output.*;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlOutputFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkArgument;

public class GLWindowOutputFactory {

    @Nonnull
    private final GLDrawables     glDrawables;
    @Nonnull
    private final WlOutputFactory wlOutputFactory;
    @Nonnull
    private final OutputFactory   outputFactory;

    @Inject
    GLWindowOutputFactory(@Nonnull final GLDrawables glDrawables,
                          @Nonnull final WlOutputFactory wlOutputFactory,
                          @Nonnull final OutputFactory outputFactory) {
        this.glDrawables = glDrawables;
        this.wlOutputFactory = wlOutputFactory;
        this.outputFactory = outputFactory;
    }

    public GLWindowOutput create(@Nonnull final String xDisplay,
                                 @Nonnull final GLProfile profile,
                                 @Nonnegative final int width,
                                 @Nonnegative final int height) {
        checkArgument(width > 0);
        checkArgument(height > 0);

        final GLWindow glWindow = createGLWindow(xDisplay,
                                                 profile,
                                                 width,
                                                 height);
        final WlOutput wlOutput = createWlOutput(glWindow);

        this.glDrawables.get()
                        .add(glWindow);

        return GLWindowOutput.create(glWindow,
                                     wlOutput);
    }

    private GLWindow createGLWindow(final String xDisplay,
                                    final GLProfile profile,
                                    final int width,
                                    final int height) {
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

    private WlOutput createWlOutput(final GLWindow glWindow) {
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

        final WlOutput wlOutput = this.wlOutputFactory.create(this.outputFactory.create(outputGeometry,
                                                                                        outputMode));
        addGLWindowListener(wlOutput,
                            glWindow);
        return wlOutput;
    }

    private void addGLWindowListener(final WlOutput wlOutput,
                                     final GLWindow glWindow) {
        glWindow.addWindowListener(new WindowListener() {
            @Override
            public void windowResized(final WindowEvent e) {
                final float[] pixelsPerMM = glWindow.getPixelsPerMM(new float[2]);
                final Output output = wlOutput.getOutput();
                final OutputGeometry newGeometry = output.getGeometry()
                                                         .toBuilder()
                                                         .physicalWidth((int) (glWindow.getSurfaceWidth() / pixelsPerMM[0]))
                                                         .physicalHeight((int) (glWindow.getSurfaceHeight() / pixelsPerMM[1]))
                                                         .build();
                output.update(wlOutput.getResources(),
                              newGeometry);
                wlOutput.getResources()
                        .forEach(WlOutputResource::done);
            }

            @Override
            public void windowMoved(final WindowEvent e) {
                final Output output = wlOutput.getOutput();
                final OutputGeometry newGeometry = output.getGeometry()
                                                         .toBuilder()
                                                         .x(glWindow.getX())
                                                         .y(glWindow.getY())
                                                         .build();
                output.update(wlOutput.getResources(),
                              newGeometry);
                wlOutput.getResources()
                        .forEach(WlOutputResource::done);
            }

            @Override
            public void windowDestroyNotify(final WindowEvent e) {

            }

            @Override
            public void windowDestroyed(final WindowEvent e) {
                GLWindowOutputFactory.this.glDrawables.get()
                                                      .remove(glWindow);
                wlOutput.destroy();
            }

            @Override
            public void windowGainedFocus(final WindowEvent e) {

            }

            @Override
            public void windowLostFocus(final WindowEvent e) {

            }

            @Override
            public void windowRepaint(final WindowUpdateEvent e) {

            }
        });
    }
}