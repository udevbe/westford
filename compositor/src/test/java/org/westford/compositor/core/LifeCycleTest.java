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
package org.westford.compositor.core;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.shared.WlShmFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westford.Signal;
import org.westford.compositor.protocol.WlCompositor;
import org.westford.compositor.protocol.WlDataDeviceManager;
import org.westford.compositor.protocol.WlShell;
import org.westford.compositor.protocol.WlSubcompositor;
import org.westford.launch.LifeCycleSignals;

import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LifeCycleTest {

    @Mock
    private Display             display;
    @Mock
    private JobExecutor         jobExecutor;
    @Mock
    private WlCompositor        wlCompositor;
    @Mock
    private WlDataDeviceManager wlDataDeviceManager;
    @Mock
    private WlShell             wlShell;
    @Mock
    private WlSubcompositor     wlSubcompositor;
    @Mock
    private LifeCycleSignals    lifeCycleSignals;

    @InjectMocks
    private LifeCycle lifeCycle;

    @Before
    public void setUp() throws Exception {
        when(this.lifeCycleSignals.getStartSignal()).thenReturn(mock(Signal.class));
        when(this.lifeCycleSignals.getStopSignal()).thenReturn(mock(Signal.class));
        when(this.lifeCycleSignals.getActivateSignal()).thenReturn(mock(Signal.class));
        when(this.lifeCycleSignals.getDeactivateSignal()).thenReturn(mock(Signal.class));
    }

    @Test
    public void testRun() throws Exception {
        //given
        when(this.display.addShmFormat(WlShmFormat.ARGB8888.value)).thenReturn(1234);
        when(this.display.addShmFormat(WlShmFormat.XRGB8888.value)).thenReturn(5678);

        //when
        this.lifeCycle.start();
        //then
        verify(this.jobExecutor).start();
        verify(this.display).initShm();
        verify(this.display).addSocket(startsWith("wayland-"));
        verify(this.display).run();
    }

    @Test
    public void testShutDown() throws Exception {
        //given
        //when
        this.lifeCycle.stop();
        //then
        verify(this.display).terminate();
        verify(this.jobExecutor).fireFinishedEvent();
    }
}