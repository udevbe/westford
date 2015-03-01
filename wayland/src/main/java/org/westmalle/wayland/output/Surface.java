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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.Lists;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Matrices;
import com.hackoeur.jglm.Vec4;
import com.hackoeur.jglm.support.FastMath;
import org.freedesktop.wayland.server.*;
import org.westmalle.wayland.protocol.WlCompositor;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.hackoeur.jglm.Mat4.MAT4_IDENTITY;

@AutoFactory(className = "SurfaceFactory")
public class Surface {

    @Nonnull
    private final RegionFactory        regionFactory;
    @Nonnull
    private final WlCompositorResource wlCompositorResource;

    //pending state
    @Nonnull
    private SurfaceState pendingState = SurfaceState.builder()
                                                    .build();

    //pending derivable states
    @Nonnull
    private final List<Mat4> pendingCompositorTransforms = new LinkedList<>();
    @Nonnull
    private       Point      pendingBufferOffset         = Point.ZERO;

    //committed state
    @Nonnull
    private SurfaceState state = SurfaceState.builder()
                                             .build();

    //committed derived states
    private boolean   destroyed           = false;
    @Nonnull
    private Mat4      compositorTransform = MAT4_IDENTITY;
    @Nonnull
    private Mat4      transform           = MAT4_IDENTITY;
    @Nonnull
    private Mat4      inverseTransform    = MAT4_IDENTITY;
    @Nonnull
    private Point     position            = Point.ZERO;
    @Nonnull
    private Rectangle size                = Rectangle.ZERO;


    @Nonnull
    private final List<WlCallbackResource> callbacks = Lists.newLinkedList();

    Surface(@Nonnull @Provided final RegionFactory regionFactory,
            @Nonnull final WlCompositorResource wlCompositorResource) {
        this.regionFactory = regionFactory;
        this.wlCompositorResource = wlCompositorResource;
    }

