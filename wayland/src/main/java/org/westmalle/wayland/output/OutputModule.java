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

import com.google.common.util.concurrent.Service;

import org.freedesktop.wayland.server.Display;
import org.westmalle.wayland.platform.Libc;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static dagger.Provides.Type.SET;

@Module
public class OutputModule {

    @Provides
    @Singleton
    GLDrawables provideGLGlDrawables() {
        return new GLDrawables();
    }

    @Provides
    @Singleton
    Display provideDisplay() {
        return Display.create();
    }

    @Singleton
    @Provides
    JobExecutor provideWlJobExecutor(final Display display) {
        final int[] pipe = configure(pipe());
        final int pipeR  = pipe[0];
        final int pipeWR = pipe[1];

        return new JobExecutor(display,
                               pipeR,
                               pipeWR);
    }

    private int[] pipe() {
        final int[] pipeFds = new int[2];
        Libc.pipe(pipeFds);
        return pipeFds;
    }

    private int[] configure(final int[] pipeFds) {
        final int readFd  = pipeFds[0];
        final int writeFd = pipeFds[1];

        final int readFlags = Libc.fcntl(readFd,
                                         Libc.F_GETFD,
                                         0);
        Libc.fcntl(readFd,
                   Libc.F_SETFD,
                   readFlags | Libc.FD_CLOEXEC);

        final int writeFlags = Libc.fcntl(writeFd,
                                          Libc.F_GETFD,
                                          0);
        Libc.fcntl(writeFd,
                   Libc.F_SETFD,
                   writeFlags | Libc.FD_CLOEXEC);

        return pipeFds;
    }

    @Provides(type = SET)
    Service provideService(final ShellService shellService) {
        return shellService;
    }
}
