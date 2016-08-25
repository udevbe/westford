package org.westmalle.launcher.indirect;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Size;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.launcher.Launcher;
import org.westmalle.nativ.glibc.Libc;
import org.westmalle.nativ.glibc.pollfd;
import org.westmalle.nativ.linux.signalfd_siginfo;
import org.westmalle.tty.Tty;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

import static org.westmalle.nativ.glibc.Libc.EINTR;

@AutoFactory(allowSubclasses = true,
             //FIXME thisclassnameistoodamnlong!
             className = "PrivateDrmLauncherIndirectParentFactory")
public class LauncherIndirectParent implements Launcher {

    public static final  int                          ACTIVATE         = 1;
    public static final  int                          DEACTIVATE       = 2;
    private static final Logger                       LOGGER           = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final        Signal<Object, Slot<Object>> activateSignal   = new Signal<>();
    private final        Signal<Object, Slot<Object>> deactivateSignal = new Signal<>();

    @Nonnull
    private final Libc             libc;
    private final int              signalFd;
    @Nonnull
    private final Pointer<Integer> sock;
    @Nonnull
    private final Tty              tty;

    LauncherIndirectParent(@Provided @Nonnull final Libc libc,
                           @Provided @Nonnull final Tty tty,
                           final int signalFd,
                           @Nonnull final Pointer<Integer> sock) {
        this.libc = libc;
        this.signalFd = signalFd;
        this.sock = sock;
        this.tty = tty;
    }

    @Override
    public void switchTty(final int vt) {

    }

    @Override
    public int open(@Ptr(String.class) final long path,
                    final int flags) {
        return 0;
    }

    @Override
    public void setDrmMaster(final int fd) {

    }

    @Override
    public void dropDrmMaster(final int fd) {

    }

    @Override
    public Signal<Object, Slot<Object>> getActivateSignal() {
        return this.activateSignal;
    }

    @Override
    public Signal<Object, Slot<Object>> getDeactivateSignal() {
        return this.deactivateSignal;
    }

    public void pollEvents() {
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
        //else {
        //unsupported signal.
        //}
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
}
