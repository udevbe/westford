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

import com.github.zubnix.jaccall.Pointer;
import com.github.zubnix.jaccall.Size;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libEGL.PointerEglGetPlatformDisplayEXT;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CLIENT_APIS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_EXTENSIONS;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VENDOR;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VERSION;

@RunWith(MockitoJUnitRunner.class)
public class X11EglOutputFactoryTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private LibEGL                     libEGL;
    @Mock
    private PrivateX11EglOutputFactory privateX11EglOutputFactory;
    @InjectMocks
    private X11EglOutputFactory        x11EglOutputFactory;

    @Test
    public void testCreateNoEsAPi() throws Exception {
        //given
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage("eglBindAPI failed");

        final long display = 123456;
        final int  window  = 12345;
        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(0);
        when(this.libEGL.eglBindAPI(EGL_OPENGL_API)).thenReturn(1);
        //when
        this.x11EglOutputFactory.create(display,
                                        window);
        //then
        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
        //runtime exception is thrown
        verifyNoMoreInteractions(this.libEGL);
    }

    @Test
    public void testCreateNoEglExtPlatformX11() {
        //given
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage("Required extension EGL_EXT_platform_x11 not available.");

        final long display = 12346;
        final int  window  = 12345;
        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_foobar");
        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
        //when
        this.x11EglOutputFactory.create(display,
                                        window);
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

        final long display = 76437;
        final int  window  = 12345;
        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
        when(this.libEGL.eglGetProcAddress(anyLong())).thenReturn(PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> 0L).address);

        //when
        this.x11EglOutputFactory.create(display,
                                        window);
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

        final long display    = 76437;
        final int  window     = 12345;
        final long eglDisplay = 9768426;

        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
        when(this.libEGL.eglGetProcAddress(anyLong())).thenReturn(PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address);

        when(this.libEGL.eglInitialize(eglDisplay,
                                       0,
                                       0)).thenReturn(0);

        //when
        this.x11EglOutputFactory.create(display,
                                        window);
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
    public void testCreateFailedChooseConfig() throws Exception {
        //given
        this.exception.expect(RuntimeException.class);
        this.exception.expectMessage("eglChooseConfig() failed");

        final long display            = 76437;
        final int  window             = 12345;
        final long eglDisplay         = 9768426;
        final long eglClientApis      = Pointer.nref("mock egl client apis").address;
        final long eglVendor          = Pointer.nref("mock egl vendor").address;
        final long eglVersion         = Pointer.nref("mock egl version").address;
        final long num_configs        = 4;
        final long egl_config_attribs = 5;
        final long configs            = 6;
        final int  configs_size       = 256 * Size.sizeof((Pointer) null);

        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
        when(this.libEGL.eglGetProcAddress(anyLong())).thenReturn(PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address);
        when(this.libEGL.eglInitialize(eglDisplay,
                                       0,
                                       0)).thenReturn(1);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_VENDOR)).thenReturn(eglVendor);
        when(this.libEGL.eglQueryString(eglDisplay,
                                        EGL_VERSION)).thenReturn(eglVersion);
        when(this.libEGL.eglChooseConfig(eglDisplay,
                                         egl_config_attribs,
                                         configs,
                                         configs_size,
                                         num_configs)).thenReturn(0);
        //when
        this.x11EglOutputFactory.create(display,
                                        window);
        //then
        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
                                           EGL_EXTENSIONS);
        verify(this.libEGL).eglInitialize(eglDisplay,
                                          0,
                                          0);
        verify(this.libEGL).eglChooseConfig(eglDisplay,
                                            egl_config_attribs,
                                            configs,
                                            configs_size,
                                            num_configs);
        //runtime exception is thrown
        verifyNoMoreInteractions(this.libEGL);
    }

