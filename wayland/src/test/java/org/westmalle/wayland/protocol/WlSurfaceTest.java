package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.*;
import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.server.jna.WaylandServerLibraryMapping;
import org.freedesktop.wayland.shared.WlOutputTransform;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.westmalle.wayland.output.Surface;

import javax.media.nativewindow.util.Rectangle;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerLibrary.class,
                        //following classes are final, so we have to powermock them:
                        WlCallbackFactory.class
                })
public class WlSurfaceTest {

    @Mock
    private WlCallbackFactory    wlCallbackFactory;
    @Mock
    private WlCompositorResource compositorResource;
    @Mock
    private Surface              surface;

    @Mock
    private WaylandServerLibraryMapping waylandServerLibraryMapping;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(WaylandServerLibrary.class);
        when(WaylandServerLibrary.INSTANCE()).thenReturn(this.waylandServerLibraryMapping);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBufferTransform() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final int transform = WlOutputTransform.NORMAL.getValue();

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);

        //when
        wlSurface.setBufferTransform(wlSurfaceResource,
                                     transform);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        final Client client = mock(Client.class);
        final int version = 5;
        final int id = 100;

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        final WlSurfaceResource wlSurfaceResource = wlSurface.create(client,
                                                                     version,
                                                                     id);
        //then
        assertThat(wlSurfaceResource).isNotNull();
        assertThat(wlSurfaceResource.getImplementation()).isSameAs(wlSurface);
    }

    @Test
    public void testDestroy() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.destroy(wlSurfaceResource);
        //then
        verify(wlSurfaceResource,
               times(1)).destroy();
    }

    @Test
    public void testAttach() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);
        final int x = 11;
        final int y = 22;

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.attach(wlSurfaceResource,
                         wlBufferResource,
                         x,
                         y);
        //then
        verify(this.surface).attachBuffer(wlBufferResource,
                                          x,
                                          y);
        final ArgumentCaptor<Listener> listenerArgumentCaptor = ArgumentCaptor.forClass(Listener.class);
        verify(wlBufferResource,
               times(1)).addDestroyListener(listenerArgumentCaptor.capture());
        final Listener destroyListener = listenerArgumentCaptor.getValue();
        //and when
        destroyListener.handle();
        //then
        this.surface.detachBuffer();
    }

    @Test
    public void testDoubleAttach() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource wlBufferResource0 = mock(WlBufferResource.class);
        final WlBufferResource wlBufferResource1 = mock(WlBufferResource.class);
        final int x = 11;
        final int y = 22;

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.attach(wlSurfaceResource,
                         wlBufferResource0,
                         x,
                         y);
        wlSurface.attach(wlSurfaceResource,
                         wlBufferResource1,
                         x,
                         y);
        //then
        verify(this.surface).attachBuffer(wlBufferResource0,
                                          x,
                                          y);
        verify(this.surface).attachBuffer(wlBufferResource1,
                                          x,
                                          y);

        final ArgumentCaptor<Listener> listenerArgumentCaptor0 = ArgumentCaptor.forClass(Listener.class);
        verify(wlBufferResource0,
               times(1)).addDestroyListener(listenerArgumentCaptor0.capture());
        final Listener destroyListener0 = listenerArgumentCaptor0.getValue();

        verify(WaylandServerLibrary.INSTANCE())
                .wl_list_remove(destroyListener0.getNative().link.getPointer());
    }

    @Test
    public void testDetach() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.attach(wlSurfaceResource,
                         null,
                         0,
                         0);
        //then
        verify(this.surface).detachBuffer();
    }

    @Test
    public void testAttachDetach() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);
        final int x = 11;
        final int y = 22;

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.attach(wlSurfaceResource,
                         wlBufferResource,
                         x,
                         y);
        wlSurface.attach(wlSurfaceResource,
                         null,
                         0,
                         0);
        //then
        verify(this.surface,
               times(1)).attachBuffer(wlBufferResource,
                                      x,
                                      y);
        verify(this.surface,
               times(1)).detachBuffer();

        final ArgumentCaptor<Listener> listenerArgumentCaptor = ArgumentCaptor.forClass(Listener.class);
        verify(wlBufferResource,
               times(1)).addDestroyListener(listenerArgumentCaptor.capture());
        final Listener destroyListener = listenerArgumentCaptor.getValue();

        verify(WaylandServerLibrary.INSTANCE())
                .wl_list_remove(destroyListener.getNative().link.getPointer());
    }

    @Test
    public void testDamage() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final int x = -20;
        final int y = -100;
        final int width = 500;
        final int height = 1000;

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.damage(wlSurfaceResource,
                         x,
                         y,
                         width,
                         height);
        //then
        verify(this.surface,
               times(1)).markDamaged(eq(new Rectangle(x,
                                                      y,
                                                      width,
                                                      height)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDamageNegativeWidthNegativeHeight() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final int x = 20;
        final int y = 100;
        final int width = -500;
        final int height = -1000;

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.damage(wlSurfaceResource,
                         x,
                         y,
                         width,
                         height);
        //then
    }

    @Test
    public void testFrame() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final int callbackId = 987;

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);

        final Client client = mock(Client.class);
        final int version = 5;

        when(wlSurfaceResource.getClient()).thenReturn(client);
        when(wlSurfaceResource.getVersion()).thenReturn(version);

        final WlCallback wlCallback = mock(WlCallback.class);
        when(this.wlCallbackFactory.create()).thenReturn(wlCallback);

        final WlCallbackResource wlCallbackResource = mock(WlCallbackResource.class);
        when(wlCallback.add(client,
                            version,
                            callbackId)).thenReturn(wlCallbackResource);
        //when
        wlSurface.frame(wlSurfaceResource,
                        callbackId);
        //then
        verify(this.surface).addCallback(wlCallbackResource);
    }

    @Test
    public void testSetOpaqueRegion() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.setOpaqueRegion(wlSurfaceResource,
                                  wlRegionResource);
        //then
        verify(this.surface,
               times(1)).setOpaqueRegion(wlRegionResource);
    }

    @Test
    public void testRemoveOpaqueRegion() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.setOpaqueRegion(wlSurfaceResource,
                                  null);
        //then
        verify(this.surface,
               times(1)).removeOpaqueRegion();
    }

    @Test
    public void testSetInputRegion() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlRegionResource wlRegionResource = mock(WlRegionResource.class);

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.setInputRegion(wlSurfaceResource,
                                 wlRegionResource);
        //then
        verify(this.surface,
               times(1)).setInputRegion(wlRegionResource);
    }

    @Test
    public void testRemoveInputRegion() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.setInputRegion(wlSurfaceResource,
                                 null);
        //then
        verify(this.surface,
               times(1)).removeInputRegion();
    }

    @Test
    public void testCommit() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlBufferResource wlBufferResource = mock(WlBufferResource.class);
        final int x = 11;
        final int y = 22;

        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        wlSurface.attach(wlSurfaceResource,
                         wlBufferResource,
                         x,
                         y);
        //when
        wlSurface.commit(wlSurfaceResource);
        //then
        verify(this.surface,
               times(1)).commit();
        final ArgumentCaptor<Listener> listenerArgumentCaptor = ArgumentCaptor.forClass(Listener.class);
        verify(wlBufferResource,
               times(1)).addDestroyListener(listenerArgumentCaptor.capture());
        final Listener destroyListener = listenerArgumentCaptor.getValue();
        verify(WaylandServerLibrary.INSTANCE())
                .wl_list_remove(destroyListener.getNative().link.getPointer());
    }

    @Test
    public void testCommitNoBuffer() throws Exception {
        //given
        final WlSurfaceResource wlSurfaceResource = mock(WlSurfaceResource.class);
        final WlSurface wlSurface = new WlSurface(this.wlCallbackFactory,
                                                  this.surface);
        //when
        wlSurface.commit(wlSurfaceResource);
        //then
        verify(this.surface,
               times(1)).commit();
    }
}