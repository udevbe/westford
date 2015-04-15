package org.westmalle.wayland.platform.newt.eglkms;


import com.jogamp.nativewindow.util.Insets;
import com.jogamp.nativewindow.util.Point;
import jogamp.newt.WindowImpl;

public class WindowDriver extends WindowImpl {

    @Override
    protected void createNativeImpl() {

    }

    @Override
    protected void closeNativeImpl() {

    }

    @Override
    protected void requestFocusImpl(final boolean force) {

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
        return null;
    }

    @Override
    protected void updateInsetsImpl(final Insets insets) {

    }
}
