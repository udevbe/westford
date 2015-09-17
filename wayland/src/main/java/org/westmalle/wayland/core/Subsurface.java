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

import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.LinkedList;
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
        if (isInert()) {
            return;
        }

        this.position = Point.create(x,
                                     y);
    }

    public void applyPosition() {
        if (isInert()) {
            return;
        }

        final WlSurface parentWlSurface = (WlSurface) getParentWlSurfaceResource().getImplementation();
        final WlSurface wlSurface       = (WlSurface) getWlSurfaceResource().getImplementation();

        wlSurface.getSurface()
                 .setPosition(parentWlSurface.getSurface()
                                             .global(this.position));
    }

    @Override
    public void beforeCommit(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        if (isInert()) {
            return;
        }

        if (useSync()) {
            final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final Surface surface = wlSurface.getSurface();

            //set back cached state so surface can do eg. buffer release
            surface.setState(this.cachedSurfaceState);
        }
    }

    public void commit() {
        if (isInert()) {
            return;
        }

        final WlSurface wlSurface = (WlSurface) getWlSurfaceResource().getImplementation();
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
        if (isInert()) {
            return;
        }

        if (useSync()) {
            final WlSurface wlSurface = (WlSurface) getWlSurfaceResource().getImplementation();
            final Surface surface = wlSurface.getSurface();

            //sync mode. update old state with cached state
            this.surfaceState = this.cachedSurfaceState;
            surface.getCommitSignal()
                   .emit(this.surfaceState);
        }

        applyPosition();
    }

    public void setSync() {
        if (isInert()) {
            return;
        }

        this.sync = true;
    }

    public void setDesync() {
        if (isInert()) {
            return;
        }

        this.sync = false;
    }

    private boolean useSync() {
        if (this.sync) {
            return true;
        }
        else {
            /*
             * We must use sync mode if at least one parent up in the hierarchy is in sync mode,
             * even if we don't use sync mode.
             */
            final WlSurface parentWlSurface = (WlSurface) getParentWlSurfaceResource().getImplementation();
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

    public void above(@Nonnull final WlSurfaceResource sibling) {
        if (isInert()) {
            return;
        }

        placement(false,
                  sibling);
    }

    public void below(@Nonnull final WlSurfaceResource sibling) {
        if (isInert()) {
            return;
        }

        placement(true,
                  sibling);
    }

    private void placement(final boolean below,
                           final WlSurfaceResource sibling) {
        final WlSurface wlSurface = (WlSurface) getWlSurfaceResource().getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final WlCompositorResource wlCompositorResource = surface.getWlCompositorResource();
        final WlCompositor         wlCompositor         = (WlCompositor) wlCompositorResource.getImplementation();
        final Compositor           compositor           = wlCompositor.getCompositor();

        final LinkedList<WlSurfaceResource> subsurfaceStack = compositor.getPendingSubsurfaceStack(getParentWlSurfaceResource());
        final int                           siblingPosition = subsurfaceStack.indexOf(sibling);

        subsurfaceStack.remove(getWlSurfaceResource());
        subsurfaceStack.add(below ? siblingPosition : siblingPosition + 1,
                            getWlSurfaceResource());
    }

    @Nonnull
    public WlSurfaceResource getWlSurfaceResource() {
        return this.wlSurfaceResource;
    }

    @Nonnull
    public WlSurfaceResource getParentWlSurfaceResource() {
        return this.parentWlSurfaceResource;
    }

    public boolean isInert() {
        /*
         * Docs say a subsurface must become inert if it's parent is destroyed.
         */
        final WlSurface parentWlSurface = (WlSurface) getParentWlSurfaceResource().getImplementation();
        return parentWlSurface.getSurface()
                              .isDestroyed();
    }
}
