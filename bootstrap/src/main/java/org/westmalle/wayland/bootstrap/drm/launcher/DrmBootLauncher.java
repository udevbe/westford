package org.westmalle.wayland.bootstrap.drm.launcher;


import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.bootstrap.JvmLauncher;
import org.westmalle.wayland.bootstrap.drm.DrmBoot;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.glibc.Libpthread;
import org.westmalle.wayland.nativ.glibc.sigset_t;
import org.westmalle.wayland.tty.Tty;

import java.io.IOException;
import java.util.logging.Logger;

public class DrmBootLauncher {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private void showHelp() {
        System.out.println("The drm launcher program changes the native signal masks before forking a new compositor jvm.\n" +
                           "This ensures that the compositor jvm can catch native signals in any thread of its choosing.\n" +
                           "If we did not change the signal masks, threads out of normal execution control (like the garbage collector thread)\n" +
                           "could receive native signals, and fire the default handler which would exit the jvm.\n\n" +
                           "Usage: java -DbackEnd=DrmEgl -jar jarfile [option=arg...] [args]\n" +
                           "\t option=<arg> \t Pass <arg> to the compositor jvm option as an option, eg. option=-Dkey=value or option=-Xmx=1234m\n" +
                           "\t <args> \t Pass <args> as-is to the child jvm as program arguments.");
    }


    private void setupTty(final DrmLauncher drmLauncher) {

        final Libpthread libpthread = drmLauncher.libpthread();
        final Tty        tty        = drmLauncher.tty();

        final short acqSig = tty.getAcqSig();
        final short relSig = tty.getRelSig();

        /* Block the signals that we want to catch. Do this here again in case we are started in a special single
         * threaded jvm environment.
         */
        final sigset_t sigset = new sigset_t();
        libpthread.sigemptyset(Pointer.ref(sigset).address);
        libpthread.sigaddset(Pointer.ref(sigset).address,
                             acqSig);
        libpthread.sigaddset(Pointer.ref(sigset).address,
                             relSig);
        libpthread.pthread_sigmask(Libc.SIG_BLOCK,
                                   Pointer.ref(sigset).address,
                                   0L);

        //TODO listen for tty rel & acq signals & handle them


        /*
         * Make sure we cleanup nicely if the program stops.
         */
        final Thread shutdownHook = new Thread() {
            @Override
            public void run() {
                tty.close();
            }
        };
        Runtime.getRuntime()
               .addShutdownHook(shutdownHook);
    }

    private void dropPrivileges(final DrmLauncher drmLauncher) {
        final Libc libc = drmLauncher.libc();

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

    public void launch(final String[] args) throws IOException, InterruptedException {
        if (args.length == 1 &&
            (args[0].equals("--help") || args[0].equals("-h"))) {
            showHelp();
            System.exit(0);
        }

        final DrmLauncher drmLauncher = DaggerDrmLauncher.builder()
                                                         .build();

        setupTty(drmLauncher);
        dropPrivileges(drmLauncher);

        //start our actual compositor
        System.exit(new JvmLauncher().fork(args,
                                           DrmBoot.class.getName())
                                     .waitFor());
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        new DrmBootLauncher().launch(args);
    }
}
