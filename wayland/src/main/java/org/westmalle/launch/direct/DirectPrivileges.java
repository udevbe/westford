package org.westmalle.launch.direct;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.westmalle.launch.Privileges;
import org.westmalle.nativ.glibc.Libc;
import org.westmalle.nativ.libdrm.Libdrm;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class DirectPrivileges implements Privileges {


    @Nonnull
    private final Libc   libc;
    @Nonnull
    private final Libdrm libdrm;

    @Inject
    DirectPrivileges(@Nonnull final Libc libc,
                     @Nonnull final Libdrm libdrm) {
        this.libc = libc;
        this.libdrm = libdrm;
    }

    @Override
    public int open(@Ptr(String.class) final long path,
                    final int flags) {
        return this.libc.open(Pointer.nref(path).address,
                              flags);
    }

    @Override
    public void setDrmMaster(final int fd) {
        if (this.libdrm.drmSetMaster(fd) != 0) {
            throw new RuntimeException("failed to set drm master: " + this.libc.getStrError());
        }
    }

    @Override
    public void dropDrmMaster(final int fd) {
        if (this.libdrm.drmDropMaster(fd) != 0) {
            throw new RuntimeException("failed to drop drm master: " + this.libc.getStrError());
        }
    }
}
