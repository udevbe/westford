package org.westmalle.wayland.bootstrap.drm.indirect;


import dagger.Component;
import org.westmalle.launch.Launcher;
import org.westmalle.launch.indirect.IndirectModule;
import org.westmalle.tty.TtyModule;
import org.westmalle.wayland.bootstrap.drm.DrmEglCompositor;
import org.westmalle.wayland.core.CoreModule;
import org.westmalle.wayland.drm.egl.DrmEglPlatformModule;
import org.westmalle.wayland.gles2.Gles2RendererModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CoreModule.class,
                      Gles2RendererModule.class,
                      DrmEglPlatformModule.class,
                      TtyModule.class,
                      IndirectModule.class})
public interface IndirectDrmEglCompositor extends DrmEglCompositor {
    Launcher launcher();
}
