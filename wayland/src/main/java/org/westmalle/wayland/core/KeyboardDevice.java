package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Ints;
import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
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
import org.westmalle.wayland.nativ.libc.Libc;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.westmalle.wayland.nativ.libc.Libc.MAP_FAILED;
import static org.westmalle.wayland.nativ.libc.Libc.MAP_SHARED;
import static org.westmalle.wayland.nativ.libc.Libc.PROT_READ;
import static org.westmalle.wayland.nativ.libc.Libc.PROT_WRITE;

@AutoFactory(className = "KeyboardDeviceFactory")
public class KeyboardDevice {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyboardDevice.class);

    @Nonnull
    private final EventBus eventBus = new EventBus((exception,
                                                    context) -> LOGGER.error("",
                                                                             exception));
    @Nonnull
    private final Display           display;
    @Nonnull
    private final NativeFileFactory nativeFileFactory;
    @Nonnull
    private final Libc              libc;

    @Nonnull
    private final Compositor compositor;
    @Nonnull
    private final Xkb        xkb;

    @Nonnull
    private final Set<Integer>                pressedKeys          = new HashSet<>();
    @Nonnull
    private       Optional<Keymap>            keymap               = Optional.empty();
    @Nonnull
    private       Optional<DestroyListener>   focusDestroyListener = Optional.empty();
    @Nonnull
    private       Optional<WlSurfaceResource> focus                = Optional.empty();

    private int keySerial;

    KeyboardDevice(@Provided @Nonnull final Display display,
                   @Provided @Nonnull final NativeFileFactory nativeFileFactory,
                   @Provided @Nonnull final Libc libc,
                   @Nonnull final Compositor compositor,
                   @Nonnull final Xkb xkb) {
        this.display = display;
        this.nativeFileFactory = nativeFileFactory;
        this.libc = libc;
        this.compositor = compositor;
        this.xkb = xkb;
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
        getFocus().ifPresent(wlSurfaceResource ->
                                     findKeyboardResource(wlKeyboardResources,
                                                          wlSurfaceResource).ifPresent(wlKeyboardResource ->
                                                                                               wlKeyboardResource.key(nextKeyboardSerial(),
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

    @Nonnull
    public Optional<Keymap> getKeymap() {
        return this.keymap;
    }

    private int updateKeymapFile(final NativeString nativeKeyMapping) {
        final int size = (int) nativeKeyMapping.getPointer()
                                               .size();
        final int fd = this.nativeFileFactory.createAnonymousFile(size);
        final Pointer keymapArea = this.libc.mmap(null,
                                                  size,
                                                  PROT_READ | PROT_WRITE,
                                                  MAP_SHARED,
                                                  fd,
                                                  0);
        if (keymapArea.equals(MAP_FAILED)) {
            this.libc.close(fd);
            throw new LastErrorException(Native.getLastError());
        }

        this.libc.strcpy(keymapArea,
                         nativeKeyMapping.getPointer());
        return fd;
    }

    @Nonnull
    public Xkb getXkb() {
        return this.xkb;
    }
}
