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

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class Signal<U, T extends Slot<U>> {

    private final Set<T> slots = new HashSet<>();

    public void connect(@Nonnull final T slot) {
        this.slots.add(slot);
    }

    public void disconnect(@Nonnull final T slot) {
        this.slots.remove(slot);
    }

    public void emit(@Nonnull final U event) {
        new HashSet<>(this.slots).forEach(slot -> slot.handle(event));
    }

    public boolean isConnected(@Nonnull final T slot) {
        return this.slots.contains(slot);
    }
}
