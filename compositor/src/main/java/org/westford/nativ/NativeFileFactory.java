/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.nativ;


import org.freedesktop.jaccall.Pointer;
import org.westford.nativ.glibc.Libc;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;

@Singleton
public class NativeFileFactory {

    private static final String TEMPLATE = "/westford-shared-XXXXXX";


    private final Libc libc;

    @Inject
    NativeFileFactory(@Nonnull final Libc libc) {
        this.libc = libc;
    }

    /**
     * Create a new, unique, anonymous file of the given size, and return the file descriptor for it. The file
     * descriptor is set CLOEXEC. The file is immediately suitable for mmap()'ing the given size at offset zero.
     * <p/>
     * The file should not have a permanent backing store like a disk, but may have if XDG_RUNTIME_DIR is not properly
     * implemented in OS.
     * <p/>
     * The file name is deleted from the file system.
     * <p/>
     * The file is suitable for buffer sharing between processes by transmitting the file descriptor over Unix sockets
     * using the SCM_RIGHTS methods.
     */
    public int createAnonymousFile(@Nonnegative final int size) throws UncheckedIOException {

        final String path = System.getenv("XDG_RUNTIME_DIR");
        if (path == null) {
            throw new IllegalStateException("Cannot create temporary file: XDG_RUNTIME_DIR not set");
        }

        final long name = Pointer.nref(path + TEMPLATE).address;
        final int  fd   = this.libc.mkstemp(name);
        if (-1 == fd) {
            throw new UncheckedIOException(new IOException("Failed to create temporary file: " + this.libc.getStrError()));
        }

        int flags = this.libc.fcntl(fd,
                                    Libc.F_GETFD,
                                    0);
        if (-1 == flags) {
            throw new UncheckedIOException(new IOException("Failed to query file flags: " + this.libc.getStrError()));
        }

        flags |= Libc.FD_CLOEXEC;
        final int ret = this.libc.fcntl(fd,
                                        Libc.F_SETFD,
                                        flags);
        if (-1 == ret) {
            throw new UncheckedIOException(new IOException("Failed to set file flags: " + this.libc.getStrError()));
        }

        final int ftruncate = this.libc.ftruncate(fd,
                                                  size);
        if (-1 == ftruncate) {
            throw new UncheckedIOException(new IOException("Failed to truncate file: " + this.libc.getStrError()));
        }

        final int unlink = this.libc.unlink(name);
        if (-1 == unlink) {
            throw new UncheckedIOException(new IOException("Failed to unlink file: " + this.libc.getStrError()));
        }

        return fd;
    }
}
