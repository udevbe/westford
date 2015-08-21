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
package org.westmalle.wayland.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.freedesktop.wayland.shared.WlKeyboardKeymapFormat;
import org.westmalle.wayland.core.events.Key;
import org.westmalle.wayland.core.events.KeyboardFocus;
import org.westmalle.wayland.core.events.KeyboardFocusGained;
import org.westmalle.wayland.core.events.KeyboardFocusLost;
import org.westmalle.wayland.nativ.NativeFileFactory;
import org.westmalle.wayland.nativ.NativeString;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon;
import org.westmalle.wayland.protocol.WlSurface;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.westmalle.wayland.nativ.libc.Libc.MAP_FAILED;
import static org.westmalle.wayland.nativ.libc.Libc.MAP_SHARED;
import static org.westmalle.wayland.nativ.libc.Libc.PROT_READ;
import static org.westmalle.wayland.nativ.libc.Libc.PROT_WRITE;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_KEY_DOWN;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_KEY_UP;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_LAYOUT_EFFECTIVE;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_MODS_DEPRESSED;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_MODS_LATCHED;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_MODS_LOCKED;

@AutoFactory(className = "KeyboardDeviceFactory")
public class KeyboardDevice {

    @Nonnull
    private final Bus bus = new Bus(ThreadEnforcer.ANY);
    @Nonnull
    private final Display           display;
    @Nonnull
    private final NativeFileFactory nativeFileFactory;
    @Nonnull
    private final Libc              libc;

    @Nonnull
    private final Libxkbcommon libxkbcommon;
    @Nonnull
    private final Compositor   compositor;
    @Nonnull
    private final Set<Integer> pressedKeys = new HashSet<>();
    @Nonnull
    private Xkb xkb;
    @Nonnull
    private Optional<DestroyListener>   focusDestroyListener = Optional.empty();
    @Nonnull
    private Optional<WlSurfaceResource> focus                = Optional.empty();

    private int keymapFd   = -1;
    @Nonnegative
    private int keymapSize = 0;

    private int keySerial;

    KeyboardDevice(@Provided @Nonnull final Display display,
                   @Provided @Nonnull final NativeFileFactory nativeFileFactory,
                   @Provided @Nonnull final Libc libc,
                   @Provided @Nonnull final Libxkbcommon libxkbcommon,
                   @Nonnull final Compositor compositor,
                   @Nonnull final Xkb xkb) {
        this.display = display;
        this.nativeFileFactory = nativeFileFactory;
        this.libc = libc;
        this.libxkbcommon = libxkbcommon;
        this.compositor = compositor;
        this.xkb = xkb;
    }

    public void key(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources,
                    final int key,
                    @Nonnull final WlKeyboardKeyState wlKeyboardKeyState) {

        int           stateComponentMask = 0;
        final Pointer xkbState           = getXkb().getState();
        final int     evdevKey           = key + 8;
        if (wlKeyboardKeyState.equals(WlKeyboardKeyState.PRESSED)) {
            if (getPressedKeys().add(key)) {
                stateComponentMask = this.libxkbcommon.xkb_state_update_key(xkbState,
                                                                            evdevKey,
                                                                            XKB_KEY_DOWN);
            }
        }
        else {
            if (getPressedKeys().remove(key)) {
                stateComponentMask = this.libxkbcommon.xkb_state_update_key(xkbState,
                                                                            evdevKey,
                                                                            XKB_KEY_UP);
            }
        }

        final int time = this.compositor.getTime();
        this.bus.post(Key.create(time,
                                 key,
                                 wlKeyboardKeyState));
        doKey(wlKeyboardResources,
              time,
              key,
              wlKeyboardKeyState);

        handleStateComponentMask(wlKeyboardResources,
                                 stateComponentMask);
    }

