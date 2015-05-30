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
import com.jogamp.newt.opengl.GLWindow;
import org.westmalle.wayland.egl.EglRenderEngine;
import org.westmalle.wayland.egl.EglRenderEngineFactory;
import org.westmalle.wayland.egl.HasEglOutput;
import org.westmalle.wayland.output.*;
import org.westmalle.wayland.protocol.*;
import org.westmalle.wayland.x11.X11OutputFactory;

class Boot {

    private void strap(final OutputComponent outputComponent) {

        final RendererFactory     rendererFactory     = outputComponent.shmRendererFactory();
        final CompositorFactory   compositorFactory   = outputComponent.compositorFactory();
        final WlCompositorFactory wlCompositorFactory = outputComponent.wlCompositorFactory();
        final WlSeatFactory       wlSeatFactory       = outputComponent.wlSeatFactory();
        final WlShellFactory      wlShellFactory      = outputComponent.wlShellFactory();

        final X11OutputFactory outputFactory = outputComponent.x11Component()
                                                              .outputFactory();

        final EglRenderEngineFactory renderEngineFactory = outputComponent.eglComponent()
                                                                          .renderEngineFactory();

        //create an output
        //create an X opengl enabled x11 window
        final WlOutput wlOutput = outputFactory.create(System.getenv("DISPLAY"),
                                                       800,
                                                       600);
        //setup our render engine
        final EglRenderEngine renderEngine = renderEngineFactory.create();
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

        //setup seat
        //create a seat that listens for input on the X opengl window and passes it on to a wayland seat.
        //these objects will listen for input events
        final WlSeat wlSeat = wlSeatFactory.create();
//        joglSeatFactory.create(glWindow,
//                               wlSeat,
//                               compositor);

        //enable wl_shell protocol
        wlShellFactory.create(wlCompositor);
        //TODO enable xdg_shell protocol
    }

    private void run(final OutputComponent outputComponent) {
        //start all services, 1 thread per service & exit main thread.
        new ServiceManager(outputComponent.services()).startAsync();
    }

    public static void main(final String[] args) {
        final OutputComponent outputComponent = DaggerOutputComponent.create();

        final Boot boot = new Boot();
        boot.strap(outputComponent);
        boot.run(outputComponent);
    }
}