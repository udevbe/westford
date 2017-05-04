package org.westford.compositor.core

import org.freedesktop.jaccall.Pointer
import org.freedesktop.jaccall.Pointer.malloc
import org.westford.nativ.libpixman1.Libpixman1
import org.westford.nativ.libpixman1.pixman_region32
import org.westford.nativ.libpixman1.Struct_pixman_region32
import javax.inject.Inject

class FiniteRegionFactory @Inject internal constructor(private val privateFiniteRegionFactory: PrivateFiniteRegionFactory,
                                                       private val libpixman1: Libpixman1) {

    fun create(): FiniteRegion {
        val pixman_region32Pointer = malloc<pixman_region32>(Struct_pixman_region32.SIZE,
                                                             pixman_region32::class.java)
        this.libpixman1.pixman_region32_init(pixman_region32Pointer.address)

        return create(pixman_region32Pointer)
    }

    fun create(pixman_region32Pointer: Pointer<pixman_region32>): FiniteRegion {
        return this.privateFiniteRegionFactory.create(pixman_region32Pointer)
    }
}
