package org.westmalle.wayland.egl;

import dagger.Subcomponent;

@Subcomponent
public interface EglComponent {
    EglRenderEngineFactory renderEngineFactory();
}
