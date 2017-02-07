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
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlCallbackResource;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.Signal;
import org.westford.Slot;
import org.westford.compositor.core.calc.Mat4;
import org.westford.compositor.core.events.KeyboardFocusGained;
import org.westford.compositor.core.events.KeyboardFocusLost;
import org.westford.compositor.protocol.WlRegion;
import org.westford.compositor.protocol.WlSurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@AutoFactory(className = "SurfaceFactory",
             allowSubclasses = true)
public class Surface {

    /*
     * Overall state
     */
    @Nonnull
    private final Set<SurfaceView> surfaceViews = new HashSet<>();
    @Nonnull
    private       Optional<Role>   surfaceRole  = Optional.empty();

    /*
     * Signals
     */
    @Nonnull
    private final Signal<KeyboardFocusLost, Slot<KeyboardFocusLost>>     keyboardFocusLostSignal   = new Signal<>();
    @Nonnull
    private final Signal<KeyboardFocusGained, Slot<KeyboardFocusGained>> keyboardFocusGainedSignal = new Signal<>();
    @Nonnull
    private final Signal<SurfaceState, Slot<SurfaceState>>               applySurfaceStateSignal   = new Signal<>();
    @Nonnull
    private final Signal<SurfaceView, Slot<SurfaceView>>                 viewCreatedSignal         = new Signal<>();

    /*
     * Business dependencies
     */
    @Nonnull
    private final FiniteRegionFactory finiteRegionFactory;
    @Nonnull
    private final Compositor          compositor;
    @Nonnull
    private final Renderer            renderer;
    @Nonnull
    private final SurfaceViewFactory  surfaceViewFactory;
    @Nonnull
    private final List<WlCallbackResource> callbacks       = new LinkedList<>();
    @Nonnull
    private final Set<WlKeyboardResource>  keyboardFocuses = new HashSet<>();

    /*
     * pending state
     */
    @Nonnull
    private final SurfaceState.Builder      pendingState                 = SurfaceState.builder();
    @Nonnull
    private       Optional<DestroyListener> pendingBufferDestroyListener = Optional.empty();
    @Nonnull
    private final LinkedList<Subsurface>    pendingSubsurfaces           = new LinkedList<>();

    /*
     * committed state
     */
    @Nonnull
    private       SurfaceState        state    = SurfaceState.builder()
                                                             .build();
    @Nonnull
    private final LinkedList<Sibling> siblings = new LinkedList<>();


    /*
     * committed derived states
     */
    private boolean destroyed;
    @Nonnull
    private Mat4      transform        = Transforms.NORMAL;
    @Nonnull
    private Mat4      inverseTransform = Transforms.NORMAL;
    @Nonnull
    private Rectangle size             = Rectangle.ZERO;

    /*
     * render state
     */
    private Optional<SurfaceRenderState> renderState = Optional.empty();

    Surface(@Nonnull @Provided final FiniteRegionFactory finiteRegionFactory,
            @Nonnull @Provided final Compositor compositor,
            @Nonnull @Provided final Renderer renderer,
            @Nonnull @Provided final SurfaceViewFactory surfaceViewFactory) {
        this.finiteRegionFactory = finiteRegionFactory;
        this.compositor = compositor;
        this.renderer = renderer;
        this.surfaceViewFactory = surfaceViewFactory;
    }

    @Nonnull
    public Signal<KeyboardFocusLost, Slot<KeyboardFocusLost>> getKeyboardFocusLostSignal() {
        return this.keyboardFocusLostSignal;
    }

