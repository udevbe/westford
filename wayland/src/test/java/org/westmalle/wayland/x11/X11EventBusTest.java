package org.westmalle.wayland.x11;

import com.google.common.eventbus.Subscribe;
import com.sun.jna.Pointer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.nativ.Libc;
import org.westmalle.wayland.nativ.Libxcb;
import org.westmalle.wayland.nativ.xcb_button_press_event_t;
import org.westmalle.wayland.nativ.xcb_button_release_event_t;
import org.westmalle.wayland.nativ.xcb_client_message_data_t;
import org.westmalle.wayland.nativ.xcb_enter_notify_event_t;
import org.westmalle.wayland.nativ.xcb_expose_event_t;
import org.westmalle.wayland.nativ.xcb_focus_in_event_t;
import org.westmalle.wayland.nativ.xcb_focus_out_event_t;
import org.westmalle.wayland.nativ.xcb_generic_event_t;
import org.westmalle.wayland.nativ.xcb_key_press_event_t;
import org.westmalle.wayland.nativ.xcb_key_release_event_t;
import org.westmalle.wayland.nativ.xcb_leave_notify_event_t;
import org.westmalle.wayland.nativ.xcb_motion_notify_event_t;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.westmalle.wayland.nativ.Libxcb.XCB_BUTTON_PRESS;
import static org.westmalle.wayland.nativ.Libxcb.XCB_BUTTON_RELEASE;
import static org.westmalle.wayland.nativ.Libxcb.XCB_CLIENT_MESSAGE;
import static org.westmalle.wayland.nativ.Libxcb.XCB_ENTER_NOTIFY;
import static org.westmalle.wayland.nativ.Libxcb.XCB_EXPOSE;
import static org.westmalle.wayland.nativ.Libxcb.XCB_FOCUS_IN;
import static org.westmalle.wayland.nativ.Libxcb.XCB_FOCUS_OUT;
import static org.westmalle.wayland.nativ.Libxcb.XCB_KEY_PRESS;
import static org.westmalle.wayland.nativ.Libxcb.XCB_KEY_RELEASE;
import static org.westmalle.wayland.nativ.Libxcb.XCB_LEAVE_NOTIFY;
import static org.westmalle.wayland.nativ.Libxcb.XCB_MOTION_NOTIFY;

@RunWith(MockitoJUnitRunner.class)
public class X11EventBusTest {

    @Mock
    private Libxcb      libxcb;
    @Mock
    private Libc        libc;
    @Mock
    private Pointer     xcbConnection;
    @InjectMocks
    private X11EventBus x11EventBus;

