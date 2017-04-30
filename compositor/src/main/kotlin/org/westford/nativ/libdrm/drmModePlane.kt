package org.westford.nativ.libdrm

import org.freedesktop.jaccall.CType
import org.freedesktop.jaccall.Field
import org.freedesktop.jaccall.Struct

@Struct(Field(name = "count_formats",
              type = CType.UNSIGNED_INT),
        Field(name = "crtc_id",
              type = CType.UNSIGNED_INT),
        Field(name = "crtc_x",
              type = CType.UNSIGNED_INT),
        Field(name = "crtc_y",
              type = CType.UNSIGNED_INT),
        Field(name = "fb_id",
              type = CType.UNSIGNED_INT),
        Field(name = "formats",
              type = CType.POINTER,
              dataType = Int::class),
        Field(name = "gamma_size",
              type = CType.UNSIGNED_INT),
        Field(name = "plane_id",
              type = CType.UNSIGNED_INT),
        Field(name = "possible_crtcs",
              type = CType.UNSIGNED_INT),
        Field(name = "x",
              type = CType.UNSIGNED_INT),
        Field(name = "y",
              type = CType.UNSIGNED_INT)) class drmModePlane : drmModePlane_Jaccall_StructType()
