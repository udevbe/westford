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
import com.sun.jna.Pointer;
import org.westmalle.wayland.nativ.libc.Libc;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon;

import javax.annotation.Nonnull;

import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_KEYMAP_FORMAT_TEXT_V1;

@AutoFactory(className = "XkbFactory",
             allowSubclasses = true)
public class Xkb {

    @Nonnull
    private final Libc         libc;
    @Nonnull
    private final Libxkbcommon libxkbcommon;

    @Nonnull
    private final Pointer xkbContext;
    @Nonnull
    private final Pointer xkbState;
    @Nonnull
    private final Pointer xkbKeymap;

    Xkb(@Provided @Nonnull final Libc libc,
        @Provided @Nonnull final Libxkbcommon libxkbcommon,
        @Nonnull final Pointer xkbContext,
        @Nonnull final Pointer xkbState,
        @Nonnull final Pointer xkbKeymap) {
        this.libc = libc;
        this.libxkbcommon = libxkbcommon;
        this.xkbContext = xkbContext;
        this.xkbState = xkbState;
        this.xkbKeymap = xkbKeymap;
    }

    public String getKeymapString() {
        final Pointer keymapStringPointer = this.libxkbcommon.xkb_keymap_get_as_string(this.xkbKeymap,
                                                                                       XKB_KEYMAP_FORMAT_TEXT_V1);
        if (keymapStringPointer == null) {
            throw new RuntimeException("Got an error while trying to get keymap as string. " +
                                       "Unfortunately the docs of the xkb library do not specify how we to get more information " +
                                       "about the error, so you'll have to do it with this lousy exception.");
        }

        final String keymapString = keymapStringPointer.getString(0);
        this.libc.free(keymapStringPointer);

        return keymapString;
    }

    @Override
    protected void finalize() throws Throwable {
        this.libxkbcommon.xkb_context_unref(getContext());
        this.libxkbcommon.xkb_keymap_unref(getKeymap());
        this.libxkbcommon.xkb_state_unref(getState());
        super.finalize();
    }

    @Nonnull
    public Pointer getContext() {
        return this.xkbContext;
    }

    @Nonnull
    public Pointer getKeymap() {
        return this.xkbKeymap;
    }

    @Nonnull
    public Pointer getState() {
        return this.xkbState;
    }
}
