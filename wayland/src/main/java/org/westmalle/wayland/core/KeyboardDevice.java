package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Ints;

import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.westmalle.wayland.core.events.Key;
import org.westmalle.wayland.nativ.NativeFileFactory;
import org.westmalle.wayland.nativ.NativeString;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

@AutoFactory(className = "KeyboardDeviceFactory")
public class KeyboardDevice {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyboardDevice.class);

    @Nonnull
    private final EventBus eventBus = new EventBus((exception,
                                                    context) -> LOGGER.error("",
                                                                             exception));
    @Nonnull
    private final Display display;
    @Nonnull
    private final NativeFileFactory nativeFileFactory;
    @Nonnull
    private final Compositor compositor;
    @Nonnull
    private Optional<Keymap> keymap =Optional.empty();
    @Nonnull
    private final Set<Integer>                pressedKeys          = new HashSet<>();
    @Nonnull
    private       Optional<DestroyListener>   focusDestroyListener = Optional.empty();
    @Nonnull
    private       Optional<WlSurfaceResource> focus                = Optional.empty();
    private int keySerial;

    KeyboardDevice(@Provided @Nonnull final Display display,
                   @Provided @Nonnull final NativeFileFactory nativeFileFactory,
                   @Nonnull final Compositor compositor) {
        this.display = display;
        this.nativeFileFactory = nativeFileFactory;
        this.compositor = compositor;
    }

    public void key(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources,
                    final int key,
                    @Nonnull final WlKeyboardKeyState wlKeyboardKeyState) {
        final int time = this.compositor.getTime();
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
        this.eventBus.post(Key.create(time,
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
        this.focus.ifPresent(oldFocusResource -> oldFocusResource.unregister(this.focusDestroyListener.get()));
        this.focusDestroyListener = Optional.empty();
        this.focus = wlSurfaceResource;
        getFocus().ifPresent(focusResource -> {
            this.focusDestroyListener = Optional.of(() -> updateFocus(Optional.<WlSurfaceResource>empty()));
            focusResource.register(this.focusDestroyListener.get());
        });
    }

    public void register(@Nonnull final Object listener) {
        this.eventBus.register(listener);
    }

    public void unregister(@Nonnull final Object listener) {
        this.eventBus.unregister(listener);
    }

    public void updateKeymap(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources,
                             @Nonnull final Optional<Keymap> keymap) {
        this.keymap = keymap;
        getKeymap().ifPresent(keymapping -> {
            final NativeString nativeKeyMapping = new NativeString(keymapping.getMap());
            wlKeyboardResources.forEach(wlKeyboardResource ->
                                                wlKeyboardResource.keymap(keymapping.getFormat()
                                                                                    .getValue(),
                                                                          updateKeymapFile(nativeKeyMapping),
                                                                          nativeKeyMapping.length()));
        });
    }

    private int updateKeymapFile(final NativeString nativeKeyMapping) {
        final int fd = this.nativeFileFactory.createAnonymousFile(nativeKeyMapping.length());
        //TODO mmap
        return fd;
    }

    @Nonnull
    public Optional<Keymap> getKeymap() {
        return this.keymap;
    }
}
