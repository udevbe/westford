package org.westford.compositor.protocol;


import org.westford.compositor.core.PointerDevice;
import org.westford.compositor.core.PointerDeviceFactory;
import org.westford.compositor.core.RenderPlatform;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class WlPointerFactory {

    @Nonnull
    private final RenderPlatform          renderPlatform;
    @Nonnull
    private final PointerDeviceFactory    pointerDeviceFactory;
    @Nonnull
    private final PrivateWlPointerFactory privateWlPointerFactory;

    @Inject
    WlPointerFactory(@Nonnull final RenderPlatform renderPlatform,
                     @Nonnull final PointerDeviceFactory pointerDeviceFactory,
                     @Nonnull final PrivateWlPointerFactory privateWlPointerFactory) {
        this.renderPlatform = renderPlatform;
        this.pointerDeviceFactory = pointerDeviceFactory;
        this.privateWlPointerFactory = privateWlPointerFactory;
    }

    public WlPointer create() {
        final PointerDevice pointerDevice = this.pointerDeviceFactory.create();
        final WlPointer     wlPointer     = this.privateWlPointerFactory.create(pointerDevice);

        this.renderPlatform.getRenderOutputNewSignal()
                           .connect(event ->
                                            pointerDevice.getClampRegion()
                                                         .add(event.getRenderOutput()
                                                                   .getWlOutput()
                                                                   .getOutput()
                                                                   .getRegion()));
        //TODO unit test
        this.renderPlatform.getRenderOutputDestroyedSignal()
                           .connect(event -> {
                               pointerDevice.getClampRegion()
                                            .remove(event.getRenderOutput()
                                                         .getWlOutput()
                                                         .getOutput()
                                                         .getRegion());
                               pointerDevice.clamp(wlPointer.getResources(),
                                                   pointerDevice.getPosition());
                           });

        return wlPointer;
    }
}
