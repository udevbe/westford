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
import org.freedesktop.wayland.server.WlOutputResource;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.core.calc.Vec4;
import org.westmalle.wayland.core.events.OutputTransform;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Set;

@AutoFactory(className = "OutputFactory",
             allowSubclasses = true)
public class Output {

    private final Signal<OutputTransform, Slot<OutputTransform>> transformSignal      = new Signal<>();
    private final Signal<OutputGeometry, Slot<OutputGeometry>>   outputGeometrySignal = new Signal<>();
    private final Signal<OutputMode, Slot<OutputMode>>           outputModeSignal     = new Signal<>();

    @Nonnull
    private final String name;

    @Nonnegative
    private float scale            = 1f;
    @Nonnull
    private Mat4  transform        = Mat4.IDENTITY;
    @Nonnull
    private Mat4  inverseTransform = Mat4.IDENTITY;

    @Nonnull
    private OutputGeometry outputGeometry;
    @Nonnull
    private OutputMode     outputMode;

    Output(@Nonnull final String name,
           @Nonnull final OutputGeometry outputGeometry,
           @Nonnull final OutputMode outputMode) {
        this.name = name;
        this.outputGeometry = outputGeometry;
        this.outputMode = outputMode;
    }

    public Output update(@Nonnull final Set<WlOutputResource> resources,
                         @Nonnull final OutputGeometry outputGeometry) {
        if (!this.outputGeometry.equals(outputGeometry)) {
            this.outputGeometry = outputGeometry;
            this.outputGeometrySignal.emit(outputGeometry);
            updateOutputTransform();
        }

        resources.forEach(this::notifyGeometry);
        return this;
    }

    private void updateOutputTransform() {

        final Mat4 scaleMat = Transforms.SCALE(this.scale);
        final Mat4 moveMat = Transforms.TRANSLATE(-this.outputGeometry.getX(),
                                                  -this.outputGeometry.getY());
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

            this.transformSignal.emit(OutputTransform.create());
        }
    }

    public Output update(@Nonnull final Set<WlOutputResource> resources,
                         @Nonnull final OutputMode outputMode) {
        if (!this.outputMode.equals(outputMode)) {
            this.outputMode = outputMode;
            this.outputModeSignal.emit(outputMode);
        }
        resources.forEach(this::notifyMode);
        return this;
    }

    public Output notifyMode(@Nonnull final WlOutputResource wlOutputResource) {
        wlOutputResource.mode(this.outputMode.getFlags(),
                              this.outputMode.getWidth(),
                              this.outputMode.getHeight(),
                              this.outputMode.getRefresh());
        return this;
    }

    public Output notifyGeometry(@Nonnull final WlOutputResource wlOutputResource) {
        wlOutputResource.geometry(this.outputGeometry.getX(),
                                  this.outputGeometry.getY(),
                                  this.outputGeometry.getPhysicalWidth(),
                                  this.outputGeometry.getPhysicalHeight(),
                                  this.outputGeometry.getSubpixel(),
                                  this.outputGeometry.getMake(),
                                  this.outputGeometry.getModel(),
                                  this.outputGeometry.getTransform());
        return this;
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

    @Nonnull
    public Mat4 getInverseTransform() {
        return this.inverseTransform;
    }

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

    public Signal<OutputGeometry, Slot<OutputGeometry>> getGeometrySignal() {
        return this.outputGeometrySignal;
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
}
