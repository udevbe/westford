#include <stdlib.h>
#include <stdio.h>
#include <signal.h>

#include <jni.h>

#include "Config.h"

static void
setup_signals(void)
{
    sigset_t sigset;
    sigemptyset(&sigset);

    //the signals being blocked here must the same signals defined in the org.westford.tty.Tty class.
    sigaddset(&sigset,
              SIGRTMIN);
    sigaddset(&sigset,
              SIGRTMIN + 1);
    if(0 != pthread_sigmask(SIG_BLOCK,
                            &sigset,
                            NULL)){
        fprintf(stderr, "ERROR: Failed to block SIGRTMIN and SIGRTMIN+1 signals.\n");
	    exit(1);
    }
}

static JNIEnv*
create_vm(int argc, char **argv)
{
	JavaVM* jvm;
	JNIEnv* env;
	JavaVMInitArgs args;
	JavaVMOption options[argc+1];
	int i;

	args.version = JNI_VERSION_1_6;
	args.nOptions = argc;

    //TODO use config file to reference installed jar file directly
	options[0].optionString = "-Djava.class.path=drm.direct-"VERSION_MAJOR"."VERSION_MINOR"."VERSION_PATCH"-"VERSION_EXT".jar";
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
invoke_class(JNIEnv* env)
{
	jclass bootClass;
	jmethodID mainMethod;
	const char* cls = "org/westford/wayland/bootstrap/drm/direct/Boot";
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

int main(int argc, char **argv)
{
    setup_signals();
	JNIEnv* env = create_vm(argc, argv);
	invoke_class(env);
	return 0;
}