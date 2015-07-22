package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.eventbus.EventBus;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.westmalle.wayland.core.events.Key;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

@AutoFactory(className = "KeyboardDeviceFactory")
public class KeyboardDevice {

    @Nonnull
    private final EventBus inputBus       = new EventBus();
    @Nonnull
    private final Display display;

    private Optional<WlSurfaceResource> focus = Optional.empty();
    private int keySerial;

    KeyboardDevice(@Provided @Nonnull final Display display) {
        this.display = display;
    }

    public void key(final Set<WlKeyboardResource> wlKeyboardResources,
                    final int time,
                    final int key,
                    final WlKeyboardKeyState wlKeyboardKeyState){
        doKey(wlKeyboardResources,
              time,
              key,
              wlKeyboardKeyState);
        this.inputBus.post(Key.create(time,
                                      key,
                                      wlKeyboardKeyState));
    }

    private void doKey(final Set<WlKeyboardResource> wlKeyboardResources,
                       final int time,
                       final int key,
                       final WlKeyboardKeyState wlKeyboardKeyState) {
        getFocus().ifPresent(wlSurfaceResource -> findKeyboardResource(wlKeyboardResources,
                                                                       wlSurfaceResource)
                .ifPresent(wlKeyboardResource -> wlKeyboardResource.key(nextKeySerial(),
                                                                        time,
                                                                        key,
                                                                        wlKeyboardKeyState.getValue())));
    }

    public int nextKeySerial(){
        this.keySerial = this.display.nextSerial();
        return this.keySerial;
    }

    public int getKeySerial() {
        return this.keySerial;
    }

    public void setFocus(Optional<WlSurfaceResource> wlSurfaceResource){
        this.focus = wlSurfaceResource;
    }

    public Optional<WlSurfaceResource> getFocus() {
        return this.focus;
    }

    private Optional<WlKeyboardResource> findKeyboardResource(final Set<WlKeyboardResource> wlKeyboardResources,
                                                            final WlSurfaceResource wlSurfaceResource) {
        for (final WlKeyboardResource wlKeyboardResource : wlKeyboardResources) {
            if (wlSurfaceResource.getClient()
                    .equals(wlKeyboardResource.getClient())) {
                return Optional.of(wlKeyboardResource);
            }
        }
        return Optional.empty();
    }

    public void register(final Object listener) {
        this.inputBus.register(listener);
    }

    public void unregister(final Object listener) {
        this.inputBus.unregister(listener);
    }
}
