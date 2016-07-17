//Copyright 2016 Erik De Rijcke
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


import org.westmalle.wayland.core.events.RenderBegin;
import org.westmalle.wayland.core.events.RenderEndAfterSwap;
import org.westmalle.wayland.core.events.RenderEndBeforeSwap;
import org.westmalle.wayland.core.events.Signal;
import org.westmalle.wayland.core.events.Slot;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface Connector {

    void accept(@Nonnull Renderer renderer);

    @Nonnull
    WlOutput getWlOutput();

    default void renderBegin() {
        getRenderBeginSignal().emit(RenderBegin.create(System.nanoTime()));
    }

    default void renderEndBeforeSwap() {
        getRenderEndBeforeSwapSignal().emit(RenderEndBeforeSwap.create(System.nanoTime()));
    }

    default void renderEndAfterSwap() {
        getRenderEndAfterSwapSignal().emit(RenderEndAfterSwap.create(System.nanoTime()));
    }

    Signal<RenderBegin, Slot<RenderBegin>> getRenderBeginSignal();

    Signal<RenderEndBeforeSwap, Slot<RenderEndBeforeSwap>> getRenderEndBeforeSwapSignal();

    Signal<RenderEndAfterSwap, Slot<RenderEndAfterSwap>> getRenderEndAfterSwapSignal();
}
