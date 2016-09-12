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
    private final WlCompositor        wlCompositor;
    @Nonnull
    private final WlDataDeviceManager wlDataDeviceManager;
    @Nonnull
    private final WlShell             wlShell;
    @Nonnull
    private final WlSubcompositor     wlSubcompositor;

    @Nonnull
    private final LifeCycleSignals lifeCycleSignals;
    @Nonnull
    private final Display          display;
    @Nonnull
    private final JobExecutor      jobExecutor;

    @Inject
    LifeCycle(@Nonnull final LifeCycleSignals lifeCycleSignals,
              @Nonnull final Display display,
              @Nonnull final JobExecutor jobExecutor,
              @Nonnull final WlCompositor wlCompositor,
              @Nonnull final WlDataDeviceManager wlDataDeviceManager,
              @Nonnull final WlShell wlShell,
              @Nonnull final WlSubcompositor wlSubcompositor) {
        this.lifeCycleSignals = lifeCycleSignals;
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
        this.lifeCycleSignals.getStartSignal()
                             .emit("START");
        this.display.run();
    }

    public void stop() {
        this.wlCompositor.destroy();
        this.wlDataDeviceManager.destroy();
        this.wlShell.destroy();
        this.wlSubcompositor.destroy();

        this.lifeCycleSignals.getStopSignal()
                             .emit("CLOSE");
        this.jobExecutor.fireFinishedEvent();
        this.display.terminate();
    }
}
