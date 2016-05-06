package org.westmalle.wayland.x11;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.core.RenderOutput;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class X11OutputTest {

    private final int xWindow = 12345;
    @Mock
    private X11EglOutputFactory  x11EglOutputFactory;
    @Mock
    private X11EventBus          x11EventBus;
    @Mock
    private Map<String, Integer> atoms;

    @Test
    public void testGetEglOutput() throws Exception {
        //given
        final long xcbConnection = 789326432;
        final long xDisplay      = 483287463;
        final X11Output x11Output = new X11Output(this.x11EglOutputFactory,
                                                  this.x11EventBus,
                                                  xcbConnection,
                                                  xDisplay,
                                                  this.xWindow,
                                                  this.atoms);
        //when
        x11Output.getEglOutput();
        //then
        verify(this.x11EglOutputFactory).create(xDisplay,
                                                this.xWindow);
    }

    @Test
    public void testGetEglOutputInstance() throws Exception {
        //given
        final long xcbConnection = 789326432;
        final long xDisplay      = 483287463;
        final X11Output x11Output = new X11Output(this.x11EglOutputFactory,
                                                  this.x11EventBus,
                                                  xcbConnection,
                                                  xDisplay,
                                                  this.xWindow,
                                                  this.atoms);
        //when
        final RenderOutput renderOutput0 = x11Output.getEglOutput();
        final RenderOutput renderOutput1 = x11Output.getEglOutput();
        //then
        assertThat(renderOutput0).isSameAs(renderOutput1);
    }
}