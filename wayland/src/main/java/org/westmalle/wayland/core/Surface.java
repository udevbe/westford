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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlCallbackResource;
import org.freedesktop.wayland.server.WlCompositorResource;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlRegionResource;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.core.calc.Vec4;
import org.westmalle.wayland.core.events.KeyboardFocusGained;
import org.westmalle.wayland.core.events.KeyboardFocusLost;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlRegion;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@AutoFactory(className = "SurfaceFactory")
public class Surface {

    /*
     * Signals
     */
    @Nonnull
    private final Signal<KeyboardFocusLost, Slot<KeyboardFocusLost>>     keyboardFocusLostSignal   = new Signal<>();
    @Nonnull
    private final Signal<KeyboardFocusGained, Slot<KeyboardFocusGained>> keyboardFocusGainedSignal = new Signal<>();
    @Nonnull
    private final Signal<Point, Slot<Point>>                             positionSignal            = new Signal<>();
    @Nonnull
    private final Signal<SurfaceState, Slot<SurfaceState>>               commitSignal              = new Signal<>();

    @Nonnull
    private final FiniteRegionFactory  finiteRegionFactory;
    @Nonnull
    private final WlCompositorResource wlCompositorResource;
    @Nonnull
    private final List<WlCallbackResource>  callbacks                    = new LinkedList<>();
    @Nonnull
    private final Set<WlKeyboardResource>   keyboardFocuses              = new HashSet<>();
    @Nonnull
    private       Optional<Role>            surfaceRole                  = Optional.empty();
    @Nonnull
    private       Optional<DestroyListener> pendingBufferDestroyListener = Optional.empty();

    /*
     * pending state
     */
    @Nonnull
    private SurfaceState pendingState = SurfaceState.builder()
                                                    .build();
    /*
     * committed state
     */
    @Nonnull
    private SurfaceState state        = SurfaceState.builder()
                                                    .build();
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

    Surface(@Nonnull @Provided final FiniteRegionFactory finiteRegionFactory,
            @Nonnull final WlCompositorResource wlCompositorResource) {
        this.finiteRegionFactory = finiteRegionFactory;
        this.wlCompositorResource = wlCompositorResource;
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
        final Region newDamage = getPendingState().getDamage()
                                                  .orElseGet(this.finiteRegionFactory::create)
                                                  .add(damage);
        final SurfaceState pendingSurfaceState = getPendingState().toBuilder()
                                                                  .damage(Optional.of(newDamage))
                                                                  .build();
        setPendingState(pendingSurfaceState);
        return this;
    }

    @Nonnull
    public SurfaceState getPendingState() {
        return this.pendingState;
    }

    public void setPendingState(@Nonnull final SurfaceState pendingState) {
        this.pendingState = pendingState;
    }

    @Nonnull
    public Surface attachBuffer(@Nonnull final WlBufferResource buffer,
                                final int dx,
                                final int dy) {
        getPendingState().getBuffer()
                         .ifPresent(wlBufferResource -> wlBufferResource.unregister(this.pendingBufferDestroyListener.get()));
        this.pendingBufferDestroyListener = Optional.of(this::detachBuffer);
        buffer.register(this.pendingBufferDestroyListener.get());
        final SurfaceState pendingSurfaceState = getPendingState().toBuilder()
                                                                  .buffer(Optional.of(buffer))
                                                                  .positionTransform(Transforms.TRANSLATE(dx,
                                                                                                          dy)
                                                                                               .multiply(getState().getPositionTransform()))
                                                                  .build();
        setPendingState(pendingSurfaceState);
        return this;
    }

    @Nonnull
    public SurfaceState getState() {
        return this.state;
    }

    public void setState(@Nonnull final SurfaceState state) {
        this.state = state;
    }

    public Optional<Role> getRole() {
        return this.surfaceRole;
    }

    public void setRole(@Nonnull final Role role) {
        this.surfaceRole = Optional.of(role);
    }

    @Nonnull
    public Surface commit() {
        final Optional<WlBufferResource> buffer = getState().getBuffer();
        if (buffer.isPresent()) {
            //signal client that the previous buffer can be reused as we will now use the
            //newly attached buffer.
            buffer.get()
                  .release();
        }
        //check update transformation
        final boolean needsTransformUpdate = needsTransformUpdate();
        //flush states
        setState(getPendingState());
        if (needsTransformUpdate) {
            updateTransform();
        }
        updateSize();
        //reset pending buffer state
        detachBuffer();

        getCommitSignal().emit(getState());

        //FIXME should we automatically request a render here?
        final WlCompositor wlCompositor = (WlCompositor) this.wlCompositorResource.getImplementation();
        wlCompositor.getCompositor()
                    .requestRender();
        return this;
    }

    public boolean needsTransformUpdate() {
        final SurfaceState pendingState = getPendingState();
        final SurfaceState state        = getState();
        return pendingState.getScale() != state.getScale()
               || !pendingState.getBufferTransform()
                               .equals(state.getBufferTransform())
               || !pendingState.getPositionTransform()
                               .equals(state.getPositionTransform());
    }

