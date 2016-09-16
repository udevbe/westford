package org.westmalle.launch.indirect;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.CLong;
import org.freedesktop.jaccall.Pointer;
import org.westmalle.launch.JvmLauncher;
import org.westmalle.launch.Launcher;
import org.westmalle.nativ.glibc.Libc;
import org.westmalle.nativ.glibc.Libc_Symbols;
import org.westmalle.nativ.glibc.iovec;
import org.westmalle.nativ.glibc.msghdr;
import org.westmalle.nativ.glibc.pollfd;
import org.westmalle.nativ.linux.signalfd_siginfo;
import org.westmalle.nativ.linux.stat;
import org.westmalle.tty.Tty;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.logging.Logger;

import static org.freedesktop.jaccall.Pointer.calloc;
import static org.freedesktop.jaccall.Size.sizeof;
import static org.westmalle.nativ.glibc.Libc.EINTR;
import static org.westmalle.nativ.linux.Major.DRM_MAJOR;
import static org.westmalle.nativ.linux.Major.INPUT_MAJOR;

@AutoFactory(allowSubclasses = true,
             className = "PrivateIndirectLauncherFactory")
public class IndirectLauncher implements Launcher {

    public static final int ACTIVATE   = 0;
    public static final int DEACTIVATE = 1;
    public static final int OPEN       = 3;

    public static final String SOCKETFD_0 = "SOCKETFD_0=%d";
    public static final String SOCKETFD_1 = "SOCKETFD_1=%d";
    public static final String CHILD_MAIN = "CHILD_MAIN=%s";

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Nonnull
    private final JvmLauncher      jvmLauncher;
    @Nonnull
    private final Libc             libc;
    @Nonnull
    private final Tty              tty;
    @Nonnull
    private final Pointer<Integer> sock;
    private final int              signalFd;

    @Inject
    IndirectLauncher(@Provided @Nonnull final JvmLauncher jvmLauncher,
                     @Provided @Nonnull final Libc libc,
                     @Provided @Nonnull final Tty tty,
                     @Nonnull final Pointer<Integer> sock,
                     final int signalFd) {
        this.jvmLauncher = jvmLauncher;
        this.libc = libc;
        this.tty = tty;
        this.sock = sock;
        this.signalFd = signalFd;
    }

    //This is the child process' main method, created in #launch(Class, String[]).
    public static void main(final String[] args) throws ClassNotFoundException,
                                                        NoSuchMethodException,
                                                        InvocationTargetException,
                                                        IllegalAccessException {
        dropPrivileges();

        if (System.getProperty(SOCKETFD_0) == null ||
            System.getProperty(SOCKETFD_1) == null ||
            System.getProperty(CHILD_MAIN) == null) {
            throw new IllegalStateException("Not all required system properties have been set. Note that this program is not meant to be ran directly.");
        }

        final String   childMain = System.getProperty(CHILD_MAIN);
        final Class<?> main      = Class.forName(childMain);
        main.getMethod("main",
                       String[].class)
            .invoke(null,
                    (Object) args);
    }

    private static void dropPrivileges() {
        new Libc_Symbols().link();
        final Libc libc = new Libc();

        if (libc.geteuid() == 0) {
            LOGGER.info("Effective user id is 0 (root), trying to drop privileges.");

            //check if we're running in a sudo environment and get the real uid & gid from env vars.
            final String sudo_uid = System.getenv("SUDO_UID");
            final int    uid      = sudo_uid != null ? Integer.parseInt(sudo_uid) : libc.getgid();

            final String sudo_gid = System.getenv("SUDO_GID");
            final int    gid      = sudo_gid != null ? Integer.parseInt(sudo_gid) : libc.getuid();

            LOGGER.info(String.format("Real user id is %d. Real group id is %d.",
                                      uid,
                                      gid));

            if (libc.setgid(gid) < 0 ||
                libc.setuid(uid) < 0) {
                throw new Error("dropping privileges failed.");
            }
        }
    }

    @Override
    public void launch(final Class<?> main,
                       final String[] args) throws Exception {
        //fork ourselves
        final Process fork = this.jvmLauncher.fork(Arrays.asList(String.format(SOCKETFD_0,
                                                                               this.sock.dref(0)),
                                                                 String.format(SOCKETFD_1,
                                                                               this.sock.dref(1)),
                                                                 String.format(CHILD_MAIN,
                                                                               main.getName())),
                                                   args,
                                                   IndirectLauncher.class.getName());
        new Thread() {
            @Override
            public void run() {
                System.exit(waitForChild(fork));
            }
        }.start();

        pollEvents();
    }

