/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.core;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.jaccall.WaylandServerCore;
import org.westford.nativ.glibc.Libc;

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
    private final Pointer<Byte> eventNewJobBuffer   = Pointer.nref(EVENT_NEW_JOB);
    @Nonnull
    private final Pointer<Byte> eventFinishedBuffer = Pointer.nref(EVENT_FINISHED);
    @Nonnull
    private final Pointer<Byte> eventReadBuffer     = Pointer.nref((byte) 0);

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
                                                                          WaylandServerCore.WL_EVENT_READABLE,
                                                                          this));
        }
        else {
            throw new IllegalStateException("Job executor already started.");
        }
    }

    public void fireFinishedEvent() {
        this.libc.write(this.pipeWR,
                        this.eventFinishedBuffer.address,
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
                        this.eventNewJobBuffer.address,
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
                       this.eventReadBuffer.address,
                       1);
        return this.eventReadBuffer.dref();
    }

    private void clean() {
        this.libc.close(this.pipeR);
        this.libc.close(this.pipeWR);
        this.eventSource.get()
                        .remove();
        this.eventSource = Optional.empty();
    }
}
