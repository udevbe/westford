/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.Signal;
import org.westford.Slot;
import org.westford.compositor.protocol.WlSurface;

import javax.annotation.Nonnull;
import java.util.LinkedList;

@AutoFactory(allowSubclasses = true,
             className = "PrivateSubsurfaceFactory")
public class Subsurface implements Role {

    @Nonnull
    private final Scene             scene;
    @Nonnull
    private final WlSurfaceResource parentWlSurfaceResource;
    @Nonnull
    private final WlSurfaceResource wlSurfaceResource;

    private final Signal<Boolean, Slot<Boolean>> effectiveSyncSignal = new Signal<>();
    private       boolean                        effectiveSync       = true;

    private boolean inert    = false;
    private boolean sync     = true;
    @Nonnull
    private Point   position = Point.ZERO;
    @Nonnull
    private SurfaceState surfaceState;
    @Nonnull
    private SurfaceState cachedSurfaceState;

    Subsurface(@Nonnull @Provided final Scene scene,
               @Nonnull final WlSurfaceResource parentWlSurfaceResource,
               @Nonnull final WlSurfaceResource wlSurfaceResource,
               @Nonnull final SurfaceState surfaceState) {
        this.scene = scene;
        this.parentWlSurfaceResource = parentWlSurfaceResource;
        this.wlSurfaceResource = wlSurfaceResource;
        this.surfaceState = surfaceState;
        this.cachedSurfaceState = surfaceState;
    }

    @Override
    public void beforeCommit(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        if (isInert()) {
            return;
        }

        if (isEffectiveSync()) {
            final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
            final Surface   surface   = wlSurface.getSurface();

            //set back cached state so surface can do eg. buffer release
            surface.setState(getCachedSurfaceState());
        }
    }

    public boolean isInert() {
        return this.inert;
    }

    public boolean isEffectiveSync() {
        return this.effectiveSync;
    }

    @Nonnull
    public SurfaceState getCachedSurfaceState() {
        return this.cachedSurfaceState;
    }

    public void setInert(final boolean inert) {
        this.inert = inert;
    }

    public void onParentApply() {
        if (isInert()) {
            return;
        }

        final SurfaceState cachedSurfaceState = getCachedSurfaceState();
        if (isEffectiveSync() &&
            !getSurfaceState().equals(cachedSurfaceState)) {
            //sync mode. update old state with cached state
            this.surfaceState = cachedSurfaceState;
            apply(cachedSurfaceState);
        }

        applyPosition();
    }

    @Nonnull
    public SurfaceState getSurfaceState() {
        return this.surfaceState;
    }

    public void apply(final SurfaceState surfaceState) {
        if (isInert()) {
            return;
        }

        if (isEffectiveSync()) {
            final WlSurface    wlSurface       = (WlSurface) getWlSurfaceResource().getImplementation();
            final Surface      surface         = wlSurface.getSurface();
            final SurfaceState oldSurfaceState = getSurfaceState();
            if (!surface.getState()
                        .equals(oldSurfaceState)) {
                //replace new state with old state
                surface.apply(oldSurfaceState);
                this.cachedSurfaceState = surfaceState;
            }
        }
        else {
            //desync mode, our 'old' state is always the newest state.
            this.cachedSurfaceState = surfaceState;
            this.surfaceState = surfaceState;
        }
    }

    public void applyPosition() {
        if (isInert()) {
            return;
        }

        final WlSurface parentWlSurface = (WlSurface) getParentWlSurfaceResource().getImplementation();
        final WlSurface wlSurface       = (WlSurface) getWlSurfaceResource().getImplementation();

        final Surface parentSurface = parentWlSurface.getSurface();
        final Point   global        = parentSurface.global(getPosition());

        wlSurface.getSurface()
                 .setPosition(global);
    }

    @Nonnull
    public WlSurfaceResource getWlSurfaceResource() {
        return this.wlSurfaceResource;
    }

    @Nonnull
    public WlSurfaceResource getParentWlSurfaceResource() {
        return this.parentWlSurfaceResource;
    }

    @Nonnull
    public Point getPosition() {
        return this.position;
    }

    public void setPosition(@Nonnull final Point position) {
        if (isInert()) {
            return;
        }

        this.position = position;
    }

    public void setSync(final boolean sync) {
        if (isInert()) {
            return;
        }

        this.sync = sync;

        final WlSurface parentWlSurface = (WlSurface) getParentWlSurfaceResource().getImplementation();
        parentWlSurface.getSurface()
                       .getRole()
                       .ifPresent(role -> {
                           if (role instanceof Subsurface) {
                               //TODO unit test this
                               final Subsurface parentSubsurface = (Subsurface) role;
                               updateEffectiveSync(parentSubsurface.isEffectiveSync());
                           }
                           else {
                               updateEffectiveSync(false);
                           }
                       });
    }

    public void updateEffectiveSync(final boolean parentEffectiveSync) {
        final boolean oldEffectiveSync = this.effectiveSync;
        this.effectiveSync = this.sync || parentEffectiveSync;

        if (oldEffectiveSync != isEffectiveSync()) {
            /*
             * If we were in sync mode and now our effective mode is desync, we have to apply our cached state
             * immediately
             */
            //TODO unit test this
            if (!isEffectiveSync()) {
                final WlSurface wlSurface = (WlSurface) getWlSurfaceResource().getImplementation();
                wlSurface.getSurface()
                         .apply(getCachedSurfaceState());
            }

            getEffectiveSyncSignal().emit(isEffectiveSync());
        }
    }

    public Signal<Boolean, Slot<Boolean>> getEffectiveSyncSignal() {
        return this.effectiveSyncSignal;
    }

    public void above(@Nonnull final WlSurfaceResource sibling) {
        if (isInert()) {
            return;
        }

        placement(false,
                  sibling);
    }

    private void placement(final boolean below,
                           final WlSurfaceResource sibling) {
        final LinkedList<WlSurfaceResource> pendingSubsurfaceStack = this.scene.getPendingSubsurfaceStack(getParentWlSurfaceResource());
        final int                           siblingPosition        = pendingSubsurfaceStack.indexOf(sibling);

        pendingSubsurfaceStack.remove(getWlSurfaceResource());
        pendingSubsurfaceStack.add(below ? siblingPosition : siblingPosition + 1,
                                   getWlSurfaceResource());

        //Note: committing the subsurface stack happens in WlCompositor.
    }

    public void below(@Nonnull final WlSurfaceResource sibling) {
        if (isInert()) {
            return;
        }

        placement(true,
                  sibling);
    }
}
