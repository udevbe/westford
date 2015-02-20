package org.westmalle.wayland.output;

import org.freedesktop.wayland.server.Display;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ShellServiceTest {

    @Mock
    private Display display;
    @Mock
    private JobExecutor jobExecutor;
    @InjectMocks
    private ShellService shellService;

    @Test
    public void testStartUp() throws Exception {
        //given
        //when
        this.shellService.startUp();
        //then
        verify(this.jobExecutor,
               times(1)).start();
    }

    @Test
    public void testRun() throws Exception {
        //given
        //when
        this.shellService.run();
        //then
        verify(this.display,
               times(1)).initShm();
        verify(this.display,
               times(1)).addSocket(startsWith("wayland-"));
        verify(this.display,
               times(1)).run();
    }

    @Test
    public void testShutDown() throws Exception {
        //given
        //when
        this.shellService.shutDown();
        //then
        verify(this.display,
               times(1)).terminate();
        verify(this.jobExecutor,
               times(1)).fireFinishedEvent();
    }
}