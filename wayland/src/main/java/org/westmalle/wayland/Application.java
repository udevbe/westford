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
package org.westmalle.wayland;

import dagger.Component;
import org.westmalle.wayland.core.CompositorFactory;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.core.ShellService;
import org.westmalle.wayland.egl.EglComponent;
import org.westmalle.wayland.protocol.WlCompositorFactory;
import org.westmalle.wayland.protocol.WlDataDeviceManagerFactory;
import org.westmalle.wayland.protocol.WlShellFactory;
import org.westmalle.wayland.x11.X11Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = CoreModule.class)
public interface Application {
    //generic compositor output
    CompositorFactory compositorFactory();

    //core protocol
    WlCompositorFactory wlCompositorFactory();

    WlDataDeviceManagerFactory wlDataDeviceManagerFactory();

    WlShellFactory wlShellFactory();

    //running
    ShellService shellService();

    //render implementations
    X11Component x11();

    EglComponent egl();
}
