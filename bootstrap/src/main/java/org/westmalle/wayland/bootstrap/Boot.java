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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.CompositorFactory;
import org.westmalle.wayland.core.DaggerOutputComponent;
import org.westmalle.wayland.core.OutputComponent;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.core.RendererFactory;
import org.westmalle.wayland.egl.EglGles2RenderEngine;
import org.westmalle.wayland.egl.EglRenderEngineFactory;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlCompositorFactory;
import org.westmalle.wayland.protocol.WlDataDeviceManagerFactory;
import org.westmalle.wayland.protocol.WlOutput;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.protocol.WlSeatFactory;
import org.westmalle.wayland.protocol.WlShellFactory;
import org.westmalle.wayland.x11.X11OutputFactory;
import org.westmalle.wayland.x11.X11SeatFactory;

import java.util.Arrays;

class Boot {

    private static final Logger LOGGER = LoggerFactory.getLogger(Boot.class);

    public static void main(final String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread,
                                                   throwable) -> LOGGER.error("Got uncaught exception",
                                                                              throwable));

        LOGGER.info("Starting Westmalle:\n"
                    + "\tArguments: {}",
                    args.length == 0 ? "<none>" : Arrays.toString(args));
        final OutputComponent outputComponent = DaggerOutputComponent.create();

        final Boot boot = new Boot();
        boot.strap(outputComponent);
    }

    private void strap(final OutputComponent outputComponent) {

        final RendererFactory            rendererFactory            = outputComponent.shmRendererFactory();
        final CompositorFactory          compositorFactory          = outputComponent.compositorFactory();
        final WlCompositorFactory        wlCompositorFactory        = outputComponent.wlCompositorFactory();
        final WlSeatFactory              wlSeatFactory              = outputComponent.wlSeatFactory();
        final WlDataDeviceManagerFactory wlDataDeviceManagerFactory = outputComponent.wlDataDeviceManagerFactory();
        final WlShellFactory             wlShellFactory             = outputComponent.wlShellFactory();

        final X11OutputFactory outputFactory = outputComponent.x11Component()
                                                              .outputFactory();
        final X11SeatFactory seatFactory = outputComponent.x11Component()
                                                          .seatFactory();

        final EglRenderEngineFactory renderEngineFactory = outputComponent.eglComponent()
                                                                          .renderEngineFactory();

        //create an output
        //create an X opengl enabled x11 window
        final WlOutput wlOutput = outputFactory.create(System.getenv("DISPLAY"),
                                                       800,
                                                       600);
        //setup our render engine
        final EglGles2RenderEngine renderEngine = renderEngineFactory.create();
        //create an shm renderer that passes on shm buffers to it's render implementation
        final Renderer renderer = rendererFactory.create(renderEngine);

        //setup compositing
        //create a compositor with shell and scene logic
        final Compositor compositor = compositorFactory.create(renderer);
        //add our output to the compositor
        //TODO add hotplug functionality
        compositor.getWlOutputs()
                  .add(wlOutput);
        //create a wayland compositor that delegates it's requests to a shell implementation.
        final WlCompositor wlCompositor = wlCompositorFactory.create(compositor);

        //create data device manager
        wlDataDeviceManagerFactory.create();

        //setup seat
        //create a seat that listens for input on the X opengl window and passes it on to a wayland seat.
        //these objects will listen for input events
        final WlSeat wlSeat = wlSeatFactory.create();
        seatFactory.create(wlOutput,
                           wlSeat,
                           compositor);

        //enable wl_shell protocol
        wlShellFactory.create(wlCompositor);
        //enable xdg_shell protocol
        //TODO implement xdg_shell protocol

        //start the thingamabah
        outputComponent.shellService()
                       .start();
    }
}