    private int waitForChild(final Process fork) {
        try {
            return fork.waitFor();
        }
        catch (final InterruptedException e) {
            LOGGER.warning("Waiting for child was interrupted. Will wait again.");
            return waitForChild(fork);
        }
    }

    private void pollEvents() {
        while (true) {
            final Pointer<pollfd> pollfds = Pointer.malloc(2 * pollfd.SIZE,
                                                           pollfd.class);

            final pollfd pollSocket = pollfds.dref(0);
            final pollfd pollSignal = pollfds.dref(1);

            pollSocket.fd(this.sock.dref(0));
            pollSocket.events((short) Libc.POLLIN);

            pollSignal.fd(this.signalFd);
            pollSignal.events((short) Libc.POLLIN);

            final int n = this.libc.poll(pollfds.address,
                                         2,
                                         -1);
            if (n < 0) {
                LOGGER.severe("poll failed: " + this.libc.getStrError());
            }
            if ((pollSocket.revents() & Libc.POLLIN) != 0) {
                handleSocketMsg();
            }
            if (pollSignal.revents() != 0) {
                handleSignal();
            }

            pollfds.close();
        }
    }

    private void handleSocketMsg() {

        final int nroControlBytes = (int) this.libc.CMSG_SPACE(sizeof((Integer) null));
        final int nroBufBytes     = 256;

        try (final Pointer<msghdr> msghdrPointer = calloc(1,
                                                          msghdr.SIZE,
                                                          msghdr.class);
             final Pointer<Void> control = calloc(1,
                                                  nroControlBytes);
             final Pointer<Void> buf = calloc(1,
                                              nroBufBytes)) {

            final msghdr msg = msghdrPointer.dref();
            final iovec  iov = new iovec();
            long         len;

            iov.iov_base(buf);
            iov.iov_len(new CLong(nroBufBytes));
            msg.msg_iov(Pointer.ref(iov));
            msg.msg_iovlen(new CLong(1));
            msg.msg_control(control);
            msg.msg_controllen(new CLong(nroControlBytes));

            do {
                len = this.libc.recvmsg(this.sock.dref(0),
                                        Pointer.ref(msg).address,
                                        0);
            } while (len < 0 && this.libc.getErrno() == EINTR);

            if (len < 1) {
                throw new UncheckedIOException(new IOException("Launched failed to receive message from child process: " + this.libc.getStrError()));
            }

            final privilege_req message = buf.castp(privilege_req.class)
                                             .dref();
            switch (message.opcode()) {
                case OPEN:
                    handleOpen(message,
                               len);
                    break;
            }
        }
    }

