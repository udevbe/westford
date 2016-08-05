/*
 * Westmalle Wayland Compositor.
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
package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.WlCompositorRequestsV3;
import org.freedesktop.wayland.server.WlCompositorRequestsV4;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westmalle.wayland.core.Compositor;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.core.Scene;
import org.westmalle.wayland.core.Surface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Singleton
public class WlCompositor extends Global<WlCompositorResource> implements WlCompositorRequestsV4, ProtocolObject<WlCompositorResource> {

    private final Set<WlCompositorResource> resources = Collections.newSetFromMap(new WeakHashMap<>());

    @Nonnull
    private final WlSurfaceFactory                               wlSurfaceFactory;
    @Nonnull
    private final WlRegionFactory                                wlRegionFactory;
    @Nonnull
    private final org.westmalle.wayland.core.FiniteRegionFactory finiteRegionFactory;
    @Nonnull
    private final org.westmalle.wayland.core.SurfaceFactory      surfaceFactory;
    @Nonnull
    private final Compositor                                     compositor;
    @Nonnull
    private final Scene                                          scene;
    @Nonnull
    private final Renderer                                       renderer;

    @Inject
    WlCompositor(@Nonnull final Display display,
                 @Nonnull final WlSurfaceFactory wlSurfaceFactory,
                 @Nonnull final WlRegionFactory wlRegionFactory,
                 @Nonnull final org.westmalle.wayland.core.FiniteRegionFactory finiteRegionFactory,
                 @Nonnull final org.westmalle.wayland.core.SurfaceFactory surfaceFactory,
                 @Nonnull final Compositor compositor,
                 @Nonnull final Scene scene,
                 @Nonnull final Renderer renderer) {
        super(display,
              WlCompositorResource.class,
              VERSION);
        this.wlSurfaceFactory = wlSurfaceFactory;
        this.wlRegionFactory = wlRegionFactory;
        this.finiteRegionFactory = finiteRegionFactory;
        this.surfaceFactory = surfaceFactory;
        this.compositor = compositor;
        this.scene = scene;
        this.renderer = renderer;
    }

    @Override
    public WlCompositorResource onBindClient(final Client client,
                                             final int version,
                                             final int id) {
        return add(client,
                   version,
                   id);
    }

    @Override
    public void createSurface(final WlCompositorResource compositorResource,
                              final int id) {
        final Surface   surface   = this.surfaceFactory.create();
        final WlSurface wlSurface = this.wlSurfaceFactory.create(surface);

        final WlSurfaceResource wlSurfaceResource = wlSurface.add(compositorResource.getClient(),
                                                                  compositorResource.getVersion(),
                                                                  id);
        //TODO unit test destroy handler
        wlSurfaceResource.register(() -> {
            this.scene.getSurfacesStack()
                      .remove(wlSurfaceResource);
            this.scene.removeSubsurfaceStack(wlSurfaceResource);
            surface.markDestroyed();
            this.renderer.onDestroy(wlSurfaceResource);
            this.compositor.requestRender();
        });

        this.scene.getSurfacesStack()
                  .addLast(wlSurfaceResource);

        //TODO unit test commit handler
        surface.getApplySurfaceStateSignal()
               .connect(event -> this.scene.commitSubsurfaceStack(wlSurfaceResource));
    }

    @Override
    public void createRegion(final WlCompositorResource resource,
                             final int id) {
        this.wlRegionFactory.create(this.finiteRegionFactory.create())
                            .add(resource.getClient(),
                                 resource.getVersion(),
                                 id);
    }

    @Nonnull
    @Override
    public WlCompositorResource create(@Nonnull final Client client,
                                       @Nonnegative final int version,
                                       final int id) {
        return new WlCompositorResource(client,
                                        version,
                                        id,
                                        this);
    }

    @Nonnull
    @Override
    public Set<WlCompositorResource> getResources() {
        return this.resources;
    }
}
