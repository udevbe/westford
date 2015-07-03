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

import org.freedesktop.wayland.server.Display;
import org.westmalle.wayland.nativ.Libc;
import org.westmalle.wayland.nativ.NativeModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = NativeModule.class)
public class OutputModule {

    @Provides
    @Singleton
    InfiniteRegion provideInfiniteRegion(final FiniteRegionFactory finiteRegionFactory) {
        return new InfiniteRegion(finiteRegionFactory);
    }

    @Provides
    @Singleton
    NullRegion provideNullRegion() {
        return new NullRegion();
    }

    @Provides
    @Singleton
    Display provideDisplay() {
        return Display.create();
    }

    @Singleton
    @Provides
    JobExecutor provideJobExecutor(final Display display,
                                   final Libc libc) {
        final int[] pipeFds = new int[2];
        libc.pipe(pipeFds);
        final int[] pipe = configure(pipeFds,
                                     libc);
        final int pipeR  = pipe[0];
        final int pipeWR = pipe[1];

        return new JobExecutor(display,
                               pipeR,
                               pipeWR,
                               libc);
    }

    private int[] configure(final int[] pipeFds,
                            final Libc libc) {
        final int readFd  = pipeFds[0];
        final int writeFd = pipeFds[1];

        final int readFlags = libc.fcntl(readFd,
                                         Libc.F_GETFD,
                                         0);
        libc.fcntl(readFd,
                   Libc.F_SETFD,
                   readFlags | Libc.FD_CLOEXEC);

        final int writeFlags = libc.fcntl(writeFd,
                                          Libc.F_GETFD,
                                          0);
        libc.fcntl(writeFd,
                   Libc.F_SETFD,
                   writeFlags | Libc.FD_CLOEXEC);

        return pipeFds;
    }
}
