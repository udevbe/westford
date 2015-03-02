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
    public void testStartUp() throws Exception {
        //given
        //when
        this.shellService.startUp();
        //then
        verify(this.jobExecutor).start();
    }

    @Test
    public void testRun() throws Exception {
        //given
        //when
        this.shellService.run();
        //then
        verify(this.display).initShm();
        verify(this.display).addSocket(startsWith("wayland-"));
        verify(this.display).run();
    }

    @Test
    public void testShutDown() throws Exception {
        //given
        //when
        this.shellService.shutDown();
        //then
        verify(this.display).terminate();
        verify(this.jobExecutor).fireFinishedEvent();
    }
}