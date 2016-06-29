package org.westmalle.wayland.drm.egl;


import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;
import org.westmalle.wayland.core.EglConnector;
import org.westmalle.wayland.drm.DrmConnector;
import org.westmalle.wayland.nativ.libdrm.Libdrm;
import org.westmalle.wayland.nativ.libdrm.page_flip_handler;
import org.westmalle.wayland.nativ.libgbm.Libgbm;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.westmalle.wayland.nativ.libdrm.Libdrm.DRM_MODE_PAGE_FLIP_EVENT;

//TODO put all gbm/egl specifics here
@AutoFactory(allowSubclasses = true,
             className = "GbmEglConnectorFactory")
public class GbmEglConnector implements EglConnector {

    @Nonnull
    private final Libgbm                     libgbm;
    @Nonnull
    private final Libdrm                     libdrm;
    private final int                        drmFd;
    private       long                       gbmBo;
    private final long                       gbmSurface;
    private final int                        fbId;
    @Nonnull
    private final Pointer<page_flip_handler> pageFlipHandler;
    @Nonnull
    private final DrmConnector               drmConnector;

    GbmEglConnector(@Nonnull @Provided final Libgbm libgbm,
                    @Nonnull @Provided final Libdrm libdrm,
                    final int drmFd,
                    final long gbmBo,
                    final long gbmSurface,
                    final int fbId,
                    @Nonnull
                    final Pointer<page_flip_handler> pageFlipHandler,
                    @Nonnull final DrmConnector drmConnector) {
        this.libgbm = libgbm;
        this.libdrm = libdrm;
        this.drmFd = drmFd;
        this.gbmBo = gbmBo;
        this.gbmSurface = gbmSurface;
        this.fbId = fbId;
        this.pageFlipHandler = pageFlipHandler;
        this.drmConnector = drmConnector;
    }

    @Override
    public void end() {
        final long gbmBo = this.libgbm.gbm_surface_lock_front_buffer(this.gbmSurface);
        this.libdrm.drmModeSetCrtc(this.drmFd,
                                   this.drmConnector.getCrtcId(),
                                   this.fbId,
                                   0,
                                   0,
                                   Pointer.nref(this.drmConnector.getDrmModeConnector()
                                                                 .connector_id()).address,
                                   1,
                                   Pointer.ref(this.drmConnector.getMode()).address);
        this.libdrm.drmModePageFlip(this.drmFd,
                                    this.drmConnector.getCrtcId(),
                                    this.fbId,
                                    DRM_MODE_PAGE_FLIP_EVENT,
                                    0L);
        //TODO handle drmFd events by registering it as an event source

        this.libdrm.drmHandleEvent(this.drmFd,
                                   this.pageFlipHandler.address);

        this.libgbm.gbm_surface_release_buffer(this.gbmSurface,
                                               this.gbmBo);
        this.gbmBo = gbmBo;
    }

    private void pageFlipHandler(final int fd,
                                 @Unsigned final int sequence,
                                 @Unsigned final int tv_sec,
                                 @Unsigned final int tv_usec,
                                 @Ptr final long user_data) {
        Pointer.wrap(Integer.class,
                     user_data)
               .write(0);
    }

    @Override
    public long getEglSurface() {
        return 0;
    }

    @Nonnull
    @Override
    public Optional<WlOutput> getWlOutput() {
        return this.drmConnector.getWlOutput();
    }

    @Nonnull
    public DrmConnector getDrmConnector() {
        return this.drmConnector;
    }
}
