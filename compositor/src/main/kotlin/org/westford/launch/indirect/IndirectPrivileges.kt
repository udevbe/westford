package org.westford.launch.indirect

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.jaccall.CLong
import org.freedesktop.jaccall.Pointer
import org.freedesktop.jaccall.Ptr
import org.freedesktop.jaccall.Size.sizeof
import org.westford.launch.Privileges
import org.westford.nativ.glibc.Libc
import org.westford.nativ.glibc.iovec
import org.westford.nativ.glibc.msghdr
import org.westford.nativ.linux.Socket

@AutoFactory(allowSubclasses = true,
             className = "PrivatePrivilegesProxyFactory") class IndirectPrivileges(@param:Provided private val libc: Libc,
                                                                                   private val launcherFd: Int) : Privileges {

    override fun open(@Ptr(String::class) path: Long,
                      flags: Int): Int {
        sendOpenRequest(path,
                        flags)
        return receiveOpenReply()
    }

    private fun sendOpenRequest(@Ptr(String::class) path: Long,
                                flags: Int) {
        val payloadSize = launcher_open.SIZE + this.libc.strlen(path) + 1
        val messageSize = launcher_open.SIZE + payloadSize
        Pointer.calloc(1,
                       messageSize).castp<launcher_open>(launcher_open::class.java).use {
            if (it.address == 0L) {
                throw RuntimeException(String.format("Unable to allocate %d bytes. Memory full?",
                                                     messageSize))
            }

            it.get().opcode(NativeConstants.OPCODE_WESTMALLE_LAUNCHER_OPEN)
            it.get().flags(flags)
            this.libc.strcpy(it.get().path().address,
                             path)

            var len: Long
            do {
                len = this.libc.send(this.launcherFd,
                                     it.address,
                                     messageSize.toLong(),
                                     0)
            }
            while (len < 0 && this.libc.errno == Libc.EINTR)
        }
    }

    private fun receiveOpenReply(): Int {
        Pointer.calloc(1,
                       sizeof(null as Int?)).use {
            Pointer.calloc<msghdr>(1,
                                   msghdr.SIZE,
                                   msghdr::class.java).use { msg ->
                Pointer.calloc<iovec>(1,
                                      iovec.SIZE,
                                      iovec::class.java).use { iov ->
                    val controlSize = this.libc.CMSG_SPACE(sizeof(null as Int?).toLong()).toInt()
                    val control = Pointer.calloc(1,
                                                 controlSize)

                    iov.get().iov_base(it)
                    val retSize = sizeof(null as Int?)
                    iov.get().iov_len(CLong(retSize.toLong()))

                    msg.get().msg_iov(iov)
                    msg.get().msg_iovlen(CLong(1))
                    msg.get().msg_control(control)
                    msg.get().msg_controllen(CLong(controlSize.toLong()))

                    var len: Long
                    do {
                        len = this.libc.recvmsg(this.launcherFd,
                                                msg.address,
                                                Socket.MSG_CMSG_CLOEXEC)
                    }
                    while (len < 0 && this.libc.errno == Libc.EINTR)

                    if (len != retSize.toLong() || it.castp(Int::class.java).get() < 0) {
                        throw RuntimeException("Receive an illegal open reply.")
                    }

                    val cmsg = this.libc.CMSG_FIRSTHDR(msg.get())
                    if (cmsg.address == 0L || cmsg.get().cmsg_level() !== Socket.SOL_SOCKET || cmsg.get().cmsg_type() !== Socket.SCM_RIGHTS) {
                        throw RuntimeException("invalid control message")
                    }

                    return this.libc.CMSG_DATA(cmsg).castp<Int>(Int::class.java).get()
                }
            }
        }
    }

    //NOOP set drm master is done in the parent process
    override fun setDrmMaster(fd: Int) = Unit

    //NOOP drop drm master is done in the parent process
    override fun dropDrmMaster(fd: Int) = Unit
}
