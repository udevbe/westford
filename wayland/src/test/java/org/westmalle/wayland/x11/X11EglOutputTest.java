package org.westmalle.wayland.x11;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.nativ.libEGL.LibEGL;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class X11EglOutputTest {

    @Mock
    private LibEGL  libEGL;

    @Test
    public void testBegin() throws Exception {
        //given
        final long eglDisplay = 12345;
        final long eglSurface = 67890;
        final long eglContext = 11235;

        final X11EglOutput x11EglOutput = new X11EglOutput(this.libEGL,
                                                           eglDisplay,
                                                           eglSurface,
                                                           eglContext);
        //when
        x11EglOutput.begin();
        //then
        verify(this.libEGL).eglMakeCurrent(eglDisplay,
                                           eglSurface,
                                           eglSurface,
                                           eglContext);
    }

    @Test
    public void testEnd() throws Exception {
        //given
        final long eglDisplay = 12345;
        final long eglSurface = 67890;
        final long eglContext = 11235;

        final X11EglOutput x11EglOutput = new X11EglOutput(this.libEGL,
                                                           eglDisplay,
                                                           eglSurface,
                                                           eglContext);
        //when
        x11EglOutput.end();
        //then
        verify(this.libEGL).eglSwapBuffers(eglDisplay,
                                           eglSurface);
    }
}