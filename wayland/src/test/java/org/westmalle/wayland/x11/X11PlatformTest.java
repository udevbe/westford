//package org.westmalle.wayland.x11;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.westmalle.wayland.core.Platform;
//
//import java.util.Map;
//
//import static com.google.common.truth.Truth.assertThat;
//import static org.mockito.Mockito.verify;
//
//@RunWith(MockitoJUnitRunner.class)
//public class X11PlatformTest {
//
//    private final int xWindow = 12345;
//    @Mock
//    private X11EglPlatformFactory x11EglPlatformFactory;
//    @Mock
//    private X11EventBus           x11EventBus;
//    @Mock
//    private Map<String, Integer>  atoms;
//
//    @Test
//    public void testGetEglOutput() throws Exception {
//        //given
//        final long xcbConnection = 789326432;
//        final long xDisplay      = 483287463;
//        final X11Platform x11Platform = new X11Platform(this.x11EglPlatformFactory,
//                                                        this.x11EventBus,
//                                                        xcbConnection,
//                                                        xDisplay,
//                                                        this.xWindow,
//                                                        this.atoms);
//        //when
//        x11Platform.getEglOutput();
//        //then
//        verify(this.x11EglPlatformFactory).create(xDisplay,
//                                                  this.xWindow);
//    }
//
//    @Test
//    public void testGetEglOutputInstance() throws Exception {
//        //given
//        final long xcbConnection = 789326432;
//        final long xDisplay      = 483287463;
//        final X11Platform x11Platform = new X11Platform(this.x11EglPlatformFactory,
//                                                        this.x11EventBus,
//                                                        xcbConnection,
//                                                        xDisplay,
//                                                        this.xWindow,
//                                                        this.atoms);
//        //when
//        final Platform platform0 = x11Platform.getEglOutput();
//        final Platform platform1 = x11Platform.getEglOutput();
//        //then
//        assertThat(platform0).isSameAs(platform1);
//    }
//}