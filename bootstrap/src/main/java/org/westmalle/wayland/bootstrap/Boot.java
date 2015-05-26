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
import com.jogamp.opengl.GLProfile;
import org.westmalle.wayland.jogl.JoglComponent;
import org.westmalle.wayland.jogl.JoglOutputFactory;
import org.westmalle.wayland.jogl.JoglRenderEngineFactory;
import org.westmalle.wayland.jogl.JoglSeatFactory;
import org.westmalle.wayland.output.*;
import org.westmalle.wayland.protocol.*;

public class Boot {

    private void strap(final OutputComponent outputComponent) {

        final RendererFactory     shmRendererFactory  = outputComponent.shmRendererFactory();
        final CompositorFactory   compositorFactory   = outputComponent.compositorFactory();
        final WlCompositorFactory wlCompositorFactory = outputComponent.wlCompositorFactory();
        final WlSeatFactory       wlSeatFactory       = outputComponent.wlSeatFactory();
        final WlShellFactory      wlShellFactory      = outputComponent.wlShellFactory();

        final JoglComponent           joglComponent           = outputComponent.newJoglComponent();
        final JoglOutputFactory       joglOutputFactory       = joglComponent.outputFactory();
        final JoglRenderEngineFactory joglRenderEngineFactory = joglComponent.renderEngineFactory();
        final JoglSeatFactory         joglSeatFactory         = joglComponent.seatFactory();

        //create an output
        //create an X opengl enabled window
        final WlOutput joglOutput = joglOutputFactory.create(System.getenv("DISPLAY"),
                                                             GLProfile.getGL2ES2(),
                                                             800,
                                                             600);
        //setup our render engine
        //create an opengl render engine that uses shm buffers and can output to an opengl window
        final GLWindow glWindow = (GLWindow) joglOutput.getOutput()
                                                       .getImplementation();
        final RenderEngine joglRenderEngine = joglRenderEngineFactory.create(glWindow.getContext());
        //create an shm renderer that passes on shm buffers to it's render implementation
        final Renderer renderer = shmRendererFactory.create(joglRenderEngine);

        //setup compositing
        //create a compositor with shell and scene logic
        final Compositor compositor = compositorFactory.create(renderer);
        //add our output to the compositor
        //TODO add hotplug functionality
        compositor.getWlOutputs()
                  .add(joglOutput);
        //create a wayland compositor that delegates it's requests to a shell implementation.
        final WlCompositor wlCompositor = wlCompositorFactory.create(compositor);

        //setup seat
        //create a seat that listens for input on the X opengl window and passes it on to a wayland seat.
        //these objects will listen for input events
        final WlSeat wlSeat = wlSeatFactory.create();
        joglSeatFactory.create(glWindow,
                               wlSeat,
                               compositor);

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