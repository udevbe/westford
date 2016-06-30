package org.westmalle.wayland.drm;

import org.freedesktop.jaccall.Unsigned;

public interface DrmPageFlipCallback {
    default void onPageFlip(@Unsigned final int sequence,
                            @Unsigned final int tv_sec,
                            @Unsigned final int tv_usec) {}

    default void onVBlank(@Unsigned final int sequence,
                          @Unsigned final int tv_sec,
                          @Unsigned final int tv_usec) {}
}
