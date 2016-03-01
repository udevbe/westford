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
//package org.westmalle.wayland.protocol;
//
//import com.sun.jna.Pointer;
//import org.freedesktop.wayland.server.Client;
//import org.freedesktop.wayland.server.Display;
//import org.freedesktop.wayland.server.WlOutputResource;
//import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
//import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
//import org.freedesktop.wayland.util.InterfaceMeta;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.westmalle.wayland.core.Output;
//
//import static com.google.common.truth.Truth.assertThat;
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyInt;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({
//                        //following classes have static methods, so we have to powermock them:
//                        WaylandServerLibrary.class,
//                        InterfaceMeta.class
//                })
//public class WlOutputTest {
//
//    @Mock
//    private WaylandServerLibraryMapping waylandServerLibraryMapping;
//    @Mock
//    private InterfaceMeta               interfaceMeta;
//    @Mock
//    private Pointer                     globalPointer;
//
//
//    @Mock
//    private Display display;
//    @Mock
//    private Output  output;
//
//    private WlOutput wlOutput;
//
//    @Before
//    public void setUp() throws Exception {
//        PowerMockito.mockStatic(WaylandServerLibrary.class,
//                                InterfaceMeta.class);
//        when(InterfaceMeta.get((Class<?>) any())).thenReturn(this.interfaceMeta);
//        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
//        when(this.waylandServerLibraryMapping.wl_global_create(any(),
//                                                               any(),
//                                                               anyInt(),
//                                                               any(),
//                                                               any())).thenReturn(this.globalPointer);
//        when(this.waylandServerLibraryMapping.wl_resource_get_version(any())).thenReturn(2);
//        this.wlOutput = new WlOutput(this.display,
//                                     this.output);
//    }
//
//    @Test
//    public void testOnBindClient() throws Exception {
//        //given
//        final Pointer resourcePointer = mock(Pointer.class);
//        when(this.waylandServerLibraryMapping.wl_resource_create(any(),
//                                                                 any(),
//                                                                 anyInt(),
//                                                                 anyInt())).thenReturn(resourcePointer);
//        when(this.output.notifyGeometry(any())).thenReturn(this.output);
//        when(this.output.notifyMode(any())).thenReturn(this.output);
//
//        //when
//        final WlOutputResource wlOutputResource = this.wlOutput.onBindClient(mock(Client.class),
//                                                                             1,
//                                                                             1);
//        //then
//        assertThat(wlOutputResource).isNotNull();
//        assertThat(wlOutputResource.getImplementation()).isSameAs(this.wlOutput);
//        verify(this.output).notifyGeometry(wlOutputResource);
//        verify(this.output).notifyMode(wlOutputResource);
//    }
//}