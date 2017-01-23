package org.westford.compositor.wlshell;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.wayland.server.WlBufferResource;
import org.freedesktop.wayland.server.WlSurfaceResource;
import org.westford.compositor.core.Buffer;
import org.westford.compositor.core.EglOutput;
import org.westford.compositor.core.FullscreenRenderer;
import org.westford.compositor.core.RenderOutput;
import org.westford.compositor.drm.egl.DrmEglOutput;
import org.westford.compositor.gles2.Gles2Renderer;
import org.westford.compositor.x11.egl.X11EglOutput;

import javax.annotation.Nonnull;

@AutoFactory(allowSubclasses = true,
             className = "ShellSurfaceFullscreenRendererFactory")
public class ShellSurfaceFullscreenRenderer implements FullscreenRenderer {


    //fallback renderer
    private final Gles2Renderer gles2Renderer;

    ShellSurfaceFullscreenRenderer(@Provided Gles2Renderer gles2Renderer) {
        this.gles2Renderer = gles2Renderer;
    }

    @Override
    public void visit(final DrmEglOutput drmEglOutput) {
        //TODO can do direct scanout, use 'real' fullscreen

    }

    @Override
    public void visit(final X11EglOutput x11EglOutput) {
        //can't do direct scanout, use 'fake' fullscreen
        //TODO pass our wlsurface?
//        gles2Renderer.render(x11EglOutput,
//                             Collections.singleton(null));
    }

    @Override
    public void visit(@Nonnull final RenderOutput renderOutput) {
        //can't render anything. not enough information.
        throw new UnsupportedOperationException(String.format("Need an egl capable renderOutput. Got %s",
                                                              renderOutput));
    }

    @Override
    public void visit(@Nonnull final EglOutput eglConnector) {
        //TODO can't do direct scanout, use 'fake' fullscreen
        //TODO pass our wlsurface?
//        gles2Renderer.render(x11EglOutput,
//                             Collections.singleton(null));
    }

    @Override
    public void onDestroy(@Nonnull final WlSurfaceResource wlSurfaceResource) {

    }

    @Nonnull
    @Override
    public Buffer queryBuffer(@Nonnull final WlBufferResource wlBufferResource) {
        return null;
    }
}
