package org.westmalle.wayland.x11;

import com.sun.jna.Pointer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.egl.EglOutput;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class X11OutputTest {

    private final int xWindow = 12345;
    @Mock
    private X11EglOutputFactory x11EglOutputFactory;
    @Mock
    private X11EventBus         x11EventBus;
    @Mock
    private Pointer             xcbConnection;
    @Mock
    private Pointer              xDisplay;
    @Mock
    private Map<String, Integer> atoms;

    @Test
    public void testGetEglOutput() throws Exception {
        //given
        final X11Output x11Output = new X11Output(this.x11EglOutputFactory,
                                                  this.x11EventBus,
                                                  this.xcbConnection,
                                                  this.xDisplay,
                                                  this.xWindow,
                                                  this.atoms);
        //when
        x11Output.getEglOutput();
        //then
        verify(this.x11EglOutputFactory).create(this.xDisplay,
                                                this.xWindow);
    }

    @Test
    public void testGetEglOutputInstance() throws Exception {
        //given
        final X11Output x11Output = new X11Output(this.x11EglOutputFactory,
                                                  this.x11EventBus,
                                                  this.xcbConnection,
                                                  this.xDisplay,
                                                  this.xWindow,
                                                  this.atoms);
        //when
        final EglOutput eglOutput0 = x11Output.getEglOutput();
        final EglOutput eglOutput1 = x11Output.getEglOutput();
        //then
        assertThat(eglOutput0).isSameAs(eglOutput1);
    }
}