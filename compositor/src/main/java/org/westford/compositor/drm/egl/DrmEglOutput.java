/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.compositor.drm.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Size;
import org.freedesktop.jaccall.Unsigned;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventSource;
import org.freedesktop.wayland.server.WlBufferResource;
import org.westford.compositor.core.Buffer;
import org.westford.compositor.core.EglOutput;
import org.westford.compositor.core.EglOutputState;
import org.westford.compositor.core.Output;
import org.westford.compositor.core.OutputMode;
import org.westford.compositor.core.Rectangle;
import org.westford.compositor.core.Region;
import org.westford.compositor.core.Scene;
import org.westford.compositor.core.Subscene;
import org.westford.compositor.core.Surface;
import org.westford.compositor.core.SurfaceView;
import org.westford.compositor.drm.DrmOutput;
import org.westford.compositor.drm.DrmPageFlipCallback;
import org.westford.compositor.gles2.Gles2Painter;
import org.westford.compositor.gles2.Gles2PainterFactory;
import org.westford.compositor.gles2.Gles2Renderer;
import org.westford.compositor.protocol.WlOutput;
import org.westford.compositor.protocol.WlSurface;
import org.westford.nativ.glibc.Libc;
import org.westford.nativ.libdrm.Libdrm;
import org.westford.nativ.libgbm.Libgbm;
import org.westford.nativ.libgbm.Pointerdestroy_user_data;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static org.westford.nativ.libdrm.Libdrm.DRM_MODE_PAGE_FLIP_EVENT;
import static org.westford.nativ.libgbm.Libgbm.GBM_FORMAT_ARGB8888;
import static org.westford.nativ.libgbm.Libgbm.GBM_FORMAT_XRGB8888;

@AutoFactory(allowSubclasses = true,
             className = "DrmEglOutputFactory")
public class DrmEglOutput implements EglOutput, DrmPageFlipCallback {

    @Nonnull
    private final Libc    libc;
    @Nonnull
    private final Libgbm  libgbm;
    @Nonnull
    private final Libdrm  libdrm;
    @Nonnull
    private final Display display;

    @Nonnull
    private final Gles2PainterFactory gles2PainterFactory;
    @Nonnull
    private final Gles2Renderer       gles2Renderer;

    @Nonnull
    private final Scene        scene;
    @Nonnull
    private final GbmBoFactory gbmBoFactory;
    private final int          drmFd;
    private final long         gbmSurface;
    @Nonnull
    private final DrmOutput    drmOutput;
    private final long         eglSurface;
    private final long         eglContext;
    private final long         eglDisplay;
    private final long         gbmDevice;
    @Nonnull
    private       GbmBo        gbmBo;
    @Nonnull
    private       GbmBo        nextGbmBo;
    private boolean               renderPending       = false;
    private boolean               pageFlipPending     = false;
    private Optional<Runnable>    afterPageFlipRender = Optional.empty();
    private Optional<EventSource> onIdleEventSource   = Optional.empty();
    private boolean enabled;
    private Optional<EglOutputState> state = Optional.empty();

    DrmEglOutput(@Nonnull @Provided final Libc libc,
                 @Nonnull @Provided final Libgbm libgbm,
                 @Nonnull @Provided final Libdrm libdrm,
                 @Nonnull @Provided final Display display,
                 @Nonnull @Provided final org.westford.compositor.gles2.Gles2PainterFactory gles2PainterFactory,
                 @Nonnull @Provided final Gles2Renderer gles2Renderer,
                 @Nonnull @Provided final Scene scene,
                 @Nonnull @Provided final GbmBoFactory gbmBoFactory,
                 final int drmFd,
                 final long gbmDevice,
                 @Nonnull final GbmBo gbmBo,
                 final long gbmSurface,
                 @Nonnull final DrmOutput drmOutput,
                 final long eglSurface,
                 final long eglContext,
                 final long eglDisplay) {
        this.libc = libc;
        this.libgbm = libgbm;
        this.libdrm = libdrm;
        this.display = display;
        this.gles2PainterFactory = gles2PainterFactory;
        this.gles2Renderer = gles2Renderer;
        this.scene = scene;
        this.gbmBoFactory = gbmBoFactory;
        this.drmFd = drmFd;
        this.gbmDevice = gbmDevice;
        this.gbmBo = gbmBo;
        this.nextGbmBo = gbmBo;
        this.gbmSurface = gbmSurface;
        this.drmOutput = drmOutput;
        this.eglSurface = eglSurface;
        this.eglContext = eglContext;
        this.eglDisplay = eglDisplay;
    }

    private void schedulePageFlip() {
        this.libdrm.drmModePageFlip(this.drmFd,
                                    this.drmOutput.getCrtcId(),
                                    getFbId(this.nextGbmBo),
                                    DRM_MODE_PAGE_FLIP_EVENT,
                                    Pointer.from(this).address);
        this.pageFlipPending = true;
    }