    @Test
    public void testHandle() throws Exception {
        //given
        final int                 fd       = 0;
        final int                 mask     = 0;
        final xcb_generic_event_t keyPress = new xcb_generic_event_t();
        keyPress.response_type = XCB_KEY_PRESS;
        final xcb_generic_event_t keyRelease = new xcb_generic_event_t();
        keyRelease.response_type = XCB_KEY_RELEASE;
        final xcb_generic_event_t buttonPress = new xcb_generic_event_t();
        buttonPress.response_type = XCB_BUTTON_PRESS;
        final xcb_generic_event_t buttonRelease = new xcb_generic_event_t();
        buttonRelease.response_type = XCB_BUTTON_RELEASE;
        final xcb_generic_event_t motionNotify = new xcb_generic_event_t();
        motionNotify.response_type = XCB_MOTION_NOTIFY;
        final xcb_generic_event_t expose = new xcb_generic_event_t();
        expose.response_type = XCB_EXPOSE;
        final xcb_generic_event_t enterNotify = new xcb_generic_event_t();
        enterNotify.response_type = XCB_ENTER_NOTIFY;
        final xcb_generic_event_t leaveNotify = new xcb_generic_event_t();
        leaveNotify.response_type = XCB_LEAVE_NOTIFY;
        final xcb_generic_event_t clientMessage = new xcb_generic_event_t();
        clientMessage.response_type = XCB_CLIENT_MESSAGE;
        final xcb_generic_event_t focusIn = new xcb_generic_event_t();
        focusIn.response_type = XCB_FOCUS_IN;
        final xcb_generic_event_t focusOut = new xcb_generic_event_t();
        focusOut.response_type = XCB_FOCUS_OUT;

        when(this.libxcb.xcb_poll_for_event(this.xcbConnection)).thenReturn(keyPress,
                                                                            keyRelease,
                                                                            buttonPress,
                                                                            buttonRelease,
                                                                            motionNotify,
                                                                            expose,
                                                                            enterNotify,
                                                                            leaveNotify,
                                                                            clientMessage,
                                                                            focusIn,
                                                                            focusOut,
                                                                            null);
        final DummyHandler dummyHandler = new DummyHandler();
        this.x11EventBus.register(dummyHandler);

        //when
        this.x11EventBus.handle(fd,
                                mask);

        //then
        verify(this.libc).free(keyPress.getPointer());
        verify(this.libc).free(keyRelease.getPointer());
        verify(this.libc).free(buttonPress.getPointer());
        verify(this.libc).free(buttonRelease.getPointer());
        verify(this.libc).free(motionNotify.getPointer());
        verify(this.libc).free(expose.getPointer());
        verify(this.libc).free(enterNotify.getPointer());
        verify(this.libc).free(leaveNotify.getPointer());
        verify(this.libc).free(clientMessage.getPointer());
        verify(this.libc).free(focusIn.getPointer());
        verify(this.libc).free(focusOut.getPointer());

        assertThat(dummyHandler.keyPressSeen).isTrue();
        assertThat(dummyHandler.keyReleaseSeen).isTrue();
        assertThat(dummyHandler.buttonPressSeen).isTrue();
        assertThat(dummyHandler.buttonReleaseSeen).isTrue();
        assertThat(dummyHandler.motionSeen).isTrue();
        assertThat(dummyHandler.exposeSeen).isTrue();
        assertThat(dummyHandler.enterNotifySeen).isTrue();
        assertThat(dummyHandler.leaveNotifySeen).isTrue();
        assertThat(dummyHandler.clientMessageSeen).isTrue();
        assertThat(dummyHandler.focusInSeen).isTrue();
        assertThat(dummyHandler.focusOutSeen).isTrue();
    }

    public class DummyHandler {

        boolean keyPressSeen      = false;
        boolean keyReleaseSeen    = false;
        boolean buttonPressSeen   = false;
        boolean buttonReleaseSeen = false;
        boolean motionSeen        = false;
        boolean exposeSeen        = false;
        boolean enterNotifySeen   = false;
        boolean leaveNotifySeen   = false;
        boolean clientMessageSeen = false;
        boolean focusInSeen       = false;
        boolean focusOutSeen      = false;

        @Subscribe
        public void handle(final xcb_key_press_event_t event) {
            this.keyPressSeen = true;
        }

        @Subscribe
        public void handle(final xcb_key_release_event_t event) {
            this.keyReleaseSeen = true;
        }

        @Subscribe
        public void handle(final xcb_button_press_event_t event) {
            this.buttonPressSeen = true;
        }

        @Subscribe
        public void handle(final xcb_button_release_event_t event) {
            this.buttonReleaseSeen = true;
        }

        @Subscribe
        public void handle(final xcb_motion_notify_event_t event) {
            this.motionSeen = true;
        }

        @Subscribe
        public void handle(final xcb_expose_event_t event) {
            this.exposeSeen = true;
        }

        @Subscribe
        public void handle(final xcb_enter_notify_event_t event) {
            this.enterNotifySeen = true;
        }

        @Subscribe
        public void handle(final xcb_leave_notify_event_t event) {
            this.leaveNotifySeen = true;
        }

        @Subscribe
        public void handle(final xcb_client_message_data_t event) {
            this.clientMessageSeen = true;
        }

        @Subscribe
        public void handle(final xcb_focus_in_event_t event) {
            this.focusInSeen = true;
        }

        @Subscribe
        public void handle(final xcb_focus_out_event_t event) {
            this.focusOutSeen = true;
        }
    }
}