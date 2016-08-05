//Copyright 2015 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.bootstrap;

import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.WlKeyboardResource;
import org.westmalle.wayland.bootstrap.dispmanx.DaggerDispmanxEglCompositor;
import org.westmalle.wayland.bootstrap.dispmanx.DispmanxEglCompositor;
import org.westmalle.wayland.bootstrap.drm.DrmBoot;
import org.westmalle.wayland.bootstrap.html5.DaggerHtml5X11EglCompositor;
import org.westmalle.wayland.bootstrap.html5.Html5X11EglCompositor;
import org.westmalle.wayland.bootstrap.x11.DaggerX11EglCompositor;
import org.westmalle.wayland.bootstrap.x11.X11EglCompositor;
import org.westmalle.wayland.bootstrap.x11.X11PlatformConfigSimple;
import org.westmalle.wayland.core.KeyboardDevice;
import org.westmalle.wayland.core.LifeCycle;
import org.westmalle.wayland.core.PointerDevice;
import org.westmalle.wayland.core.TouchDevice;
import org.westmalle.wayland.nativ.glibc.Libc;
import org.westmalle.wayland.nativ.glibc.Libc_Symbols;
import org.westmalle.wayland.nativ.glibc.Libpthread;
import org.westmalle.wayland.nativ.glibc.Libpthread_Symbols;
import org.westmalle.wayland.nativ.glibc.sigset_t;
import org.westmalle.wayland.protocol.WlKeyboard;
import org.westmalle.wayland.protocol.WlSeat;
import org.westmalle.wayland.x11.X11PlatformModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Boot {

    private static final String optionPrefix = "option=";

    private static final Logger LOGGER   = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String BACK_END = "backEnd";


    public static void main(final String[] args) throws IOException, InterruptedException {
        configureLogger();
        LOGGER.info("Starting Westmalle");

        initBackEnd(args);
    }

    private static void configureLogger() throws IOException {
        final FileHandler fileHandler = new FileHandler("westmalle.log");
        fileHandler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(fileHandler);

        Thread.setDefaultUncaughtExceptionHandler((thread,
                                                   throwable) -> {
            LOGGER.severe("Got uncaught exception " + throwable.getMessage());
            throwable.printStackTrace();
        });
    }

    private static String parseBackend() {
        String backEnd = System.getProperty(BACK_END);
        if (backEnd == null) {
            LOGGER.warning("No back end specified. Defaulting to 'X11Egl'. Specify your back end with -DbackEnd=<value>.\n" +
                           "Available back ends:\n" +
                           "\tX11Egl\n" +
                           "\tDispmanxEgl\n" +
                           "\tDrmEgl\n" +
                           "\tHtml5X11Egl");
            backEnd = "X11Egl";
        }

        return backEnd;
    }

    private static void initBackEnd(final String[] args) throws IOException, InterruptedException {
        final Boot boot = new Boot();

        switch (parseBackend()) {
            case "X11Egl":
                LOGGER.info("Detected X11Egl backend.");
                boot.strap(DaggerX11EglCompositor.builder());
                break;
            case "DispmanxEgl":
                LOGGER.info("Detected DispmanxEgl backend.");
                boot.strap(DaggerDispmanxEglCompositor.builder());
                break;
            case "DrmEgl":
                LOGGER.info("Detected DrmEgl backend.");
                boot.strap(args);
                break;
            case "Html5X11Egl":
                LOGGER.info("Detected Html5X11Egl backend.");
                boot.strap(DaggerHtml5X11EglCompositor.builder());
                break;
            default:
                LOGGER.severe("Unknown backend %d. Available back ends:\n" +
                              "\tX11Egl\n" +
                              "\tDispmanxEgl\n" +
                              "\tDrmEgl\n" +
                              "\tHtml5X11Egl");
        }
    }

    private void strap(final String[] args) throws IOException, InterruptedException {

        if (args.length == 1 &&
            (args[0].equals("--help") || args[0].equals("-h"))) {
            System.out.println("The drm launcher program changes the native signal masks before forking a new compositor jvm.\n" +
                               "This ensures that the compositor jvm can catch native signals in any thread of its choosing.\n" +
                               "If we did not change the signal masks, threads out of normal execution control (like the garbage collector thread)\n" +
                               "could receive native signals, and fire the default handler which would exit the jvm.\n\n" +
                               "Usage: java -DbackEnd=DrmEgl -jar jarfile [option=arg...] [args]\n" +
                               "\t option=<arg> \t Pass <arg> to the compositor jvm option as an option, eg. option=-Dkey=value or option=-Xmx=1234m\n" +
                               "\t <args> \t Pass <args> as-is to the child jvm as program arguments.");
            System.exit(0);
        }

        new Libc_Symbols().link();
        new Libpthread_Symbols().link();

        final Libc       libc       = new Libc();
        final Libpthread libpthread = new Libpthread();

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

        /*
         * Fork new jvm.
         */
        System.exit(startNewJavaProcess(options,
                                        DrmBoot.class.getName(),
                                        programArgs).waitFor());
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

    private void strap(final DaggerHtml5X11EglCompositor.Builder builder) {
        /**
         * Create an X11 compositor with X11 config and wrap it in a html5 compositor.
         */
        final Html5X11EglCompositor html5X11EglCompositor = builder.x11PlatformModule(new X11PlatformModule(new X11PlatformConfigSimple()))
                                                                   .build();

        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = html5X11EglCompositor.lifeCycle();

        /*
         * Get the X11 seat that listens for input on the X connection and passes it on to a wayland seat.
         * Additional html5 seats will be created dynamically when a remote client connects.
         */
        final WlSeat wlSeat = html5X11EglCompositor.wlSeat();

        /*
         * Setup keyboard focus tracking to follow mouse pointer.
         */
        final WlKeyboard wlKeyboard = wlSeat.getWlKeyboard();
        final PointerDevice pointerDevice = wlSeat.getWlPointer()
                                                  .getPointerDevice();
        pointerDevice.getPointerFocusSignal()
                     .connect(event -> wlKeyboard.getKeyboardDevice()
                                                 .setFocus(wlKeyboard.getResources(),
                                                           pointerDevice.getFocus()));

        lifeCycle.start();
    }

    private void strap(final DaggerDispmanxEglCompositor.Builder builder) {

        final DispmanxEglCompositor dispmanxEglCompositor = builder.build();

        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = dispmanxEglCompositor.lifeCycle();

        final WlSeat wlSeat = dispmanxEglCompositor.seatFactory()
                                                   .create("seat0",
                                                           "",
                                                           "",
                                                           "",
                                                           "",
                                                           "");

        /*
         * Setup keyboard focus tracking to follow mouse pointer & touch.
         */
        final PointerDevice pointerDevice = wlSeat.getWlPointer()
                                                  .getPointerDevice();
        final TouchDevice touchDevice = wlSeat.getWlTouch()
                                              .getTouchDevice();

        final WlKeyboard              wlKeyboard          = wlSeat.getWlKeyboard();
        final KeyboardDevice          keyboardDevice      = wlKeyboard.getKeyboardDevice();
        final Set<WlKeyboardResource> wlKeyboardResources = wlKeyboard.getResources();

        pointerDevice.getPointerFocusSignal()
                     .connect(event -> keyboardDevice.setFocus(wlKeyboardResources,
                                                               pointerDevice.getFocus()));
        touchDevice.getTouchDownSignal()
                   .connect(event -> keyboardDevice.setFocus(wlKeyboardResources,
                                                             touchDevice.getGrab()));
        /*
         * Start the compositor.
         */
        lifeCycle.start();
    }

    private void strap(final DaggerX11EglCompositor.Builder builder) {

        /*
         * Inject X11 config.
         */
        final X11EglCompositor x11EglCompositor = builder.x11PlatformModule(new X11PlatformModule(new X11PlatformConfigSimple()))
                                                         .build();

        /*
         * Keep this first as weston demo clients *really* like their globals
         * to be initialized in a certain order, else they segfault...
         */
        final LifeCycle lifeCycle = x11EglCompositor.lifeCycle();

        /*Get the seat that listens for input on the X connection and passes it on to a wayland seat.
         */
        final WlSeat wlSeat = x11EglCompositor.wlSeat();

        /*
         * Setup keyboard focus tracking to follow mouse pointer.
         */
        final WlKeyboard wlKeyboard = wlSeat.getWlKeyboard();
        final PointerDevice pointerDevice = wlSeat.getWlPointer()
                                                  .getPointerDevice();
        pointerDevice.getPointerFocusSignal()
                     .connect(event -> wlKeyboard.getKeyboardDevice()
                                                 .setFocus(wlKeyboard.getResources(),
                                                           pointerDevice.getFocus()));
        /*
         * Start the compositor.
         */
        lifeCycle.start();
    }
}