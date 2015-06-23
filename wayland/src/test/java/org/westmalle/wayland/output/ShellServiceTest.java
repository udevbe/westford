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
package org.westmalle.wayland.output;

import org.freedesktop.wayland.server.Display;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ShellServiceTest {

    @Mock
    private Display      display;
    @Mock
    private JobExecutor  jobExecutor;
    @InjectMocks
    private ShellService shellService;

    @Test
    public void testRun() throws Exception {
        //given
        //when
        this.shellService.start();
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
        this.shellService.stop();
        //then
        verify(this.display).terminate();
        verify(this.jobExecutor).fireFinishedEvent();
    }
}