    @Nonnull
    public Xkb getXkb() {
        return this.xkb;
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
                                     match(wlKeyboardResources,
                                           wlSurfaceResource).forEach(wlKeyboardResource ->
                                                                              wlKeyboardResource.key(nextKeyboardSerial(),
                                                                                                     time,
                                                                                                     key,
                                                                                                     wlKeyboardKeyState.getValue())));
    }

    private void handleStateComponentMask(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources,
                                          final int stateComponentMask) {
        if ((stateComponentMask & (XKB_STATE_MODS_DEPRESSED |
                                   XKB_STATE_MODS_LATCHED |
                                   XKB_STATE_MODS_LOCKED |
                                   XKB_STATE_LAYOUT_EFFECTIVE)) != 0) {
            final int modsDepressed = this.libxkbcommon.xkb_state_serialize_mods(getXkb().getState(),
                                                                                 XKB_STATE_MODS_DEPRESSED);
            final int modsLatched = this.libxkbcommon.xkb_state_serialize_mods(getXkb().getState(),
                                                                               XKB_STATE_MODS_LATCHED);
            final int modsLocked = this.libxkbcommon.xkb_state_serialize_mods(getXkb().getState(),
                                                                              XKB_STATE_MODS_LOCKED);
            final int group = this.libxkbcommon.xkb_state_serialize_layout(getXkb().getState(),
                                                                           XKB_STATE_LAYOUT_EFFECTIVE);
            wlKeyboardResources.forEach(wlKeyboardResource ->
                                                wlKeyboardResource.modifiers(this.display.nextSerial(),
                                                                             modsDepressed,
                                                                             modsLatched,
                                                                             modsLocked,
                                                                             group));
        }
    }

    @Nonnull
    public Optional<WlSurfaceResource> getFocus() {
        return this.focus;
    }

    private Set<WlKeyboardResource> match(final Set<WlKeyboardResource> wlKeyboardResources,
                                          final WlSurfaceResource wlSurfaceResource) {
        //find keyboard resources that match this keyboard device
        final WlSurface               wlSurface       = (WlSurface) wlSurfaceResource.getImplementation();
        final Surface                 surface         = wlSurface.getSurface();
        final Set<WlKeyboardResource> keyboardFocuses = new HashSet<>(surface.getKeyboardFocuses());
        keyboardFocuses.retainAll(wlKeyboardResources);

        return keyboardFocuses;
    }

    public int nextKeyboardSerial() {
        this.keySerial = this.display.nextSerial();
        return this.keySerial;
    }

    public void setXkb(@Nonnull final Xkb xkb) {
        this.xkb = xkb;
        //TODO report pressed keys to xkb state when setting new xkb
    }

    public int getKeyboardSerial() {
        return this.keySerial;
    }

    public void setFocus(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources,
                         @Nonnull final Optional<WlSurfaceResource> newFocus) {
        final Optional<WlSurfaceResource> oldFocus = getFocus();
        if (!oldFocus.equals(newFocus)) {
            updateFocus(wlKeyboardResources,
                        oldFocus,
                        newFocus);
        }
    }

    private int[] toIntArray(final Set<Integer> set) {
        final int[] ret = new int[set.size()];
        int         i   = 0;
        for (final Integer e : set) { ret[i++] = e; }
        return ret;
    }

    private void updateFocus(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources,
                             final Optional<WlSurfaceResource> oldFocus,
                             final Optional<WlSurfaceResource> newFocus) {
        this.focus = newFocus;
        this.bus.post(KeyboardFocus.create(newFocus));

        oldFocus.ifPresent(oldFocusResource -> {
            oldFocusResource.unregister(this.focusDestroyListener.get());
            this.focusDestroyListener = Optional.empty();

            final WlSurface wlSurface = (WlSurface) oldFocusResource.getImplementation();
            final Surface surface = wlSurface.getSurface();

            final Set<WlKeyboardResource> clientKeyboardResources = filter(wlKeyboardResources,
                                                                           oldFocusResource.getClient());
            surface.getKeyboardFocuses()
                   .removeAll(clientKeyboardResources);
            surface.post(KeyboardFocusLost.create(clientKeyboardResources));

            clientKeyboardResources.forEach(oldFocusKeyboardResource ->
                                                    oldFocusKeyboardResource.leave(nextKeyboardSerial(),
                                                                                   oldFocusResource));
        });

        newFocus.ifPresent(newFocusResource -> {
            this.focusDestroyListener = Optional.of(() -> updateFocus(wlKeyboardResources,
                                                                      newFocus,
                                                                      Optional.<WlSurfaceResource>empty()));
            newFocusResource.register(this.focusDestroyListener.get());

            final WlSurface wlSurface = (WlSurface) newFocusResource.getImplementation();
            final Surface surface = wlSurface.getSurface();

            final Set<WlKeyboardResource> clientKeyboardResources = filter(wlKeyboardResources,
                                                                           newFocusResource.getClient());
            surface.getKeyboardFocuses()
                   .addAll(clientKeyboardResources);
            surface.post(KeyboardFocusGained.create(clientKeyboardResources));

            match(wlKeyboardResources,
                  newFocusResource).forEach(newFocusKeyboardResource -> {
                final ByteBuffer keys = ByteBuffer.allocateDirect(Integer.BYTES * this.pressedKeys.size());
                keys.asIntBuffer()
                    .put(toIntArray(getPressedKeys()));
                newFocusKeyboardResource.enter(nextKeyboardSerial(),
                                               newFocusResource,
                                               keys);
            });
        });
    }


    private Set<WlKeyboardResource> filter(final Set<WlKeyboardResource> wlKeyboardResources,
                                           final Client client) {
        //filter out keyboard resources that do not belong to the given client.
        return wlKeyboardResources.stream()
                                  .filter(wlKeyboardResource -> wlKeyboardResource.getClient()
                                                                                  .equals(client))
                                  .collect(Collectors.toSet());
    }

    public void register(@Nonnull final Object listener) {
        this.bus.register(listener);
    }

    public void unregister(@Nonnull final Object listener) {
        this.bus.unregister(listener);
    }

    public void emitKeymap(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources) {
        if (this.keymapFd >= 0) {
            wlKeyboardResources.forEach(wlKeyboardResource ->
                                                wlKeyboardResource.keymap(WlKeyboardKeymapFormat.XKB_V1.getValue(),
                                                                          this.keymapFd,
                                                                          this.keymapSize));
        }
    }

    public void updateKeymap() {
        final NativeString nativeKeyMapping = new NativeString(getXkb().getKeymapString());

        //-1 to get rid of the null terminator
        final int size = (int) nativeKeyMapping.getPointer()
                                               .size() - 1;
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

        if (this.keymapFd >= 0) {
            this.libc.close(this.keymapFd);
        }
        this.keymapFd = fd;
        this.keymapSize = size;
    }
}
