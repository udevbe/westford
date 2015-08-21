//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.nativ;


import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.westmalle.wayland.nativ.libc.Libc;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NativeFileFactory {

    private static final String TEMPLATE = "/westmalle-shared-XXXXXX";


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
    public int createAnonymousFile(@Nonnegative final int size) throws LastErrorException {

        final String path = System.getenv("XDG_RUNTIME_DIR");
        if (path == null) {
            throw new IllegalStateException("Cannot create temporary file: XDG_RUNTIME_DIR not set");
        }

        final String name = path + TEMPLATE;
        //FIXME assumes ascii-only string
        final Pointer m = new Memory(name.length() + 1);
        m.setString(0,
                    name);
        final int fd = this.libc.mkstemp(m);

        try {
            int flags = this.libc.fcntl(fd,
                                        Libc.F_GETFD,
                                        0);
            flags |= Libc.FD_CLOEXEC;
            this.libc.fcntl(fd,
                            Libc.F_SETFD,
                            flags);
            this.libc.ftruncate(fd,
                                size);
            this.libc.unlink(m);
        }
        catch (LastErrorException e) {
            this.libc.close(fd);
            throw e;
        }
        return fd;
    }
}
