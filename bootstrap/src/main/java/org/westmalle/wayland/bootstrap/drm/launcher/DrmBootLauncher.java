package org.westmalle.wayland.bootstrap.drm.launcher;


import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.bootstrap.drm.DrmBoot;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.glibc.Libpthread;
import org.westmalle.wayland.nativ.glibc.sigset_t;
import org.westmalle.wayland.tty.Tty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DrmBootLauncher {

    private static final Logger LOGGER        = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String OPTION_PREFIX = "option=";

    private Process startNewJavaProcess(final List<String> options,
                                        final String mainClass,
                                        final List<String> arguments) throws IOException {
        final ProcessBuilder processBuilder = createProcess(options,
                                                            mainClass,
                                                            arguments);
        processBuilder.inheritIO();
        return processBuilder.start();
    }

    private ProcessBuilder createProcess(final List<String> options,
                                         final String mainClass,
                                         final List<String> arguments) {
        final String jvm       = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final String classpath = System.getProperty("java.class.path");

        final List<String> command = new ArrayList<>();
        command.add(jvm);
        if (!options.isEmpty()) {
            command.addAll(options);
        }
        command.add(mainClass);
        if (!arguments.isEmpty()) {
            command.addAll(arguments);
        }

        final ProcessBuilder      processBuilder = new ProcessBuilder(command);
        final Map<String, String> environment    = processBuilder.environment();
        environment.put("CLASSPATH",
                        classpath);
        return processBuilder;
    }

    private void showHelp() {
        System.out.println("The drm launcher program changes the native signal masks before forking a new compositor jvm.\n" +
                           "This ensures that the compositor jvm can catch native signals in any thread of its choosing.\n" +
                           "If we did not change the signal masks, threads out of normal execution control (like the garbage collector thread)\n" +
                           "could receive native signals, and fire the default handler which would exit the jvm.\n\n" +
                           "Usage: java -DbackEnd=DrmEgl -jar jarfile [option=arg...] [args]\n" +
                           "\t option=<arg> \t Pass <arg> to the compositor jvm option as an option, eg. option=-Dkey=value or option=-Xmx=1234m\n" +
                           "\t <args> \t Pass <args> as-is to the child jvm as program arguments.");
    }

    private void blockSignals(final Libc libc,
                              final Libpthread libpthread) {
        /*
         * Block the signals that we want to catch in our child process.
         */
        final sigset_t sigset = new sigset_t();
        libpthread.sigemptyset(Pointer.ref(sigset).address);
        libpthread.sigaddset(Pointer.ref(sigset).address,
                             libc.SIGRTMIN());
        libpthread.pthread_sigmask(Libc.SIG_BLOCK,
                                   Pointer.ref(sigset).address,
                                   0L);
    }

    private void setup(final Tty tty) {

        //TODO listen for tty rel & acq signals & handle them
        final short acqSig = tty.getAcqSig();
        final short relSig = tty.getRelSig();


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

    private void dropPrivileges(final Libc libc) {
        if (libc.geteuid() == 0) {
            LOGGER.info("Effective user id is 0 (root), trying to drop privileges.");
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

    private void launchCompositor(final String[] args) throws IOException, InterruptedException {
        final List<String> options     = new LinkedList<>();
        final List<String> programArgs = new LinkedList<>();

        for (final String arg : args) {
            if (arg.startsWith(OPTION_PREFIX)) {
                options.add(arg.substring(OPTION_PREFIX.length()));
            }
            else {
                programArgs.add(arg);
            }
        }

        /*
         * Fork new jvm.
         */
        System.exit(startNewJavaProcess(options,
                                        DrmBoot.class.getName(),
                                        programArgs).waitFor());
    }

    public void launch(final String[] args) throws IOException, InterruptedException {
        if (args.length == 1 &&
            (args[0].equals("--help") || args[0].equals("-h"))) {
            showHelp();
            System.exit(0);
        }

        final DrmLauncher drmLauncher = DaggerDrmLauncher.builder()
                                                         .build();

        final Libc       libc       = drmLauncher.libc();
        final Libpthread libpthread = drmLauncher.libpthread();
        final Tty        tty        = drmLauncher.tty();

        setup(tty);
        blockSignals(libc,
                     libpthread);

        dropPrivileges(libc);
        launchCompositor(args);
    }
}
