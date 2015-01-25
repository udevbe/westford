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
package org.trinity.wayland.output;

import com.google.common.collect.Lists;
import org.freedesktop.wayland.server.WlSurfaceRequests;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.trinity.wayland.protocol.WlSurface;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.media.nativewindow.util.Point;
import javax.media.nativewindow.util.PointImmutable;
import javax.media.nativewindow.util.RectangleImmutable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

@Singleton
public class Scene {
    private final LinkedList<WlSurfaceResource> SurfacesStack = Lists.newLinkedList();

    @Inject
    Scene() {
    }

    public LinkedList<WlSurfaceResource> getSurfacesStack() { return this.SurfacesStack; }

    public PointImmutable relativeCoordinate(final WlSurfaceResource surfaceResource,
                                             final int absX,
                                             final int absY) {
        final WlSurfaceRequests implementation = surfaceResource.getImplementation();
        final Surface Surface = ((WlSurface) implementation).getSurface();

        final PointImmutable position = Surface.getPosition();
        final int offsetX = position.getX();
        final int offsetY = position.getY();
        return new Point(absX - offsetX,
                         absY - offsetY);
    }

    public Optional<WlSurfaceResource> findSurfaceAtCoordinate(final int absX,
                                                               final int absY) {
        final Iterator<WlSurfaceResource> SurfaceIterator = getSurfacesStack().descendingIterator();

        while (SurfaceIterator.hasNext()) {
            final WlSurfaceResource surfaceResource = SurfaceIterator.next();
            final WlSurfaceRequests implementation = surfaceResource.getImplementation();
            final Surface Surface = ((WlSurface) implementation).getSurface();

            final Optional<Region> inputRegion = Surface.getInputRegion();
            if (inputRegion.isPresent()) {

                final PointImmutable position = Surface.getPosition();
                final int offsetX = position.getX();
                final int offsetY = position.getY();

                for (final RectangleImmutable rectangle : inputRegion.get()
                                                                     .asList()) {
                    final int x1 = rectangle.getX() + offsetX;
                    final int y1 = rectangle.getY() + offsetY;

                    final int x2 = x1 + rectangle.getWidth();
                    final int y2 = y1 + rectangle.getHeight();

                    if (x1 <= absX && x1 <= x2 && y1 <= absY && absY <= y2) {
                        return Optional.of(surfaceResource);
                    }
                }
            }
        }

        return Optional.empty();
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
