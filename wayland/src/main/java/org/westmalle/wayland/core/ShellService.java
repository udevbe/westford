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
import org.freedesktop.wayland.shared.WlShmFormat;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class ShellService {

    @Nonnull
    private final Display     display;
    @Nonnull
    private final JobExecutor jobExecutor;

    @Inject
    ShellService(@Nonnull final Display display,
                 @Nonnull final JobExecutor jobExecutor) {
        this.display = display;
        this.jobExecutor = jobExecutor;
    }

    public void start() {
        this.jobExecutor.start();
        this.display.initShm();
        this.display.addSocket("wayland-0");
        this.display.run();
    }

    public void stop() {
        this.display.terminate();
        this.jobExecutor.fireFinishedEvent();
    }
}