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
package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.WlOutputResource;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.core.calc.Vec4;
import org.westmalle.wayland.core.events.OutputTransform;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Set;

@AutoFactory(className = "PrivateOutputFactory",
             allowSubclasses = true)
public class Output {

    private final Signal<OutputTransform, Slot<OutputTransform>> transformSignal  = new Signal<>();
    private final Signal<OutputMode, Slot<OutputMode>>           outputModeSignal = new Signal<>();

    @Nonnull
    private final String name;
    @Nonnull
    private final FiniteRegion region;
    @Nonnegative
    private float scale            = 1f;
    @Nonnull
    private Mat4  transform        = Mat4.IDENTITY;
    @Nonnull
    private Mat4  inverseTransform = Mat4.IDENTITY;
    @Nonnull
    private OutputGeometry outputGeometry = OutputGeometry.builder()
                                                          .physicalWidth(0)
                                                          .physicalHeight(0)
                                                          .make("")
                                                          .model("")
                                                          .x(0)
                                                          .y(0)
                                                          .subpixel(0)
                                                          .transform(0)
                                                          .build();
    @Nonnull
    private OutputMode     outputMode     = OutputMode.builder()
                                                      .width(0)
                                                      .height(0)
                                                      .refresh(0)
                                                      .flags(0)
                                                      .build();

    Output(@Provided @Nonnull final FiniteRegion finiteRegion,
           @Nonnull final String name) {
        this.region = finiteRegion;
        this.name = name;
    }

    public void update(@Nonnull final Set<WlOutputResource> resources,
                       @Nonnull final OutputGeometry outputGeometry) {
        if (!this.outputGeometry.equals(outputGeometry)) {
            this.outputGeometry = outputGeometry;
            updateOutputTransform();
        }

        resources.forEach(this::notifyGeometry);
    }

    private void updateOutputTransform() {

        final Mat4 scaleMat = Transforms.SCALE(this.scale);
        final int  x        = this.outputGeometry.getX();
        final int  y        = this.outputGeometry.getY();
        final Mat4 moveMat = Transforms.TRANSLATE(x,
                                                  y);
        final Mat4 transformMat;
        final int  transformNr = this.outputGeometry.getTransform();
        if (transformNr == WlOutputTransform.NORMAL.value) {
            transformMat = Mat4.IDENTITY;
        }
        else if (transformNr == WlOutputTransform._90.value) {
            transformMat = Transforms._90;
        }
        else if (transformNr == WlOutputTransform._180.value) {
            transformMat = Transforms._180;
        }
        else if (transformNr == WlOutputTransform._270.value) {
            transformMat = Transforms._270;
        }
        else if (transformNr == WlOutputTransform.FLIPPED.value) {
            transformMat = Transforms.FLIPPED;
        }
        else if (transformNr == WlOutputTransform.FLIPPED_90.value) {
            transformMat = Transforms.FLIPPED_90;
        }
        else if (transformNr == WlOutputTransform.FLIPPED_180.value) {
            transformMat = Transforms.FLIPPED_180;
        }
        else if (transformNr == WlOutputTransform.FLIPPED_270.value) {
            transformMat = Transforms.FLIPPED_270;
        }
        else {
            transformMat = Mat4.IDENTITY;
        }

        final Mat4 newTransform = transformMat.multiply(scaleMat)
                                              .multiply(moveMat);
        if (!this.transform.equals(newTransform)) {
            this.transform = newTransform;
            this.inverseTransform = this.transform.invert();
            updateRegion();
            this.transformSignal.emit(OutputTransform.create());
        }
    }

    private void updateRegion() {
        final Point regionTopLeft = this.transform.multiply(Point.ZERO.toVec4())
                                                  .toPoint();
        final Point regionBottomRight = this.transform.multiply(Point.create(this.outputMode.getWidth(),
                                                                             this.outputMode.getHeight())
                                                                     .toVec4())
                                                      .toPoint();
        //TODO fire region event?
        this.region.clear();
        //TODO check if the region is properly updated in the unit tests
        this.region.add(Rectangle.create(regionTopLeft,
                                         regionBottomRight.getX() - regionTopLeft.getX(),
                                         regionBottomRight.getY() - regionTopLeft.getY()));
    }

    public Output update(@Nonnull final Set<WlOutputResource> resources,
                         @Nonnull final OutputMode outputMode) {
        if (!this.outputMode.equals(outputMode)) {
            this.outputMode = outputMode;
            updateRegion();
            this.outputModeSignal.emit(outputMode);
        }
        resources.forEach(this::notifyMode);
        return this;
    }

    public void notifyMode(@Nonnull final WlOutputResource wlOutputResource) {
        wlOutputResource.mode(this.outputMode.getFlags(),
                              this.outputMode.getWidth(),
                              this.outputMode.getHeight(),
                              this.outputMode.getRefresh());
    }

    public void notifyGeometry(@Nonnull final WlOutputResource wlOutputResource) {
        wlOutputResource.geometry(this.outputGeometry.getX(),
                                  this.outputGeometry.getY(),
                                  this.outputGeometry.getPhysicalWidth(),
                                  this.outputGeometry.getPhysicalHeight(),
                                  this.outputGeometry.getSubpixel(),
                                  this.outputGeometry.getMake(),
                                  this.outputGeometry.getModel(),
                                  this.outputGeometry.getTransform());
    }

    @Nonnull
    public OutputMode getMode() {
        return this.outputMode;
    }

    @Nonnull
    public OutputGeometry getGeometry() {
        return this.outputGeometry;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nonnull
    public Point local(@Nonnull final Point global) {
        final Vec4 localPoint = this.inverseTransform.multiply(global.toVec4());
        return Point.create((int) localPoint.getX(),
                            (int) localPoint.getY());
    }

    /**
     * translate from compositor space to output space
     *
     * @return
     */
    @Nonnull
    public Mat4 getInverseTransform() {
        return this.inverseTransform;
    }

    /**
     * translate from output space to compositor space
     *
     * @return
     */
    @Nonnull
    public Mat4 getTransform() {
        return this.transform;
    }

    @Nonnull
    public Point global(@Nonnull final Point outputLocal) {
        final Vec4 globalPoint = this.transform.multiply(outputLocal.toVec4());
        return Point.create((int) globalPoint.getX(),
                            (int) globalPoint.getY());
    }

    public Signal<OutputTransform, Slot<OutputTransform>> getTransformSignal() {
        return this.transformSignal;
    }

    public Signal<OutputMode, Slot<OutputMode>> getModeSignal() {
        return this.outputModeSignal;
    }

    @Nonnegative
    public float getScale() {
        return this.scale;
    }

    public void setScale(@Nonnegative final float scale) {
        this.scale = scale;
        updateOutputTransform();
    }

    @Nonnull
    public FiniteRegion getRegion() {
        return this.region;
    }
}
