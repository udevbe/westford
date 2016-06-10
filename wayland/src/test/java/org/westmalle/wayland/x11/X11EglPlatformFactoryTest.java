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
//import org.freedesktop.jaccall.Size;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//import org.westmalle.wayland.nativ.libEGL.EglCreatePlatformWindowSurfaceEXT;
//import org.westmalle.wayland.nativ.libEGL.LibEGL;
//import org.westmalle.wayland.nativ.libEGL.PointerEglCreatePlatformWindowSurfaceEXT;
//import org.westmalle.wayland.nativ.libEGL.PointerEglGetPlatformDisplayEXT;
//
//import static org.mockito.Matchers.anyInt;
//import static org.mockito.Matchers.anyLong;
//import static org.mockito.Matchers.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoMoreInteractions;
//import static org.mockito.Mockito.when;
//import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CLIENT_APIS;
//import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_EXTENSIONS;
//import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
//import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
//import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_API;
//import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
//import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VENDOR;
//import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_VERSION;
//
//@RunWith(MockitoJUnitRunner.class)
//public class X11EglPlatformFactoryTest {
//
//    @Rule
//    public ExpectedException exception = ExpectedException.none();
//    @Mock
//    private LibEGL                     libEGL;
//    @Mock
//    private PrivateX11EglOutputFactory privateX11EglOutputFactory;
//    @InjectMocks
//    private X11EglPlatformFactory      x11EglPlatformFactory;
//
//    @Test
//    public void testCreateNoEsAPi() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("eglBindAPI failed");
//
//        final long display = 123456;
//        final int  window  = 12345;
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(0);
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_API)).thenReturn(1);
//        //when
//        this.x11EglPlatformFactory.create(display,
//                                          window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        //runtime exception is thrown
//        verifyNoMoreInteractions(this.libEGL);
//    }
//
//    @Test
//    public void testCreateNoEglExtPlatformX11() {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("Required extension EGL_EXT_platform_x11 not available.");
//
//        final long display = 12346;
//        final int  window  = 12345;
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
//        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_foobar");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
//        //when
//        this.x11EglPlatformFactory.create(display,
//                                          window);
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        //runtime exception is thrown
//        verifyNoMoreInteractions(this.libEGL);
//    }
//
//    @Test
//    public void testCreateNoEglDisplay() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("eglGetDisplay() failed");
//
//        final long display = 76437;
//        final int  window  = 12345;
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
//        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
//        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
//            final long func_name = (Long) invocation.getArguments()[0];
//            final String funcName = Pointer.wrap(String.class,
//                                                 func_name)
//                                           .dref();
//            if (funcName.equals("eglGetPlatformDisplayEXT")) {
//                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> 0).address;
//            }
//            return 0L;
//        });
//        //when
//        this.x11EglPlatformFactory.create(display,
//                                          window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//
//        //runtime exception is thrown
//        verifyNoMoreInteractions(this.libEGL);
//    }
//
//    @Test
//    public void testCreateFailedInit() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("eglInitialize() failed");
//
//        final long display    = 76437;
//        final int  window     = 12345;
//        final long eglDisplay = 9768426;
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
//        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
//        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
//            final long func_name = (Long) invocation.getArguments()[0];
//            final String funcName = Pointer.wrap(String.class,
//                                                 func_name)
//                                           .dref();
//            if (funcName.equals("eglGetPlatformDisplayEXT")) {
//                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
//            }
//            return 0L;
//        });
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       0,
//                                       0)).thenReturn(0);
//
//        //when
//        this.x11EglPlatformFactory.create(display,
//                                          window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          0,
//                                          0);
//        //runtime exception is thrown
//        verifyNoMoreInteractions(this.libEGL);
//    }
//
//    @Test
//    public void testCreateFailedChooseConfig() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("eglChooseConfig() failed");
//
//        final long display            = 76437;
//        final int  window             = 12345;
//        final long eglDisplay         = 9768426;
//        final long eglClientApis      = Pointer.nref("mock egl client apis").address;
//        final long eglVendor          = Pointer.nref("mock egl vendor").address;
//        final long eglVersion         = Pointer.nref("mock egl version").address;
//        final long num_configs        = 4;
//        final long egl_config_attribs = 5;
//        final long configs            = 6;
//        final int  configs_size       = 256 * Size.sizeof((Pointer) null);
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
//        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
//        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
//            final long func_name = (Long) invocation.getArguments()[0];
//            final String funcName = Pointer.wrap(String.class,
//                                                 func_name)
//                                           .dref();
//            if (funcName.equals("eglGetPlatformDisplayEXT")) {
//                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
//            }
//            return 0L;
//        });
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       0,
//                                       0)).thenReturn(1);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(this.libEGL.eglChooseConfig(eglDisplay,
//                                         egl_config_attribs,
//                                         configs,
//                                         configs_size,
//                                         num_configs)).thenReturn(0);
//        //when
//        this.x11EglPlatformFactory.create(display,
//                                          window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          0,
//                                          0);
//        verify(this.libEGL).eglChooseConfig(eglDisplay,
//                                            egl_config_attribs,
//                                            configs,
//                                            configs_size,
//                                            num_configs);
//        //runtime exception is thrown
//        verifyNoMoreInteractions(this.libEGL);
//    }
//
//    @Test
//    public void testCreateNoConfigs() throws Exception {
//        //given
//        this.exception.expect(RuntimeException.class);
//        this.exception.expectMessage("failed to find suitable EGLConfig");
//
//        final long display       = 76437;
//        final int  window        = 12345;
//        final long eglDisplay    = 9768426;
//        final long eglClientApis = Pointer.nref("mock egl client apis").address;
//        final long eglVendor     = Pointer.nref("mock egl vendor").address;
//        final long eglVersion    = Pointer.nref("mock egl version").address;
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
//        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
//        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
//            final long func_name = (Long) invocation.getArguments()[0];
//            final String funcName = Pointer.wrap(String.class,
//                                                 func_name)
//                                           .dref();
//            if (funcName.equals("eglGetPlatformDisplayEXT")) {
//                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
//            }
//            return 0L;
//        });
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       0,
//                                       0)).thenReturn(1);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
//                                         anyLong(),
//                                         anyLong(),
//                                         anyInt(),
//                                         anyLong())).thenAnswer(invocation -> {
//            final long num_configs = (Long) invocation.getArguments()[4];
//            Pointer.wrap(Integer.class,
//                         num_configs)
//                   .write(0);
//            return 1;
//        });
//
//        //when
//        this.x11EglPlatformFactory.create(display,
//                                          window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          0,
//                                          0);
//        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
//                                            anyLong(),
//                                            anyLong(),
//                                            anyInt(),
//                                            anyLong());
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
//        final long display       = 76437;
//        final int  window        = 12345;
//        final long eglDisplay    = 9768426;
//        final long eglClientApis = Pointer.nref("mock egl client apis").address;
//        final long eglVendor     = Pointer.nref("mock egl vendor").address;
//        final long eglVersion    = Pointer.nref("mock egl version").address;
//        final long config        = 2486;
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
//        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
//        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
//            final long func_name = (Long) invocation.getArguments()[0];
//            final String funcName = Pointer.wrap(String.class,
//                                                 func_name)
//                                           .dref();
//            if (funcName.equals("eglGetPlatformDisplayEXT")) {
//                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
//            }
//            return 0L;
//        });
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       0,
//                                       0)).thenReturn(1);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
//                                         anyLong(),
//                                         anyLong(),
//                                         anyInt(),
//                                         anyLong())).thenAnswer(invocation -> {
//            final long configs = (Long) invocation.getArguments()[2];
//            Pointer.wrap(Pointer.class,
//                         configs)
//                   .write(Pointer.wrap(config));
//
//            final long num_configs = (Long) invocation.getArguments()[4];
//            Pointer.wrap(Integer.class,
//                         num_configs)
//                   .write(1);
//            return 1;
//        });
//        when(this.libEGL.eglCreateContext(eq(eglDisplay),
//                                          eq(config),
//                                          eq(EGL_NO_CONTEXT),
//                                          anyLong())).thenReturn(0L);
//
//        //when
//        this.x11EglPlatformFactory.create(display,
//                                          window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          0,
//                                          0);
//        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
//                                            anyLong(),
//                                            anyLong(),
//                                            anyInt(),
//                                            anyLong());
//        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
//                                             eq(config),
//                                             eq(EGL_NO_CONTEXT),
//                                             anyLong());
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
//        final long display       = 76437;
//        final int  window        = 12345;
//        final long eglDisplay    = 9768426;
//        final long eglClientApis = Pointer.nref("mock egl client apis").address;
//        final long eglVendor     = Pointer.nref("mock egl vendor").address;
//        final long eglVersion    = Pointer.nref("mock egl version").address;
//        final long config        = 2486;
//        final long context       = 6842;
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
//        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
//
//        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
//            final long func_name = (Long) invocation.getArguments()[0];
//            final String funcName = Pointer.wrap(String.class,
//                                                 func_name)
//                                           .dref();
//            if (funcName.equals("eglGetPlatformDisplayEXT")) {
//                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
//            }
//            else if (funcName.equals("eglCreatePlatformWindowSurfaceEXT")) {
//                return PointerEglCreatePlatformWindowSurfaceEXT.nref((dpy, config1, native_window, attrib_list) -> 0L).address;
//            }
//            return 0L;
//        });
//
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       0,
//                                       0)).thenReturn(1);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
//                                         anyLong(),
//                                         anyLong(),
//                                         anyInt(),
//                                         anyLong())).thenAnswer(invocation -> {
//            final long configs = (Long) invocation.getArguments()[2];
//            Pointer.wrap(Pointer.class,
//                         configs)
//                   .write(Pointer.wrap(config));
//
//            final long num_configs = (Long) invocation.getArguments()[4];
//            Pointer.wrap(Integer.class,
//                         num_configs)
//                   .write(1);
//            return 1;
//        });
//        when(this.libEGL.eglCreateContext(eq(eglDisplay),
//                                          eq(config),
//                                          eq(EGL_NO_CONTEXT),
//                                          anyLong())).thenReturn(context);
//        //when
//        this.x11EglPlatformFactory.create(display,
//                                          window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          0,
//                                          0);
//        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
//                                            anyLong(),
//                                            anyLong(),
//                                            anyInt(),
//                                            anyLong());
//        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
//                                             eq(config),
//                                             eq(EGL_NO_CONTEXT),
//                                             anyLong());
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
//        final long display       = 76437;
//        final int  window        = 12345;
//        final long eglDisplay    = 9768426;
//        final long eglClientApis = Pointer.nref("mock egl client apis").address;
//        final long eglVendor     = Pointer.nref("mock egl vendor").address;
//        final long eglVersion    = Pointer.nref("mock egl version").address;
//        final long config        = 2486;
//        final long context       = 6842;
//        final long eglSurface    = 847453;
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
//        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
//
//        EglCreatePlatformWindowSurfaceEXT eglCreatePlatformWindowSurfaceEXT = mock(EglCreatePlatformWindowSurfaceEXT.class);
//        when(eglCreatePlatformWindowSurfaceEXT.$(eq(eglDisplay),
//                                                 eq(config),
//                                                 anyLong(),
//                                                 anyLong())).thenReturn(eglSurface);
//
//        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
//            final long func_name = (Long) invocation.getArguments()[0];
//            final String funcName = Pointer.wrap(String.class,
//                                                 func_name)
//                                           .dref();
//            if (funcName.equals("eglGetPlatformDisplayEXT")) {
//                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
//            }
//            else if (funcName.equals("eglCreatePlatformWindowSurfaceEXT")) {
//                return PointerEglCreatePlatformWindowSurfaceEXT.nref(eglCreatePlatformWindowSurfaceEXT).address;
//            }
//            return 0L;
//        });
//
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       0,
//                                       0)).thenReturn(1);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
//                                         anyLong(),
//                                         anyLong(),
//                                         anyInt(),
//                                         anyLong())).thenAnswer(invocation -> {
//            final long configs = (Long) invocation.getArguments()[2];
//            Pointer.wrap(Pointer.class,
//                         configs)
//                   .write(Pointer.wrap(config));
//
//            final long num_configs = (Long) invocation.getArguments()[4];
//            Pointer.wrap(Integer.class,
//                         num_configs)
//                   .write(1);
//            return 1;
//        });
//        when(this.libEGL.eglCreateContext(eq(eglDisplay),
//                                          eq(config),
//                                          eq(EGL_NO_CONTEXT),
//                                          anyLong())).thenReturn(context);
//
//        when(this.libEGL.eglMakeCurrent(eglDisplay,
//                                        eglSurface,
//                                        eglSurface,
//                                        context)).thenReturn(0);
//        //when
//        this.x11EglPlatformFactory.create(display,
//                                          window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          0L,
//                                          0L);
//        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
//                                            anyLong(),
//                                            anyLong(),
//                                            anyInt(),
//                                            anyLong());
//        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
//                                             eq(config),
//                                             eq(EGL_NO_CONTEXT),
//                                             anyLong());
//        verify(eglCreatePlatformWindowSurfaceEXT).$(eq(eglDisplay),
//                                                    eq(config),
//                                                    anyLong(),
//                                                    anyLong());
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
//        final long display       = 76437;
//        final int  window        = 12345;
//        final long eglDisplay    = 9768426;
//        final long eglClientApis = Pointer.nref("mock egl client apis").address;
//        final long eglVendor     = Pointer.nref("mock egl vendor").address;
//        final long eglVersion    = Pointer.nref("mock egl version").address;
//        final long config        = 2486;
//        final long context       = 6842;
//        final long eglSurface    = 847453;
//
//        when(this.libEGL.eglBindAPI(EGL_OPENGL_ES_API)).thenReturn(1);
//        final Pointer<String> eglQueryString = Pointer.nref("EGL_EXT_platform_x11");
//        when(this.libEGL.eglQueryString(EGL_NO_DISPLAY,
//                                        EGL_EXTENSIONS)).thenReturn(eglQueryString.address);
//
//        EglCreatePlatformWindowSurfaceEXT eglCreatePlatformWindowSurfaceEXT = mock(EglCreatePlatformWindowSurfaceEXT.class);
//        when(eglCreatePlatformWindowSurfaceEXT.$(eq(eglDisplay),
//                                                 eq(config),
//                                                 anyLong(),
//                                                 anyLong())).thenReturn(eglSurface);
//
//        when(this.libEGL.eglGetProcAddress(anyLong())).thenAnswer(invocation -> {
//            final long func_name = (Long) invocation.getArguments()[0];
//            final String funcName = Pointer.wrap(String.class,
//                                                 func_name)
//                                           .dref();
//            if (funcName.equals("eglGetPlatformDisplayEXT")) {
//                return PointerEglGetPlatformDisplayEXT.nref((platform, native_display, attrib_list) -> eglDisplay).address;
//            }
//            else if (funcName.equals("eglCreatePlatformWindowSurfaceEXT")) {
//                return PointerEglCreatePlatformWindowSurfaceEXT.nref(eglCreatePlatformWindowSurfaceEXT).address;
//            }
//            return 0L;
//        });
//
//        when(this.libEGL.eglInitialize(eglDisplay,
//                                       0,
//                                       0)).thenReturn(1);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_CLIENT_APIS)).thenReturn(eglClientApis);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VENDOR)).thenReturn(eglVendor);
//        when(this.libEGL.eglQueryString(eglDisplay,
//                                        EGL_VERSION)).thenReturn(eglVersion);
//        when(this.libEGL.eglChooseConfig(eq(eglDisplay),
//                                         anyLong(),
//                                         anyLong(),
//                                         anyInt(),
//                                         anyLong())).thenAnswer(invocation -> {
//            final long configs = (Long) invocation.getArguments()[2];
//            Pointer.wrap(Pointer.class,
//                         configs)
//                   .write(Pointer.wrap(config));
//
//            final long num_configs = (Long) invocation.getArguments()[4];
//            Pointer.wrap(Integer.class,
//                         num_configs)
//                   .write(1);
//            return 1;
//        });
//        when(this.libEGL.eglCreateContext(eq(eglDisplay),
//                                          eq(config),
//                                          eq(EGL_NO_CONTEXT),
//                                          anyLong())).thenReturn(context);
//
//        when(this.libEGL.eglMakeCurrent(eglDisplay,
//                                        eglSurface,
//                                        eglSurface,
//                                        context)).thenReturn(1);
//
//        //when
//        this.x11EglPlatformFactory.create(display,
//                                          window);
//        //then
//        verify(this.libEGL).eglBindAPI(EGL_OPENGL_ES_API);
//        verify(this.libEGL).eglQueryString(EGL_NO_DISPLAY,
//                                           EGL_EXTENSIONS);
//        verify(this.libEGL).eglInitialize(eglDisplay,
//                                          0L,
//                                          0L);
//        verify(this.libEGL).eglChooseConfig(eq(eglDisplay),
//                                            anyLong(),
//                                            anyLong(),
//                                            anyInt(),
//                                            anyLong());
//        verify(this.libEGL).eglCreateContext(eq(eglDisplay),
//                                             eq(config),
//                                             eq(EGL_NO_CONTEXT),
//                                             anyLong());
//        verify(eglCreatePlatformWindowSurfaceEXT).$(eq(eglDisplay),
//                                                    eq(config),
//                                                    anyLong(),
//                                                    anyLong());
//        verify(this.libEGL).eglMakeCurrent(eglDisplay,
//                                           eglSurface,
//                                           eglSurface,
//                                           context);
//    }
//}