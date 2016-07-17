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
package org.westmalle.wayland.nativ.libc;

import org.freedesktop.jaccall.*;

import javax.inject.Singleton;

@Singleton
@Lib("c")
//TODO split this class out based on the different headers implemented by (gnu) libc
public class Libc {

    /**
     * duplicate file descriptor
     */
    public static final int F_DUPFD    = 0;
    /**
     * get file descriptor flags
     */
    public static final int F_GETFD    = 1;
    /**
     * set file descriptor flags
     */
    public static final int F_SETFD    = 2;
    /**
     * get file status flags
     */
    public static final int F_GETFL    = 3;
    /**
     * set file status flags
     */
    public static final int F_SETFL    = 4;
    /**
     * get SIGIO/SIGURG proc/pgrp
     */
    public static final int F_GETOWN   = 5;
    /**
     * set SIGIO/SIGURG proc/pgrp
     */
    public static final int F_SETOWN   = 6;
    /**
     * get record locking information
     */
    public static final int F_GETLK    = 7;
    /**
     * set record locking information
     */
    public static final int F_SETLK    = 8;
    /**
     * F_SETLK; wait if blocked
     */
    public static final int F_SETLKW   = 9;
    //file descriptor flags (F_GETFD, F_SETFD)
    /**
     * close-on-exec flag
     */
    public static final int FD_CLOEXEC = 1;
    // record locking flags (F_GETLK, F_SETLK, F_SETLKW)
    /**
     * shared or read lock
     */
    public static final int F_RDLCK    = 1;
    /**
     * unlock
     */
    public static final int F_UNLCK    = 2;
    /**
     * exclusive or write lock
     */
    public static final int F_WRLCK    = 3;
    /**
     * Wait until lock is granted
     */
    public static final int F_WAIT     = 0x010;
    /**
     * Use flock(2) semantics for lock
     */
    public static final int F_FLOCK    = 0x020;
    /**
     * Use POSIX semantics for lock
     */
    public static final int F_POSIX    = 0x040;
    public static final int O_RDONLY   = 0x0000;
    public static final int O_WRONLY   = 0x0001;
    public static final int O_RDWR     = 0x0002;
    public static final int O_ACCMODE  = 0x0003;

    /***
     * Operation not permitted
     */
    public static final int EPERM   = 1;
    /***
     * No such file or directory
     */
    public static final int ENOENT  = 2;
    /***
     * No such process
     */
    public static final int ESRCH   = 3;
    /**
     * Interrupted system call
     */
    public static final int EINTR   = 4;
    /**
     * I/O error
     */
    public static final int EIO     = 5;
    /**
     * No such device or address
     */
    public static final int ENXIO   = 6;
    /**
     * Argument list too long
     */
    public static final int E2BIG   = 7;
    /**
     * Exec format error
     */
    public static final int ENOEXEC = 8;
    /**
     * Bad file number
     */
    public static final int EBADF   = 9;
    /**
     * No child processes
     */
    public static final int ECHILD  = 10;
    /**
     * Try again
     */
    public static final int EAGAIN  = 11;
    /**
     * Out of memory
     */
    public static final int ENOMEM  = 12;
    /**
     * Permission denied
     */
    public static final int EACCES  = 13;
    /**
     * Bad address
     */
    public static final int EFAULT  = 14;
    /**
     * Block device required
     */
    public static final int ENOTBLK = 15;
    /**
     * Device or resource busy
     */
    public static final int EBUSY   = 16;
    /**
     * File exists
     */
    public static final int EEXIST  = 17;
    /**
     * Cross-device link
     */
    public static final int EXDEV   = 18;
    /**
     * No such device
     */
    public static final int ENODEV  = 19;
    /**
     * Not a directory
     */
    public static final int ENOTDIR = 20;
    /**
     * Is a directory
     */
    public static final int EISDIR  = 21;
    /**
     * Invalid argument
     */
    public static final int EINVAL  = 22;
    /**
     * File table overflow
     */
    public static final int ENFILE  = 23;
    /**
     * Too many open files
     */
    public static final int EMFILE  = 24;
    /**
     * Not a typewriter
     */
    public static final int ENOTTY  = 25;
    /**
     * Text file busy
     */
    public static final int ETXTBSY = 26;
    /**
     * File too large
     */
    public static final int EFBIG   = 27;
    /**
     * No space left on device
     */
    public static final int ENOSPC  = 28;
    /**
     * Illegal seek
     */
    public static final int ESPIPE  = 29;
    /**
     * Read-only file system
     */
    public static final int EROFS   = 30;
    /**
     * Too many links
     */
    public static final int EMLINK  = 31;
    /**
     * Broken pipe
     */
    public static final int EPIPE   = 32;
    /**
     * Math argument out of domain of func
     */
    public static final int EDOM    = 33;
    /**
     * Math result not representable
     */
    public static final int ERANGE  = 34;

    /**
     * Page can be read.
     */
    public static final int  PROT_READ      = 0x1;
    /**
     * Page can be written.
     */
    public static final int  PROT_WRITE     = 0x2;
    /**
     * Page can be executed.
     */
    public static final int  PROT_EXEC      = 0x4;
    /**
     * Page can not be accessed.
     */
    public static final int  PROT_NONE      = 0x0;
    /**
     * Extend change to start of  growsdown vma (mprotect only).
     */
    public static final int  PROT_GROWSDOWN = 0x01000000;
    /**
     * Extend change to start of  growsup vma (mprotect only).
     */
    public static final int  PROT_GROWSUP   = 0x02000000;
    /**
     * Share changes.
     */
    public static final int  MAP_SHARED     = 0x01;
    /**
     * Changes are private.
     */
    public static final int  MAP_PRIVATE    = 0x02;
    //-1
    public static final long MAP_FAILED     = 0xFFFFFFFF;

    @Symbol
    @Ptr
    public native long errno();

    private final Pointer<Integer> errno_p = Pointer.wrap(int.class,
                                                          errno());

    public int getErrno() {
        return this.errno_p.dref();
    }

    public native int write(int fd,
                            @Ptr long buffer,
                            int n_byte);

    public native int open(@Ptr(String.class) long path,
                           int flags);

    public native int close(int fd);

    public native void read(int fd,
                            @Ptr long buffer,
                            int n_byte);

    public native int fcntl(int fd,
                            int operation,
                            int args);

    public native int pipe(@Ptr long pipeFds);

    public native int unlink(final @Ptr long pathname);

    public native int mkstemp(final @Ptr long template);

    public native int ftruncate(int fd,
                                int length);

    @Ptr
    public native long mmap(@Ptr long addr,
                            int len,
                            int prot,
                            int flags,
                            int fildes,
                            int off);

    @Ptr
    public native long strcpy(@Ptr long dest,
                              @Ptr long src);

    public native int setjmp(@Ptr long env);

    @Ptr(Void.class)
    public native long memcpy(@Ptr(Void.class) long dest,
                              @Ptr(Void.class) long src,
                              int num);

}
