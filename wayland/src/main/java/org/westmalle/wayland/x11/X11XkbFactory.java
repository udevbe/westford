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
package org.westmalle.wayland.x11;

import com.sun.jna.Pointer;
import org.westmalle.wayland.core.Xkb;
import org.westmalle.wayland.core.XkbFactory;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon;
import org.westmalle.wayland.nativ.libxkbcommonx11.Libxkbcommonx11;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_CONTEXT_NO_FLAGS;
import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_KEYMAP_COMPILE_NO_FLAGS;

public class X11XkbFactory {

    @Nonnull
    private final Libxkbcommon    libxkbcommon;
    @Nonnull
    private final XkbFactory      xkbFactory;
    @Nonnull
    private final Libxkbcommonx11 libxkbcommonx11;

    @Inject
    X11XkbFactory(@Nonnull final Libxkbcommon libxkbcommon,
                  @Nonnull final XkbFactory xkbFactory,
                  @Nonnull final Libxkbcommonx11 libxkbcommonx11) {
        this.libxkbcommon = libxkbcommon;
        this.xkbFactory = xkbFactory;
        this.libxkbcommonx11 = libxkbcommonx11;
    }

    public Xkb create(final Pointer xcbConnection) {

        final Pointer xkbContext = this.libxkbcommon.xkb_context_new(XKB_CONTEXT_NO_FLAGS);
        if (xkbContext == null) {
            throw new RuntimeException("Got an error while trying to create xkb context. " +
                                       "Unfortunately the docs of the xkb library do not specify how we to get more information " +
                                       "about the error, so you'll have to do it with this lousy exception.");
        }

        final int device_id = this.libxkbcommonx11.xkb_x11_get_core_keyboard_device_id(xcbConnection);
        if (device_id == -1) {
            throw new RuntimeException("Got an error while trying to fetch keyboard device id from X11 backend. " +
                                       "Unfortunately the docs of the xkb library do not specify how we to get more information " +
                                       "about the error, so you'll have to do it with this lousy exception.");
        }

        final Pointer keymap = this.libxkbcommonx11.xkb_x11_keymap_new_from_device(xkbContext,
                                                                                   xcbConnection,
                                                                                   device_id,
                                                                                   XKB_KEYMAP_COMPILE_NO_FLAGS);
        if (keymap == null) {
            throw new RuntimeException("Got an error while trying to get x11 keymap. " +
                                       "Unfortunately the docs of the xkb library do not specify how we to get more information " +
                                       "about the error, so you'll have to do it with this lousy exception.");
        }

        final Pointer state = this.libxkbcommonx11.xkb_x11_state_new_from_device(keymap,
                                                                                 xcbConnection,
                                                                                 device_id);

        return this.xkbFactory.create(xkbContext,
                                      state,
                                      keymap);
    }
}
