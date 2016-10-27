//TODO launch child process and communicate with a unix socket

#include <stdlib.h>
#include <stdio.h>
#include <signal.h>

#include <jni.h>

struct westmalle_launch {
	int tty;
	int ttynr;
	int sock[2];
	int drm_fd;
	int last_input_fd;
	int kb_mode;

	int signalfd;

	pid_t child;
};

static void
setenv_fd(const char *env, int fd)
{
	char buf[32];

	snprintf(buf, sizeof buf, "%d", fd);
	setenv(env, buf, 1);
}


static void
close_input_fds(struct westmalle_launch *wl)
{
	struct stat s;
	int fd;

	for (fd = 3; fd <= wl->last_input_fd; fd++) {
		if (fstat(fd, &s) == 0 && major(s.st_rdev) == INPUT_MAJOR) {
			/* EVIOCREVOKE may fail if the kernel doesn't
			 * support it, but all we can do is ignore it. */
			ioctl(fd, EVIOCREVOKE, 0);
			close(fd);
		}
	}
}

static int
send_reply(struct westmalle_launch *wl, int reply)
{
	int len;

	do {
		len = send(wl->sock[0], &reply, sizeof reply, 0);
	} while (len < 0 && errno == EINTR);

	return len;
}

static int
handle_open(struct westmalle_launch *wl, struct msghdr *msg, ssize_t len)
{
	int fd = -1, ret = -1;
	char control[CMSG_SPACE(sizeof(fd))];
	struct cmsghdr *cmsg;
	struct stat s;
	struct msghdr nmsg;
	struct iovec iov;
	struct westmalle_launcher_open *message;
	union cmsg_data *data;

	message = msg->msg_iov->iov_base;
	if ((size_t)len < sizeof(*message))
		goto err0;

	/* Ensure path is null-terminated */
	((char *) message)[len-1] = '\0';

	fd = open(message->path, message->flags);
	if (fd < 0) {
		fprintf(stderr, "Error opening device %s: %m\n",
			message->path);
		goto err0;
	}

	if (fstat(fd, &s) < 0) {
		close(fd);
		fd = -1;
		fprintf(stderr, "Failed to stat %s\n", message->path);
		goto err0;
	}

	if (major(s.st_rdev) != INPUT_MAJOR &&
	    major(s.st_rdev) != DRM_MAJOR) {
		close(fd);
		fd = -1;
		fprintf(stderr, "Device %s is not an input or drm device\n",
			message->path);
		goto err0;
	}

err0:
	memset(&nmsg, 0, sizeof nmsg);
	nmsg.msg_iov = &iov;
	nmsg.msg_iovlen = 1;
	if (fd != -1) {
		nmsg.msg_control = control;
		nmsg.msg_controllen = sizeof control;
		cmsg = CMSG_FIRSTHDR(&nmsg);
		cmsg->cmsg_level = SOL_SOCKET;
		cmsg->cmsg_type = SCM_RIGHTS;
		cmsg->cmsg_len = CMSG_LEN(sizeof(fd));
		data = (union cmsg_data *) CMSG_DATA(cmsg);
		data->fd = fd;
		nmsg.msg_controllen = cmsg->cmsg_len;
		ret = 0;
	}
	iov.iov_base = &ret;
	iov.iov_len = sizeof ret;

	do {
		len = sendmsg(wl->sock[0], &nmsg, 0);
	} while (len < 0 && errno == EINTR);

	if (len < 0)
		return -1;

	if (fd != -1 && major(s.st_rdev) == DRM_MAJOR)
		wl->drm_fd = fd;
	if (fd != -1 && major(s.st_rdev) == INPUT_MAJOR &&
	    wl->last_input_fd < fd)
		wl->last_input_fd = fd;

	return 0;
}

static int
handle_socket_msg(struct westmalle_launch *wl)
{
	char control[CMSG_SPACE(sizeof(int))];
	char buf[BUFSIZ];
	struct msghdr msg;
	struct iovec iov;
	int ret = -1;
	ssize_t len;
	struct westmalle_launcher_message *message;

	memset(&msg, 0, sizeof(msg));
	iov.iov_base = buf;
	iov.iov_len  = sizeof buf;
	msg.msg_iov = &iov;
	msg.msg_iovlen = 1;
	msg.msg_control = control;
	msg.msg_controllen = sizeof control;

	do {
		len = recvmsg(wl->sock[0], &msg, 0);
	} while (len < 0 && errno == EINTR);

	if (len < 1)
		return -1;

	message = (void *) buf;
	switch (message->opcode) {
	case WESTMALLE_LAUNCHER_OPEN:
		ret = handle_open(wl, &msg, len);
		break;
	}

	return ret;
}

