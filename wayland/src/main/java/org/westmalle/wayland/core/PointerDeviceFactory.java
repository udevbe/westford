package org.westmalle.wayland.core;


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

    PointerDevice create() {
        final FiniteRegion outputsRegion = this.finiteRegionFactory.create();
        this.renderPlatform.getRenderOutputs()
                           .forEach(renderOutput ->
                                            outputsRegion.add(renderOutput.getWlOutput()
                                                                          .getOutput()
                                                                          .getRegion()));
        //listen for add/removal of outputs
        this.renderPlatform.getRenderOutputNewSignal()
                           .connect(event ->
                                            outputsRegion.add(event.getRenderOutput()
                                                                   .getWlOutput()
                                                                   .getOutput()
                                                                   .getRegion()));
        this.renderPlatform.getRenderOutputDestroyedSignal()
                           .connect(event ->
                                            outputsRegion.remove(event.getRenderOutput()
                                                                      .getWlOutput()
                                                                      .getOutput()
                                                                      .getRegion()));

        return this.privatePointerDeviceFactory.create(outputsRegion);
    }
}
