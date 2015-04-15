package org.westmalle.wayland.platform.eglkms;


import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.nativewindow.egl.EGLGraphicsDevice;
import com.jogamp.opengl.egl.EGL;
import com.sun.jna.Pointer;

import org.westmalle.wayland.platform.CLibrary;
import org.westmalle.wayland.platform.eglkms.gbm.GbmLibrary;

import javax.inject.Inject;

import jogamp.newt.DisplayImpl;
import jogamp.opengl.egl.EGLDisplayUtil;

public class DisplayDriver extends DisplayImpl{

    private final CLibrary cLibrary;
    private final GbmLibrary gbmLibrary;
    private int fd;

    @Inject
    DisplayDriver(final CLibrary cLibrary,
                         final GbmLibrary gbmLibrary) {
        this.cLibrary = cLibrary;
        this.gbmLibrary = gbmLibrary;
    }

    @Override
    protected void createNativeImpl() {
        this.fd = this.cLibrary.open("/dev/dri/card0", CLibrary.O_RDWR);
        final Pointer gbmDevice = this.gbmLibrary.gbm_create_device(fd);
        final EGLGraphicsDevice
                eglGraphicsDevice =
                EGLDisplayUtil.eglCreateEGLGraphicsDevice(Pointer.nativeValue(gbmDevice),
                                                          AbstractGraphicsDevice.DEFAULT_CONNECTION,
                                                          AbstractGraphicsDevice.DEFAULT_UNIT);
        eglGraphicsDevice.open();


        final  String extensions = EGL.eglQueryString(eglGraphicsDevice.getHandle(), EGL.EGL_EXTENSIONS);

        if (!extensions.contains("EGL_KHR_surfaceless_opengl")) {
            System.err.println("no surfaceless support, cannot initialize\n");
            System.exit(1);
        }
    }

    @Override
    protected void closeNativeImpl(final AbstractGraphicsDevice aDevice) {
        this.fd = 0;
        aDevice.close();
    }

    @Override
    protected void dispatchMessagesNative() {
        //TODO
    }

    public int getFd() {
        return fd;
    }
}
