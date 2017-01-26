package org.westford.compositor.core;


import javax.annotation.Nonnull;
import javax.inject.Inject;

public class PointerDeviceFactory {

    @Nonnull
    private final PrivatePointerDeviceFactory privatePointerDeviceFactory;
    @Nonnull
    private final RenderPlatform              renderPlatform;
    @Nonnull
    private final FiniteRegionFactory         finiteRegionFactory;

    @Inject
    PointerDeviceFactory(@Nonnull final PrivatePointerDeviceFactory privatePointerDeviceFactory,
                         @Nonnull final RenderPlatform renderPlatform,
                         @Nonnull final FiniteRegionFactory finiteRegionFactory) {
        this.privatePointerDeviceFactory = privatePointerDeviceFactory;
        this.renderPlatform = renderPlatform;
        this.finiteRegionFactory = finiteRegionFactory;
    }

    public PointerDevice create() {
        final FiniteRegion  outputsRegion = this.finiteRegionFactory.create();
        final PointerDevice pointerDevice = this.privatePointerDeviceFactory.create(outputsRegion);

        this.renderPlatform.getWlOutputs()
                           .forEach(wlOutput ->
                                            outputsRegion.add(wlOutput.getOutput()
                                                                      .getRegion()));
        return pointerDevice;
    }
}
