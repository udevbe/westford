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
package org.westford.nativ

import org.freedesktop.jaccall.Pointer
import org.westford.nativ.glibc.Libc
import java.io.IOException
import java.io.UncheckedIOException
import javax.annotation.Nonnegative
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class NativeFileFactory @Inject internal constructor(private val libc: Libc) {

    /**
     * Create a new, unique, anonymous file of the given size, and return the file descriptor for it. The file
     * descriptor is set CLOEXEC. The file is immediately suitable for mmap()'ing the given size at offset zero.
     *
     *
     * The file should not have a permanent backing store like a disk, but may have if XDG_RUNTIME_DIR is not properly
     * implemented in OS.
     *
     *
     * The file name is deleted from the file system.
     *
     *
     * The file is suitable for buffer sharing between processes by transmitting the file descriptor over Unix sockets
     * using the SCM_RIGHTS methods.
     */
    @Throws(UncheckedIOException::class) fun createAnonymousFile(@Nonnegative size: Int): Int {

        val path = System.getenv("XDG_RUNTIME_DIR") ?: throw IllegalStateException("Cannot create temporary file: XDG_RUNTIME_DIR not set")

        val name = Pointer.nref(path + TEMPLATE).address
        val fd = this.libc.mkstemp(name)
        if (-1 == fd) {
            throw UncheckedIOException(IOException("Failed to create temporary file: " + this.libc.strError))
        }

        var flags = this.libc.fcntl(fd,
                                    Libc.F_GETFD,
                                    0)
        if (-1 == flags) {
            throw UncheckedIOException(IOException("Failed to query file flags: " + this.libc.strError))
        }

        flags = flags or Libc.FD_CLOEXEC
        val ret = this.libc.fcntl(fd,
                                  Libc.F_SETFD,
                                  flags)
        if (-1 == ret) {
            throw UncheckedIOException(IOException("Failed to set file flags: " + this.libc.strError))
        }

        val ftruncate = this.libc.ftruncate(fd,
                                            size)
        if (-1 == ftruncate) {
            throw UncheckedIOException(IOException("Failed to truncate file: " + this.libc.strError))
        }

        val unlink = this.libc.unlink(name)
        if (-1 == unlink) {
            throw UncheckedIOException(IOException("Failed to unlink file: " + this.libc.strError))
        }

        return fd
    }

    companion object {

        private val TEMPLATE = "/westford-shared-XXXXXX"
    }
}
