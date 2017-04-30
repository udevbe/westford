package org.westford.launch

import org.freedesktop.jaccall.Ptr

interface Privileges {
    fun open(@Ptr(String::class) path: Long,
             flags: Int): Int

    fun setDrmMaster(fd: Int)

    fun dropDrmMaster(fd: Int)
}
