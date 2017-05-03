/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.nativ.glibc

import org.freedesktop.jaccall.*
import org.westford.nativ.linux.Kdev_t
import org.westford.nativ.linux.Stat
import org.westford.nativ.linux.stat
import javax.inject.Singleton

@Singleton @Lib(value = "c",
                version = 6) //TODO split this class out based on the different headers implemented by (gnu) glibc
class Libc {
    private val errno_p = Pointer.wrap<Int>(Int::class.javaPrimitiveType!!,
                                            errno())

    //runtime constants
    fun SIGRTMIN(): Int = __libc_current_sigrtmin()

    external fun __libc_current_sigrtmin(): Int

    fun SIGRTMAX(): Int = __libc_current_sigrtmax()

    external fun __libc_current_sigrtmax(): Int

    fun CMSG_SPACE(len: Long): Long = CMSG_ALIGN(len) + CMSG_ALIGN(cmsghdr_Jaccall_StructType.SIZE.toLong())

    fun CMSG_ALIGN(len: Long): Long = len + Size.sizeof(null as CLong?) - 1 and (Size.sizeof(null as CLong?) - 1).inv().toLong()

    fun CMSG_LEN(len: Long): Long = CMSG_ALIGN(cmsghdr_Jaccall_StructType.SIZE + len)

    fun CMSG_DATA(cmsg: Pointer<cmsghdr>): Pointer<Byte> = (cmsg + 1).castp(Byte::class.java)

    fun CMSG_FIRSTHDR(mhdr: msghdr): Pointer<cmsghdr> {
        return if (mhdr.msg_controllen.toLong() >= cmsghdr_Jaccall_StructType.SIZE) mhdr.msg_control.castp(cmsghdr::class.java)
        else Pointer.wrap<cmsghdr>(cmsghdr::class.java,
                                   0L)
    }

    @Symbol @Ptr external fun errno(): Long

    val strError: String
        get() = Pointer.wrap<String>(String::class.java,
                                     strerror(errno)).get()

    @Ptr(String::class) external fun strerror(errnum: Int): Long

    var errno: Int
        get() = this.errno_p.get()
        set(value) = this.errno_p.set(value)

    external fun write(fd: Int,
                       @Ptr buffer: Long,
                       n_byte: Int): Int

    external fun open(@Ptr(String::class) path: Long,
                      flags: Int): Int

    external fun close(fd: Int): Int

    @Lng external fun read(fd: Int,
                           @Ptr buffer: Long,
                           n_byte: Int): Long

    external fun fcntl(fd: Int,
                       operation: Int,
                       args: Int): Int

    external fun pipe(@Ptr pipeFds: Long): Int

    external fun unlink(@Ptr pathname: Long): Int

    external fun mkstemp(@Ptr template: Long): Int

    external fun ftruncate(fd: Int,
                           length: Int): Int

    @Ptr external fun mmap(@Ptr addr: Long,
                           len: Int,
                           prot: Int,
                           flags: Int,
                           fildes: Int,
                           off: Int): Long

    @Ptr external fun strcpy(@Ptr dest: Long,
                             @Ptr src: Long): Long

    external fun setjmp(@Ptr env: Long): Int

    @Ptr(Void::class) external fun memcpy(@Ptr(Void::class) dest: Long,
                                          @Ptr(Void::class) src: Long,
                                          num: Int): Long

    external fun ioctl(fd: Int,
                       @Unsigned request: Long,
                       @Ptr arg: Long): Int

    external fun ioctl(fd: Int,
                       @Unsigned request: Long,
                       arg: Byte): Int

    external fun cfmakeraw(@Ptr termios_p: Long)

    external fun tcgetattr(fd: Int,
                           @Ptr termios_p: Long): Int

    external fun tcsetattr(fd: Int,
                           optional_actions: Int,
                           @Ptr termios_p: Long): Int

    external fun tcflush(fd: Int,
                         queue_selector: Int): Int

    fun fstat(fd: Int,
              @Ptr(stat::class) buf: Long): Int = __fxstat(Stat._STAT_VER,
                                                           fd,
                                                           buf)

    external fun __fxstat(ver: Int,
                          fd: Int,
                          @Ptr(stat::class) buf: Long): Int

    @Unsigned fun major(@Unsigned dev: Int): Int = Kdev_t.MAJOR(dev)

    @Unsigned fun minor(@Unsigned dev: Int): Int = Kdev_t.MINOR(dev)

    @Ptr(String::class) external fun getlogin(): Long

    @Unsigned external fun geteuid(): Int

    @Unsigned external fun getuid(): Int

    @Unsigned external fun getgid(): Int

    external fun setgid(@Unsigned gid: Int): Int

    external fun setuid(@Unsigned getuid: Int): Int

    external fun setenv(@Ptr(String::class) name: Long,
                        @Ptr(String::class) value: Long,
                        overwrite: Int): Int

    external fun unsetenv(@Ptr(String::class) name: Long): Int

    external fun poll(@Ptr(pollfd::class) fds: Long,
                      @Lng nfds: Long,
                      timeout: Int): Int

    external fun signalfd(fd: Int,
                          @Ptr(sigset_t::class) mask: Long,
                          flags: Int): Int

    external fun socketpair(domain: Int,
                            type: Int,
                            protocol: Int,
                            @Ptr(Int::class) sv: Long): Int

    @Lng external fun send(sockfd: Int,
                           @Ptr buf: Long,
                           @Lng len: Long,
                           flags: Int): Long

    @Lng external fun recv(sockfd: Int,
                           @Ptr buf: Long,
                           @Lng len: Long,
                           flags: Int): Long

    @Lng external fun recvmsg(sockfd: Int,
                              @Ptr(msghdr::class) msg: Long,
                              flags: Int): Long