    private int getFbId(final GbmBo gbmBo) {
        final long fbIdP = this.libgbm.gbm_bo_get_user_data(gbmBo.getGbmBo());
        if (fbIdP != 0L) {
            return Pointer.wrap(Integer.class,
                                fbIdP)
                          .dref();
        }

        final Pointer<Integer> fb = Pointer.calloc(1,
                                                   Size.sizeof((Integer) null),
                                                   Integer.class);
        final long gbmBoPtr = this.gbmBo.getGbmBo();
        final int  format   = this.libgbm.gbm_bo_get_format(gbmBoPtr);
        final int  width    = this.libgbm.gbm_bo_get_width(gbmBoPtr);
        final int  height   = this.libgbm.gbm_bo_get_height(gbmBoPtr);
        final int  stride   = this.libgbm.gbm_bo_get_stride(gbmBoPtr);
        final int  handle   = (int) this.libgbm.gbm_bo_get_handle(gbmBoPtr);

        final Pointer<Integer> handles = Pointer.nref(handle,
                                                      0,
                                                      0,
                                                      0);
        final Pointer<Integer> pitches = Pointer.nref(stride,
                                                      0,
                                                      0,
                                                      0);
        final Pointer<Integer> offsets = Pointer.nref(new int[4]);

        final int ret = this.libdrm.drmModeAddFB2(this.drmFd,
                                                  width,
                                                  height,
                                                  format,
                                                  handles.address,
                                                  pitches.address,
                                                  offsets.address,
                                                  fb.address,
                                                  0);
        if (ret != 0) {
            throw new RuntimeException("failed to create fb");
        }

        this.libgbm.gbm_bo_set_user_data(gbmBoPtr,
                                         fb.address,
                                         Pointerdestroy_user_data.nref(this::destroyUserData).address);

        return fb.dref();
    }

    @Override
    public void onPageFlip(@Unsigned final int sequence,
                           @Unsigned final int tv_sec,
                           @Unsigned final int tv_usec) {
        this.gbmBo.close();

        this.gbmBo = this.nextGbmBo;
        this.pageFlipPending = false;

        this.afterPageFlipRender.ifPresent(Runnable::run);
        this.afterPageFlipRender = Optional.empty();
    }

    private void destroyUserData(@Ptr final long bo,
                                 @Ptr final long data) {
        final Pointer<Integer> fbIdP = Pointer.wrap(Integer.class,
                                                    data);
        final Integer fbId = fbIdP.dref();
        this.libdrm.drmModeRmFB(this.drmFd,
                                fbId);
        fbIdP.close();
    }

    private void doRender(@Nonnull final WlOutput wlOutput) {
        this.onIdleEventSource = Optional.empty();

        final Gles2Painter gles2Painter = this.gles2PainterFactory.create(this,
                                                                          wlOutput);
        try (final Gles2Painter painter = gles2Painter) {
            final Subscene subscene = this.scene.subsection(wlOutput.getOutput()
                                                                    .getRegion());
            paint(wlOutput,
                  painter,
                  subscene);

            //TODO paint cursors on separate overlay
            subscene.geCursorViews()
                    .forEach(painter::paint);
        }
        //FIXME how to compose different gbm_bos?
        if (gles2Painter.hasPainted()) {
            this.nextGbmBo = this.gbmBoFactory.create(gbmSurface);
        }
        schedulePageFlip();

        this.display.flushClients();
        this.renderPending = false;
    }

    private void paint(@Nonnull final WlOutput wlOutput,
                       final Gles2Painter gles2Painter,
                       final Subscene subscene) {

        //naive generic single pass, bottom to top overdraw rendering.
        final List<SurfaceView>     lockViews      = subscene.getLockViews();
        final Optional<SurfaceView> fullscreenView = subscene.getFullscreenView();

        if (!lockViews.isEmpty()) {
            lockViews.forEach(gles2Painter::paint);
            //lockscreen(s) hide(s) all other screens.
            return;
        }

        if (fullscreenView.isPresent() && paintFullscreen(gles2Painter,
                                                          wlOutput,
                                                          fullscreenView.get())) {
            //fullscreen view painted, don't bother painting underlying views
            return;
        }

        subscene.getBackgroundView()
                .ifPresent(gles2Painter::paint);
        subscene.getUnderViews()
                .forEach(gles2Painter::paint);
        subscene.getApplicationViews()
                .forEach(gles2Painter::paint);
        subscene.getOverViews()
                .forEach(gles2Painter::paint);
    }

    @Override
    public void disable() {
        this.afterPageFlipRender = Optional.empty();
        this.onIdleEventSource.ifPresent(EventSource::remove);
        this.enabled = false;
    }

