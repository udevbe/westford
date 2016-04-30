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

import org.freedesktop.jaccall.Pointer;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon;

import javax.annotation.Nonnull;

import static org.westmalle.wayland.nativ.libxkbcommon.Libxkbcommon.XKB_KEYMAP_FORMAT_TEXT_V1;

@AutoFactory(className = "XkbFactory",
             allowSubclasses = true)
public class Xkb {

    @Nonnull
    private final Libxkbcommon libxkbcommon;

    private final long xkbContext;
    private final long xkbState;
    private final long xkbKeymap;

    Xkb(@Provided @Nonnull final Libxkbcommon libxkbcommon,
        final long xkbContext,
        final long xkbState,
        final long xkbKeymap) {
        this.libxkbcommon = libxkbcommon;
        this.xkbContext = xkbContext;
        this.xkbState = xkbState;
        this.xkbKeymap = xkbKeymap;
    }

    public String getKeymapString() {
        try (final Pointer<String> keymapStringPointer = Pointer.wrap(String.class,
                                                                      this.libxkbcommon.xkb_keymap_get_as_string(this.xkbKeymap,
                                                                                                                 XKB_KEYMAP_FORMAT_TEXT_V1))) {
            if (keymapStringPointer.address == 0L) {
                throw new RuntimeException("Got an error while trying to get keymap as string. " +
                                           "Unfortunately the docs of the xkb library do not specify how we to get more information " +
                                           "about the error, so you'll have to do it with this lousy exception.");
            }
            return keymapStringPointer.dref();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.libxkbcommon.xkb_context_unref(getContext());
        this.libxkbcommon.xkb_keymap_unref(getKeymap());
        this.libxkbcommon.xkb_state_unref(getState());
        super.finalize();
    }

    public long getContext() {
        return this.xkbContext;
    }

    public long getKeymap() {
        return this.xkbKeymap;
    }

    public long getState() {
        return this.xkbState;
    }
}
