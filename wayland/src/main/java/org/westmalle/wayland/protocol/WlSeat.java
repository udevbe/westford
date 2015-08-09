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
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlPointerResource;
import org.freedesktop.wayland.server.WlSeatRequestsV4;
import org.freedesktop.wayland.server.WlSeatResource;
import org.freedesktop.wayland.server.WlTouchResource;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.Seat;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

@AutoFactory(className = "WlSeatFactory")
public class WlSeat extends Global<WlSeatResource> implements WlSeatRequestsV4, ProtocolObject<WlSeatResource> {

    private final Set<WlSeatResource> resources = Sets.newSetFromMap(new WeakHashMap<>());

    private final WlDataDevice wlDataDevice;
    private final Seat         seat;

    @Nonnull
    private final WlPointer  wlPointer;
    @Nonnull
    private final WlKeyboard wlKeyboard;
    @Nonnull
    private final WlTouch    wlTouch;

    private final Map<WlSeatResource, WlPointerResource>  wlPointerResources  = new HashMap<>();
    private final Map<WlSeatResource, WlKeyboardResource> wlKeyboardResources = new HashMap<>();
    private final Map<WlSeatResource, WlTouchResource>    wlTouchResources    = new HashMap<>();

    WlSeat(@Provided @Nonnull final Display display,
           @Provided @Nonnull final WlDataDevice wlDataDevice,
           @Nonnull final Seat seat,
           @Nonnull final WlPointer wlPointer,
           @Nonnull final WlKeyboard wlKeyboard,
           @Nonnull final WlTouch wlTouch) {
        super(display,
              WlSeatResource.class,
              VERSION);
        this.wlDataDevice = wlDataDevice;
        this.seat = seat;
        this.wlPointer = wlPointer;
        this.wlKeyboard = wlKeyboard;
        this.wlTouch = wlTouch;
    }

    @Override
    public WlSeatResource onBindClient(final Client client,
                                       final int version,
                                       final int id) {
        //FIXME check if we support given version.
        final WlSeatResource wlSeatResource = add(client,
                                                  version,
                                                  id);
        wlSeatResource.register(() -> {
            WlSeat.this.wlPointerResources.remove(wlSeatResource);
            WlSeat.this.wlKeyboardResources.remove(wlSeatResource);
            WlSeat.this.wlTouchResources.remove(wlSeatResource);
        });

        getSeat().emitCapabilities(Collections.singleton(wlSeatResource));

        return wlSeatResource;
    }

    @Override
    public void getPointer(final WlSeatResource wlSeatResource,
                           final int id) {
        final WlPointerResource wlPointerResource = getWlPointer().add(wlSeatResource.getClient(),
                                                                       wlSeatResource.getVersion(),
                                                                       id);
        this.wlPointerResources.put(wlSeatResource,
                                    wlPointerResource);
        wlPointerResource.register(() -> WlSeat.this.wlPointerResources.remove(wlSeatResource));
    }

    @Nonnull
    public WlPointer getWlPointer() {
        return this.wlPointer;
    }

    @Override
    public void getKeyboard(final WlSeatResource wlSeatResource,
                            final int id) {
        final WlKeyboard wlKeyboard = getWlKeyboard();
        final WlKeyboardResource wlKeyboardResource = wlKeyboard.add(wlSeatResource.getClient(),
                                                                     wlSeatResource.getVersion(),
                                                                     id);
        this.wlKeyboardResources.put(wlSeatResource,
                                     wlKeyboardResource);
        wlKeyboardResource.register(() -> WlSeat.this.wlKeyboardResources.remove(wlSeatResource));

        final KeyboardDevice keyboardDevice = wlKeyboard.getKeyboardDevice();
        keyboardDevice.updateKeymap(Collections.singleton(wlKeyboardResource),
                                    keyboardDevice.getKeymap());
    }

    @Nonnull
    public WlKeyboard getWlKeyboard() {
        return this.wlKeyboard;
    }

    @Override
    public void getTouch(final WlSeatResource wlSeatResource,
                         final int id) {
        final WlTouchResource wlTouchResource = getWlTouch().add(wlSeatResource.getClient(),
                                                                 wlSeatResource.getVersion(),
                                                                 id);
        this.wlTouchResources.put(wlSeatResource,
                                  wlTouchResource);
        wlTouchResource.register(() -> WlSeat.this.wlTouchResources.remove(wlSeatResource));
    }

    @Nonnull
    public WlTouch getWlTouch() {
        return this.wlTouch;
    }

    public Optional<WlKeyboardResource> getWlKeyboardResource(final WlSeatResource wlSeatResource) {
        return Optional.ofNullable(this.wlKeyboardResources.get(wlSeatResource));
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
        return new WlSeatResource(client,
                                  version,
                                  id,
                                  this);
    }

    public Seat getSeat() {
        return this.seat;
    }

    public Optional<WlPointerResource> getWlPointerResource(final WlSeatResource wlSeatResource) {
        return Optional.ofNullable(this.wlPointerResources.get(wlSeatResource));
    }

    public Optional<WlTouchResource> getWlTouchResource(final WlSeatResource wlSeatResource) {
        return Optional.ofNullable(this.wlTouchResources.get(wlSeatResource));
    }

    public WlDataDevice getWlDataDevice() {
        return this.wlDataDevice;
    }
}