    @Nonnull
    public Surface updateTransform() {
        final SurfaceState state = getState();

        //set scaling first
        Mat4 result = Transforms.SCALE(state.getScale());
        //apply positioning
        result = state.getPositionTransform()
                      .multiply(result);
        //client buffer transform;
        result = state.getBufferTransform()
                      .multiply(result);
        //homogenized
        result = Transforms.SCALE(1f / result.getM33())
                           .multiply(result);

        this.transform = result;
        this.inverseTransform = getTransform().invert();
        return this;
    }

    public void updateSize() {
        final SurfaceState               state  = getState();
        final Optional<WlBufferResource> buffer = state.getBuffer();
        final int                        scale  = state.getScale();
        if (buffer.isPresent()) {
            final WlBufferResource wlBufferResource = buffer.get();
            final ShmBuffer shmBuffer = ShmBuffer.get(wlBufferResource);
            if (shmBuffer == null) {
                throw new RuntimeException("Got a buffer that is not an shm buffer!");
            }
            final int width = shmBuffer.getWidth() / scale;
            final int height = shmBuffer.getHeight() / scale;
            this.size = Rectangle.builder()
                                 .width(width)
                                 .height(height)
                                 .build();
        }
        else {
            this.size = Rectangle.ZERO;
        }
    }

    @Nonnull
    public Surface detachBuffer() {
        getPendingState().getBuffer()
                         .ifPresent(wlBufferResource -> wlBufferResource.unregister(this.pendingBufferDestroyListener.get()));
        this.pendingBufferDestroyListener = Optional.empty();
        final SurfaceState pendingSurfaceState = getPendingState().toBuilder()
                                                                  .buffer(Optional.<WlBufferResource>empty())
                                                                  .damage(Optional.<Region>empty())
                                                                  .build();
        setPendingState(pendingSurfaceState);
        return this;
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
        final SurfaceState pendingSurfaceState = getPendingState().toBuilder()
                                                                  .opaqueRegion(Optional.<Region>empty())
                                                                  .build();
        setPendingState(pendingSurfaceState);
        return this;
    }

    @Nonnull
    public Surface setOpaqueRegion(@Nonnull final WlRegionResource wlRegionResource) {
        final WlRegion wlRegion = (WlRegion) wlRegionResource.getImplementation();
        final Region   region   = wlRegion.getRegion();
        final SurfaceState pendingSurfaceState = getPendingState().toBuilder()
                                                                  .opaqueRegion(Optional.of(region))
                                                                  .build();
        setPendingState(pendingSurfaceState);
        return this;
    }

    @Nonnull
    public Surface removeInputRegion() {
        final SurfaceState pendingSurfaceState = getPendingState().toBuilder()
                                                                  .inputRegion(Optional.<Region>empty())
                                                                  .build();
        setPendingState(pendingSurfaceState);
        return this;
    }

    @Nonnull
    public Surface setInputRegion(@Nonnull final WlRegionResource wlRegionResource) {
        final WlRegion wlRegion = (WlRegion) wlRegionResource.getImplementation();
        final Region   region   = wlRegion.getRegion();
        final SurfaceState pendingSurfaceState = getPendingState().toBuilder()
                                                                  .inputRegion(Optional.of(region))
                                                                  .build();
        setPendingState(pendingSurfaceState);
        return this;
    }

    @Nonnull
    public Surface setPosition(@Nonnull final Point global) {
        final SurfaceState state = getState();
        final int          scale = state.getScale();
        final SurfaceState currentState = state.toBuilder()
                                               .positionTransform(Transforms.TRANSLATE(global.getX() * scale,
                                                                                       global.getY() * scale))
                                               .build();
        setState(currentState);
        updateTransform();

        getPositionSignal().emit(global);

        //FIXME don't automatically request a render when we update the position(?)
        final WlCompositor wlCompositor = (WlCompositor) this.wlCompositorResource.getImplementation();
        wlCompositor.getCompositor()
                    .requestRender();

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
    public Point local(@Nonnull final Point global) {
        final Vec4 localPoint = getInverseTransform().multiply(global.toVec4());
        return Point.create((int) localPoint.getX(),
                            (int) localPoint.getY());
    }

    @Nonnull
    public Mat4 getInverseTransform() {
        return this.inverseTransform;
    }

    @Nonnull
    public Point global(@Nonnull final Point local) {
        final Vec4 localPoint = getTransform().multiply(local.toVec4());
        return Point.create(Math.round(localPoint.getX()),
                            Math.round(localPoint.getY()));
    }

    @Nonnull
    public Rectangle getSize() {
        return this.size;
    }

    public Surface setScale(@Nonnegative final int scale) {
        final SurfaceState pendingSurfaceState = getPendingState().toBuilder()
                                                                  .scale(scale)
                                                                  .build();
        setPendingState(pendingSurfaceState);
        return this;
    }

    @Nonnull
    public Surface setBufferTransform(@Nonnull final Mat4 bufferTransform) {
        final SurfaceState pendingSurfaceState = getPendingState().toBuilder()
                                                                  .bufferTransform(bufferTransform)
                                                                  .build();
        setPendingState(pendingSurfaceState);
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
    public Signal<Point, Slot<Point>> getPositionSignal() {
        return this.positionSignal;
    }

    @Nonnull
    public Signal<SurfaceState, Slot<SurfaceState>> getCommitSignal() {
        return this.commitSignal;
    }
}