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
package org.westmalle.wayland.protocol;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.collect.Sets;
import org.freedesktop.wayland.server.*;
import org.westmalle.wayland.output.OutputGeometry;
import org.westmalle.wayland.output.OutputMode;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlOutputFactory")
public class WlOutput extends Global<WlOutputResource> implements WlOutputRequestsV2, ProtocolObject<WlOutputResource> {

    private final Set<WlOutputResource> resources = Sets.newSetFromMap(new WeakHashMap<>());
    @Nonnull
    private OutputGeometry outputGeometry;
    @Nonnull
    private OutputMode outputMode;

    WlOutput(@Provided final Display display,
             @Nonnull final OutputGeometry outputGeometry,
             @Nonnull final OutputMode outputMode) {
        super(display,
              WlOutputResource.class,
              VERSION);
        this.outputGeometry = outputGeometry;
        this.outputMode = outputMode;
    }

    @Override
    public WlOutputResource onBindClient(final Client client,
                                         final int version,
                                         final int id) {
        final WlOutputResource wlOutputResource = add(client,
                                         version,
                                         id);
        notifyGeometry(wlOutputResource);
        notifyMode(wlOutputResource);
        return wlOutputResource;
    }

    private void notifyMode(final WlOutputResource wlOutputResource) {
        wlOutputResource.mode(this.outputMode.getFlags(),
                              this.outputMode.getWidth(),
                              this.outputMode.getHeight(),
                              this.outputMode.getRefresh());
    }

    private void notifyGeometry(final WlOutputResource wlOutputResource) {
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
    @Override
    public Set<WlOutputResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public WlOutputResource create(@Nonnull final Client client,
                                   @Nonnegative final int version,
                                   final int id) {
        return new WlOutputResource(client,
                                    version,
                                    id,
                                    this);
    }

    public void update(final OutputGeometry outputGeometry){
        getResources().forEach(this::notifyGeometry);
    }

    public void update(final OutputMode outputMode){
        getResources().forEach(this::notifyMode);
    }

    @Nonnull
    public OutputGeometry getOutputGeometry() {
        return this.outputGeometry;
    }

    @Nonnull
    public OutputMode getOutputMode() {
        return this.outputMode;
    }
}