//    @Test
//    public void testCreateNoConfigs() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("failed to find suitable EGLConfig");
//
//        final Pointer display       = mock(Pointer.class);
//        final int     window        = 12345;
//        final Pointer eglDisplay    = mock(Pointer.class);
//        final Pointer eglClientApis = mock(Pointer.class);
//        final Pointer eglVendor     = mock(Pointer.class);
//        final Pointer eglVersion    = mock(Pointer.class);
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(true);
//        final Pointer eglQueryString = mock(Pointer.class);
//        when(eglQueryString.getString(0)).thenReturn("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString);
//        when(this.libEGL.eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
//                                                  display,
//                                                  null)).thenReturn(eglDisplay);
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       null,
//                                       null)).thenReturn(true);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(eglClientApis.getString(0)).thenReturn("mock egl client apis");
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(eglVendor.getString(0)).thenReturn("mock egl vendor");
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(eglVersion.getString(0)).thenReturn("mock egl version");
//        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
//                                         any(),
//                                         any(),
//                                         anyInt(),
//                                         any())).thenAnswer(invocation -> {
//            Object arg4 = invocation.getArguments()[4];
//            final Memory num_configs1 = (Memory) arg4;
//            num_configs1.setInt(0,
//                                0);
//            return true;
//        });
//
//        //when
//        this.x11EglOutputFactory.create(display,
//                                        window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
//                                                     display,
//                                                     null);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          null,
//                                          null);
//        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
//                                            any(),
//                                            any(),
//                                            anyInt(),
//                                            any());
//        //runtime exception is thrown
//        verifyNoMoreInteractions(this.libEGL);
//    }
//
//    @Test
//    public void testCreateFailedContextCreation() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("eglCreateContext() failed");
//
//        final Pointer display       = mock(Pointer.class);
//        final int     window        = 12345;
//        final Pointer eglDisplay    = mock(Pointer.class);
//        final Pointer eglClientApis = mock(Pointer.class);
//        final Pointer eglVendor     = mock(Pointer.class);
//        final Pointer eglVersion    = mock(Pointer.class);
//        final Pointer config        = new Memory(Pointer.SIZE);
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(true);
//        final Pointer eglQueryString = mock(Pointer.class);
//        when(eglQueryString.getString(0)).thenReturn("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString);
//        when(this.libEGL.eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
//                                                  display,
//                                                  null)).thenReturn(eglDisplay);
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       null,
//                                       null)).thenReturn(true);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(eglClientApis.getString(0)).thenReturn("mock egl client apis");
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(eglVendor.getString(0)).thenReturn("mock egl vendor");
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(eglVersion.getString(0)).thenReturn("mock egl version");
//        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
//                                         any(),
//                                         any(),
//                                         anyInt(),
//                                         any())).thenAnswer(invocation -> {
//            final Object arg2 = invocation.getArguments()[2];
//            final Memory configs = (Memory) arg2;
//            configs.setPointer(0,
//                               config);
//
//            final Object arg4 = invocation.getArguments()[4];
//            final Memory num_configs = (Memory) arg4;
//            num_configs.setInt(0,
//                               16);
//            return true;
//        });
//        when(this.libEGL.eglCreateContext(eq(eglDisplay),
//                                          eq(config),
//                                          eq(EGL_NO_CONTEXT),
//                                          any())).thenReturn(null);
//
//        //when
//        this.x11EglOutputFactory.create(display,
//                                        window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
//                                                     display,
//                                                     null);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          null,
//                                          null);
//        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
//                                            any(),
//                                            any(),
//                                            anyInt(),
//                                            any());
//        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
//                                             eq(config),
//                                             eq(EGL_NO_CONTEXT),
//                                             any());
//        //runtime exception is thrown
//        verifyNoMoreInteractions(this.libEGL);
//    }
//
//    @Test
//    public void testCreateFailedSurfaceCreation() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("eglCreateWindowSurface() failed");
//
//        final Pointer display       = mock(Pointer.class);
//        final int     window        = 12345;
//        final Pointer eglDisplay    = mock(Pointer.class);
//        final Pointer eglClientApis = mock(Pointer.class);
//        final Pointer eglVendor     = mock(Pointer.class);
//        final Pointer eglVersion    = mock(Pointer.class);
//        final Pointer config        = new Memory(Pointer.SIZE);
//        final Pointer context       = mock(Pointer.class);
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(true);
//        final Pointer eglQueryString = mock(Pointer.class);
//        when(eglQueryString.getString(0)).thenReturn("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString);
//        when(this.libEGL.eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
//                                                  display,
//                                                  null)).thenReturn(eglDisplay);
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       null,
//                                       null)).thenReturn(true);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(eglClientApis.getString(0)).thenReturn("mock egl client apis");
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(eglVendor.getString(0)).thenReturn("mock egl vendor");
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(eglVersion.getString(0)).thenReturn("mock egl version");
//        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
//                                         any(),
//                                         any(),
//                                         anyInt(),
//                                         any())).thenAnswer(invocation -> {
//            final Object arg2 = invocation.getArguments()[2];
//            final Memory configs = (Memory) arg2;
//            configs.setPointer(0,
//                               config);
//
//            final Object arg4 = invocation.getArguments()[4];
//            final Memory num_configs = (Memory) arg4;
//            num_configs.setInt(0,
//                               16);
//            return true;
//        });
//        when(this.libEGL.eglCreateContext(eq(eglDisplay),
//                                          eq(config),
//                                          eq(EGL_NO_CONTEXT),
//                                          any())).thenReturn(context);
//        when(this.libEGL.eglCreatePlatformWindowSurfaceEXT(eq(eglDisplay),
//                                                           eq(config),
//                                                           any(),
//                                                           any())).thenReturn(null);
//        //when
//        this.x11EglOutputFactory.create(display,
//                                        window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
//                                                     display,
//                                                     null);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          null,
//                                          null);
//        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
//                                            any(),
//                                            any(),
//                                            anyInt(),
//                                            any());
//        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
//                                             eq(config),
//                                             eq(EGL_NO_CONTEXT),
//                                             any());
//        verify(this.libEGL).eglCreatePlatformWindowSurfaceEXT(eq(eglDisplay),
//                                                              eq(config),
//                                                              any(),
//                                                              any());
//        //runtime exception is thrown
//        verifyNoMoreInteractions(this.libEGL);
//    }
//
//    @Test
//    public void testCreateFailedMakeCurrent() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("eglMakeCurrent() failed");
//
//        final Pointer display       = mock(Pointer.class);
//        final int     window        = 12345;
//        final Pointer eglDisplay    = mock(Pointer.class);
//        final Pointer eglClientApis = mock(Pointer.class);
//        final Pointer eglVendor     = mock(Pointer.class);
//        final Pointer eglVersion    = mock(Pointer.class);
//        final Pointer config        = new Memory(Pointer.SIZE);
//        final Pointer context       = mock(Pointer.class);
//        final Pointer eglSurface    = mock(Pointer.class);
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(true);
//        final Pointer eglQueryString = mock(Pointer.class);
//        when(eglQueryString.getString(0)).thenReturn("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString);
//        when(this.libEGL.eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
//                                                  display,
//                                                  null)).thenReturn(eglDisplay);
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       null,
//                                       null)).thenReturn(true);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(eglClientApis.getString(0)).thenReturn("mock egl client apis");
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(eglVendor.getString(0)).thenReturn("mock egl vendor");
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(eglVersion.getString(0)).thenReturn("mock egl version");
//        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
//                                         any(),
//                                         any(),
//                                         anyInt(),
//                                         any())).thenAnswer(invocation -> {
//            final Object arg2 = invocation.getArguments()[2];
//            final Memory configs = (Memory) arg2;
//            configs.setPointer(0,
//                               config);
//
//            final Object arg4 = invocation.getArguments()[4];
//            final Memory num_configs = (Memory) arg4;
//            num_configs.setInt(0,
//                               16);
//            return true;
//        });
//        when(this.libEGL.eglCreateContext(eq(eglDisplay),
//                                          eq(config),
//                                          eq(EGL_NO_CONTEXT),
//                                          any())).thenReturn(context);
//        when(this.libEGL.eglCreatePlatformWindowSurfaceEXT(eq(eglDisplay),
//                                                           eq(config),
//                                                           any(),
//                                                           any())).thenReturn(eglSurface);
//        when(this.libEGL.eglMakeCurrent(eglDisplay,
//                                        eglSurface,
//                                        eglSurface,
//                                        context)).thenReturn(false);
//        //when
//        this.x11EglOutputFactory.create(display,
//                                        window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
//                                                     display,
//                                                     null);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          null,
//                                          null);
//        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
//                                            any(),
//                                            any(),
//                                            anyInt(),
//                                            any());
//        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
//                                             eq(config),
//                                             eq(EGL_NO_CONTEXT),
//                                             any());
//        verify(this.libEGL).eglCreatePlatformWindowSurfaceEXT(eq(eglDisplay),
//                                                              eq(config),
//                                                              any(),
//                                                              any());
//        verify(this.libEGL).eglMakeCurrent(eglDisplay,
//                                           eglSurface,
//                                           eglSurface,
//                                           context);
//        //runtime exception is thrown
//        verifyNoMoreInteractions(this.libEGL);
//    }
//
//    @Test
//    public void testCreate() throws Exception {
//        //given
//        final Pointer display       = mock(Pointer.class);
//        final int     window        = 12345;
//        final Pointer eglDisplay    = mock(Pointer.class);
//        final Pointer eglClientApis = mock(Pointer.class);
//        final Pointer eglVendor     = mock(Pointer.class);
//        final Pointer eglVersion    = mock(Pointer.class);
//        final Pointer config        = new Memory(Pointer.SIZE);
//        final Pointer context       = mock(Pointer.class);
//        final Pointer eglSurface    = mock(Pointer.class);
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(true);
//        final Pointer eglQueryString = mock(Pointer.class);
//        when(eglQueryString.getString(0)).thenReturn("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString);
//        when(this.libEGL.eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
//                                                  display,
//                                                  null)).thenReturn(eglDisplay);
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       null,
//                                       null)).thenReturn(true);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(eglClientApis.getString(0)).thenReturn("mock egl client apis");
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(eglVendor.getString(0)).thenReturn("mock egl vendor");
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(eglVersion.getString(0)).thenReturn("mock egl version");
//        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
//                                         any(),
//                                         any(),
//                                         anyInt(),
//                                         any())).thenAnswer(invocation -> {
//            final Object arg2 = invocation.getArguments()[2];
//            final Memory configs = (Memory) arg2;
//            configs.setPointer(0,
//                               config);
//
//            final Object arg4 = invocation.getArguments()[4];
//            final Memory num_configs = (Memory) arg4;
//            num_configs.setInt(0,
//                               16);
//            return true;
//        });
//        when(this.libEGL.eglCreateContext(eq(eglDisplay),
//                                          eq(config),
//                                          eq(EGL_NO_CONTEXT),
//                                          any())).thenReturn(context);
//        when(this.libEGL.eglCreatePlatformWindowSurfaceEXT(eq(eglDisplay),
//                                                           eq(config),
//                                                           any(),
//                                                           any())).thenReturn(eglSurface);
//        when(this.libEGL.eglMakeCurrent(eglDisplay,
//                                        eglSurface,
//                                        eglSurface,
//                                        context)).thenReturn(true);
//
//        //when
//        this.x11EglOutputFactory.create(display,
//                                        window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglGetPlatformDisplayEXT(EGL_PLATFORM_X11_KHR,
//                                                     display,
//                                                     null);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          null,
//                                          null);
//        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
//                                            any(),
//                                            any(),
//                                            anyInt(),
//                                            any());
//        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
//                                             eq(config),
//                                             eq(EGL_NO_CONTEXT),
//                                             any());
//        verify(this.libEGL).eglCreatePlatformWindowSurfaceEXT(eq(eglDisplay),
//                                                              eq(config),
//                                                              any(),
//                                                              any());
//        verify(this.libEGL).eglMakeCurrent(eglDisplay,
//                                           eglSurface,
//                                           eglSurface,
//                                           context);
//    }
}