package org.westmalle.launch;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JvmLauncher {

    private static final String DEBUG_COMMAND = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%d";
    private static       int    DEBUG_PORT    = 6000;

    @Nonnull
    public Process fork(@Nonnull final String mainClassName) throws IOException, InterruptedException {
        return startNewJavaProcess(Collections.emptyMap(),
                                   mainClassName);
    }

    private Process startNewJavaProcess(final Map<String, String> environment,
                                        final String mainClass) throws IOException {
        final ProcessBuilder processBuilder = createProcess(environment,
                                                            mainClass);
        processBuilder.inheritIO();
        return processBuilder.start();
    }

    private ProcessBuilder createProcess(final Map<String, String> environmentExtra,
                                         final String mainClass) {
        final String jvm       = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final String classpath = System.getProperty("java.class.path");

        final List<String> command = new ArrayList<>();
        command.add(jvm);
        command.add(String.format(DEBUG_COMMAND,
                                  ++DEBUG_PORT));
        command.add(mainClass);

        final ProcessBuilder      processBuilder = new ProcessBuilder(command);
        final Map<String, String> environment    = processBuilder.environment();
        environment.putAll(environmentExtra);
        environment.put("CLASSPATH",
                        classpath);
        return processBuilder;
    }

    @Nonnull
    public Process fork(@Nonnull final Map<String, String> environmentExtra,
                        @Nonnull final String mainClassName) throws IOException, InterruptedException {
        return startNewJavaProcess(environmentExtra,
                                   mainClassName);
    }

    //TODO embed: init a jvm in the current process using jni api
//    public void embed(final String[] args,
//                      final String mainClassName) {
//    }
}