    private void handleOpen(final privilege_req message,
                            final long len) {
//        int  fd = -1, ret = -1;
//        char control[ CMSG_SPACE(sizeof(fd))];
//        struct cmsghdr *cmsg;
//        struct stat s;
//        struct msghdr nmsg;
//        struct iovec iov;
//        struct weston_launcher_open *message;
//        union cmsg_data *data;
//
//

        try {
            final int payloadSize = message.payload_size()
                                           .intValue();
            if (len < privilege_req.SIZE + payloadSize) {
                throw new IOException("Received request open message whose received size is smaller than it's self described size.");
            }

	    /* Ensure path is null-terminated */
            final privilege_req_open privilegeReqOpen = message.payload()
                                                               .castp(privilege_req_open.class)
                                                               .dref();
            privilegeReqOpen.path()
                            .writei(payloadSize - 1,
                                    (byte) 0);

            int fd = this.libc.open(privilegeReqOpen.path().address,
                                    privilegeReqOpen.flags());
            if (fd < 0) {
                throw new IOException(String.format("Error opening path %s : %s",
                                                    privilegeReqOpen.path()
                                                                    .castp(String.class)
                                                                    .dref(),
                                                    this.libc.getStrError()));
            }

            stat s = new stat();
            if (this.libc.fstat(fd,
                                Pointer.ref(s).address) < 0) {
                this.libc.close(fd);
                fd = -1;
                throw new IOException(String.format("Failed to stat %s",
                                                    privilegeReqOpen.path()
                                                                    .castp(String.class)
                                                                    .dref()));
            }

            if (this.libc.major(s.st_rdev()) != INPUT_MAJOR &&
                this.libc.major(s.st_rdev()) != DRM_MAJOR) {
                this.libc.close(fd);
                fd = -1;
                throw new IOException(String.format("Device %s is not an input or drm device",
                                                    privilegeReqOpen.path()
                                                                    .castp(String.class)
                                                                    .dref()));
            }
//
//        err0:
//        memset( & nmsg, 0, sizeof nmsg);
//        nmsg.msg_iov = &iov;
//        nmsg.msg_iovlen = 1;
//        if (fd != -1) {
//            nmsg.msg_control = control;
//            nmsg.msg_controllen = sizeof control;
//            cmsg = CMSG_FIRSTHDR( & nmsg);
//            cmsg -> cmsg_level = SOL_SOCKET;
//            cmsg -> cmsg_type = SCM_RIGHTS;
//            cmsg -> cmsg_len = CMSG_LEN(sizeof(fd));
//            data = (union cmsg_data *)CMSG_DATA(cmsg);
//            data -> fd = fd;
//            nmsg.msg_controllen = cmsg -> cmsg_len;
//            ret = 0;
//        }
//        iov.iov_base = &ret;
//        iov.iov_len = sizeof ret;
//
//        if (wl -> verbose) {
//            fprintf(stderr,
//                    "weston-launch: opened %s: ret: %d, fd: %d\n",
//                    message -> path,
//                    ret,
//                    fd);
//        }
//        do {
//            len = sendmsg(wl -> sock[0], & nmsg, 0);
//        } while (len < 0 && errno == EINTR);
//
//        if (len < 0) { return -1; }
//
//        if (fd != -1 && major(s.st_rdev) == DRM_MAJOR) { wl -> drm_fd = fd; }
//        if (fd != -1 && major(s.st_rdev) == INPUT_MAJOR &&
//            wl -> last_input_fd < fd) { wl -> last_input_fd = fd; }
//
//        return 0;
        }
        catch (final IOException e) {
            //TODO handle
            LOGGER.throwing(IndirectLauncher.class.getName(),
                            "handleOpen",
                            e);
//        memset( & nmsg, 0, sizeof nmsg);
//        nmsg.msg_iov = &iov;
//        nmsg.msg_iovlen = 1;
//        if (fd != -1) {
//            nmsg.msg_control = control;
//            nmsg.msg_controllen = sizeof control;
//            cmsg = CMSG_FIRSTHDR( & nmsg);
//            cmsg -> cmsg_level = SOL_SOCKET;
//            cmsg -> cmsg_type = SCM_RIGHTS;
//            cmsg -> cmsg_len = CMSG_LEN(sizeof(fd));
//            data = (union cmsg_data *)CMSG_DATA(cmsg);
//            data -> fd = fd;
//            nmsg.msg_controllen = cmsg -> cmsg_len;
//            ret = 0;
//        }
//        iov.iov_base = &ret;
//        iov.iov_len = sizeof ret;
        }
    }

    private void handleSignal() {

        final signalfd_siginfo sig = new signalfd_siginfo();

        if (this.libc.read(this.signalFd,
                           Pointer.ref(sig).address,
                           signalfd_siginfo.SIZE) != signalfd_siginfo.SIZE) {
            throw new Error("reading signalfd failed: " + this.libc.getStrError());
        }

        if (sig.ssi_signo() == this.tty.getAcqSig()) {
            this.tty.handleVtEnter();
            //TODO setDrmMaster if an open of a drm fd was received
            sendReply(ACTIVATE);
        }
        else if (sig.ssi_signo() == this.tty.getRelSig()) {
            sendReply(DEACTIVATE);
            //TODO dropDrmMaster if an open of a drm fd was received
            this.tty.handleVtLeave();
        }
        //else {
        //unsupported signal.
        //}
    }

    private void sendReply(final int reply) {
        long len;

        do {
            len = this.libc.send(this.sock.dref(0),
                                 Pointer.nref(reply).address,
                                 sizeof((Integer) null),
                                 0);
        } while (len < 0 && this.libc.getErrno() == EINTR);
    }
}
