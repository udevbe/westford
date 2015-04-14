package org.westmalle.wayland.platform.eglkms;


import com.jogamp.opengl.egl.EGL;
import com.sun.jna.Pointer;

import org.westmalle.wayland.platform.CLibrary;
import org.westmalle.wayland.platform.eglkms.drm.DrmLibrary;
import org.westmalle.wayland.platform.eglkms.drm.drmModeConnection;
import org.westmalle.wayland.platform.eglkms.drm.drmModeConnector;
import org.westmalle.wayland.platform.eglkms.drm.drmModeEncoder;
import org.westmalle.wayland.platform.eglkms.drm.drmModeRes;

import java.nio.IntBuffer;

import javax.inject.Inject;

public class EglKMSDisplayFactory {

    private final CLibrary cLibrary;
    private final GbmLibrary gbmLibrary;
    private final DrmLibrary drmLibrary;

    @Inject
    EglKMSDisplayFactory(final CLibrary cLibrary,
                         final GbmLibrary gbmLibrary,
                         final DrmLibrary drmLibrary) {
        this.cLibrary = cLibrary;
        this.gbmLibrary = gbmLibrary;
        this.drmLibrary = drmLibrary;
    }

    public long create(){
        final int fd = this.cLibrary.open("/dev/dri/card0", CLibrary.O_RDWR);
        final Pointer gbmDevice = this.gbmLibrary.gbm_create_device(fd);
        final long dpy = EGL.eglGetDisplay(Pointer.nativeValue(gbmDevice));

        final IntBuffer minor = IntBuffer.allocate(1);
        final IntBuffer major = IntBuffer.allocate(1);
        //TODO check return value?
        EGL.eglInitialize(dpy, major, minor);

        final  String ver = EGL.eglQueryString(dpy, EGL.EGL_VERSION);
        System.out.println(String.format("Found EGL version %s",ver));
        final  String extensions = EGL.eglQueryString(dpy, EGL.EGL_EXTENSIONS);

        if (!extensions.contains("EGL_KHR_surfaceless_opengl")) {
            System.err.println("no surfaceless support, cannot initialize\n");
            System.exit(1);
        }



    /* Find the first available connector with modes */
        drmModeRes resources;
        drmModeConnector connector = null;
        drmModeEncoder encoder;
        int i;

        resources = this.drmLibrary.drmModeGetResources(fd);
        if (resources == null) {
            System.err.println("drmModeGetResources failed\n");
            System.exit(1);
        }

        for (i = 0; i < resources.count_connectors; i++) {
            connector = this.drmLibrary.drmModeGetConnector(fd, resources.connectors.getInt(i*4));
            if (connector == null)
                continue;

            if (connector.connection == drmModeConnection.DRM_MODE_CONNECTED &&
                           connector.count_modes > 0)
                break;

            this.drmLibrary.drmModeFreeConnector(connector);
        }

        if (i == resources.count_connectors) {
            System.err.println("No currently active connector found.\n");
            System.exit(1);
        }

        for (i = 0; i < resources.count_encoders; i++) {
            encoder = this.drmLibrary.drmModeGetEncoder(fd, resources.encoders.getInt(i*4));

            if (encoder == null)
                continue;

            if (encoder.encoder_id == connector.encoder_id)
                break;

            this.drmLibrary.drmModeFreeEncoder(encoder);
        }





        return dpy;
    }
}