static int
handle_signal(struct westmalle_launch *wl)
{
	struct signalfd_siginfo sig;
	int pid, status, ret;

	if (read(wl->signalfd, &sig, sizeof sig) != sizeof sig) {
		error(0, errno, "reading signalfd failed");
		return -1;
	}

	switch (sig.ssi_signo) {
	case SIGCHLD:
		pid = waitpid(-1, &status, 0);
		if (pid == wl->child) {
			wl->child = 0;
			if (WIFEXITED(status))
				ret = WEXITSTATUS(status);
			else if (WIFSIGNALED(status))
				/*
				 * If westmalle dies because of signal N, we
				 * return 10+N. This is distinct from
				 * westmalle-launch dying because of a signal
				 * (128+N).
				 */
				ret = 10 + WTERMSIG(status);
			else
				ret = 0;
			quit(wl, ret);
		}
		break;
	case SIGTERM:
	case SIGINT:
		if (wl->child)
			kill(wl->child, sig.ssi_signo);
		break;
	case SIGUSR1:
		send_reply(wl, WESTMALLE_LAUNCHER_DEACTIVATE);
		close_input_fds(wl);
		drmDropMaster(wl->drm_fd);
		ioctl(wl->tty, VT_RELDISP, 1);
		break;
	case SIGUSR2:
		ioctl(wl->tty, VT_RELDISP, VT_ACKACQ);
		drmSetMaster(wl->drm_fd);
		send_reply(wl, WESTMALLE_LAUNCHER_ACTIVATE);
		break;
	default:
		return -1;
	}

	return 0;
}

static int
setup_signals(struct westmalle_launch *wl)
{
	int ret;
	sigset_t mask;
	struct sigaction sa;

	memset(&sa, 0, sizeof sa);
	sa.sa_handler = SIG_DFL;
	sa.sa_flags = SA_NOCLDSTOP | SA_RESTART;
	ret = sigaction(SIGCHLD, &sa, NULL);
	assert(ret == 0);

	sa.sa_handler = SIG_IGN;
	sa.sa_flags = 0;
	sigaction(SIGHUP, &sa, NULL);

	ret = sigemptyset(&mask);
	assert(ret == 0);
	sigaddset(&mask, SIGCHLD);
	sigaddset(&mask, SIGINT);
	sigaddset(&mask, SIGTERM);
	sigaddset(&mask, SIGUSR1);
	sigaddset(&mask, SIGUSR2);
	ret = sigprocmask(SIG_BLOCK, &mask, NULL);
	assert(ret == 0);

	wl->signalfd = signalfd(-1, &mask, SFD_NONBLOCK | SFD_CLOEXEC);
	if (wl->signalfd < 0)
		return -errno;

	return 0;
}

static int
setup_launcher_socket(struct westmalle_launch *wl)
{
	if (socketpair(AF_LOCAL, SOCK_SEQPACKET, 0, wl->sock) < 0)
		error(1, errno, "socketpair failed");

	if (fcntl(wl->sock[0], F_SETFD, FD_CLOEXEC) < 0)
		error(1, errno, "fcntl failed");

	return 0;
}


static int
setup_tty(struct westmalle_launch *wl)
{
	struct stat buf;
	struct vt_mode mode = { 0 };
	char *t;

    int tty0 = open("/dev/tty0", O_WRONLY | O_CLOEXEC);
    char filename[16];

    if (tty0 < 0)
        error(1, errno, "could not open tty0");

    if (ioctl(tty0, VT_OPENQRY, &wl->ttynr) < 0 || wl->ttynr == -1)
        error(1, errno, "failed to find non-opened console");

    snprintf(filename, sizeof filename, "/dev/tty%d", wl->ttynr);
    wl->tty = open(filename, O_RDWR | O_NOCTTY);
    close(tty0);

	if (wl->tty < 0)
		error(1, errno, "failed to open tty");

	if (fstat(wl->tty, &buf) == -1 ||
	    major(buf.st_rdev) != TTY_MAJOR || minor(buf.st_rdev) == 0)
		error(1, 0, "westmalle-launch must be run from a virtual terminal");

	if (ioctl(wl->tty, KDGKBMODE, &wl->kb_mode))
		error(1, errno, "failed to get current keyboard mode: %m\n");

	if (ioctl(wl->tty, KDSKBMUTE, 1) &&
	    ioctl(wl->tty, KDSKBMODE, K_OFF))
		error(1, errno, "failed to set K_OFF keyboard mode: %m\n");

	if (ioctl(wl->tty, KDSETMODE, KD_GRAPHICS))
		error(1, errno, "failed to set KD_GRAPHICS mode on tty: %m\n");

	mode.mode = VT_PROCESS;
	mode.relsig = SIGUSR1;
	mode.acqsig = SIGUSR2;
	if (ioctl(wl->tty, VT_SETMODE, &mode) < 0)
		error(1, errno, "failed to take control of vt handling\n");

	return 0;
}

