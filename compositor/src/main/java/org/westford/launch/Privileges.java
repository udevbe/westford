package org.westford.launch;


import org.freedesktop.jaccall.Ptr;

public interface Privileges {
    int open(@Ptr(String.class) long path,
             int flags);

    void setDrmMaster(int fd);

    void dropDrmMaster(int fd);
}
