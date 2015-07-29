package org.westmalle.wayland.nativ;


import com.sun.jna.LastErrorException;
import com.sun.jna.Pointer;

import org.westmalle.wayland.nativ.libc.Libc;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NativeFileFactory {

    private final Libc libc;

    @Inject
    NativeFileFactory(@Nonnull final Libc libc) {
        this.libc = libc;
    }

    /**
     * Create a new, unique, anonymous file of the given size, and return the file descriptor for it. The file
     * descriptor is set CLOEXEC. The file is immediately suitable for mmap()'ing the given size at offset zero. <p/>
     * The file should not have a permanent backing store like a disk, but may have if XDG_RUNTIME_DIR is not properly
     * implemented in OS. <p/> The file name is deleted from the file system. <p/> The file is suitable for buffer
     * sharing between processes by transmitting the file descriptor over Unix sockets using the SCM_RIGHTS methods.
     */
    public int createAnonymousFile(final int size) {
        String template = "/westmalle-shared-XXXXXX";
        String path;

        int fd;

        path = System.getenv("XDG_RUNTIME_DIR");
        if (path == null) {
            throw new LastErrorException(Libc.ENOENT);
        }

        String name = path + template;

        fd = createTmpfileCloexec(new NativeString(name).getPointer());

        if (fd < 0) {
            return -1;
        }

        if (this.libc.ftruncate(fd,
                                size) < 0) {
            this.libc.close(fd);
            return -1;
        }

        return fd;
    }

    private int createTmpfileCloexec(Pointer tmpname) {
        int fd;

        fd = this.libc.mkstemp(tmpname);
        if (fd >= 0) {
            fd = setCloexecOrClose(fd);
            this.libc.unlink(tmpname);
        }

        return fd;
    }

    private int setCloexecOrClose(int fd) {
        int flags;

        if (fd == -1) {
            return -1;
        }

        flags = this.libc.fcntl(fd, Libc.F_GETFD, 0);
        if (flags == -1) {
            this.libc.close(fd);
            return -1;
        }

        if (this.libc.fcntl(fd,
                            Libc.F_SETFD,
                            flags | Libc.FD_CLOEXEC) == -1) {
            this.libc.close(fd);
            return -1;
        }

        return fd;
    }
}
