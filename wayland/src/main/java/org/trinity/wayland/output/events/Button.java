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
package org.trinity.wayland.output.events;

import org.freedesktop.wayland.shared.WlPointerButtonState;

public class Button {
    private final int                  time;
    private final int                  button;
    private final WlPointerButtonState buttonState;

    public Button(final int time,
                  final int button,
                  final WlPointerButtonState buttonState) {
        this.time = time;
        this.button = button;
        this.buttonState = buttonState;
    }

    public int getButton() {
        return this.button;
    }

    public WlPointerButtonState getButtonState() {
        return this.buttonState;
    }

    public int getTime() {
        return this.time;
    }
}
