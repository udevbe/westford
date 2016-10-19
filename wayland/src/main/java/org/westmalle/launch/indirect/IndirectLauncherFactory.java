package org.westmalle.launch.indirect;


import org.freedesktop.jaccall.Pointer;
import org.westmalle.nativ.glibc.Libc;
import org.westmalle.nativ.glibc.Libpthread;
import org.westmalle.nativ.glibc.sigset_t;
import org.westmalle.tty.Tty;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.westmalle.nativ.glibc.Libc.AF_LOCAL;
import static org.westmalle.nativ.glibc.Libc.FD_CLOEXEC;
import static org.westmalle.nativ.glibc.Libc.F_SETFD;
import static org.westmalle.nativ.glibc.Libc.SFD_CLOEXEC;
import static org.westmalle.nativ.glibc.Libc.SFD_NONBLOCK;
import static org.westmalle.nativ.glibc.Libc.SOCK_SEQPACKET;

public class IndirectLauncherFactory {

    @Nonnull
    private final Libpthread                     libpthread;
    @Nonnull
    private final Libc                           libc;
    @Nonnull
    private final Tty                            tty;
    @Nonnull
    private final PrivateIndirectLauncherFactory privateIndirectLauncherFactory;

    @Inject
    IndirectLauncherFactory(@Nonnull final Libpthread libpthread,
                            @Nonnull final Libc libc,
                            @Nonnull final Tty tty,
                            @Nonnull final PrivateIndirectLauncherFactory privateIndirectLauncherFactory) {
        this.libpthread = libpthread;
        this.libc = libc;
        this.tty = tty;
        this.privateIndirectLauncherFactory = privateIndirectLauncherFactory;
    }

    public IndirectLauncher create() {
        final int              signalFd = setupTty();
        final Pointer<Integer> sock     = setupSocket();

        final IndirectLauncher indirectLaunchable = this.privateIndirectLauncherFactory.create(sock,
                                                                                               signalFd);
        return indirectLaunchable;
    }

    private int setupTty() throws UncheckedIOException {

        final short acqSig = this.tty.getAcqSig();
        final short relSig = this.tty.getRelSig();

        /* Block the signals that we want to catch. Do this here again in case we are started in a special single
         * threaded jvm environment (robovm?)
         */
        final sigset_t sigset = new sigset_t();
        this.libpthread.sigemptyset(Pointer.ref(sigset).address);
        this.libpthread.sigaddset(Pointer.ref(sigset).address,
                                  acqSig);
        this.libpthread.sigaddset(Pointer.ref(sigset).address,
                                  relSig);
        this.libpthread.pthread_sigmask(Libc.SIG_BLOCK,
                                        Pointer.ref(sigset).address,
                                        0L);

        final int signalFd = this.libc.signalfd(-1,
                                                Pointer.ref(sigset).address,
                                                SFD_NONBLOCK | SFD_CLOEXEC);
        if (signalFd < 0) {
            throw new UncheckedIOException(new IOException("Failed to create signal file descriptor: " + this.libc.getStrError()));
        }

        //TODO listen for tty rel & acq signals & handle them

        /*
         * Make sure we cleanup nicely if the program stops.
         */
        final Thread shutdownHook = new Thread() {
            @Override
            public void run() {
                IndirectLauncherFactory.this.tty.close();
            }
        };
        Runtime.getRuntime()
               .addShutdownHook(shutdownHook);

        return signalFd;
    }

    private Pointer<Integer> setupSocket() throws UncheckedIOException {
        final Pointer<Integer> sock = Pointer.nref(0,
                                                   0);

        if (this.libc.socketpair(AF_LOCAL,
                                 SOCK_SEQPACKET,
                                 0,
                                 sock.address) < 0) {
            throw new UncheckedIOException(new IOException("Failed to create socket pair: " + this.libc.getStrError()));
        }

        if (this.libc.fcntl(sock.dref(0),
                            F_SETFD,
                            FD_CLOEXEC) < 0) {
            throw new UncheckedIOException(new IOException("fcntl failed"));
        }

        return sock;
    }
}
