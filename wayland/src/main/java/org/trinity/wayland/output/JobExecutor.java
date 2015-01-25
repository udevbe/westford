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
package org.trinity.wayland.output;

import com.google.common.collect.Lists;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class JobExecutor implements EventLoop.FileDescriptorEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutor.class);


    private static final byte                 EVENT_NEW_JOB  = 1;
    private static final byte                 EVENT_FINISHED = 0;
    private static final LinkedList<Runnable> NO_JOBS        = new LinkedList<>();

    private final byte[] eventNewJobBuffer   = new byte[]{EVENT_NEW_JOB};
    private final byte[] eventFinishedBuffer = new byte[]{EVENT_FINISHED};
    private final byte[] eventReadBuffer     = new byte[1];

    private final ReentrantLock        jobsLock    = new ReentrantLock();
    private final LinkedList<Runnable> pendingJobs = Lists.newLinkedList();


    private Optional<EventSource> eventSource = Optional.empty();

    private final Display  display;
    private final int      pipeR;
    private final int      pipeWR;
    private final CLibrary libc;

    @Inject
    JobExecutor(final Display display,
                final int pipeR,
                final int pipeWR,
                final CLibrary libc) {
        this.display = display;
        this.pipeR = pipeR;
        this.pipeWR = pipeWR;
        this.libc = libc;
    }

    public void start() throws IOException {
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

    public void fireFinishedEvent() throws IOException {
        this.libc.write(this.pipeWR,
                        this.eventFinishedBuffer,
                        1);
    }

    public void submit(@Nonnull final Runnable job) {
        checkNotNull(job);

        try {
            this.jobsLock.lock();
            this.pendingJobs.add(job);
            //wake up event thread
            fireNewJobEvent();
        }
        catch (final IOException e) {
            //"rollback"
            this.pendingJobs.remove(job);
            LOGGER.error("Can not submit job",
                         e);
        }
        finally {
            this.jobsLock.unlock();
        }
    }

    private void clean() {
        this.libc.close(this.pipeR);
        this.libc.close(this.pipeWR);
        this.eventSource.get()
                        .remove();
        this.eventSource = Optional.empty();
    }

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
        return this.eventReadBuffer[0];
    }

    private LinkedList<Runnable> commit() {
        LinkedList<Runnable> jobs = NO_JOBS;
        try {
            this.jobsLock.lock();
            if (!this.pendingJobs.isEmpty()) {
                jobs = Lists.newLinkedList(this.pendingJobs);
                this.pendingJobs.clear();
            }
        }
        finally {
            this.jobsLock.unlock();
        }
        return jobs;
    }

    private void fireNewJobEvent() throws IOException {
        this.libc.write(this.pipeWR,
                        this.eventNewJobBuffer,
                        1);
    }
}
