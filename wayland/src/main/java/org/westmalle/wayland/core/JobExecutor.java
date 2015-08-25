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
package org.westmalle.wayland.core;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.westmalle.wayland.nativ.libc.Libc;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class JobExecutor implements EventLoop.FileDescriptorEventHandler {

    private static final byte                 EVENT_NEW_JOB  = 1;
    private static final byte                 EVENT_FINISHED = 0;
    @Nonnull
    private static final LinkedList<Runnable> NO_JOBS        = new LinkedList<>();

    @Nonnull
    private final Pointer eventNewJobBuffer   = new Memory(1) {
        {
            setByte(0,
                    EVENT_NEW_JOB);
        }
    };
    @Nonnull
    private final Pointer eventFinishedBuffer = new Memory(1) {
        {
            setByte(0,
                    EVENT_FINISHED);
        }
    };
    @Nonnull
    private final Pointer eventReadBuffer     = new Memory(1);

    @Nonnull
    private final ReentrantLock        jobsLock    = new ReentrantLock();
    @Nonnull
    private final LinkedList<Runnable> pendingJobs = new LinkedList<>();
    @Nonnull
    private final Display display;
    private final int     pipeR;
    private final int     pipeWR;
    private final Libc    libc;
    @Nonnull
    private Optional<EventSource> eventSource = Optional.empty();

    @Inject
    JobExecutor(@Nonnull final Display display,
                final int pipeR,
                final int pipeWR,
                final Libc libc) {
        this.display = display;
        this.pipeR = pipeR;
        this.pipeWR = pipeWR;
        this.libc = libc;
    }

    public void start() {
        if (!this.eventSource.isPresent()) {
            this.eventSource = Optional.of(this.display.getEventLoop()
                                                       .addFileDescriptor(this.pipeR,
                                                                          EventLoop.EVENT_READABLE,
                                                                          this));
        }
        else {
            throw new IllegalStateException("Job executor already started.");
        }
    }

    public void fireFinishedEvent() {
        this.libc.write(this.pipeWR,
                        this.eventFinishedBuffer,
                        1);
    }

    public void submit(@Nonnull final Runnable job) {
        try {
            this.jobsLock.lock();
            this.pendingJobs.add(job);
            //wake up event thread
            fireNewJobEvent();
        }
        finally {
            this.jobsLock.unlock();
        }
    }

    private void fireNewJobEvent() {
        this.libc.write(this.pipeWR,
                        this.eventNewJobBuffer,
                        1);
    }

    @Override
    public int handle(final int fd,
                      final int mask) {
        final LinkedList<Runnable> jobs = commit();
        while (this.eventSource.isPresent()) {
            if (!(handleNextEvent(jobs))) {
                break;
            }
        }

        return 0;
    }

    private LinkedList<Runnable> commit() {
        LinkedList<Runnable> jobs = NO_JOBS;
        try {
            this.jobsLock.lock();
            if (!this.pendingJobs.isEmpty()) {
                jobs = new LinkedList<>(this.pendingJobs);
                this.pendingJobs.clear();
            }
        }
        finally {
            this.jobsLock.unlock();
        }
        return jobs;
    }

    private boolean handleNextEvent(final LinkedList<Runnable> jobs) {
        final byte event = read();
        if (event == EVENT_FINISHED) {
            clean();
            return false;
        }
        else if (event == EVENT_NEW_JOB) {
            jobs.pop()
                .run();
            return !jobs.isEmpty();
        }
        else {
            throw new IllegalStateException("Got illegal event code " + event);
        }
    }

    private byte read() {
        this.libc.read(this.pipeR,
                       this.eventReadBuffer,
                       1);
        return this.eventReadBuffer.getByte(0);
    }

    private void clean() {
        this.libc.close(this.pipeR);
        this.libc.close(this.pipeWR);
        this.eventSource.get()
                        .remove();
        this.eventSource = Optional.empty();
    }
}
