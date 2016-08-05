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

import javax.annotation.Nonnull;
import java.util.Set;

@AutoFactory(className = "OutputFactory",
             allowSubclasses = true)
public class Output {

    @Nonnull
    private final String         name;
    @Nonnull
    private       OutputGeometry outputGeometry;
    @Nonnull
    private       OutputMode     outputMode;

    Output(@Nonnull final String name,
           @Nonnull final OutputGeometry outputGeometry,
           @Nonnull final OutputMode outputMode) {
        this.name = name;
        this.outputGeometry = outputGeometry;
        this.outputMode = outputMode;
    }

    public Output update(@Nonnull final Set<WlOutputResource> resources,
                         @Nonnull final OutputGeometry outputGeometry) {
        this.outputGeometry = outputGeometry;
        resources.forEach(this::notifyGeometry);
        return this;
    }

    public Output update(@Nonnull final Set<WlOutputResource> resources,
                         @Nonnull final OutputMode outputMode) {
        this.outputMode = outputMode;
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
}
