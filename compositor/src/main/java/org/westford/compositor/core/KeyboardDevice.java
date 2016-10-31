/*
 * Westford Wayland Compositor.
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
package org.westford.compositor.core;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;
import org.freedesktop.wayland.shared.WlKeyboardKeymapFormat;
import org.westford.Signal;
import org.westford.Slot;
import org.westford.compositor.core.events.Key;
import org.westford.compositor.core.events.KeyboardFocus;
import org.westford.compositor.core.events.KeyboardFocusGained;
import org.westford.compositor.core.events.KeyboardFocusLost;
import org.westford.compositor.protocol.WlSurface;
import org.westford.nativ.NativeFileFactory;
import org.westford.nativ.glibc.Libc;
import org.westford.nativ.libxkbcommon.Libxkbcommon;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.westford.nativ.libxkbcommon.Libxkbcommon.XKB_KEY_DOWN;
import static org.westford.nativ.libxkbcommon.Libxkbcommon.XKB_KEY_UP;
import static org.westford.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_LAYOUT_EFFECTIVE;
import static org.westford.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_MODS_DEPRESSED;
import static org.westford.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_MODS_LATCHED;
import static org.westford.nativ.libxkbcommon.Libxkbcommon.XKB_STATE_MODS_LOCKED;

@AutoFactory(className = "KeyboardDeviceFactory",
             allowSubclasses = true)
public class KeyboardDevice {

    @Nonnull
    private final Signal<Key, Slot<Key>>                     keySignal           = new Signal<>();
    @Nonnull
    private final Signal<KeyboardFocus, Slot<KeyboardFocus>> keyboardFocusSignal = new Signal<>();

    @Nonnull
    private final Display           display;
    @Nonnull
    private final NativeFileFactory nativeFileFactory;
    @Nonnull
    private final Libc              libc;

    @Nonnull
    private final Libxkbcommon libxkbcommon;
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

    private boolean consumeNextKeyEvent;

    KeyboardDevice(@Provided @Nonnull final Display display,
                   @Provided @Nonnull final NativeFileFactory nativeFileFactory,
                   @Provided @Nonnull final Libc libc,
                   @Provided @Nonnull final Libxkbcommon libxkbcommon,
                   @Nonnull final Xkb xkb) {
        this.display = display;
        this.nativeFileFactory = nativeFileFactory;
        this.libc = libc;
        this.libxkbcommon = libxkbcommon;
        this.xkb = xkb;
    }

    /**
     * Find the keyboard focused surface and deliver a key event to the client of the focused surface.
     *
     * @param wlKeyboardResources A set of all keyboard resources that will be used to find the client.
     * @param key                 the key to deliver
     * @param wlKeyboardKeyState  the state of the key.
     */
    public void key(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources,
                    final int time,
                    final int key,
                    @Nonnull final WlKeyboardKeyState wlKeyboardKeyState) {

        int        stateComponentMask = 0;
        final long xkbState           = getXkb().getState();
        final int  evdevKey           = key + 8;
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

        this.keySignal.emit(Key.create(time,
                                       key,
                                       wlKeyboardKeyState));

        if (this.consumeNextKeyEvent) {
            this.consumeNextKeyEvent = false;
        }
        else {
            doKey(wlKeyboardResources,
                  time,
                  key,
                  wlKeyboardKeyState);

            handleStateComponentMask(wlKeyboardResources,
                                     stateComponentMask);
        }
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
                                                                                                     wlKeyboardKeyState.value)));
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
        //we're not updating the state when updating xkb as that would potentially introduce to much bugs
        this.xkb = xkb;
    }

    public void consumeNextKeyEvent() {
        this.consumeNextKeyEvent = true;
    }

    @Nonnull
    public Signal<Key, Slot<Key>> getKeySignal() {
        return this.keySignal;
    }

    @Nonnull
    public Signal<KeyboardFocus, Slot<KeyboardFocus>> getKeyboardFocusSignal() {
        return this.keyboardFocusSignal;
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
        getKeyboardFocusSignal().emit(KeyboardFocus.create(newFocus));

        oldFocus.ifPresent(oldFocusResource -> {
            oldFocusResource.unregister(this.focusDestroyListener.get());
            this.focusDestroyListener = Optional.empty();

            final WlSurface wlSurface = (WlSurface) oldFocusResource.getImplementation();
            final Surface   surface   = wlSurface.getSurface();

            final Set<WlKeyboardResource> clientKeyboardResources = filter(wlKeyboardResources,
                                                                           oldFocusResource.getClient());
            surface.getKeyboardFocuses()
                   .removeAll(clientKeyboardResources);
            surface.getKeyboardFocusLostSignal()
                   .emit(KeyboardFocusLost.create(clientKeyboardResources));

            clientKeyboardResources.forEach(oldFocusKeyboardResource ->
                                                    oldFocusKeyboardResource.leave(nextKeyboardSerial(),
                                                                                   oldFocusResource));
        });

        newFocus.ifPresent(newFocusResource -> {
            this.focusDestroyListener = Optional.of(() -> updateFocus(wlKeyboardResources,
                                                                      newFocus,
                                                                      Optional.empty()));
            newFocusResource.register(this.focusDestroyListener.get());

            final WlSurface wlSurface = (WlSurface) newFocusResource.getImplementation();
            final Surface   surface   = wlSurface.getSurface();

            final Set<WlKeyboardResource> clientKeyboardResources = filter(wlKeyboardResources,
                                                                           newFocusResource.getClient());
            surface.getKeyboardFocuses()
                   .addAll(clientKeyboardResources);
            surface.getKeyboardFocusGainedSignal()
                   .emit(KeyboardFocusGained.create(clientKeyboardResources));

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

    public void emitKeymap(@Nonnull final Set<WlKeyboardResource> wlKeyboardResources) {
        if (this.keymapFd >= 0) {
            wlKeyboardResources.forEach(wlKeyboardResource ->
                                                wlKeyboardResource.keymap(WlKeyboardKeymapFormat.XKB_V1.value,
                                                                          this.keymapFd,
                                                                          this.keymapSize));
        }
    }

    public void updateKeymap() {
        final String nativeKeyMapping = getXkb().getKeymapString();

        final int size = nativeKeyMapping.length();
        final int fd   = this.nativeFileFactory.createAnonymousFile(size);
        final long keymapArea = this.libc.mmap(0L,
                                               size,
                                               Libc.PROT_READ | Libc.PROT_WRITE,
                                               Libc.MAP_SHARED,
                                               fd,
                                               0);
        if (keymapArea == Libc.MAP_FAILED) {
            this.libc.close(fd);
            throw new Error("MAP_FAILED: " + this.libc.getErrno());
        }

        this.libc.strcpy(keymapArea,
                         Pointer.nref(nativeKeyMapping).address);

        if (this.keymapFd >= 0) {
            this.libc.close(this.keymapFd);
        }
        this.keymapFd = fd;
        this.keymapSize = size;
    }
}
