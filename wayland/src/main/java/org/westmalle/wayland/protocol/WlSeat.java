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
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.WlSeatRequestsV4;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.shared.WlSeatCapability;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlSeatFactory")
public class WlSeat extends Global<WlSeatResource> implements WlSeatRequestsV4, ProtocolObject<WlSeatResource> {

    private final Set<WlSeatResource> resources = Sets.newSetFromMap(new WeakHashMap<>());
    private final WlDataDevice wlDataDevice;

    private Optional<WlPointer>  optionalWlPointer  = Optional.empty();
    private Optional<WlKeyboard> optionalWlKeyboard = Optional.empty();
    private Optional<WlTouch>    optionalWlTouch    = Optional.empty();

    WlSeat(@Provided final Display display,
           @Provided final WlDataDevice wlDataDevice) {
        super(display,
              WlSeatResource.class,
              VERSION);
        this.wlDataDevice = wlDataDevice;
    }

    @Override
    public WlSeatResource onBindClient(final Client client,
                                       final int version,
                                       final int id) {
        //FIXME check if we support given version.
        return add(client,
                   version,
                   id);
    }

    @Override
    public void getPointer(final WlSeatResource resource,
                           final int id) {
        this.optionalWlPointer.ifPresent(wlPointer ->
                                                 wlPointer.add(resource.getClient(),
                                                               resource.getVersion(),
                                                               id));
    }

    @Override
    public void getKeyboard(final WlSeatResource resource,
                            final int id) {
        this.optionalWlKeyboard.ifPresent(wlKeyboard ->
                                                  wlKeyboard.add(resource.getClient(),
                                                                 resource.getVersion(),
                                                                 id));
    }

    @Override
    public void getTouch(final WlSeatResource resource,
                         final int id) {
        this.optionalWlTouch.ifPresent(wlTouch ->
                                               wlTouch.add(resource.getClient(),
                                                           resource.getVersion(),
                                                           id));
    }

    public Optional<WlKeyboard> getOptionalWlKeyboard() {
        return this.optionalWlKeyboard;
    }

    public void setWlKeyboard(final WlKeyboard newWlKeyboard) {
        this.optionalWlKeyboard = Optional.of(newWlKeyboard);
        getResources().forEach(this::emiteCapabilities);
    }

    @Nonnull
    @Override
    public Set<WlSeatResource> getResources() {
        return this.resources;
    }

    @Nonnull
    @Override
    public WlSeatResource create(@Nonnull final Client client,
                                 @Nonnegative final int version,
                                 final int id) {
        final WlSeatResource resource = new WlSeatResource(client,
                                                           version,
                                                           id,
                                                           this);
        emiteCapabilities(resource);
        return resource;
    }

    private void emiteCapabilities(final WlSeatResource wlSeatResource) {
        int capabilities = 0;
        if (this.optionalWlPointer.isPresent()) {
            capabilities |= WlSeatCapability.POINTER.getValue();
        }
        if (this.optionalWlKeyboard.isPresent()) {
            capabilities |= WlSeatCapability.KEYBOARD.getValue();
        }
        if (this.optionalWlTouch.isPresent()) {
            capabilities |= WlSeatCapability.TOUCH.getValue();
        }
        wlSeatResource.capabilities(capabilities);
    }

    public void removeWlKeyboard() {
        this.optionalWlKeyboard = Optional.empty();
        getResources().forEach(this::emiteCapabilities);
    }

    public Optional<WlPointer> getOptionalWlPointer() {
        return this.optionalWlPointer;
    }

    public void setWlPointer(@Nonnull final WlPointer newWlPointer) {
        this.optionalWlPointer = Optional.of(newWlPointer);
        getResources().forEach(this::emiteCapabilities);
    }

    public void removeWlPointer() {
        this.optionalWlPointer = Optional.empty();
        getResources().forEach(this::emiteCapabilities);
    }

    public Optional<WlTouch> getOptionalWlTouch() {
        return this.optionalWlTouch;
    }

    public void setWlTouch(final WlTouch wlTouch) {
        this.optionalWlTouch = Optional.of(wlTouch);
        getResources().forEach(this::emiteCapabilities);
    }

    public void removeWlTouch() {
        this.optionalWlTouch = Optional.empty();
        getResources().forEach(this::emiteCapabilities);
    }

    public WlDataDevice getWlDataDevice() {
        return this.wlDataDevice;
    }
}