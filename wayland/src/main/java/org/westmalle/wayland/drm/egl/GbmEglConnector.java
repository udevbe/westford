//Copyright 2016 Erik De Rijcke
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
package org.westmalle.wayland.drm.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Size;
import org.freedesktop.jaccall.Unsigned;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.EventSource;
import org.westmalle.wayland.core.EglConnector;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.drm.DrmConnector;
import org.westmalle.wayland.drm.DrmPageFlipCallback;
import org.westmalle.wayland.nativ.libdrm.Libdrm;
import org.westmalle.wayland.nativ.libgbm.Libgbm;
import org.westmalle.wayland.nativ.libgbm.Pointerdestroy_user_data;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.westmalle.wayland.nativ.libdrm.Libdrm.DRM_MODE_PAGE_FLIP_EVENT;

//TODO put all gbm/egl specifics here
@AutoFactory(allowSubclasses = true,
             className = "GbmEglConnectorFactory")
public class GbmEglConnector implements EglConnector, DrmPageFlipCallback {

    @Nonnull
    private final Libgbm  libgbm;
    @Nonnull
    private final Libdrm  libdrm;
    @Nonnull
    private final Display display;

    private final int          drmFd;
    private       long         gbmBo;
    private final long         gbmSurface;
    @Nonnull
    private final DrmConnector drmConnector;
    private final long         eglSurface;
    private final long         eglContext;
    private final long         eglDisplay;

    private long nextGbmBo;

    private Optional<EventSource>           renderJobEvent   = Optional.empty();
    private Optional<EventLoop.IdleHandler> delayedRenderJob = Optional.empty();

    private boolean pageFlipPending;

    GbmEglConnector(@Nonnull @Provided final Libgbm libgbm,
                    @Nonnull @Provided final Libdrm libdrm,
                    @Nonnull @Provided final Display display,
                    final int drmFd,
                    final long gbmBo,
                    final long gbmSurface,
                    @Nonnull final DrmConnector drmConnector,
                    final long eglSurface,
                    final long eglContext,
                    final long eglDisplay) {
        this.libgbm = libgbm;
        this.libdrm = libdrm;
        this.display = display;
        this.drmFd = drmFd;
        this.gbmBo = gbmBo;
        this.gbmSurface = gbmSurface;
        this.drmConnector = drmConnector;
        this.eglSurface = eglSurface;
        this.eglContext = eglContext;
        this.eglDisplay = eglDisplay;
    }

    @Override
    public void end() {
        this.nextGbmBo = this.libgbm.gbm_surface_lock_front_buffer(this.gbmSurface);
        this.libdrm.drmModePageFlip(this.drmFd,
                                    this.drmConnector.getCrtcId(),
                                    getFbId(this.nextGbmBo),
                                    DRM_MODE_PAGE_FLIP_EVENT,
                                    Pointer.from(this).address);
        this.pageFlipPending = true;
    }

    @Override
    public void onPageFlip(@Unsigned final int sequence,
                           @Unsigned final int tv_sec,
                           @Unsigned final int tv_usec) {
        this.libgbm.gbm_surface_release_buffer(this.gbmSurface,
                                               this.gbmBo);
        this.gbmBo = this.nextGbmBo;
        this.pageFlipPending = false;

        this.delayedRenderJob.ifPresent(render -> {
            assert (!this.renderJobEvent.isPresent());
            whenIdle(render);
            this.delayedRenderJob = Optional.empty();
        });
    }

    public int getFbId(final long gbmBo) {
        final long fbIdP = this.libgbm.gbm_bo_get_user_data(gbmBo);
        if (fbIdP != 0L) {
            return Pointer.wrap(Integer.class,
                                fbIdP)
                          .dref();
        }

        final Pointer<Integer> fb = Pointer.calloc(1,
                                                   Size.sizeof((Integer) null),
                                                   Integer.class);
        final int width  = this.libgbm.gbm_bo_get_width(gbmBo);
        final int height = this.libgbm.gbm_bo_get_height(gbmBo);
        final int stride = this.libgbm.gbm_bo_get_stride(gbmBo);
        final int handle = (int) this.libgbm.gbm_bo_get_handle(gbmBo);
        final int ret = this.libdrm.drmModeAddFB(this.drmFd,
                                                 width,
                                                 height,
                                                 (byte) 24,
                                                 (byte) 32,
                                                 stride,
                                                 handle,
                                                 fb.address);
        if (ret != 0) {
            throw new RuntimeException("failed to create fb");
        }

        this.libgbm.gbm_bo_set_user_data(gbmBo,
                                         fb.address,
                                         Pointerdestroy_user_data.nref(this::destroyUserData).address);

        return fb.dref();
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
    public WlOutput getWlOutput() {
        return this.drmConnector.getWlOutput();
    }

    @Nonnull
    public DrmConnector getDrmConnector() {
        return this.drmConnector;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {
        //TODO unit test 3 cases here: schedule idle, no-op when already scheduled, delayed render when pageflip pending

        if (this.pageFlipPending) {
            if (!this.delayedRenderJob.isPresent()) {
                whenPageFlip(() -> renderOn(renderer));
            }
        }
        else if (!this.renderJobEvent.isPresent()) {
            whenIdle(() -> renderOn(renderer));
        }
    }

    private void whenPageFlip(final EventLoop.IdleHandler idleHandler) {
        this.delayedRenderJob = Optional.of(idleHandler);
    }

    private void whenIdle(final EventLoop.IdleHandler idleHandler) {
        this.renderJobEvent = Optional.of(this.display.getEventLoop()
                                                      .addIdle(idleHandler));
    }

    private void renderOn(@Nonnull final Renderer renderer) {
        this.renderJobEvent.get()
                           .remove();
        this.renderJobEvent = Optional.empty();
        renderer.visit(this);
        this.display.flushClients();
    }
}
