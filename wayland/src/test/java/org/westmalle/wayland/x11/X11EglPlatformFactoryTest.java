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
package org.westmalle.wayland.x11;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Size;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.core.GlRenderer;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.nativ.libEGL.EglCreatePlatformWindowSurfaceEXT;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libEGL.PointerEglCreatePlatformWindowSurfaceEXT;
import org.westmalle.wayland.nativ.libEGL.PointerEglGetPlatformDisplayEXT;
import org.westmalle.wayland.x11.egl.PrivateX11EglPlatformFactory;
import org.westmalle.wayland.x11.egl.X11EglConnectorFactory;
import org.westmalle.wayland.x11.egl.X11EglPlatformFactory;

import javax.annotation.Nonnull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CLIENT_APIS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_EXTENSIONS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VENDOR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VERSION;

@RunWith(MockitoJUnitRunner.class)
public class X11EglPlatformFactoryTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private LibEGL                       libEGL;
    @Mock
    private PrivateX11EglPlatformFactory privateX11EglPlatformFactory;
    @Mock
    private X11Platform                  x11Platform;
    @Mock
    private GlRenderer                   glRenderer;
    @Mock
    private X11EglConnectorFactory       x11EglConnectorFactory;
    @InjectMocks
    private X11EglPlatformFactory        x11EglPlatformFactory;

    @Before
    public void setUp() {
        final X11Connector                 x11Connector  = mock(X11Connector.class);
        final List<Optional<X11Connector>> x11Connectors = new LinkedList<>();
        x11Connectors.add(Optional.of(x11Connector));
        when(this.x11Platform.getConnectors()).thenReturn(x11Connectors);
    }

    @Test
    public void testCreateNoEglExtPlatformX11() {
        //given
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage("Required extension EGL_EXT_platform_x11 not available.");

        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_foobar");
        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
        //when
        this.x11EglPlatformFactory.create();
        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
                                           EGL_EXTENSIONS);
        //runtime exception is thrown
        verifyNoMoreInteractions(this.libEGL);
    }

    @Test
    public void testCreateNoEglDisplay() throws Exception {
        //given
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage("eglGetDisplay() failed");

        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
            final long func_name = (Long) invocation.getArguments()[0];
            final String funcName = Pointer.wrap(String.class,
                                                 func_name)
                                           .dref();
            if (funcName.equals("eglGetPlatformDisplayEXT")) {
                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> 0).address;
            }
            return 0L;
        });
        //when
        this.x11EglPlatformFactory.create();
        //then
        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
                                           EGL_EXTENSIONS);

        //runtime exception is thrown
        verifyNoMoreInteractions(this.libEGL);
    }

    @Test
    public void testCreateFailedInit() throws Exception {
        //given
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage("eglInitialize() failed");

        final long eglDisplay = 9768426;

        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
            final long func_name = (Long) invocation.getArguments()[0];
            final String funcName = Pointer.wrap(String.class,
                                                 func_name)
                                           .dref();
            if (funcName.equals("eglGetPlatformDisplayEXT")) {
                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
            }
            return 0L;
        });
        when(this.libEGL.eglInitialize(eglDisplay,
                                       0,
                                       0)).thenReturn(0);

        //when
        this.x11EglPlatformFactory.create();
        //then
        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
                                           EGL_EXTENSIONS);
        verify(this.libEGL).eglInitialize(eglDisplay,
                                          0,
                                          0);
        //runtime exception is thrown
        verifyNoMoreInteractions(this.libEGL);
    }

    @Test
    public void testCreateFailedContextCreation() throws Exception {
        //given
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage("eglCreateContext() failed");

        final long eglDisplay    = 9768426;
        final long eglClientApis = Pointer.nref("mock egl client apis").address;
        final long eglVendor     = Pointer.nref("mock egl vendor").address;
        final long eglVersion    = Pointer.nref("mock egl version").address;
        final long config        = 2486;

        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
            final long func_name = (Long) invocation.getArguments()[0];
            final String funcName = Pointer.wrap(String.class,
                                                 func_name)
                                           .dref();
            if (funcName.equals("eglGetPlatformDisplayEXT")) {
                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
            }
            return 0L;
        });
        when(this.libEGL.eglInitialize(eglDisplay,
                                       0,
                                       0)).thenReturn(1);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_VENDOR)).thenReturn(eglVendor);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_VERSION)).thenReturn(eglVersion);
        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
                                         anyLong(),
                                         anyLong(),
                                         anyInt(),
                                         anyLong())).thenAnswer(invocation -> {
            final long configs = (Long) invocation.getArguments()[2];
            Pointer.wrap(Pointer.class,
                         configs)
                   .write(Pointer.wrap(config));

            final long num_configs = (Long) invocation.getArguments()[4];
            Pointer.wrap(Integer.class,
                         num_configs)
                   .write(1);
            return 1;
        });
        when(this.libEGL.eglCreateContext(eq(eglDisplay),
                                          eq(config),
                                          eq(EGL_NO_CONTEXT),
                                          anyLong())).thenReturn(0L);

        //when
        this.x11EglPlatformFactory.create();
        //then
        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
                                           EGL_EXTENSIONS);
        verify(this.libEGL).eglInitialize(eglDisplay,
                                          0,
                                          0);
        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
                                            anyLong(),
                                            anyLong(),
                                            anyInt(),
                                            anyLong());
        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
                                             eq(config),
                                             eq(EGL_NO_CONTEXT),
                                             anyLong());
        //runtime exception is thrown
        verifyNoMoreInteractions(this.libEGL);
    }

    @Test
    public void testCreateFailedSurfaceCreation() throws Exception {
        //given
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage("eglCreateWindowSurface() failed");

        final long eglDisplay    = 9768426;
        final long eglClientApis = Pointer.nref("mock egl client apis").address;
        final long eglVendor     = Pointer.nref("mock egl vendor").address;
        final long eglVersion    = Pointer.nref("mock egl version").address;
        final long config        = 2486;
        final long context       = 6842;

        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
        final Pointer<String> eglExtensions = Pointer.nref("dummy extensions");
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_EXTENSIONS)).thenReturn(eglExtensions.address);

        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
            final long func_name = (Long) invocation.getArguments()[0];
            final String funcName = Pointer.wrap(String.class,
                                                 func_name)
                                           .dref();
            if (funcName.equals("eglGetPlatformDisplayEXT")) {
                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
            }
            else if (funcName.equals("eglCreatePlatformWindowSurfaceEXT")) {
                return PointerEglCreatePlatformWindowSurfaceEXT.nref((dpy, config1, native_window, attrib_list) -> 0L).address;
            }
            return 0L;
        });

        when(this.libEGL.eglInitialize(eglDisplay,
                                       0,
                                       0)).thenReturn(1);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_VENDOR)).thenReturn(eglVendor);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_VERSION)).thenReturn(eglVersion);
        when(this.glRenderer.eglConfig(eq(eglDisplay),
                                       eq("dummy extensions"))).thenReturn(config);
        when(this.libEGL.eglCreateContext(eq(eglDisplay),
                                          eq(config),
                                          eq(EGL_NO_CONTEXT),
                                          anyLong())).thenReturn(context);
        //when
        this.x11EglPlatformFactory.create();
        //then
        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
                                           EGL_EXTENSIONS);
        verify(this.libEGL).eglInitialize(eglDisplay,
                                          0,
                                          0);
        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
                                            anyLong(),
                                            anyLong(),
                                            anyInt(),
                                            anyLong());
        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
                                             eq(config),
                                             eq(EGL_NO_CONTEXT),
                                             anyLong());
        //runtime exception is thrown
        verifyNoMoreInteractions(this.libEGL);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final long eglDisplay    = 9768426;
        final long eglClientApis = Pointer.nref("mock egl client apis").address;
        final long eglVendor     = Pointer.nref("mock egl vendor").address;
        final long eglVersion    = Pointer.nref("mock egl version").address;
        final long config        = 2486;
        final long context       = 6842;
        final long eglSurface    = 847453;

        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);

        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
        final Pointer<String> eglDisplayExtensions = Pointer.nref("mock_extension");
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_EXTENSIONS)).thenReturn(eglDisplayExtensions.address);

        final EglCreatePlatformWindowSurfaceEXT eglCreatePlatformWindowSurfaceEXT = mock(EglCreatePlatformWindowSurfaceEXT.class);
        when(eglCreatePlatformWindowSurfaceEXT.$(eq(eglDisplay),
                                                 eq(config),
                                                 anyLong(),
                                                 anyLong())).thenReturn(eglSurface);

        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
            final long func_name = (Long) invocation.getArguments()[0];
            final String funcName = Pointer.wrap(String.class,
                                                 func_name)
                                           .dref();
            if (funcName.equals("eglGetPlatformDisplayEXT")) {
                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
            }
            else if (funcName.equals("eglCreatePlatformWindowSurfaceEXT")) {
                return PointerEglCreatePlatformWindowSurfaceEXT.nref(eglCreatePlatformWindowSurfaceEXT).address;
            }
            return 0L;
        });

        when(this.libEGL.eglInitialize(eglDisplay,
                                       0,
                                       0)).thenReturn(1);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_VENDOR)).thenReturn(eglVendor);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_VERSION)).thenReturn(eglVersion);

        when(this.glRenderer.eglConfig(eq(eglDisplay),
                                       eq("mock_extension"))).thenReturn(config);

        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
                                         anyLong(),
                                         anyLong(),
                                         anyInt(),
                                         anyLong())).thenAnswer(invocation -> {
            final long configs = (Long) invocation.getArguments()[2];
            Pointer.wrap(Pointer.class,
                         configs)
                   .write(Pointer.wrap(config));

            final long num_configs = (Long) invocation.getArguments()[4];
            Pointer.wrap(Integer.class,
                         num_configs)
                   .write(1);
            return 1;
        });
        when(this.libEGL.eglCreateContext(eq(eglDisplay),
                                          eq(config),
                                          eq(EGL_NO_CONTEXT),
                                          anyLong())).thenReturn(context);

        //when
        this.x11EglPlatformFactory.create();
        //then
        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
                                           EGL_EXTENSIONS);
        verify(this.libEGL).eglInitialize(eglDisplay,
                                          0L,
                                          0L);
        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
                                             eq(config),
                                             eq(EGL_NO_CONTEXT),
                                             anyLong());
        verify(eglCreatePlatformWindowSurfaceEXT).$(eq(eglDisplay),
                                                    eq(config),
                                                    anyLong(),
                                                    anyLong());
    }
}