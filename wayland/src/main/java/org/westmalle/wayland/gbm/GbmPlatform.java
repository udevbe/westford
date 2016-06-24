package org.westmalle.wayland.gbm;

import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.core.Renderer;

import javax.annotation.Nonnull;

@AutoFactory
public class GbmPlatform implements Platform {

    GbmPlatform() {
    }

    @Nonnull
    @Override
    public Connector[] getConnectors() {
        return null;
    }

    @Override
    public void accept(@Nonnull final Renderer renderer) {

    }
}
