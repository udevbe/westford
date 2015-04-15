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
package org.westmalle.wayland.platform.newt;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import org.freedesktop.wayland.shared.WlPointerButtonState;
import org.westmalle.wayland.output.JobExecutor;
import org.westmalle.wayland.protocol.WlSeat;

import javax.annotation.Nonnull;

public class GLWindowSeat implements MouseListener, KeyListener {

    @Nonnull
    private final WlSeat      wlSeat;
    @Nonnull
    private final JobExecutor jobExecutor;

    GLWindowSeat(@Nonnull final WlSeat wlSeat,
                 @Nonnull final JobExecutor jobExecutor) {
        this.wlSeat = wlSeat;
        this.jobExecutor = jobExecutor;
    }

    @Override
    public void mouseClicked(final MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(final MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(final MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(final MouseEvent mouseEvent) {

        final long  time   = mouseEvent.getWhen();
        final short button = mouseEvent.getButton();

        this.wlSeat.getOptionalWlPointer()
                   .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                                                                                  .button(wlPointer.getResources(),
                                                                                          (int) time,
                                                                                          button,
                                                                                          WlPointerButtonState.PRESSED)));
    }

    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
        final long  time   = mouseEvent.getWhen();
        final short button = mouseEvent.getButton();

        this.wlSeat.getOptionalWlPointer()
                   .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                                                                                  .button(wlPointer.getResources(),
                                                                                          (int) time,
                                                                                          button,
                                                                                          WlPointerButtonState.RELEASED)));
    }

    @Override
    public void mouseMoved(final MouseEvent mouseEvent) {

        final long time = mouseEvent.getWhen();
        final int  x    = mouseEvent.getX();
        final int  y    = mouseEvent.getY();

        this.wlSeat.getOptionalWlPointer()
                   .ifPresent(wlPointer -> this.jobExecutor.submit(() -> wlPointer.getPointerDevice()
                                                                                  .motion(wlPointer.getResources(),
                                                                                          (int) time,
                                                                                          x,
                                                                                          y)));

    }


    @Override
    public void mouseDragged(final MouseEvent mouseEvent) {
        mouseMoved(mouseEvent);
    }

    @Override
    public void mouseWheelMoved(final MouseEvent mouseEvent) {

    }

    @Override
    public void keyPressed(final KeyEvent keyEvent) {

    }

    @Override
    public void keyReleased(final KeyEvent keyEvent) {

    }
}
