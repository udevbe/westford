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

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.jogamp.newt.opengl.GLWindow;
import dagger.ObjectGraph;
import org.westmalle.wayland.output.Compositor;
import org.westmalle.wayland.output.CompositorFactory;
import org.westmalle.wayland.output.ShmRenderer;
import org.westmalle.wayland.output.ShmRendererFactory;
import org.westmalle.wayland.output.gl.GLRenderEngine;
import org.westmalle.wayland.output.gl.GLRenderEngineFactory;
import org.westmalle.wayland.platform.newt.GLWindowFactory;
import org.westmalle.wayland.platform.newt.GLWindowSeatFactory;
import org.westmalle.wayland.protocol.*;

import javax.inject.Inject;
import java.util.Set;

public class EntryPoint {

    private final ServiceManager        serviceManager;
    private final GLWindowFactory       glWindowFactory;
    private final GLRenderEngineFactory glRenderEngineFactory;
    private final ShmRendererFactory    shmRendererFactory;
    private final CompositorFactory     compositorFactory;
    private final WlCompositorFactory   wlCompositorFactory;
    private final GLWindowSeatFactory   glWindowSeatFactory;
    private final WlSeatFactory         wlSeatFactory;
    private final WlShellFactory        wlShellFactory;

    @Inject
    EntryPoint(final GLWindowFactory glWindowFactory,
               final GLRenderEngineFactory glRenderEngineFactory,
               final ShmRendererFactory shmRendererFactory,
               final CompositorFactory compositorFactory,
               final WlCompositorFactory wlCompositorFactory,
               final GLWindowSeatFactory glWindowSeatFactory,
               final WlSeatFactory wlSeatFactory,
               final WlShellFactory wlShellFactory,
               final Set<Service> services) {
        this.glWindowFactory = glWindowFactory;
        this.glRenderEngineFactory = glRenderEngineFactory;
        this.shmRendererFactory = shmRendererFactory;
        this.compositorFactory = compositorFactory;
        this.wlCompositorFactory = wlCompositorFactory;
        this.glWindowSeatFactory = glWindowSeatFactory;
        this.wlSeatFactory = wlSeatFactory;
        this.wlShellFactory = wlShellFactory;

        //group services that will drive compositor
        this.serviceManager = new ServiceManager(services);
    }

    private void enter() {
        //create an output
        //create an X opengl enabled window
        final GLWindow glWindow = this.glWindowFactory.create();

        //setup our render engine
        //create an opengl render engine that uses shm buffers and outputs to an X opengl window
        final GLRenderEngine glRenderEngine = this.glRenderEngineFactory.create(glWindow);
        //create an shm renderer that passes on shm buffers to it's render implementation
        final ShmRenderer shmRenderer = this.shmRendererFactory.create(glRenderEngine);

        //setup compositing
        //create a compositor with shell and scene logic
        final Compositor compositor = this.compositorFactory.create(shmRenderer);
        //create a wayland compositor that delegates it's requests to a shell implementation.
        final WlCompositor wlCompositor = this.wlCompositorFactory.create(compositor);

        //setup seat
        //create a seat that listens for input on the X opengl window and passes it on to a wayland seat.
        //these objects will listen for input events
        final WlSeat wlSeat = this.wlSeatFactory.create();
        this.glWindowSeatFactory.create(glWindow,
                                        wlSeat,
                                        compositor);

        //enable wl_shell protocol
        this.wlShellFactory.create(wlCompositor);
        //TODO enable xdg_shell protocol

        //start all services, 1 thread per service & exit main thread.
        this.serviceManager.startAsync();
    }

    public static void main(final String[] args) {

        final BootStrapModule westmalleShellModule = new BootStrapModule();
        final ObjectGraph objectGraph = ObjectGraph.create(westmalleShellModule);
        objectGraph.injectStatics();

        objectGraph.get(EntryPoint.class)
                   .enter();
    }
}