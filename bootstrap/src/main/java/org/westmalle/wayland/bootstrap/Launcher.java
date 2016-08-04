package org.westmalle.wayland.bootstrap;

import org.freedesktop.jaccall.Pointer;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.glibc.Libc_Symbols;
import org.westmalle.wayland.nativ.glibc.Libpthread;
import org.westmalle.wayland.nativ.glibc.Libpthread_Symbols;
import org.westmalle.wayland.nativ.glibc.sigset_t;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class changes the native signal mask before launching a new jvm. This ensures that we can catch the correct native
 * signal in the thread of our choosing. If we did not change the signal mask, any thread out of our control
 * (like the gc thread) could receive the native signal, and fire the default handler (which will exit the program).
 */
public class Launcher {

    private static final String optionPrefix = "option=";

    public static void main(final String[] args) throws IOException, InterruptedException {

        if (args.length == 1 &&
            (args[0].equals("--help") || args[0].equals("-h"))) {
            System.out.println("This program changes the native signal mask before launching a new jvm.\n" +
                               "This ensures that we can catch the correct native signal in the child jvm thread of our choosing.\n" +
                               "If we did not change the signal mask, any thread out of our control (like the gc thread)\n" +
                               "could receive the native signal, and fire the default handler (which would exit the program).\n\n" +
                               "Usage:\n" +
                               "\t option=<arg> \t - Pass <arg> as a child jvm option, eg. option=-Dkey=value or option=-Xmx=1234m\n" +
                               "\t <args> \t - Pass <args> as-is to the child jvm as program arguments.");
            System.exit(0);
        }

        new Libc_Symbols().link();
        new Libpthread_Symbols().link();

        final Libc       libc       = new Libc();
        final Libpthread libpthread = new Libpthread();

        //we block the signals that we want to catch in our child process.
        final sigset_t sigset = new sigset_t();
        libpthread.sigemptyset(Pointer.ref(sigset).address);
        libpthread.sigaddset(Pointer.ref(sigset).address,
                             libc.SIGRTMIN());
        libpthread.pthread_sigmask(Libc.SIG_BLOCK,
                                   Pointer.ref(sigset).address,
                                   0L);

        final List<String> options     = new LinkedList<>();
        final List<String> programArgs = new LinkedList<>();

        for (final String arg : args) {
            if (arg.startsWith(optionPrefix)) {
                options.add(arg.substring(optionPrefix.length()));
            }
            else {
                programArgs.add(arg);
            }
        }

        System.exit(startNewJavaProcess(options,
                                        Boot.class.getName(),
                                        programArgs).waitFor());
    }

    private static Process startNewJavaProcess(@Nonnull final List<String> options,
                                               @Nonnull final String mainClass,
                                               @Nonnull final List<String> arguments) throws IOException {
        Objects.requireNonNull(options);
        Objects.requireNonNull(mainClass);
        Objects.requireNonNull(arguments);

        final ProcessBuilder processBuilder = createProcess(options,
                                                            mainClass,
                                                            arguments);
        processBuilder.inheritIO();
        return processBuilder.start();
    }


    private static ProcessBuilder createProcess(final List<String> options,
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
}
