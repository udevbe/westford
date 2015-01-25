package org.trinity.wayland.output;

import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutorTest {

    @Mock
    private Display display;
    private final int pipeR  = 1;
    private final int pipeWR = 2;
    @Mock
    private CLibrary libc;

    private JobExecutor jobExecutor;

    @Before
    public void setUp() {
        this.jobExecutor = new JobExecutor(this.display,
                                           this.pipeR,
                                           this.pipeWR,
                                           this.libc);
    }

    @Test
    public void testSingleStart() throws Exception {
        //event loop mock
        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);
        final EventSource eventSource = mock(EventSource.class);
        when(eventLoop.addFileDescriptor(anyInt(),
                                         anyInt(),
                                         any())).thenReturn(eventSource);
        this.jobExecutor.start();
        Mockito.verify(eventLoop)
               .addFileDescriptor(eq(this.pipeR),
                                  eq(EventLoop.EVENT_READABLE),
                                  any());
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleStart() throws Exception {
        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);
        final EventSource eventSource = mock(EventSource.class);
        when(eventLoop.addFileDescriptor(anyInt(),
                                         anyInt(),
                                         any())).thenReturn(eventSource);
        this.jobExecutor.start();
        this.jobExecutor.start();
    }

    @Test
    public void testSingleFireFinishedEvent() throws Exception {
        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);
        final EventSource eventSource = mock(EventSource.class);
        when(eventLoop.addFileDescriptor(anyInt(),
                                         anyInt(),
                                         any())).thenReturn(eventSource);

        when(this.libc.write(eq(this.pipeWR),
                             aryEq(new byte[]{0}),
                             eq(1))).thenAnswer(invocation -> this.jobExecutor.handle(this.pipeR,
                                                                                      1234));
        doAnswer(invocation -> {
                     byte[] buffer = (byte[]) invocation.getArguments()[1];
                     //event finished
                     buffer[0] = 0;
                     return null;
                 }
                ).when(this.libc)
                 .read(eq(this.pipeR),
                       any(),
                       anyInt());

        this.jobExecutor.start();
        this.jobExecutor.fireFinishedEvent();

        verify(this.libc).write(eq(this.pipeWR),
                                aryEq(new byte[]{0}),
                                eq(1));
        verify(eventSource).remove();
        verify(this.libc).close(this.pipeR);
        verify(this.libc).close(this.pipeWR);
    }

    @Test
    public void testSingleSubmit() throws Exception {
        //event loop mock
        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);
        final EventSource eventSource = mock(EventSource.class);
        when(eventLoop.addFileDescriptor(anyInt(),
                                         anyInt(),
                                         any())).thenReturn(eventSource);

        when(this.libc.write(eq(this.pipeWR),
                             aryEq(new byte[]{1}),
                             eq(1))).then(invocation -> {
            this.jobExecutor.handle(this.pipeR,
                                    1234);
            return null;
        });

        doAnswer(invocation -> {
                     byte[] buffer = (byte[]) invocation.getArguments()[1];
                     //new job
                     buffer[0] = 1;
                     return null;
                 }
                ).when(this.libc)
                 .read(eq(this.pipeR),
                       any(),
                       anyInt());

        final Runnable job = mock(Runnable.class);
        this.jobExecutor.start();
        this.jobExecutor.submit(job);

        verify(this.libc).write(eq(this.pipeWR),
                                aryEq(new byte[]{1}),
                                eq(1));
        verify(job).run();
    }

    @Test
    public void testDoubleSubmit() throws Exception {
        //event loop mock
        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);
        final EventSource eventSource = mock(EventSource.class);
        when(eventLoop.addFileDescriptor(anyInt(),
                                         anyInt(),
                                         any())).thenReturn(eventSource);

        when(this.libc.write(eq(this.pipeWR),
                             aryEq(new byte[]{1}),
                             eq(1))).then(invocation -> {
            this.jobExecutor.handle(this.pipeR,
                                    1234);
            return null;
        });
        doAnswer(invocation -> {
                     byte[] buffer = (byte[]) invocation.getArguments()[1];
                     //new job
                     buffer[0] = 1;
                     return null;
                 }
                ).when(this.libc)
                 .read(eq(this.pipeR),
                       any(),
                       anyInt());

        final Runnable job = mock(Runnable.class);
        this.jobExecutor.start();
        this.jobExecutor.submit(job);
        this.jobExecutor.submit(job);

        verify(this.libc,
               times(2)).write(eq(this.pipeWR),
                               aryEq(new byte[]{1}),
                               eq(1));
        verify(job,
               times(2)).run();
    }

    @Test
    public void testSubmitFireFinishedEventSubmit() throws Exception {
        //event loop mock
        final EventLoop eventLoop = mock(EventLoop.class);
        when(this.display.getEventLoop()).thenReturn(eventLoop);
        final EventSource eventSource = mock(EventSource.class);
        when(eventLoop.addFileDescriptor(anyInt(),
                                         anyInt(),
                                         any())).thenReturn(eventSource);

        //new job event mock behavior
        when(this.libc.write(eq(this.pipeWR),
                             aryEq(new byte[]{1}),
                             eq(1))).then(writeAnswer -> {
            doAnswer(readAnswer -> {
                         byte[] buffer = (byte[]) readAnswer.getArguments()[1];
                         //new job
                         buffer[0] = 1;
                         return null;
                     }
                    ).when(this.libc)
                     .read(eq(this.pipeR),
                           any(),
                           anyInt());
            this.jobExecutor.handle(this.pipeR,
                                    1234);
            return null;
        });

        //finished event mock behavior
        when(this.libc.write(eq(this.pipeWR),
                             aryEq(new byte[]{0}),
                             eq(1))).thenAnswer(writeAnswer -> {
            doAnswer(readAnswer -> {
                         byte[] buffer = (byte[]) readAnswer.getArguments()[1];
                         //event finished
                         buffer[0] = 0;
                         return null;
                     }
                    ).when(this.libc)
                     .read(eq(this.pipeR),
                           any(),
                           anyInt());
            this.jobExecutor.handle(this.pipeR,
                                    1234);
            return null;
        });

        this.jobExecutor.start();
        final Runnable job = mock(Runnable.class);
        this.jobExecutor.submit(job);
        this.jobExecutor.fireFinishedEvent();
        this.jobExecutor.submit(job);

        verify(this.libc,
               times(2)).write(eq(this.pipeWR),
                               aryEq(new byte[]{1}),
                               eq(1));
        verify(this.libc).write(eq(this.pipeWR),
                                aryEq(new byte[]{0}),
                                eq(1));

        verify(job).run();
        verify(eventSource).remove();
        verify(this.libc).close(this.pipeR);
        verify(this.libc).close(this.pipeWR);
        verifyNoMoreInteractions(job);
    }
}