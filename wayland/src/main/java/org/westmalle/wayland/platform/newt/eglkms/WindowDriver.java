package org.westmalle.wayland.platform.newt.eglkms;

import com.jogamp.nativewindow.AbstractGraphicsConfiguration;
import com.jogamp.nativewindow.AbstractGraphicsScreen;
import com.jogamp.nativewindow.Capabilities;
import com.jogamp.nativewindow.GraphicsConfigurationFactory;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.nativewindow.VisualIDHolder;
import com.jogamp.nativewindow.util.Insets;
import com.jogamp.nativewindow.util.Point;
import com.sun.jna.Pointer;

import org.westmalle.wayland.platform.newt.eglkms.gbm.GBM_BO;
import org.westmalle.wayland.platform.newt.eglkms.gbm.GBM_BO_FORMAT;
import org.westmalle.wayland.platform.newt.eglkms.gbm.GbmLibrary;

import jogamp.newt.WindowImpl;
import jogamp.newt.driver.linux.LinuxEventDeviceTracker;
import jogamp.newt.driver.linux.LinuxMouseTracker;

public class WindowDriver extends WindowImpl {

    private final LinuxMouseTracker linuxMouseTracker;
    private final LinuxEventDeviceTracker linuxEventDeviceTracker;

    public WindowDriver() {
        linuxMouseTracker = LinuxMouseTracker.getSingleton();
        linuxEventDeviceTracker = LinuxEventDeviceTracker.getSingleton();
    }

    @Override
    protected void createNativeImpl() {
        if(0!=getParentWindowHandle()) {
            throw new RuntimeException("Window parenting not supported (yet)");
        }

        final ScreenDriver screen = (ScreenDriver) getScreen();
        final DisplayDriver display = (DisplayDriver) screen.getDisplay();

        final AbstractGraphicsScreen aScreen = screen.getGraphicsScreen();

        final AbstractGraphicsConfiguration
                cfg = GraphicsConfigurationFactory
                .getFactory(getScreen().getDisplay().getGraphicsDevice(), capsRequested).chooseGraphicsConfiguration(
                        capsRequested, capsRequested, capabilitiesChooser, aScreen, VisualIDHolder.VID_UNDEFINED);
        if (null == cfg) {
            throw new NativeWindowException("Error choosing GraphicsConfiguration creating window: "+this);
        }
        final Capabilities chosenCaps = (Capabilities) cfg.getChosenCapabilities();
        // FIXME: Pass along opaque flag, since EGL doesn't determine it
        if(capsRequested.isBackgroundOpaque() != chosenCaps.isBackgroundOpaque()) {
            chosenCaps.setBackgroundOpaque(capsRequested.isBackgroundOpaque());
        }
        setGraphicsConfiguration(cfg);
        Pointer nativeWindowHandle = GbmLibrary.INSTANCE.gbm_bo_create(display.getGbmDevice(),
                                                                    screen.getWidth(),
                                                                    screen.getHeight(),
                                                                    GBM_BO_FORMAT.GBM_BO_FORMAT_XRGB8888,
                                                                    GBM_BO.GBM_BO_USE_SCANOUT | GBM_BO.GBM_BO_USE_RENDERING);
        if (nativeWindowHandle == null) {
            throw new NativeWindowException("Error creating egl window: "+cfg);
        }
        setWindowHandle(Pointer.nativeValue(nativeWindowHandle));
        if (0 == getWindowHandle()) {
            throw new NativeWindowException("Error native Window Handle is null");
        }

        addWindowListener(linuxEventDeviceTracker);
        addWindowListener(linuxMouseTracker);
        focusChanged(false, true);
    }

    @Override
    protected void closeNativeImpl() {
        removeWindowListener(linuxMouseTracker);
        removeWindowListener(linuxEventDeviceTracker);
    }

    @Override
    protected void requestFocusImpl(final boolean force) {
        focusChanged(false, true);
    }

    @Override
    protected boolean reconfigureWindowImpl(final int x,
                                            final int y,
                                            final int width,
                                            final int height,
                                            final int flags) {
        return false;
    }

    @Override
    protected Point getLocationOnScreenImpl(final int x,
                                            final int y) {
        return new Point(x,y);
    }

    @Override
    protected void updateInsetsImpl(final Insets insets) {

    }
}
