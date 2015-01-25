package org.trinity.wayland.protocol;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ProtocolObjectTest {

    @InjectMocks
    private ProtocolObjectDummy protocolObjectDummy;

    @Test
    public void testGetResourceSingleResource() throws Exception {
        final Client client = mock(Client.class);
        final Resource<?> resource = this.protocolObjectDummy.add(client,
                                                                  1,
                                                                  1);
        assertThat(this.protocolObjectDummy.getResource()).isEqualTo(Optional.of(resource));
    }

    @Test
    public void testGetResourceNoResource() throws Exception {
        assertThat(this.protocolObjectDummy.getResource()).isEqualTo(Optional.empty());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetResourceMultipleResourceResource() throws Exception {
        final Client client = mock(Client.class);
        this.protocolObjectDummy.add(client,
                                     1,
                                     1);
        this.protocolObjectDummy.add(client,
                                     1,
                                     2);
        this.protocolObjectDummy.getResource();
    }

    @Test
    public void testAddNone() throws Exception {
        assertThat(this.protocolObjectDummy.getResources()).isEmpty();
    }

    @Test
    public void testAddSingle() throws Exception {
        final Client client = mock(Client.class);
        final Resource<?> resource = this.protocolObjectDummy.add(client,
                                                                  1,
                                                                  1);
        assertThat(this.protocolObjectDummy.getResources()).contains(resource);
        assertThat(this.protocolObjectDummy.getResources()).hasSize(1);
    }

    @Test
    public void testAddMultiple() throws Exception {
        final Client client = mock(Client.class);
        final Resource<?> resource0 = this.protocolObjectDummy.add(client,
                                                                   1,
                                                                   1);
        final Resource<?> resource1 = this.protocolObjectDummy.add(client,
                                                                   1,
                                                                   2);
        final Resource<?> resource2 = this.protocolObjectDummy.add(client,
                                                                   1,
                                                                   3);
        assertThat(this.protocolObjectDummy.getResources()).contains(resource0);
        assertThat(this.protocolObjectDummy.getResources()).contains(resource1);
        assertThat(this.protocolObjectDummy.getResources()).contains(resource2);
        assertThat(this.protocolObjectDummy.getResources()).hasSize(3);
    }

//    @Test
//    public void testDestroyResourcePresent() throws Exception {
//        final Client client = mock(Client.class);
//        final Resource<?> resource0 = this.protocolObjectDummy.add(client,
//                                                                   1,
//                                                                   1);
//        final Resource<?> resource1 = this.protocolObjectDummy.add(client,
//                                                                   1,
//                                                                   2);
//        this.protocolObjectDummy.destroy(resource0);
//        assertThat(this.protocolObjectDummy.getResources()).contains(resource1);
//        assertThat(this.protocolObjectDummy.getResources()).hasSize(1);
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testDestroyResourceAbsent() throws Exception {
//        final Client client = mock(Client.class);
//        final Resource<?> resource0 = this.protocolObjectDummy.add(client,
//                                                                   1,
//                                                                   1);
//        final Resource<?> resource1 = this.protocolObjectDummy.add(client,
//                                                                   1,
//                                                                   2);
//        this.protocolObjectDummy.destroy(mock(Resource.class));
//    }
}