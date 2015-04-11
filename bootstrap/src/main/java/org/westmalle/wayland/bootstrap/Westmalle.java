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
import dagger.Component;
import org.westmalle.wayland.output.CompositorFactory;
import org.westmalle.wayland.output.ShmRendererFactory;
import org.westmalle.wayland.output.gl.GLRenderEngineFactory;
import org.westmalle.wayland.output.gl.OutputGLModule;
import org.westmalle.wayland.platform.newt.GLWindowOutputFactory;
import org.westmalle.wayland.platform.newt.GLWindowSeatFactory;
import org.westmalle.wayland.platform.newt.PlatformNewtModule;
import org.westmalle.wayland.protocol.ProtocolModule;
import org.westmalle.wayland.protocol.WlCompositorFactory;
import org.westmalle.wayland.protocol.WlSeatFactory;
import org.westmalle.wayland.protocol.WlShellFactory;

import javax.inject.Singleton;
import java.util.Set;

@Singleton
@Component(modules = {
        OutputGLModule.class,
        ProtocolModule.class,
        PlatformNewtModule.class
})
interface Westmalle {
    //platform newt
    GLWindowOutputFactory glWindowOutputFactory();

    GLWindowSeatFactory glWindowSeatFactory();

    //output
    GLRenderEngineFactory glRenderEngineFactory();

    ShmRendererFactory shmRendererFactory();

    CompositorFactory compositorFactory();

    //protocol
    WlCompositorFactory wlCompositorFactory();

    WlSeatFactory wlSeatFactory();

    WlShellFactory wlShellFactory();

    //running
    Set<Service> services();
}