    @Lng external fun sendmsg(sockfd: Int,
                              @Ptr(msghdr::class) msg: Long,
                              flags: Int): Long

    @Lng external fun strlen(@Ptr(String::class) s: Long): Long

    companion object {

        val SIG_BLOCK = 0
        val SIG_UNBLOCK = 1
        val SIG_SETMASK = 2

        val SIGUSR1 = 10
        val SIGUSR2 = 12

        const val NCCS = 32

        val POLLIN = 0x0001
        val POLLPRI = 0x0002
        val POLLOUT = 0x0004
        val POLLERR = 0x0008
        val POLLHUP = 0x0010
        val POLLNVAL = 0x0020

        val AF_LOCAL = 1
        val SOCK_SEQPACKET = 5

        /**
         * duplicate file descriptor
         */
        val F_DUPFD = 0
        /**
         * get file descriptor flags
         */
        val F_GETFD = 1
        /**
         * set file descriptor flags
         */
        val F_SETFD = 2
        /**
         * get file status flags
         */
        val F_GETFL = 3
        /**
         * set file status flags
         */
        val F_SETFL = 4
        /**
         * get SIGIO/SIGURG proc/pgrp
         */
        val F_GETOWN = 5
        /**
         * set SIGIO/SIGURG proc/pgrp
         */
        val F_SETOWN = 6
        /**
         * get record locking information
         */
        val F_GETLK = 7
        /**
         * set record locking information
         */
        val F_SETLK = 8
        /**
         * F_SETLK; wait if blocked
         */
        val F_SETLKW = 9
        //file descriptor flags (F_GETFD, F_SETFD)
        /**
         * close-on-exec flag
         */
        val FD_CLOEXEC = 1
        // record locking flags (F_GETLK, F_SETLK, F_SETLKW)
        /**
         * shared or read lock
         */
        val F_RDLCK = 1
        /**
         * unlock
         */
        val F_UNLCK = 2
        /**
         * exclusive or write lock
         */
        val F_WRLCK = 3
        /**
         * Wait until lock is granted
         */
        val F_WAIT = 0x010
        /**
         * Use flock(2) semantics for lock
         */
        val F_FLOCK = 0x020
        /**
         * Use POSIX semantics for lock
         */
        val F_POSIX = 0x040
        val O_RDONLY = 0x0000
        val O_WRONLY = 0x0001
        val O_RDWR = 0x0002
        val O_ACCMODE = 0x0003
        val O_CLOEXEC = 0x80000
        val O_NOCTTY = 0x100
        val O_NONBLOCK = 0x800

        val SFD_NONBLOCK = O_NONBLOCK
        val SFD_CLOEXEC = O_CLOEXEC

        /***
         * Operation not permitted
         */
        val EPERM = 1
        /***
         * No such file or directory
         */
        val ENOENT = 2
        /***
         * No such process
         */
        val ESRCH = 3
        /**
         * Interrupted system call
         */
        val EINTR = 4
        /**
         * I/O error
         */
        val EIO = 5
        /**
         * No such device or address
         */
        val ENXIO = 6
        /**
         * Argument list too long
         */
        val E2BIG = 7
        /**
         * Exec format error
         */
        val ENOEXEC = 8
        /**
         * Bad file number
         */
        val EBADF = 9
        /**
         * No child processes
         */
        val ECHILD = 10
        /**
         * Try again
         */
        val EAGAIN = 11
        /**
         * Out of memory
         */
        val ENOMEM = 12
        /**
         * Permission denied
         */
        val EACCES = 13
        /**
         * Bad address
         */
        val EFAULT = 14
        /**
         * Block device required
         */
        val ENOTBLK = 15
        /**
         * Device or resource busy
         */
        val EBUSY = 16
        /**
         * File exists
         */
        val EEXIST = 17
        /**
         * Cross-device link
         */
        val EXDEV = 18
        /**
         * No such device
         */
        val ENODEV = 19
        /**
         * Not a directory
         */
        val ENOTDIR = 20
        /**
         * Is a directory
         */
        val EISDIR = 21
        /**
         * Invalid argument
         */
        val EINVAL = 22
        /**
         * File table overflow
         */
        val ENFILE = 23
        /**
         * Too many open files
         */
        val EMFILE = 24
        /**
         * Not a typewriter
         */
        val ENOTTY = 25
        /**
         * Text file busy
         */
        val ETXTBSY = 26
        /**
         * File too large
         */
        val EFBIG = 27
        /**
         * No space left on device
         */
        val ENOSPC = 28
        /**
         * Illegal seek
         */
        val ESPIPE = 29
        /**
         * Read-only file system
         */
        val EROFS = 30
        /**
         * Too many links
         */
        val EMLINK = 31
        /**
         * Broken pipe
         */
        val EPIPE = 32
        /**
         * Math argument out of domain of func
         */
        val EDOM = 33
        /**
         * Math result not representable
         */
        val ERANGE = 34

        /**
         * Page can be read.
         */
        val PROT_READ = 0x1
        /**
         * Page can be written.
         */
        val PROT_WRITE = 0x2
        /**
         * Page can be executed.
         */
        val PROT_EXEC = 0x4
        /**
         * Page can not be accessed.
         */
        val PROT_NONE = 0x0
        /**
         * Extend change to start of  growsdown vma (mprotect only).
         */
        val PROT_GROWSDOWN = 0x01000000
        /**
         * Extend change to start of  growsup vma (mprotect only).
         */
        val PROT_GROWSUP = 0x02000000
        /**
         * Share changes.
         */
        val MAP_SHARED = 0x01
        /**
         * Changes are private.
         */
        val MAP_PRIVATE = 0x02
        //-1
        val MAP_FAILED: Long = 0xFFFFFFFF
    }

}
