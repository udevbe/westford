package org.westmalle.launch;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JvmLauncher {

    @Nonnull
    public Process fork(@Nonnull final String mainClassName) throws IOException, InterruptedException {
        return startNewJavaProcess(Collections.emptyList(),
                                   mainClassName);
    }

    private Process startNewJavaProcess(final List<String> options,
                                        final String mainClass) throws IOException {
        final ProcessBuilder processBuilder = createProcess(options,
                                                            mainClass);
        processBuilder.inheritIO();
        return processBuilder.start();
    }

    private ProcessBuilder createProcess(final List<String> options,
                                         final String mainClass) {
        final String jvm       = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final String classpath = System.getProperty("java.class.path");

        final List<String> command = new ArrayList<>();
        command.add(jvm);
        if (!options.isEmpty()) {
            command.addAll(options);
        }
        command.add(mainClass);

        final ProcessBuilder      processBuilder = new ProcessBuilder(command);
        final Map<String, String> environment    = processBuilder.environment();
        environment.put("CLASSPATH",
                        classpath);
        return processBuilder;
    }

    @Nonnull
    public Process fork(@Nonnull final List<String> options,
                        @Nonnull final String mainClassName) throws IOException, InterruptedException {
        return startNewJavaProcess(options,
                                   mainClassName);
    }

    //TODO embed: init a jvm in the current process using jni api
//    public void embed(final String[] args,
//                      final String mainClassName) {
//    }
}
