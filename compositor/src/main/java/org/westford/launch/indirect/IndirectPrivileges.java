package org.westford.launch.indirect;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.CLong;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.westford.launch.Privileges;
import org.westford.nativ.glibc.Libc;
import org.westford.nativ.glibc.cmsghdr;
import org.westford.nativ.glibc.iovec;
import org.westford.nativ.glibc.msghdr;
import org.westford.nativ.linux.Socket;

import javax.annotation.Nonnull;

import static org.freedesktop.jaccall.Size.sizeof;


@AutoFactory(allowSubclasses = true,
             className = "PrivatePrivilegesProxyFactory")
public class IndirectPrivileges implements Privileges {

    @Nonnull
    private final Libc libc;
    private final int  launcherFd;

    IndirectPrivileges(@Provided @Nonnull final Libc libc,
                       final int launcherFd) {
        this.libc = libc;
        this.launcherFd = launcherFd;
    }

    @Override
    public int open(@Ptr(String.class) final long path,
                    final int flags) {
        sendOpenRequest(path,
                        flags);
        return receiveOpenReply();
    }

    private void sendOpenRequest(@Ptr(String.class) final long path,
                                 final int flags) {
        final long payloadSize = launcher_open.SIZE + this.libc.strlen(path) + 1;
        final int  messageSize = (int) (launcher_open.SIZE + payloadSize);
        try (final Pointer<launcher_open> message = Pointer.calloc(1,
                                                                   messageSize)
                                                           .castp(launcher_open.class)) {
            if (message.address == 0L) {
                throw new RuntimeException(String.format("Unable to allocate %d bytes. Memory full?",
                                                         messageSize));
            }

            message.dref()
                   .opcode(NativeConstants.OPCODE_WESTMALLE_LAUNCHER_OPEN);
            message.dref()
                   .flags(flags);
            this.libc.strcpy(message.dref()
                                    .path().address,
                             path);

            long len;
            do {
                len = this.libc.send(this.launcherFd,
                                     message.address,
                                     messageSize,
                                     0);
            } while (len < 0 && this.libc.getErrno() == Libc.EINTR);
        }
    }

    private int receiveOpenReply() {
        try (Pointer<Void> ret = Pointer.calloc(1,
                                                sizeof((Integer) null));
             Pointer<msghdr> msg = Pointer.calloc(1,
                                                  msghdr.SIZE,
                                                  msghdr.class);
             Pointer<iovec> iov = Pointer.calloc(1,
                                                 iovec.SIZE,
                                                 iovec.class)) {
            final int controlSize = (int) this.libc.CMSG_SPACE(sizeof((Integer) null));
            final Pointer<Void> control = Pointer.calloc(1,
                                                         controlSize);

            iov.dref()
               .iov_base(ret);
            final int retSize = sizeof((Integer) null);
            iov.dref()
               .iov_len(new CLong(retSize));

            msg.dref()
               .msg_iov(iov);
            msg.dref()
               .msg_iovlen(new CLong(1));
            msg.dref()
               .msg_control(control);
            msg.dref()
               .msg_controllen(new CLong(controlSize));

            long len;
            do {
                len = this.libc.recvmsg(this.launcherFd,
                                        msg.address,
                                        Socket.MSG_CMSG_CLOEXEC);
            } while (len < 0 && this.libc.getErrno() == Libc.EINTR);

            if (len != retSize ||
                ret.castp(Integer.class)
                   .dref() < 0) {
                throw new RuntimeException("Receive an illegal open reply.");
            }

            final Pointer<cmsghdr> cmsg = this.libc.CMSG_FIRSTHDR(msg.dref());
            if (cmsg.address == 0L ||
                cmsg.dref()
                    .cmsg_level() != Socket.SOL_SOCKET ||
                cmsg.dref()
                    .cmsg_type() != Socket.SCM_RIGHTS) {
                throw new RuntimeException("invalid control message");
            }

            return this.libc.CMSG_DATA(cmsg)
                            .castp(Integer.class)
                            .dref();
        }
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
