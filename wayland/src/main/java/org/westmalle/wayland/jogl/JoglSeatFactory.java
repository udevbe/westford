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
package org.westmalle.wayland.jogl;

import com.jogamp.newt.opengl.GLWindow;
import org.westmalle.wayland.output.Compositor;
import org.westmalle.wayland.output.JobExecutor;
import org.westmalle.wayland.output.KeyboardFactory;
import org.westmalle.wayland.output.PointerDeviceFactory;
import org.westmalle.wayland.protocol.WlKeyboardFactory;
import org.westmalle.wayland.protocol.WlPointerFactory;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class JoglSeatFactory {
    @Nonnull
    private final JobExecutor          jobExecutor;
    @Nonnull
    private final WlPointerFactory     wlPointerFactory;
    @Nonnull
    private final WlKeyboardFactory    wlKeyboardFactory;
    @Nonnull
    private final PointerDeviceFactory pointerDeviceFactory;
    @Nonnull
    private final KeyboardFactory      keyboardFactory;

    @Inject
    JoglSeatFactory(@Nonnull final JobExecutor jobExecutor,
                    @Nonnull final WlPointerFactory wlPointerFactory,
                    @Nonnull final WlKeyboardFactory wlKeyboardFactory,
                    @Nonnull final PointerDeviceFactory pointerDeviceFactory,
                    @Nonnull final KeyboardFactory keyboardFactory) {
        this.jobExecutor = jobExecutor;
        this.wlPointerFactory = wlPointerFactory;
        this.wlKeyboardFactory = wlKeyboardFactory;
        this.pointerDeviceFactory = pointerDeviceFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Nonnull
    public JoglSeat create(@Nonnull final GLWindow glWindow,
                               @Nonnull final WlSeat wlSeat,
                               @Nonnull final Compositor compositor) {
        //this objects will post input events from the system to our wayland compositor system
        final JoglSeat joglSeat = new JoglSeat(wlSeat,
                                                           this.jobExecutor);
        //FIXME for now we put these here, these should be handled dynamically when a mouse or keyboard is
        //added or removed
        //enable pointer and keyboard for wlseat
        wlSeat.setWlPointer(this.wlPointerFactory.create(this.pointerDeviceFactory.create(compositor)));
        wlSeat.setWlKeyboard(this.wlKeyboardFactory.create(this.keyboardFactory.create()));

        glWindow.addMouseListener(joglSeat);
        glWindow.addKeyListener(joglSeat);
        return joglSeat;
    }
}