package org.westford.compositor.core;


import org.freedesktop.jaccall.Pointer;
import org.westford.nativ.libpixman1.Libpixman1;
import org.westford.nativ.libpixman1.pixman_region32;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static org.freedesktop.jaccall.Pointer.malloc;

public class FiniteRegionFactory {

    @Nonnull
    private final PrivateFiniteRegionFactory privateFiniteRegionFactory;
    @Nonnull
    private final Libpixman1                 libpixman1;

    @Inject
    FiniteRegionFactory(@Nonnull final PrivateFiniteRegionFactory privateFiniteRegionFactory,
                        @Nonnull final Libpixman1 libpixman1) {
        this.privateFiniteRegionFactory = privateFiniteRegionFactory;
        this.libpixman1 = libpixman1;
    }


    public FiniteRegion create() {
        final Pointer<pixman_region32> pixman_region32Pointer = malloc(pixman_region32.SIZE,
                                                                       pixman_region32.class);
        this.libpixman1.pixman_region32_init(pixman_region32Pointer.address);

        return create(pixman_region32Pointer);
    }

    public FiniteRegion create(final Pointer<pixman_region32> pixman_region32Pointer) {
        return this.privateFiniteRegionFactory.create(pixman_region32Pointer);
    }
}
