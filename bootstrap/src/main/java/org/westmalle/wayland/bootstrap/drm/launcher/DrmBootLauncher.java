package org.westmalle.wayland.bootstrap.drm.launcher;


import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Size;
import org.westmalle.wayland.bootstrap.JvmLauncher;
import org.westmalle.wayland.bootstrap.drm.DrmBoot;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.glibc.Libpthread;
import org.westmalle.wayland.nativ.glibc.pollfd;
import org.westmalle.wayland.nativ.glibc.sigset_t;
import org.westmalle.wayland.nativ.linux.signalfd_siginfo;
import org.westmalle.wayland.tty.Tty;

import java.io.IOException;
import java.util.logging.Logger;

import static org.westmalle.wayland.nativ.glibc.Libc.AF_LOCAL;
import static org.westmalle.wayland.nativ.glibc.Libc.EINTR;
import static org.westmalle.wayland.nativ.glibc.Libc.FD_CLOEXEC;
import static org.westmalle.wayland.nativ.glibc.Libc.F_SETFD;
import static org.westmalle.wayland.nativ.glibc.Libc.SFD_CLOEXEC;
import static org.westmalle.wayland.nativ.glibc.Libc.SFD_NONBLOCK;
import static org.westmalle.wayland.nativ.glibc.Libc.SOCK_SEQPACKET;

public class DrmBootLauncher {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static final int ACTIVATE = 1;
    public static final int DEACTIVATE = 2;

    private Libc             libc;
    private Libpthread       libpthread;
    private Tty              tty;
    private int              signalFd;
    private Pointer<Integer> sock;

    private void showHelp() {
        System.out.println("The drm launcher program changes the native signal masks before forking a new compositor jvm.\n" +
                           "This ensures that the compositor jvm can catch native signals in any thread of its choosing.\n" +
                           "If we did not change the signal masks, threads out of normal execution control (like the garbage collector thread)\n" +
                           "could receive native signals, and fire the default handler which would exit the jvm.\n\n" +
                           "Usage: java -DbackEnd=DrmEgl -jar jarfile [option=arg...] [args]\n" +
                           "\t option=<arg> \t Pass <arg> to the compositor jvm option as an option, eg. option=-Dkey=value or option=-Xmx=1234m\n" +
                           "\t <args> \t Pass <args> as-is to the child jvm as program arguments.");
    }

    private Pointer<Integer> setupSocket() {
        final Pointer<Integer> sock = Pointer.nref(0,
                                                   0);

        if (this.libc.socketpair(AF_LOCAL,
                                 SOCK_SEQPACKET,
                                 0,
                                 sock.address) < 0) {
            //TODO errno
            throw new Error("socketpair failed");
        }

        if (this.libc.fcntl(sock.dref(0),
                            F_SETFD,
                            FD_CLOEXEC) < 0) {
            //TODO errno
            throw new Error("fcntl failed");
        }

        return sock;
    }


    private int setupTty() {

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
            throw new Error("Could not create signal file descriptor.");
        }

        //TODO listen for tty rel & acq signals & handle them

        /*
         * Make sure we cleanup nicely if the program stops.
         */
        final Thread shutdownHook = new Thread() {
            @Override
            public void run() {
                DrmBootLauncher.this.tty.close();
            }
        };
        Runtime.getRuntime()
               .addShutdownHook(shutdownHook);

        return signalFd;
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
                //TODO errno
                LOGGER.severe("poll failed");
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

    private void sendReply(final int reply) {
        long len;

        do {
            len = this.libc.send(this.sock.dref(0),
                                 Pointer.nref(reply).address,
                                 Size.sizeof((Integer) null),
                                 0);
        } while (len < 0 && this.libc.getErrno() == EINTR);
    }


    private void handleSocketMsg() {
        //TODO
    }

    private void handleSignal() {

        final signalfd_siginfo sig = new signalfd_siginfo();

        if (this.libc.read(this.signalFd,
                           Pointer.ref(sig).address,
                           signalfd_siginfo.SIZE) != signalfd_siginfo.SIZE) {
            //TODO errno
            throw new Error("reading signalfd failed");
        }

        if (sig.ssi_signo() == this.tty.getAcqSig()) {
            this.tty.handleVtEnter();
            sendReply(ACTIVATE);
        }
        else if (sig.ssi_signo() == this.tty.getRelSig()) {
            sendReply(DEACTIVATE);
            this.tty.handleVtLeave();
        }
        else {
            //unsupported signal.
        }
    }

    public void launch(final String[] args) throws IOException, InterruptedException {
        if (args.length == 1 &&
            (args[0].equals("--help") || args[0].equals("-h"))) {
            showHelp();
            System.exit(0);
        }

        final DrmLauncher drmLauncher = DaggerDrmLauncher.builder()
                                                         .build();
        this.libc = drmLauncher.libc();
        this.libpthread = drmLauncher.libpthread();
        this.tty = drmLauncher.tty();

        this.signalFd = setupTty();
        this.sock = setupSocket();

        //start our actual compositor
        new JvmLauncher().fork(args,
                               DrmBoot.class.getName());

        this.libc.close(this.sock.dref(1));

        pollEvents();
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        new DrmBootLauncher().launch(args);
    }
}
