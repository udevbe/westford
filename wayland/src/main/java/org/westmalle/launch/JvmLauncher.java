package org.westmalle.launch;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JvmLauncher {

    public static final String OPTION_PREFIX = "option=";

    @Nonnull
    public Process fork(@Nonnull final List<String> options,
                        @Nonnull final String[] args,
                        @Nonnull final String mainClassName) throws IOException, InterruptedException {

        final List<String> programArgs = new LinkedList<>();

        for (final String arg : args) {
            if (arg.startsWith(OPTION_PREFIX)) {
                options.add(arg.substring(OPTION_PREFIX.length()));
            }
            else {
                programArgs.add(arg);
            }
        }

        return startNewJavaProcess(options,
                                   mainClassName,
                                   programArgs);
    }

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

    @Nonnull
    public Process fork(@Nonnull final String[] args,
                        @Nonnull final String mainClassName) throws IOException, InterruptedException {
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

        return startNewJavaProcess(options,
                                   mainClassName,
                                   programArgs);
    }

    //TODO embed: init a jvm in the current process using jni api
//    public void embed(final String[] args,
//                      final String mainClassName) {
//    }
}
