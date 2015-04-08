package org.westmalle.wayland.output;


import com.google.auto.factory.AutoFactory;

import org.freedesktop.wayland.server.WlOutputResource;

import java.util.Set;

import javax.annotation.Nonnull;

@AutoFactory(className = "OutputFactory")
public class Output {

    @Nonnull
    private OutputGeometry outputGeometry;
    @Nonnull
    private OutputMode outputMode;

    Output(@Nonnull final OutputGeometry outputGeometry,
           @Nonnull final OutputMode outputMode) {
        this.outputGeometry = outputGeometry;
        this.outputMode = outputMode;
    }

    public Output update(final Set<WlOutputResource> resources,
                         final OutputGeometry outputGeometry){
        this.outputGeometry = outputGeometry;
        resources.forEach(this::notifyGeometry);
        return this;
    }

    public Output update(final Set<WlOutputResource> resources,
                         final OutputMode outputMode){
        this.outputMode = outputMode;
        resources.forEach(this::notifyMode);
        return this;
    }

    public Output notifyMode(final WlOutputResource wlOutputResource) {
        wlOutputResource.mode(this.outputMode.getFlags(),
                              this.outputMode.getWidth(),
                              this.outputMode.getHeight(),
                              this.outputMode.getRefresh());
        return this;
    }

    public Output notifyGeometry(final WlOutputResource wlOutputResource) {
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
}
