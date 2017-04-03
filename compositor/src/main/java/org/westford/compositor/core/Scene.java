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

import org.freedesktop.wayland.server.WlSurfaceRequests;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.compositor.protocol.WlSurface;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

@Singleton
public class Scene {

    @Nonnull
    private final LinkedList<WlSurfaceResource> surfacesStack = new LinkedList<>();

    @Nonnull
    private final SceneLayer backgroundLayer;
    @Nonnull
    private final SceneLayer underLayer;
    @Nonnull
    private final SceneLayer applicationLayer;
    @Nonnull
    private final SceneLayer overLayer;
    @Nonnull
    private final SceneLayer fullscreenLayer;
    @Nonnull
    private final SceneLayer lockLayer;
    @Nonnull
    private final SceneLayer cursorLayer;

    @Nonnull
    private final InfiniteRegion infiniteRegion;

    @Inject
    Scene(@Nonnull final SceneLayer backgroundLayer,
          @Nonnull final SceneLayer underLayer,
          @Nonnull final SceneLayer applicationLayer,
          @Nonnull final SceneLayer overLayer,
          @Nonnull final SceneLayer fullscreenLayer,
          @Nonnull final SceneLayer lockLayer,
          @Nonnull final SceneLayer cursor,
          @Nonnull final InfiniteRegion infiniteRegion) {
        this.backgroundLayer = backgroundLayer;
        this.underLayer = underLayer;
        this.applicationLayer = applicationLayer;
        this.overLayer = overLayer;
        this.fullscreenLayer = fullscreenLayer;
        this.lockLayer = lockLayer;
        this.cursorLayer = cursor;
        this.infiniteRegion = infiniteRegion;
    }

    @Nonnull
    public Optional<SurfaceView> pickSurfaceView(final Point global) {

        final Iterator<SurfaceView> surfaceViewIterator = pickableSurfaces().descendingIterator();
        Optional<SurfaceView>       pointerOver         = Optional.empty();

        while (surfaceViewIterator.hasNext()) {
            final SurfaceView surfaceView = surfaceViewIterator.next();

            if (!surfaceView.isDrawable() || !surfaceView.isEnabled()) {
                continue;
            }

            final WlSurfaceResource surfaceResource = surfaceView.getWlSurfaceResource();
            final WlSurfaceRequests implementation  = surfaceResource.getImplementation();
            final Surface           surface         = ((WlSurface) implementation).getSurface();

            final Optional<Region> inputRegion = surface.getState()
                                                        .getInputRegion();
            final Region region = inputRegion.orElse(this.infiniteRegion);

            final Rectangle size = surface.getSize();

            final Point local = surfaceView.local(global);
            if (region.contains(size,
                                local)) {
                pointerOver = Optional.of(surfaceView);
                break;
            }
        }

        return pointerOver;
    }

    private LinkedList<SurfaceView> pickableSurfaces() {

        final LinkedList<SurfaceView> views = new LinkedList<>();

        if (!this.lockLayer.getSurfaceViews()
                           .isEmpty()) {
            //lockLayer screen
            views.addAll(this.lockLayer.getSurfaceViews());
        }
        else {
            views.addAll(this.fullscreenLayer.getSurfaceViews());
            views.addAll(this.backgroundLayer.getSurfaceViews());
            views.addAll(this.underLayer.getSurfaceViews());
            views.addAll(this.applicationLayer.getSurfaceViews());
            views.addAll(this.overLayer.getSurfaceViews());
        }

        //make sure we include any sub-views
        final LinkedList<SurfaceView> pickableViews = new LinkedList<>();
        views.forEach(surfaceView -> pickableViews.addAll(withSiblingViews(surfaceView)));

        return pickableViews;
    }

    public LinkedList<SurfaceView> drawableSurfaces() {

        final LinkedList<SurfaceView> drawableSurfaceViewStack = pickableSurfaces();
        //add cursor surfaces
        this.cursorLayer.getSurfaceViews()
                        .forEach(cursorSurfaceView -> drawableSurfaceViewStack.addAll(withSiblingViews(cursorSurfaceView)));

        return drawableSurfaceViewStack;
    }

    public LinkedList<SurfaceView> withSiblingViews(final SurfaceView surfaceView) {
        final LinkedList<SurfaceView> surfaceViews = new LinkedList<>();
        addSiblingViews(surfaceView,
                        surfaceViews);
        return surfaceViews;
    }

    /**
     * Gather all parent surface views, including the parent surface view and insert it with a correct order into the provided list.
     *
     * @param parentSurfaceView
     * @param surfaceViews
     */
    private void addSiblingViews(final SurfaceView parentSurfaceView,
                                 final LinkedList<SurfaceView> surfaceViews) {

        final WlSurfaceResource parentWlSurfaceResource = parentSurfaceView.getWlSurfaceResource();
        final WlSurface         parentWlSurface         = (WlSurface) parentWlSurfaceResource.getImplementation();
        final Surface           parentSurface           = parentWlSurface.getSurface();

        parentSurface.getSiblings()
                     .forEach(sibling -> {

                         final WlSurface siblingWlSurface = (WlSurface) sibling.getWlSurfaceResource()
                                                                               .getImplementation();
                         final Surface siblingSurface = siblingWlSurface.getSurface();

                         //only consider surface if it has a role.
                         //TODO we could move the views to the generic role itf.
                         if (siblingSurface.getRole()
                                           .isPresent()) {

                             siblingSurface.getViews()
                                           .forEach(siblingSurfaceView -> {

                                               if (siblingSurfaceView.getParent()
                                                                     .filter(siblingParentSurfaceView ->
                                                                                     siblingParentSurfaceView.equals(parentSurfaceView))
                                                                     .isPresent()) {
                                                   addSiblingViews(siblingSurfaceView,
                                                                   surfaceViews);
                                               }
                                               else if (siblingSurfaceView.equals(parentSurfaceView)) {
                                                   surfaceViews.addFirst(siblingSurfaceView);
                                               }
                                           });
                         }
                     });
    }

    @Nonnull
    public SceneLayer getBackgroundLayer() {
        return this.backgroundLayer;
    }

    @Nonnull
    public SceneLayer getUnderLayer() {
        return this.underLayer;
    }

    @Nonnull
    public SceneLayer getApplicationLayer() {
        return this.applicationLayer;
    }

    @Nonnull
    public SceneLayer getOverLayer() {
        return this.overLayer;
    }

    @Nonnull
    public SceneLayer getFullscreenLayer() {
        return this.fullscreenLayer;
    }

    @Nonnull
    public SceneLayer getLockLayer() {
        return this.lockLayer;
    }

    @Nonnull
    public SceneLayer getCursorLayer() {
        return this.cursorLayer;
    }

    public void removeView(@Nonnull final SurfaceView surfaceView) {
        this.backgroundLayer.getSurfaceViews()
                            .remove(surfaceView);
        this.underLayer.getSurfaceViews()
                       .remove(surfaceView);
        this.applicationLayer.getSurfaceViews()
                             .remove(surfaceView);
        this.overLayer.getSurfaceViews()
                      .remove(surfaceView);
        this.fullscreenLayer.getSurfaceViews()
                            .remove(surfaceView);
        this.lockLayer.getSurfaceViews()
                      .remove(surfaceView);
    }

    public void removeAllViews(@Nonnull final WlSurfaceResource wlSurfaceResource) {
        final WlSurface wlSurface = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface   surface   = wlSurface.getSurface();

        final Collection<SurfaceView> views = surface.getViews();
        views.forEach(this::removeView);
    }
}
