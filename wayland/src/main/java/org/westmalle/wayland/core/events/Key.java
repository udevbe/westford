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
package org.westmalle.wayland.core.events;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.shared.WlKeyboardKeyState;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoValue
public abstract class Key {
    public static Key create(final int time,
                             @Nonnegative final int key,
                             @Nonnull final WlKeyboardKeyState wlKeyboardKeyState) {
        return new AutoValue_Key(time,
                                 key,
                                 wlKeyboardKeyState);
    }

    public abstract int getTime();

    public abstract int getKey();

    public abstract WlKeyboardKeyState getKeyState();
}