    @Override
    public void enable(
            @Nonnull final WlOutput wlOutput) {
        this.enabled = true;
        render(wlOutput);
    }

    @Override
    public void render(
            @Nonnull final WlOutput wlOutput) {
        if (this.enabled) {
            scheduleRender(wlOutput);
        }
    }

    private void scheduleRender(final WlOutput wlOutput) {
        //TODO unit test 3 cases here: schedule idle, no-op when already scheduled, delayed render when pageflip pending

        //schedule a new render as soon as the pageflip ends, but only if we haven't scheduled one already
        if (this.pageFlipPending) {
            if (!this.afterPageFlipRender.isPresent()) {
                this.afterPageFlipRender = Optional.of(() -> whenIdleDoRender(wlOutput));
            }
        }
        //schedule a new render but only if we haven't scheduled one already.
        else if (!this.renderPending) {
            whenIdleDoRender(wlOutput);
        }
    }

    private void whenIdleDoRender(final WlOutput wlOutput) {
        this.renderPending = true;
        this.onIdleEventSource = Optional.of(this.display.getEventLoop()
                                                         .addIdle(() -> doRender(wlOutput)));
    }

    public void setDefaultMode() {
        final int fbId = getFbId(this.gbmBo);

        final int error = this.libdrm.drmModeSetCrtc(this.drmFd,
                                                     this.drmOutput.getCrtcId(),
                                                     fbId,
                                                     0,
                                                     0,
                                                     Pointer.nref(this.drmOutput.getDrmModeConnector()
                                                                                .connector_id()).address,
                                                     1,
                                                     Pointer.ref(this.drmOutput.getMode()).address);
        if (error != 0) {
            throw new RuntimeException(String.format("failed to drmModeSetCrtc. [%d]",
                                                     this.libc.getErrno()));
        }
    }


    private boolean paintFullscreen(final Gles2Painter gles2Painter,
                                    final WlOutput wlOutput,
                                    final SurfaceView surfaceView) {

        if (!surfaceView.isEnabled() || !surfaceView.isDrawable()) { return false; }

        final Output output = wlOutput.getOutput();

        final WlSurface wlSurface = (WlSurface) surfaceView.getWlSurfaceResource()
                                                           .getImplementation();
        final Surface surface = wlSurface.getSurface();
        final WlBufferResource wlBufferResource = surface.getState()
                                                         .getBuffer()
                                                         .get();
        final Buffer     buffer = this.gles2Renderer.queryBuffer(wlBufferResource);
        final OutputMode mode   = output.getMode();


        if (buffer.getWidth() == mode.getWidth() &&
            buffer.getHeight() == mode.getHeight()) {

            final GbmBo gbmBo = this.gbmBoFactory.create(this.gbmDevice,
                                                         wlBufferResource);
            final int format = getScanoutFormat(gbmBo,
                                                mode,
                                                surface);
            if (format == 0) {
                //no suitable scanout pixel format, fallback to gles2
                gbmBo.close();
                return gles2Painter.paint(surfaceView);
            }

            this.nextGbmBo = gbmBo;
            return true;
        }
        else {
            //no direct scanout possible, fallback to gles2
            return gles2Painter.paint(surfaceView);
        }
    }

    private int getScanoutFormat(final GbmBo clientGbmBo,
                                 final OutputMode mode,
                                 final Surface surface) {
        if (clientGbmBo.getGbmBo() == 0L) {
            return 0;
        }

        int clientFormat = this.libgbm.gbm_bo_get_format(clientGbmBo.getGbmBo());

        if (clientFormat == GBM_FORMAT_ARGB8888 && surface.getState()
                                                          .getOpaqueRegion()
                                                          .isPresent()) {
            final Region opaqueCopy = surface.getState()
                                             .getOpaqueRegion()
                                             .get()
                                             .copy();
            opaqueCopy.subtract(Rectangle.create(0,
                                                 0,
                                                 mode.getWidth(),
                                                 mode.getHeight()));
            if (opaqueCopy.isEmpty()) {
                clientFormat = GBM_FORMAT_XRGB8888;
            }
        }

        final int format = this.libgbm.gbm_bo_get_format(this.gbmBo.getGbmBo());
        if (format == clientFormat) {
            return clientFormat;
        }

        return 0;
    }


    @Override
    public long getEglSurface() {
        return this.eglSurface;
    }

    @Override
    public long getEglContext() {
        return this.eglContext;
    }

    @Override
    public long getEglDisplay() {
        return this.eglDisplay;
    }

    @Nonnull
    @Override
    public Optional<EglOutputState> getState() {
        return this.state;
    }

    @Override
    public void updateState(@Nonnull final EglOutputState eglOutputState) {
        this.state = Optional.of(eglOutputState);
    }

    @Nonnull
    public DrmOutput getDrmOutput() {
        return this.drmOutput;
    }
}
