package org.westmalle.wayland.drm.events;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DrmVBlank {

    public static DrmVBlank create(final int sequence,
                                   final int tvSec,
                                   final int tvUsec,
                                   final long userData) {
        return new AutoValue_DrmVBlank(sequence,
                                       tvSec,
                                       tvUsec,
                                       userData);
    }

    public abstract int getSequence();

    public abstract int getTvSec();

    public abstract int getTvUsec();

    public abstract long getUserData();
}
