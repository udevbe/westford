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
package org.trinity.wayland.output;

import com.google.auto.factory.AutoFactory;
import com.google.common.eventbus.EventBus;

@AutoFactory(className = "KeyboardFactory")
public class Keyboard {

    private final EventBus inputBus = new EventBus();

    Keyboard() {
    }

    public void register(final Object listener) {
        this.inputBus.register(listener);
    }

    public void unregister(final Object listener) {
        this.inputBus.unregister(listener);
    }
}
