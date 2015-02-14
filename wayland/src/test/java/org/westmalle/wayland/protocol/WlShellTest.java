package org.westmalle.wayland.protocol;

import org.freedesktop.wayland.server.jna.WaylandServerLibrary;
import org.freedesktop.wayland.util.InterfaceMeta;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
                        //following classes have static methods, so we have to powermock them:
                        WaylandServerLibrary.class,
                        InterfaceMeta.class
                })
public class WlShellTest {

    @Test
    public void testGetShellSurface() throws Exception {

    }

    @Test
    public void testOnBindClient() throws Exception {

    }

    @Test
    public void testCreate() throws Exception {

    }
}