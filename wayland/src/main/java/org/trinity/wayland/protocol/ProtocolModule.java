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
package org.trinity.wayland.protocol;

import dagger.Module;
import dagger.Provides;
import org.freedesktop.wayland.server.Display;
import org.trinity.wayland.output.OutputModule;

import javax.inject.Singleton;

@Module(
        includes = {
                OutputModule.class
        },
        injects = {
                WlSeatFactory.class,
                WlKeyboardFactory.class,
                WlPointerFactory.class,
                WlTouchFactory.class,
                WlDataDeviceFactory.class,
                WlDataDeviceFactory.class,
                WlSurfaceFactory.class,
                WlRegionFactory.class,
                WlShellSurfaceFactory.class,
                org.trinity.wayland.protocol.WlSubSurfaceFactory.class
        },
        library = true,
        //depends on wlmodule that needs a render engine
        complete = false
)
public class ProtocolModule {

    @Provides
    @Singleton
    WlSubCompositor provideWlSubCompositor(final Display display,
                                           final org.trinity.wayland.protocol.WlSubSurfaceFactory wlSubSurfaceFactory) {
        return new WlSubCompositor(display,
                                   wlSubSurfaceFactory);
    }
}
