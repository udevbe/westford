package org.westmalle.wayland.output.jogl;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.util.texture.Texture;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.jogl.JoglRenderEngine;
import org.westmalle.wayland.jogl.JoglSurfaceData;
import org.westmalle.wayland.output.Point;
import org.westmalle.wayland.output.Surface;
import org.westmalle.wayland.protocol.WlSurface;

import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ShmBuffer.class,
                 JoglSurfaceData.class})
public class JoglRenderEngineTest {

    @Mock
    private ListeningExecutorService renderThread;
    @Mock
    private GLContext                glContext;
    @Mock
    private GLDrawable               drawable;
    @Mock
    private IntBuffer                elementBuffer;
    @Mock
    private IntBuffer                vertexBuffer;
    @InjectMocks
    private JoglRenderEngine joglRenderEngine;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(ShmBuffer.class);
    }

    @Test
    public void testBegin() throws Exception {
        //given
        final List<Runnable> queue = new LinkedList<>();
        when(this.renderThread.submit(isA(Runnable.class))).thenAnswer(invocation -> {
            final Object arg0 = invocation.getArguments()[0];
            final Runnable runnable = (Runnable) arg0;
            queue.add(runnable);
            return null;
        });

        when(this.drawable.getSurfaceWidth()).thenReturn(345);
        when(this.drawable.getSurfaceHeight()).thenReturn(567);

        final GL gl = mock(GL.class);
        when(this.glContext.getGL()).thenReturn(gl);

        final GL2ES2 gl2ES2 = mock(GL2ES2.class);
        when(gl.getGL2ES2()).thenReturn(gl2ES2);
        //when
        this.joglRenderEngine.begin(this.drawable);
        //then
        verify(this.renderThread).submit((Runnable) any());
        //and when
        queue.get(0)
             .run();
        //then
        verify(gl2ES2).glClear(anyInt());
    }

    @Test
    public void testDraw() throws Exception {
        //given
        final Texture texture = mock(Texture.class);
        whenNew(Texture.class).withAnyArguments()
                              .thenReturn(texture);

        final List<Runnable> queue = new LinkedList<>();
        when(this.renderThread.submit(isA(Runnable.class))).thenAnswer(invocation -> {
            final Object arg0 = invocation.getArguments()[0];
            final Runnable runnable = (Runnable) arg0;
            queue.add(runnable);
            return null;
        });

        when(this.drawable.getSurfaceWidth()).thenReturn(345);
        when(this.drawable.getSurfaceHeight()).thenReturn(567);

        final GL gl = mock(GL.class);
        when(this.glContext.getGL()).thenReturn(gl);

        final GL2ES2 gl2ES2 = mock(GL2ES2.class);
        when(gl.getGL2ES2()).thenReturn(gl2ES2);

        doAnswer(invocation -> {
            Object arg3 = invocation.getArguments()[2];
            final IntBuffer vstatus = (IntBuffer) arg3;
            vstatus.put(0,
                        GL.GL_TRUE);
            return null;
        }).when(gl2ES2)
          .glGetShaderiv(anyInt(),
                         eq(GL2ES2.GL_COMPILE_STATUS),
                         any());

        final WlSurfaceResource surfaceResource = mock(WlSurfaceResource.class);
        final WlSurface         wlSurface       = mock(WlSurface.class);
        when(surfaceResource.getImplementation()).thenReturn(wlSurface);
        final Surface surface = mock(Surface.class);
        when(wlSurface.getSurface()).thenReturn(surface);
        final Point position = Point.create(-10,
                                            45);
        when(surface.getPosition()).thenReturn(position);

        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);

        final ShmBuffer shmBuffer = mock(ShmBuffer.class);
        when(ShmBuffer.get(wlBufferResource)).thenReturn(shmBuffer);

        when(shmBuffer.getWidth()).thenReturn(100);
        when(shmBuffer.getHeight()).thenReturn(250);

        this.joglRenderEngine.begin(this.drawable);
        queue.get(0)
             .run();
        //when
        this.joglRenderEngine.draw(surfaceResource,
                                 wlBufferResource);
        //then
        verify(this.renderThread,
               times(2)).submit((Runnable) any());
        //and when
        queue.get(1)
             .run();
        //then
        verify(gl2ES2).glLinkProgram(anyInt());
        verify(gl2ES2).glDrawElements(anyInt(),
                                      anyInt(),
                                      anyInt(),
                                      anyLong());
    }

    @Test
    public void testEnd() throws Exception {
        //given

        final List<Runnable> queue = new LinkedList<>();
        when(this.renderThread.submit(isA(Runnable.class))).thenAnswer(invocation -> {
            final Object arg0 = invocation.getArguments()[0];
            final Runnable runnable = (Runnable) arg0;
            queue.add(runnable);
            return null;
        });

        when(this.drawable.getSurfaceWidth()).thenReturn(345);
        when(this.drawable.getSurfaceHeight()).thenReturn(567);

        final GL gl = mock(GL.class);
        when(this.glContext.getGL()).thenReturn(gl);

        final GL2ES2 gl2ES2 = mock(GL2ES2.class);
        when(gl.getGL2ES2()).thenReturn(gl2ES2);
        //when
        this.joglRenderEngine.end(this.drawable);
        //then
        verify(this.renderThread).submit((Runnable) any());
        //and when
        queue.get(0)
             .run();
        //then
        verify(this.drawable).swapBuffers();
    }
}