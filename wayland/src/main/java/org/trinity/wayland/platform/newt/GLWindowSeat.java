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
package org.trinity.wayland.platform.newt;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.trinity.wayland.output.JobExecutor;
import org.trinity.wayland.protocol.WlSeat;

public class GLWindowSeat implements MouseListener, KeyListener {

    private final WlSeat      wlSeat;
    private final JobExecutor jobExecutor;

    GLWindowSeat(final WlSeat wlSeat,
                 final JobExecutor jobExecutor) {
        this.wlSeat = wlSeat;
        this.jobExecutor = jobExecutor;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {

    }

    @Override
    public void mouseEntered(final MouseEvent e) {

    }

    @Override
    public void mouseExited(final MouseEvent e) {

    }

    @Override
    public void mousePressed(final MouseEvent e) {

        final long time = e.getWhen();
        final short button = e.getButton();

        this.wlSeat.getOptionalWlPointer()
                   .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                                                                                  .button(wlPointer.getResources(),
                                                                                          (int) time,
                                                                                          button,
                                                                                          WlPointerButtonState.PRESSED)));
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        final long time = e.getWhen();
        final short button = e.getButton();

        this.wlSeat.getOptionalWlPointer()
                   .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                                                                                  .button(wlPointer.getResources(),
                                                                                          (int) time,
                                                                                          button,
                                                                                          WlPointerButtonState.RELEASED)));
    }

    @Override
    public void mouseMoved(final MouseEvent e) {

        final long time = e.getWhen();
        final int x = e.getX();
        final int y = e.getY();

        this.wlSeat.getOptionalWlPointer()
                   .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                                                                                  .motion(wlPointer.getResources(),
                                                                                          (int) time,
                                                                                          x,
                                                                                          y)));

    }


    @Override
    public void mouseDragged(final MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseWheelMoved(final MouseEvent e) {

    }

    @Override
    public void keyPressed(final KeyEvent e) {

    }

    @Override
    public void keyReleased(final KeyEvent e) {

    }
}
