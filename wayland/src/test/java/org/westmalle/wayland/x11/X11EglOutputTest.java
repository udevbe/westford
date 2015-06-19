package org.westmalle.wayland.x11;

import com.sun.jna.Pointer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.nativ.LibEGL;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class X11EglOutputTest {

    @Mock
    private LibEGL libEGL;
    @Mock
    private Pointer eglDisplay;
    @Mock
    private Pointer eglSurface;
    @Mock
    private Pointer eglContext;

    @Test
    public void testBegin() throws Exception {
        //given
        final X11EglOutput x11EglOutput = new X11EglOutput(this.libEGL,
                                                           this.eglDisplay,
                                                           this.eglSurface,
                                                           this.eglContext);
        //when
        x11EglOutput.begin();
        //then
        verify(this.libEGL).eglMakeCurrent(this.eglDisplay,
                                           this.eglSurface,
                                           this.eglSurface,
                                           this.eglContext);
    }

    @Test
    public void testEnd() throws Exception {
        //given
        final X11EglOutput x11EglOutput = new X11EglOutput(this.libEGL,
                                                           this.eglDisplay,
                                                           this.eglSurface,
                                                           this.eglContext);
        //when
        x11EglOutput.end();
        //then
        verify(this.libEGL).eglSwapBuffers(this.eglDisplay,
                                           this.eglSurface);
    }
}