package org.westford.launch.direct

import org.freedesktop.jaccall.Ptr
import org.westford.launch.Privileges
import org.westford.nativ.glibc.Libc
import org.westford.nativ.libdrm.Libdrm
import javax.inject.Inject

class DirectPrivileges @Inject internal constructor(private val libc: Libc,
                                                    private val libdrm: Libdrm) : Privileges {

    override fun open(@Ptr(String::class) path: Long,
                      flags: Int): Int = this.libc.open(path,
                                                        flags)

    override fun setDrmMaster(fd: Int) {
        if (this.libdrm.drmSetMaster(fd) != 0) {
            throw RuntimeException("failed to set drm master: " + this.libc.strError)
        }
    }

    override fun dropDrmMaster(fd: Int) {
        if (this.libdrm.drmDropMaster(fd) != 0) {
            throw RuntimeException("failed to drop drm master: " + this.libc.strError)
        }
    }
}
