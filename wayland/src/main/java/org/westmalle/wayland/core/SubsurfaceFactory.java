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
package org.westmalle.wayland.core;

import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;

public class SubsurfaceFactory {

    public Subsurface create(@Nonnull final WlSurfaceResource parentWlSurfaceResource,
                             @Nonnull final WlSurfaceResource wlSurfaceResource) {

        //TODO destroy listener for parent;
        //TODO destroy listener for surface

        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final Subsurface subsurface = new Subsurface(parentWlSurfaceResource,
                                                     wlSurfaceResource,
                                                     surface.getState());
        surface.getCommitSignal()
               .connect(event -> subsurface.commit());

        final WlSurface parentWlSurface = (WlSurface) parentWlSurfaceResource.getImplementation();
        final Surface   parentSurface   = parentWlSurface.getSurface();

        parentSurface.getCommitSignal()
                     .connect(event -> subsurface.parentCommit());
        parentSurface.getPositionSignal()
                     .connect(event -> subsurface.applyPosition());

        return subsurface;
    }
}
