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
import org.westmalle.wayland.protocol.WlCompositor;
import org.westmalle.wayland.protocol.WlDataDeviceManager;
import org.westmalle.wayland.protocol.WlShell;
import org.westmalle.wayland.protocol.WlSubcompositor;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class LifeCycle {

    @Nonnull
    private final Display             display;
    @Nonnull
    private final JobExecutor         jobExecutor;
    private final WlCompositor        wlCompositor;
    private final WlDataDeviceManager wlDataDeviceManager;
    private final WlShell             wlShell;
    private final WlSubcompositor     wlSubcompositor;

    @Inject
    LifeCycle(@Nonnull final Display display,
              @Nonnull final JobExecutor jobExecutor,
              @Nonnull final WlCompositor wlCompositor,
              @Nonnull final WlDataDeviceManager wlDataDeviceManager,
              @Nonnull final WlShell wlShell,
              @Nonnull final WlSubcompositor wlSubcompositor) {
        this.display = display;
        this.jobExecutor = jobExecutor;
        this.wlCompositor = wlCompositor;
        this.wlDataDeviceManager = wlDataDeviceManager;
        this.wlShell = wlShell;
        this.wlSubcompositor = wlSubcompositor;
    }

    public void start() {
        this.jobExecutor.start();
        this.display.initShm();
        this.display.addSocket("wayland-0");
        this.display.run();
    }

    public void stop() {
        this.wlCompositor.destroy();
        this.wlDataDeviceManager.destroy();
        this.wlShell.destroy();
        this.wlSubcompositor.destroy();

        this.display.terminate();
        this.jobExecutor.fireFinishedEvent();
    }
}