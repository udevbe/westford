package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Ints;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.westmalle.wayland.core.events.Key;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@AutoFactory(className = "KeyboardDeviceFactory")
public class KeyboardDevice {

    @Nonnull
    private final EventBus inputBus = new EventBus();
    @Nonnull
    private final Display display;

    @Nonnull
    private       Optional<Listener>          focusDestroyListener = Optional.empty();
    @Nonnull
    private       Optional<WlSurfaceResource> focus                = Optional.empty();
    @Nonnull
    private final Set<Integer>                pressedKeys          = new HashSet<>();

    private int keySerial;

    KeyboardDevice(@Provided @Nonnull final Display display) {
        this.display = display;
    }

    public void key(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources,
                    final int time,
                    final int key,
                    @Nonnull final WlKeyboardKeyState wlKeyboardKeyState) {
        if (wlKeyboardKeyState.equals(WlKeyboardKeyState.PRESSED)) {
            getPressedKeys().add(key);
        }
        else {
            getPressedKeys().remove(key);
        }

        doKey(wlKeyboardResources,
              time,
              key,
              wlKeyboardKeyState);
        this.inputBus.post(Key.create(time,
                                      key,
                                      wlKeyboardKeyState));
    }

    @Nonnull
    public Set<Integer> getPressedKeys() {
        return this.pressedKeys;
    }

    private void doKey(final Set<WlKeyboardResource> wlKeyboardResources,
                       final int time,
                       final int key,
                       final WlKeyboardKeyState wlKeyboardKeyState) {
        getFocus().ifPresent(wlSurfaceResource -> findKeyboardResource(wlKeyboardResources,
                                                                       wlSurfaceResource)
                .ifPresent(wlKeyboardResource -> wlKeyboardResource.key(nextKeyboardSerial(),
                                                                        time,
                                                                        key,
                                                                        wlKeyboardKeyState.getValue())));
    }

    @Nonnull
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

    public int nextKeyboardSerial() {
        this.keySerial = this.display.nextSerial();
        return this.keySerial;
    }

    public int getKeyboardSerial() {
        return this.keySerial;
    }

    public void setFocus(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources,
                         @Nonnull final Optional<WlSurfaceResource> wlSurfaceResource) {
        final Optional<WlSurfaceResource> oldFocus = getFocus();
        updateFocus(wlSurfaceResource);
        final Optional<WlSurfaceResource> newFocus = getFocus();
        if (!oldFocus.equals(newFocus)) {
            oldFocus.ifPresent(oldFocusResource -> findKeyboardResource(wlKeyboardResources,
                                                                        oldFocusResource).ifPresent(oldFocusKeyboardResource -> oldFocusKeyboardResource.leave(nextKeyboardSerial(),
                                                                                                                                                               oldFocusResource)));
            newFocus.ifPresent(newFocusResource -> findKeyboardResource(wlKeyboardResources,
                                                                        newFocusResource).ifPresent(newFocusKeyboardResource -> {
                final ByteBuffer keys = ByteBuffer.allocateDirect(Integer.BYTES * this.pressedKeys.size());
                keys.asIntBuffer()
                    .put(Ints.toArray(this.pressedKeys));
                newFocusKeyboardResource.enter(nextKeyboardSerial(),
                                               newFocusResource,
                                               keys);
            }));
        }
    }

    private void updateFocus(final Optional<WlSurfaceResource> wlSurfaceResource) {
        this.focusDestroyListener.ifPresent(Listener::remove);
        this.focusDestroyListener = Optional.empty();
        this.focus = wlSurfaceResource;
        getFocus().ifPresent(focusResource -> {
            this.focusDestroyListener = Optional.of(new Listener() {
                @Override
                public void handle() {
                    updateFocus(Optional.<WlSurfaceResource>empty());
                }
            });
            focusResource.addDestroyListener(this.focusDestroyListener.get());
        });
    }

    public void register(@Nonnull final Object listener) {
        this.inputBus.register(listener);
    }

    public void unregister(@Nonnull final Object listener) {
        this.inputBus.unregister(listener);
    }
}
