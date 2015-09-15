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
import java.util.Optional;

public class Subsurface implements Role {

    @Nonnull
    private final WlSurfaceResource parentWlSurfaceResource;
    @Nonnull
    private final WlSurfaceResource wlSurfaceResource;

    private boolean sync     = true;
    @Nonnull
    private Point   position = Point.ZERO;
    @Nonnull
    private SurfaceState surfaceState;
    @Nonnull
    private SurfaceState cachedSurfaceState;

    Subsurface(@Nonnull final WlSurfaceResource parentWlSurfaceResource,
               @Nonnull final WlSurfaceResource wlSurfaceResource,
               @Nonnull final SurfaceState surfaceState) {
        this.parentWlSurfaceResource = parentWlSurfaceResource;
        this.wlSurfaceResource = wlSurfaceResource;
        this.surfaceState = surfaceState;
        this.cachedSurfaceState = surfaceState;
    }

    public void setPosition(final int x,
                            final int y) {
        this.position = Point.create(x,
                                     y);
    }

    public void applyPosition() {
        final WlSurface parentWlSurface = (WlSurface) this.parentWlSurfaceResource.getImplementation();
        final WlSurface wlSurface       = (WlSurface) this.wlSurfaceResource.getImplementation();

        wlSurface.getSurface()
                 .setPosition(parentWlSurface.getSurface()
                                             .global(this.position));
    }

    @Override
    public void beforeCommit(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        if (useSync()) {
            final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final Surface surface = wlSurface.getSurface();

            //set back cached state so surface can do eg. buffer release
            surface.setState(this.cachedSurfaceState);
        }
    }

    public void commit() {
        final WlSurface wlSurface = (WlSurface) this.wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        //update cached state with new state
        this.cachedSurfaceState = surface.getState();

        if (useSync()) {
            //replace new state with old state
            surface.setState(this.surfaceState);
            surface.updateTransform();
            surface.updateSize();
        }
        else {
            //desync mode, our 'old' state is always the newest state.
            this.surfaceState = this.cachedSurfaceState;
        }
    }

    public void parentCommit() {
        if (useSync()) {
            final WlSurface wlSurface = (WlSurface) this.wlSurfaceResource.getImplementation();
            final Surface surface = wlSurface.getSurface();

            //sync mode. update old state with cached state
            this.surfaceState = this.cachedSurfaceState;
            surface.getCommitSignal()
                   .emit(this.surfaceState);
        }

        applyPosition();
    }

    public void setSync() {
        this.sync = true;
    }

    public void setDesync() {
        this.sync = false;
    }

    /*
     * We must use sync mode if at least one parent up in the hierarchy is in sync mode,
     * even if we don't use sync mode.
     */
    private boolean useSync() {

        if (this.sync) {
            return true;
        }
        else {

            final WlSurface parentWlSurface = (WlSurface) this.parentWlSurfaceResource.getImplementation();
            final Surface parentSurface = parentWlSurface.getSurface();

            boolean parentSync = false;

            final Optional<Role> optionalParentRole = parentSurface.getRole();
            if (optionalParentRole.isPresent()) {
                final Role parentRole = optionalParentRole.get();
                if (parentRole instanceof Subsurface) {
                    final Subsurface parentSubsurface = (Subsurface) parentRole;
                    parentSync = parentSubsurface.useSync();
                }
            }

            return parentSync;
        }
    }
}
