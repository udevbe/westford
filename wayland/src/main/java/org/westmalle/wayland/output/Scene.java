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
package org.westmalle.wayland.output;

import com.google.common.collect.Lists;
import org.freedesktop.wayland.server.WlSurfaceRequests;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlSurface;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;
import java.util.LinkedList;

@Singleton
public class Scene {
    private final LinkedList<WlSurfaceResource> surfacesStack = Lists.newLinkedList();

    @Inject
    Scene() {
    }

    public LinkedList<WlSurfaceResource> getSurfacesStack() { return this.surfacesStack; }

    public PointImmutable relativeCoordinate(final WlSurfaceResource surfaceResource,
                                             final PointImmutable absPosition) {
        final WlSurfaceRequests implementation = surfaceResource.getImplementation();
        final Surface Surface = ((WlSurface) implementation).getSurface();

        final PointImmutable position = Surface.getPosition();
        final int offsetX = position.getX();
        final int offsetY = position.getY();

        return new Point(absPosition.getX() - offsetX,
                         absPosition.getY() - offsetY);
    }

    public boolean needsRender(final WlSurfaceResource surfaceResource) {
        final WlSurfaceRequests implementation = surfaceResource.getImplementation();
        final Surface Surface = ((WlSurface) implementation).getSurface();
        if (Surface.isDestroyed()) {
            return true;
        }
        else {
            //for now, always redraw
            return true;
        }
    }
}
