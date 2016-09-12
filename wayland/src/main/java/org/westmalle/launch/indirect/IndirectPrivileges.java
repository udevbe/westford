package org.westmalle.launch.indirect;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Ptr;
import org.westmalle.launch.Privileges;
import org.westmalle.nativ.glibc.Libc;

import javax.annotation.Nonnull;


@AutoFactory(allowSubclasses = true,
             className = "PrivatePrivilegesProxyFactory")
public class IndirectPrivileges implements Privileges {

    @Nonnull
    private final Libc libc;
    private final int  sockFd0;
    private final int  sockFd1;

    IndirectPrivileges(@Provided @Nonnull final Libc libc,
                       final int sockFd0,
                       final int sockFd1) {
        this.libc = libc;
        this.sockFd0 = sockFd0;
        this.sockFd1 = sockFd1;
    }

    //TODO send requests over socket
    @Override
    public int open(@Ptr(String.class) final long path,
                    final int flags) {
        //TODO request open over socket
        return 0;
    }

    @Override
    public void setDrmMaster(final int fd) {
        //NOOP set drm master is done in the parent process
    }

    @Override
    public void dropDrmMaster(final int fd) {
        //NOOP drop drm master is done in the parent process
    }
}