    @Nonnull
    public List<WlCallbackResource> getFrameCallbacks() {
        return this.callbacks;
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    @Nonnull
    public Mat4 getTransform() {
        return this.transform;
    }

    @Nonnull
    public Surface markDestroyed() {
        this.destroyed = true;
        return this;
    }

    @Nonnull
    public Surface markDamaged(@Nonnull final Rectangle damage) {
        final Region newDamage = this.pendingState.getDamage()
                                                  .orElse(this.regionFactory.create())
                                                  .add(damage);
        this.pendingState = this.pendingState.toBuilder()
                                             .damage(Optional.of(newDamage))
                                             .build();
        return this;
    }

    @Nonnull
    public Surface attachBuffer(@Nonnull final WlBufferResource buffer,
                                @Nonnull final Integer relX,
                                @Nonnull final Integer relY) {

        this.pendingState = this.pendingState.toBuilder()
                                             .buffer(Optional.of(buffer))
                                             .build();
        this.pendingBufferOffset = Point.create(relX,
                                                relY);
        return this;
    }

    @Nonnull
    public List<Mat4> getPendingCompositorTransforms() {
        return this.pendingCompositorTransforms;
    }

    @Nonnull
    public Surface resetCompositorTransform() {
        this.compositorTransform = MAT4_IDENTITY;
        return this;
    }

    @Nonnull
    public Surface detachBuffer() {
        this.pendingState = this.pendingState.toBuilder()
                                             .buffer(Optional.<WlBufferResource>empty())
                                             .damage(Optional.<Region>empty())
                                             .build();
        this.pendingBufferOffset = Point.ZERO;
        return this;
    }

    /**
     * Compositor scoped position
     *
     * @return
     */
    @Nonnull
    public Point getPosition() {
        return this.position;
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
        this.state = this.pendingState;
        this.position = this.position.add(this.pendingBufferOffset);
        if (needsTransformUpdate) {
            updateCompositorTransform();
            updateTransform();
        }
        updateSize();
        //reset pending buffer state
        detachBuffer();
        final WlCompositor wlCompositor = (WlCompositor) this.wlCompositorResource.getImplementation();
        wlCompositor.getCompositor()
                    .requestRender();
        return this;
    }

    public void updateSize() {
        //TODO test this method
        final Optional<WlBufferResource> buffer = getState().getBuffer();
        if (buffer.isPresent()) {
            final WlBufferResource wlBufferResource = buffer.get();
            //FIXME we shouldn't assume the buffer to always be an shm buffer.
            final ShmBuffer shmBuffer = ShmBuffer.get(wlBufferResource);
            final int bufferWidth = shmBuffer.getWidth();
            final int bufferHeight = shmBuffer.getHeight();
            final int scale = FastMath.round(getTransform().getColumn(3)
                                                           .getW());
            this.size = Rectangle.builder()
                                 .width(bufferWidth / scale)
                                 .height(bufferHeight / scale)
                                 .build();
        }
        else {
            this.size = Rectangle.ZERO;
        }
    }

    @Nonnull
    public SurfaceState getState() {
        return this.state;
    }

    public boolean needsTransformUpdate() {
        return this.pendingState.getScale() != getState().getScale()
               || !this.pendingState.getBufferTransform()
                                    .equals(getState().getBufferTransform())
               || !this.pendingCompositorTransforms.isEmpty();
    }

    public Surface updateCompositorTransform() {
        for (final Mat4 pendingTransform : this.pendingCompositorTransforms) {
            this.compositorTransform = pendingTransform.multiply(this.compositorTransform);
        }
        this.pendingCompositorTransforms.clear();
        return this;
    }

    public Surface updateTransform() {
        //start with server transform
        Mat4 result = this.compositorTransform;
        //apply client transformation

        final Mat4 bufferTransform = getState().getBufferTransform();
        if (!bufferTransform.equals(MAT4_IDENTITY)) {
            result = bufferTransform.multiply(result);
        }
        //apply scaling
        final int scale = getState().getScale();
        if (scale != 1) {
            result = (Transforms.SCALE(scale)).multiply(result);
        }

        this.transform = result;
        this.inverseTransform = Matrices.invert(getTransform());
        return this;
    }

    @Nonnull
    public Surface addCallback(final WlCallbackResource callback) {
        this.callbacks.add(callback);
        return this;
    }

    @Nonnull
    public Surface removeOpaqueRegion() {
        this.pendingState = this.pendingState.toBuilder()
                                             .opaqueRegion(Optional.<WlRegionResource>empty())
                                             .build();
        return this;
    }

    @Nonnull
    public Surface setOpaqueRegion(@Nonnull final WlRegionResource opaqueRegion) {
        this.pendingState = this.pendingState.toBuilder()
                                             .opaqueRegion(Optional.of(opaqueRegion))
                                             .build();
        return this;
    }

    @Nonnull
    public Surface removeInputRegion() {
        this.pendingState = this.pendingState.toBuilder()
                                             .inputRegion(Optional.<WlRegionResource>empty())
                                             .build();
        return this;
    }

    @Nonnull
    public Surface setInputRegion(@Nonnull final WlRegionResource inputRegion) {
        this.pendingState = this.pendingState.toBuilder()
                                             .inputRegion(Optional.of(inputRegion))
                                             .build();
        return this;
    }

    @Nonnull
    public Surface setPosition(@Nonnull final Point position) {
        this.position = position;
        final WlCompositor wlCompositor = (WlCompositor) this.wlCompositorResource.getImplementation();
        wlCompositor.getCompositor()
                    .requestRender();
        return this;
    }

    public Surface firePaintCallbacks(final int serial) {
        final List<WlCallbackResource> callbacks = new ArrayList<>(getFrameCallbacks());
        getFrameCallbacks().clear();
        callbacks.forEach(frameCallback -> frameCallback.done(serial));
        return this;
    }

    /**
     * Translate a compositor scoped coordinate to a surface scoped coordinate.
     *
     * @param global
     *
     * @return
     */
    public Point local(final Point global) {
        final Point position = getPosition();
        final Vec4 untransformedLocalPoint = new Vec4(global.getX() - position.getX(),
                                                      global.getY() - position.getY(),
                                                      0.0f,
                                                      1.0f);
        final Vec4 localPoint;
        if (this.inverseTransform.equals(MAT4_IDENTITY)) {
            localPoint = untransformedLocalPoint;
        }
        else {
            localPoint = this.inverseTransform.multiply(untransformedLocalPoint);
        }

        return Point.create(FastMath.round(localPoint.getX() / localPoint.getW()),
                            FastMath.round(localPoint.getY() / localPoint.getW()));
    }

    /**
     * Surface scoped size and position
     *
     * @return
     */
    @Nonnull
    public Rectangle getSize() {
        return this.size;
    }

    public Surface setScale(@Nonnegative final int scale) {
        this.pendingState = this.pendingState.toBuilder()
                                             .scale(scale)
                                             .build();
        return this;
    }

    public Surface setBufferTransform(@Nonnull final Mat4 bufferTransform) {
        this.pendingState = this.pendingState.toBuilder()
                                             .bufferTransform(bufferTransform)
                                             .build();
        return this;
    }
}