////Copyright 2015 Erik De Rijcke
////
////Licensed under the Apache License,Version2.0(the"License");
////you may not use this file except in compliance with the License.
////You may obtain a copy of the License at
////
////http://www.apache.org/licenses/LICENSE-2.0
////
////Unless required by applicable law or agreed to in writing,software
////distributed under the License is distributed on an"AS IS"BASIS,
////WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
////See the License for the specific language governing permissions and
////limitations under the License.
//package org.westmalle.wayland.x11;
//
//import org.freedesktop.jaccall.Pointer;
//import org.freedesktop.wayland.server.Display;
//import org.freedesktop.wayland.server.EventLoop;
//import org.freedesktop.wayland.server.EventSource;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.westmalle.wayland.core.Compositor;
//import org.westmalle.wayland.core.OutputFactory;
//import org.westmalle.wayland.core.events.Signal;
//import org.westmalle.wayland.core.events.Slot;
//import org.westmalle.wayland.nativ.libX11.LibX11;
//import org.westmalle.wayland.nativ.libX11xcb.LibX11xcb;
//import org.westmalle.wayland.nativ.libc.Libc;
//import org.westmalle.wayland.nativ.libxcb.Libxcb;
//import org.westmalle.wayland.nativ.libxcb.xcb_generic_event_t;
//import org.westmalle.wayland.nativ.libxcb.xcb_intern_atom_reply_t;
//import org.westmalle.wayland.nativ.libxcb.xcb_screen_iterator_t;
//import org.westmalle.wayland.nativ.libxcb.xcb_screen_t;
//import org.westmalle.wayland.protocol.WlOutput;
//import org.westmalle.wayland.protocol.WlOutputFactory;
//
//import java.util.LinkedList;
//
//import static com.google.common.truth.Truth.assertThat;
//import static org.freedesktop.jaccall.Pointer.malloc;
//import static org.freedesktop.jaccall.Pointer.ref;
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyByte;
//import static org.mockito.Matchers.anyInt;
//import static org.mockito.Matchers.anyLong;
//import static org.mockito.Matchers.anyShort;
//import static org.mockito.Matchers.eq;
//import static org.mockito.Mockito.atLeastOnce;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoMoreInteractions;
//import static org.mockito.Mockito.when;
//import static org.powermock.api.mockito.PowerMockito.mock;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({WlOutputFactory.class,
//                 OutputFactory.class,
//                 X11EventBusFactory.class,
//                 EventSource.class})
//public class X11PlatformFactoryTest {
//
//    @Rule
//    public ExpectedException exception = ExpectedException.none();
//
//    @Mock
//    private Display                 display;
//    @Mock
//    private Libc                    libc;
//    @Mock
//    private LibX11                  libX11;
//    @Mock
//    private Libxcb                  libxcb;
//    @Mock
//    private PrivateX11OutputFactory privateX11OutputFactory;
//    @Mock
//    private LibX11xcb               libX11xcb;
//    @Mock
//    private X11EglPlatformFactory   x11EglPlatformFactory;
//    @Mock
//    private WlOutputFactory         wlOutputFactory;
//    @Mock
//    private OutputFactory           platformFactory;
//    @Mock
//    private X11EventBusFactory      x11EventBusFactory;
//    @Mock
//    private Compositor              compositor;
//    @InjectMocks
//    private X11PlatformFactory      x11PlatformFactory;
//
//    @Test
//    public void testCreateOpenDisplayFailed() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("XOpenDisplay() failed: :0");
//
//        final String xDisplayName = ":0";
//        final int    width        = 640;
//        final int    height       = 480;
//
//        when(this.libX11.XOpenDisplay(anyLong())).thenReturn(0L);
//
//        //when
//        this.x11PlatformFactory.create(xDisplayName,
//                                       width,
//                                       height);
//        //then
//        verify(this.libX11).XOpenDisplay(anyLong());
//        //an exception is thrown
//        verifyNoMoreInteractions(this.libX11);
//    }
//
//    @Test
//    public void testCreateConnectionHasError() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("error occurred while connecting to X server");
//
//        final String xDisplayName  = ":0";
//        final int    width         = 640;
//        final int    height        = 480;
//        final long   xDisplay      = 1234567;
//        final long   xcbConnection = 112358;
//
//        when(this.libX11.XOpenDisplay(anyLong())).thenReturn(xDisplay);
//        when(this.libX11xcb.XGetXCBConnection(xDisplay)).thenReturn(xcbConnection);
//        when(this.libxcb.xcb_connection_has_error(xcbConnection)).thenReturn(1);
//
//        //when
//        this.x11PlatformFactory.create(xDisplayName,
//                                       width,
//                                       height);
//        //then
//        verify(this.libX11).XOpenDisplay(anyLong());
//        verify(this.libX11xcb).XGetXCBConnection(xDisplay);
//        verify(this.libxcb).xcb_connection_has_error(xcbConnection);
//        //an exception is thrown
//        verifyNoMoreInteractions(this.libX11);
//        verifyNoMoreInteractions(this.libX11xcb);
//        verifyNoMoreInteractions(this.libxcb);
//    }
//
//    @Test
//    public void testCreateWindowIdCreationError() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("failed to generate X window id");
//
//        final String                xDisplayName  = ":0";
//        final int                   width         = 640;
//        final int                   height        = 480;
//        final long                  xDisplay      = 1234567;
//        final long                  xcbConnection = 112358;
//        final long                  setup         = 473289;
//        final xcb_screen_t          screen        = new xcb_screen_t();
//        final xcb_screen_iterator_t screen_iter   = new xcb_screen_iterator_t();
//        screen_iter.data(ref(screen));
//
//        when(this.libX11.XOpenDisplay(anyLong())).thenReturn(xDisplay);
//        when(this.libX11xcb.XGetXCBConnection(xDisplay)).thenReturn(xcbConnection);
//        when(this.libxcb.xcb_connection_has_error(xcbConnection)).thenReturn(0);
//        when(this.libxcb.xcb_get_setup(xcbConnection)).thenReturn(setup);
//        when(this.libxcb.xcb_setup_roots_iterator(setup)).thenReturn(ref(screen_iter).address);
//        when(this.libxcb.xcb_generate_id(xcbConnection)).thenReturn(0);
//
//        //when
//        this.x11PlatformFactory.create(xDisplayName,
//                                       width,
//                                       height);
//        //then
//        verify(this.libX11).XOpenDisplay(anyLong());
//        verify(this.libX11xcb).XGetXCBConnection(xDisplay);
//        verify(this.libxcb).xcb_connection_has_error(xcbConnection);
//        verify(this.libxcb).xcb_get_setup(xcbConnection);
//        verify(this.libxcb).xcb_setup_roots_iterator(setup);
//        verify(this.libxcb).xcb_generate_id(xcbConnection);
//        //an exception is thrown
//        verifyNoMoreInteractions(this.libX11);
//        verifyNoMoreInteractions(this.libX11xcb);
//        verifyNoMoreInteractions(this.libxcb);
//    }
//
//    @Test
//    public void testCreate() throws Exception {
//        //given
//        final LinkedList<WlOutput>                                                     wlOutputs     = mock(LinkedList.class);
//        final EventLoop                                                                eventLoop     = mock(EventLoop.class);
//        final EventSource                                                              eventSource   = mock(EventSource.class);
//        final X11EventBus                                                              x11EventBus   = mock(X11EventBus.class);
//        final Signal<Pointer<xcb_generic_event_t>, Slot<Pointer<xcb_generic_event_t>>> xEventSignal  = mock(Signal.class);
//        final String                                                                   xDisplayName  = ":0";
//        final int                                                                      width         = 640;
//        final int                                                                      height        = 480;
//        final long                                                                     xDisplay      = 1234567;
//        final long                                                                     xcbConnection = 112358;
//        final long                                                                     setup         = 473289;
//        final xcb_screen_t screen = malloc(xcb_screen_t.SIZE,
//                                           xcb_screen_t.class).dref();
//        final int cookie = 95484;
//        final int window = 431;
//        screen.root(127);
//        screen.root_visual(10);
//        screen.width_in_pixels((short) 640);
//        screen.height_in_pixels((short) 480);
//        final xcb_screen_iterator_t screen_iter = malloc(xcb_screen_iterator_t.SIZE,
//                                                         xcb_screen_iterator_t.class).dref();
//        screen_iter.data(ref(screen));
//
//        when(compositor.getPlatforms()).thenReturn(wlOutputs);
//        when(this.libX11.XOpenDisplay(anyLong())).thenReturn(xDisplay);
//        when(this.libX11xcb.XGetXCBConnection(xDisplay)).thenReturn(xcbConnection);
//        when(this.libxcb.xcb_connection_has_error(xcbConnection)).thenReturn(0);
//        when(this.libxcb.xcb_get_setup(xcbConnection)).thenReturn(setup);
//        when(this.libxcb.xcb_setup_roots_iterator(setup)).thenReturn(ref(screen_iter).address);
//        when(this.libxcb.xcb_generate_id(xcbConnection)).thenReturn(window);
//        when(this.display.getEventLoop()).thenReturn(eventLoop);
//        when(this.x11EventBusFactory.create(xcbConnection)).thenReturn(x11EventBus);
//        when(x11EventBus.getXEventSignal()).thenReturn(xEventSignal);
//        when(eventLoop.addFileDescriptor(anyInt(),
//                                         anyInt(),
//                                         any())).thenReturn(eventSource);
//        when(this.libxcb.xcb_intern_atom(eq(xcbConnection),
//                                         anyByte(),
//                                         anyShort(),
//                                         anyLong())).thenReturn(cookie);
//
//        when(this.libxcb.xcb_intern_atom_reply(xcbConnection,
//                                               cookie,
//                                               0L)).thenAnswer(invocation -> malloc(xcb_intern_atom_reply_t.SIZE,
//                                                                                    xcb_intern_atom_reply_t.class).address);
//        //when
//        final WlOutput wlOutput = this.x11PlatformFactory.create(xDisplayName,
//                                                                 width,
//                                                                 height);
//        //then
//        verify(this.libX11).XOpenDisplay(anyLong());
//        verify(this.libX11xcb).XGetXCBConnection(xDisplay);
//        verify(this.libX11xcb).XSetEventQueueOwner(xDisplay,
//                                                   LibX11xcb.XCBOwnsEventQueue);
//        verify(this.libxcb).xcb_connection_has_error(xcbConnection);
//        verify(this.libxcb).xcb_get_setup(xcbConnection);
//        verify(this.libxcb).xcb_setup_roots_iterator(setup);
//        verify(this.libxcb).xcb_generate_id(xcbConnection);
//        verify(this.libxcb).xcb_get_file_descriptor(xcbConnection);
//        verify(this.libxcb,
//               atLeastOnce()).xcb_intern_atom(eq(xcbConnection),
//                                              anyByte(),
//                                              anyShort(),
//                                              anyLong());
//        verify(this.libxcb,
//               atLeastOnce()).xcb_intern_atom_reply(xcbConnection,
//                                                    cookie,
//                                                    0L);
//        verify(this.libxcb,
//               atLeastOnce()).xcb_change_property(eq(xcbConnection),
//                                                  anyByte(),
//                                                  eq(window),
//                                                  anyInt(),
//                                                  anyInt(),
//                                                  anyByte(),
//                                                  anyInt(),
//                                                  anyLong());
//        verify(this.libxcb).xcb_create_window(eq(xcbConnection),
//                                              anyByte(),
//                                              eq(window),
//                                              eq(screen.root()),
//                                              anyShort(),
//                                              anyShort(),
//                                              eq((short) width),
//                                              eq((short) height),
//                                              anyShort(),
//                                              eq((short) Libxcb.XCB_WINDOW_CLASS_INPUT_OUTPUT),
//                                              eq(screen.root_visual()),
//                                              anyInt(),
//                                              anyLong());
//        verify(this.libxcb).xcb_map_window(xcbConnection,
//                                           window);
//        verify(this.libxcb).xcb_flush(xcbConnection);
//        verify(wlOutputs).addLast(wlOutput);
//
//        verifyNoMoreInteractions(this.libX11);
//        verifyNoMoreInteractions(this.libX11xcb);
//        verifyNoMoreInteractions(this.libxcb);
//    }
//}