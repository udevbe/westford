//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.bootstrap;

import com.google.common.util.concurrent.ServiceManager;
import com.jogamp.nativewindow.util.DimensionImmutable;
import com.jogamp.newt.MonitorMode;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLProfile;
import org.westmalle.wayland.output.*;
import org.westmalle.wayland.output.gl.GLRenderEngine;
import org.westmalle.wayland.output.gl.GLRenderEngineFactory;
import org.westmalle.wayland.platform.newt.GLWindowFactory;
import org.westmalle.wayland.platform.newt.GLWindowSeatFactory;
import org.westmalle.wayland.protocol.*;

public class Boot {

    private void strap(final Westmalle westmalle) {

        final GLWindowFactory glWindowFactory = westmalle.glWindowFactory();
        final GLRenderEngineFactory glRenderEngineFactory = westmalle.glRenderEngineFactory();
        final ShmRendererFactory shmRendererFactory = westmalle.shmRendererFactory();
        final CompositorFactory compositorFactory = westmalle.compositorFactory();
        final WlCompositorFactory wlCompositorFactory = westmalle.wlCompositorFactory();
        final GLWindowSeatFactory glWindowSeatFactory = westmalle.glWindowSeatFactory();
        final WlSeatFactory wlSeatFactory = westmalle.wlSeatFactory();
        final WlShellFactory wlShellFactory = westmalle.wlShellFactory();
        final WlOutputFactory wlOutputFactory = westmalle.wlOutputFactory();

        //create an output
        //create an X opengl enabled window

        final GLWindow glWindow = setupXOutput(glWindowFactory,
                                               wlOutputFactory);

        //setup our render engine
        //create an opengl render engine that uses shm buffers and outputs to an X opengl window
        final GLRenderEngine glRenderEngine = glRenderEngineFactory.create(glWindow);
        //create an shm renderer that passes on shm buffers to it's render implementation
        final ShmRenderer shmRenderer = shmRendererFactory.create(glRenderEngine);

        //setup compositing
        //create a compositor with shell and scene logic
        final Compositor compositor = compositorFactory.create(shmRenderer);
        //create a wayland compositor that delegates it's requests to a shell implementation.
        final WlCompositor wlCompositor = wlCompositorFactory.create(compositor);

        //setup seat
        //create a seat that listens for input on the X opengl window and passes it on to a wayland seat.
        //these objects will listen for input events
        final WlSeat wlSeat = wlSeatFactory.create();
        glWindowSeatFactory.create(glWindow,
                                   wlSeat,
                                   compositor);

        //enable wl_shell protocol
        wlShellFactory.create(wlCompositor);
        //TODO enable xdg_shell protocol
    }

    private GLWindow setupXOutput(final GLWindowFactory glWindowFactory,
                                  final WlOutputFactory wlOutputFactory) {
        final GLWindow glWindow = glWindowFactory.create(System.getenv("DISPLAY"),
                                                         GLProfile.getGL2ES2(),
                                                         800,
                                                         600);
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
        wlOutputFactory.create(outputGeometry,
                               outputMode);

        //TODO geometry & mode changes updates

        return glWindow;
    }

    private void run(final Westmalle westmalle) {
        //start all services, 1 thread per service & exit main thread.
        new ServiceManager(westmalle.services()).startAsync();
    }

    public static void main(final String[] args) {
        final Westmalle westmalle = Dagger_Westmalle.create();

        final Boot boot = new Boot();
        boot.strap(westmalle);
        boot.run(westmalle);
    }
}