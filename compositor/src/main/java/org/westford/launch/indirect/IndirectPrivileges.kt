package org.westford.launch.indirect

import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import org.freedesktop.jaccall.CLong
import org.freedesktop.jaccall.Pointer
import org.freedesktop.jaccall.Ptr
import org.westford.launch.Privileges
import org.westford.nativ.glibc.Libc
import org.westford.nativ.glibc.cmsghdr
import org.westford.nativ.glibc.iovec
import org.westford.nativ.glibc.msghdr
import org.westford.nativ.linux.Socket

import org.freedesktop.jaccall.Size.sizeof


@AutoFactory(allowSubclasses = true, className = "PrivatePrivilegesProxyFactory")
class IndirectPrivileges internal constructor(@param:Provided private val libc: Libc,
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
                messageSize)
                .castp<launcher_open>(launcher_open::class.java!!).use { message ->
            if (message.address == 0L) {
                throw RuntimeException(String.format("Unable to allocate %d bytes. Memory full?",
                        messageSize))
            }

            message.dref()
                    .opcode(NativeConstants.OPCODE_WESTMALLE_LAUNCHER_OPEN)
            message.dref()
                    .flags(flags)
            this.libc.strcpy(message.dref()
                    .path().address,
                    path)

            var len: Long
            do {
                len = this.libc.send(this.launcherFd,
                        message.address,
                        messageSize.toLong(),
                        0)
            } while (len < 0 && this.libc.errno == Libc.EINTR)
        }
    }

    private fun receiveOpenReply(): Int {
        Pointer.calloc(1,
                sizeof(null!!.toInt())).use { ret ->
            Pointer.calloc<msghdr>(1,
                    msghdr.SIZE,
                    msghdr::class.java!!).use { msg ->
                Pointer.calloc<iovec>(1,
                        iovec.SIZE,
                        iovec::class.java!!).use { iov ->
                    val controlSize = this.libc.CMSG_SPACE(sizeof(null!!.toInt()).toLong()).toInt()
                    val control = Pointer.calloc(1,
                            controlSize)

                    iov.dref()
                            .iov_base(ret)
                    val retSize = sizeof(null!!.toInt())
                    iov.dref()
                            .iov_len(CLong(retSize.toLong()))

                    msg.dref()
                            .msg_iov(iov)
                    msg.dref()
                            .msg_iovlen(CLong(1))
                    msg.dref()
                            .msg_control(control)
                    msg.dref()
                            .msg_controllen(CLong(controlSize.toLong()))

                    val len: Long
                    do {
                        len = this.libc.recvmsg(this.launcherFd,
                                msg.address,
                                Socket.MSG_CMSG_CLOEXEC)
                    } while (len < 0 && this.libc.errno == Libc.EINTR)

                    if (len != retSize.toLong() || ret.castp(Int::class.java!!)
                            .dref() < 0) {
                        throw RuntimeException("Receive an illegal open reply.")
                    }

                    val cmsg = this.libc.CMSG_FIRSTHDR(msg.dref())
                    if (cmsg.address == 0L ||
                            cmsg.dref()
                                    .cmsg_level() !== Socket.SOL_SOCKET ||
                            cmsg.dref()
                                    .cmsg_type() !== Socket.SCM_RIGHTS) {
                        throw RuntimeException("invalid control message")
                    }

                    return this.libc.CMSG_DATA(cmsg)
                            .castp<Int>(Int::class.java!!)
                            .dref()
                }
            }
        }
    }

    override fun setDrmMaster(fd: Int) {
        //NOOP set drm master is done in the parent process
    }

    override fun dropDrmMaster(fd: Int) {
        //NOOP drop drm master is done in the parent process
    }
}