static JNIEnv*
create_vm(int argc, char **argv) {
	JavaVM* jvm;
	JNIEnv* env;
	JavaVMInitArgs args;
	JavaVMOption options[argc+1];
	int i;

	args.version = JNI_VERSION_1_6;
	args.nOptions = argc;

	options[0].optionString = "-Djava.class.path=drm.indirect.jar";
    fprintf(stdout, "INFO: Adding JVM option %s.\n", options[0].optionString);

    for(i = 1; i < argc; i++) {
        options[i].optionString = argv[i];
        fprintf(stdout, "INFO: Adding JVM option %s.\n", argv[i]);
    }

	args.options = options;
	args.ignoreUnrecognized = JNI_FALSE;

	if(JNI_CreateJavaVM(&jvm, (void **)&env, &args)){
        fprintf(stderr, "ERROR: Failed to create the JVM.\n");
	    exit(1);
	}

	return env;
}
static void
invoke_class(JNIEnv* env) {
	jclass bootClass;
	jmethodID mainMethod;
	const char* cls = "org/westmalle/wayland/bootstrap/drm/indirect/Boot";
	const char* entry = "main_from_native";

	bootClass = (*env)->FindClass(env, cls);
	if(bootClass){
		mainMethod = (*env)->GetStaticMethodID(env, bootClass, entry, "()V");
	} else {
	    fprintf(stderr, "ERROR: %s not found on classpath.\n", cls);
        exit(1);
	}

    if(mainMethod){
        (*env)->CallStaticVoidMethod(env, bootClass, mainMethod);
    } else {
    	fprintf(stderr, "ERROR: Method %s not found on class %s.\n", entry, cls);
    	exit(1);
    }
}

static void
launch_compositor(struct westmalle_launch *wl, int argc, char *argv[])
{
	char *child_argv[MAX_ARGV_SIZE];
	sigset_t mask;

	if (geteuid() == 0)
		drop_privileges(wl);

	setenv_fd("WESTMALLE_TTY_FD", wl->tty);
	setenv_fd("WESTMALLE_LAUNCHER_SOCK", wl->sock[1]);

	unsetenv("DISPLAY");

	/* Do not give our signal mask to the new process. */
	sigemptyset(&mask);
	sigaddset(&mask, SIGTERM);
	sigaddset(&mask, SIGCHLD);
	sigaddset(&mask, SIGINT);
	sigprocmask(SIG_UNBLOCK, &mask, NULL);

	JNIEnv* env = create_vm(argc, argv);
	invoke_class(env);
}

int main(int argc, char **argv) {
    struct westmalle_launch wl;

    if(setup_tty(&wl) < 0){
        exit(1);
    }
    if(setup_launcher_socket(&wl) {
        exit(1);
    }
    if(setup_signals(&wl) < 0) {
        exit(1);
    }

    wl.child = fork();
	if (wl.child == -1)
		error(EXIT_FAILURE, errno, "fork failed");

	if (wl.child == 0)
		launch_compositor(&wl, argc, argv);

	close(wl.sock[1]);
	if (wl.tty != STDIN_FILENO)
		close(wl.tty);

	while (1) {
		struct pollfd fds[2];
		int n;

		fds[0].fd = wl.sock[0];
		fds[0].events = POLLIN;
		fds[1].fd = wl.signalfd;
		fds[1].events = POLLIN;

		n = poll(fds, 2, -1);
		if (n < 0)
			error(0, errno, "poll failed");
		if (fds[0].revents & POLLIN)
			handle_socket_msg(&wl);
		if (fds[1].revents)
			handle_signal(&wl);
	}

	return 0;
}