    @Nonnull
    public Signal<KeyboardFocusGained, Slot<KeyboardFocusGained>> getKeyboardFocusGainedSignal() {
        return this.keyboardFocusGainedSignal;
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    @Nonnull
    public Surface markDestroyed() {
        this.destroyed = true;
        return this;
    }

    @Nonnull
    public Surface markDamaged(@Nonnull final Rectangle damage) {

        final Region damageRegion = this.pendingState.build()
                                                     .getDamage()
                                                     .orElseGet(this.finiteRegionFactory::create);
        damageRegion.add(damage);
        this.pendingState.damage(Optional.of(damageRegion));
        return this;
    }

    @Nonnull
    public Surface attachBuffer(@Nonnull final WlBufferResource wlBufferResource,
                                final int dx,
                                final int dy) {
        getPendingState().build()
                         .getBuffer()
                         .ifPresent(previousWlBufferResource -> previousWlBufferResource.unregister(this.pendingBufferDestroyListener.get()));
        final DestroyListener detachBuffer = this::detachBuffer;
        wlBufferResource.register(detachBuffer);
        this.pendingBufferDestroyListener = Optional.of(detachBuffer);
        getPendingState().buffer(Optional.of(wlBufferResource))
                         .deltaPosition(Point.create(dx,
                                                     dy));
        return this;
    }

    @Nonnull
    public SurfaceState.Builder getPendingState() {
        return this.pendingState;
    }

    @Nonnull
    public SurfaceState getState() {
        return this.state;
    }

    public void setState(@Nonnull final SurfaceState state) {
        this.state = state;
    }

    @Nonnull
    public Optional<Role> getRole() {
        return this.surfaceRole;
    }

    public void setRole(@Nonnull final Role role) {
        this.surfaceRole = Optional.of(role);
    }

    @Nonnull
    public Surface commit() {
        final Optional<WlBufferResource> buffer = getState().getBuffer();
        //signal client that the previous buffer can be reused as we will now use the
        //newly attached buffer.
        buffer.ifPresent(WlBufferResource::release);

        //flush states
        apply(this.pendingState.build());

        //reset pending buffer state
        detachBuffer();
        return this;
    }

    public void apply(final SurfaceState surfaceState) {
        setState(surfaceState);
        updateTransform();
        updateSize();

        //copy subsurface stack to siblings list. subsurfaces always go first in the sibling list.
        this.pendingSubsurfaces.forEach(subsurface -> this.siblings.remove(subsurface.getSibling()));
        this.pendingSubsurfaces.descendingIterator()
                               .forEachRemaining(subsurface -> this.siblings.addFirst(subsurface.getSibling()));

        this.compositor.requestRender();

        getApplySurfaceStateSignal().emit(getState());
    }

    @Nonnull
    public Surface detachBuffer() {
        getPendingState().build()
                         .getBuffer()
                         .ifPresent(wlBufferResource -> wlBufferResource.unregister(this.pendingBufferDestroyListener.get()));
        this.pendingBufferDestroyListener = Optional.empty();
        getPendingState().buffer(Optional.empty())
                         .damage(Optional.empty());
        return this;
    }

    @Nonnull
    public Surface updateTransform() {
        final SurfaceState state = getState();
        this.transform = Transforms.SCALE(state.getScale())
                                   .multiply(state.getBufferTransform());
        this.inverseTransform = getTransform().invert();
        return this;
    }

    public void updateSize() {
        final SurfaceState               state                    = getState();
        final Optional<WlBufferResource> wlBufferResourceOptional = state.getBuffer();
        final int                        scale                    = state.getScale();

        this.size = Rectangle.ZERO;

        wlBufferResourceOptional.ifPresent(wlBufferResource -> {
            final Buffer buffer = this.renderer.queryBuffer(wlBufferResource);
            final int    width  = buffer.getWidth() / scale;
            final int    height = buffer.getHeight() / scale;

            this.size = Rectangle.builder()
                                 .width(width)
                                 .height(height)
                                 .build();
        });
    }

    @Nonnull
    public Signal<SurfaceState, Slot<SurfaceState>> getApplySurfaceStateSignal() {
        return this.applySurfaceStateSignal;
    }

    @Nonnull
    public Mat4 getTransform() {
        return this.transform;
    }

    @Nonnull
    public Surface addCallback(@Nonnull final WlCallbackResource callback) {
        this.callbacks.add(callback);
        return this;
    }

    @Nonnull
    public Surface removeOpaqueRegion() {
        this.pendingState.opaqueRegion(Optional.empty());
        return this;
    }

    @Nonnull
    public Surface setOpaqueRegion(@Nonnull final WlRegionResource wlRegionResource) {
        final WlRegion wlRegion = (WlRegion) wlRegionResource.getImplementation();
        final Region   region   = wlRegion.getRegion();
        this.pendingState.opaqueRegion(Optional.of(region));
        return this;
    }

    @Nonnull
    public Surface removeInputRegion() {
        this.pendingState.inputRegion(Optional.empty());
        return this;
    }

    @Nonnull
    public Surface setInputRegion(@Nonnull final WlRegionResource wlRegionResource) {
        final WlRegion wlRegion = (WlRegion) wlRegionResource.getImplementation();
        final Region   region   = wlRegion.getRegion();
        getPendingState().inputRegion(Optional.of(region));
        return this;
    }

    @Nonnull
    public Surface firePaintCallbacks(final int serial) {
        final List<WlCallbackResource> callbacks = new ArrayList<>(getFrameCallbacks());
        getFrameCallbacks().clear();
        callbacks.forEach(frameCallback -> {
            frameCallback.done(serial);
            frameCallback.destroy();
        });
        return this;
    }

    @Nonnull
    public List<WlCallbackResource> getFrameCallbacks() {
        return this.callbacks;
    }

    @Nonnull
    public Mat4 getInverseTransform() {
        return this.inverseTransform;
    }

    @Nonnull
    public Rectangle getSize() {
        return this.size;
    }

    public Surface setScale(@Nonnegative final int scale) {
        getPendingState().scale(scale);
        return this;
    }

    @Nonnull
    public Surface setBufferTransform(@Nonnull final Mat4 bufferTransform) {
        getPendingState().bufferTransform(bufferTransform);
        return this;
    }

    /**
     * The keyboards that will be used to notify the client of any keyboard events on this surface. This collection is
     * updated each time the keyboard focus changes for this surface. To keep the client from receiving keyboard events,
     * clear this list each time the focus is set for this surface. To listen for focus updates, register a keyboard focus
     * listener on this surface.
     *
     * @return a set of keyboard resources.
     */
    @Nonnull
    public Set<WlKeyboardResource> getKeyboardFocuses() {
        return this.keyboardFocuses;
    }

    @Nonnull
    public Optional<SurfaceRenderState> getRenderState() {
        return this.renderState;
    }

    public void setRenderState(@Nonnull final SurfaceRenderState renderState) {
        this.renderState = Optional.of(renderState);
    }

    @Nonnull
    public Collection<SurfaceView> getViews() {
        return Collections.unmodifiableCollection(this.surfaceViews);
    }

    public SurfaceView createView(WlSurfaceResource wlSurfaceResource,
                                  Point position) {

        final SurfaceView surfaceView = this.surfaceViewFactory.create(wlSurfaceResource,
                                                                       position);
        if (this.surfaceViews.add(surfaceView)) {
            getSiblings().forEach(sibling -> ensureSiblingView(sibling,
                                                               surfaceView));
            getPendingSubsurfaces().forEach(subsurface -> ensureSiblingView(subsurface.getSibling(),
                                                                            surfaceView));
            this.viewCreatedSignal.emit(surfaceView);
        }
        return surfaceView;
    }

    @Nonnull
    public Signal<SurfaceView, Slot<SurfaceView>> getViewCreatedSignal() {
        return this.viewCreatedSignal;
    }

    private void ensureSiblingView(@Nonnull Sibling sibling,
                                   @Nonnull final SurfaceView surfaceView) {

        final Point             siblingPosition          = sibling.getPosition();
        final WlSurfaceResource siblingWlSurfaceResource = sibling.getWlSurfaceResource();
        final WlSurface         siblingWlSurface         = (WlSurface) siblingWlSurfaceResource.getImplementation();
        final Surface           siblingSurface           = siblingWlSurface.getSurface();

        if (siblingSurface.equals(this)) {
            return;
        }

        for (SurfaceView siblingSurfaceView : siblingSurface.getViews()) {
            final Optional<SurfaceView> siblingSurfaceViewParent = siblingSurfaceView.getParent();
            if (siblingSurfaceViewParent.isPresent() && siblingSurfaceViewParent.get()
                                                                                .equals(surfaceView)) {
                //sibling already has a view with this surface as it's parent view. Do nothing.
                //TODO Perhaps we should we allow this?
                return;
            }
        }

        final SurfaceView siblingSurfaceView = siblingSurface.createView(siblingWlSurfaceResource,
                                                                         surfaceView.global(siblingPosition));
        siblingSurfaceView.setParent(surfaceView);
        surfaceView.getPositionSignal()
                   .connect(event -> siblingSurfaceView.setPosition(surfaceView.global(sibling.getPosition())));
    }

    public void addSibling(@Nonnull Sibling sibling) {
        getViews().forEach(surfaceView -> ensureSiblingView(sibling,
                                                            surfaceView));
        this.siblings.add(sibling);
        sibling.getWlSurfaceResource()
               .register(() -> removeSibling(sibling));
    }

    public void removeSibling(@Nonnull final Sibling sibling) {
        if (this.siblings.remove(sibling)) {

            final WlSurfaceResource siblingWlSurfaceResource = sibling.getWlSurfaceResource();
            final WlSurface         siblingWlSurface         = (WlSurface) siblingWlSurfaceResource.getImplementation();

            siblingWlSurface.getSurface()
                            .getViews()
                            .forEach(SurfaceView::removeParent);
        }
    }

    @Nonnull
    public LinkedList<Sibling> getSiblings() {
        return this.siblings;
    }

    public void addSubsurface(@Nonnull final Subsurface subsurface) {
        final Sibling subsurfaceSibling = subsurface.getSibling();
        getViews().forEach(surfaceView -> ensureSiblingView(subsurfaceSibling,
                                                            surfaceView));
        subsurfaceSibling.getWlSurfaceResource()
                         .register(() -> removeSubsurface(subsurface));

        this.pendingSubsurfaces.add(subsurface);
    }

    public void removeSubsurface(@Nonnull final Subsurface subsurface) {
        if (this.pendingSubsurfaces.remove(subsurface)) {
            removeSibling(subsurface.getSibling());
        }
    }

    @Nonnull
    public LinkedList<Subsurface> getPendingSubsurfaces() {
        return this.pendingSubsurfaces;
    }
}