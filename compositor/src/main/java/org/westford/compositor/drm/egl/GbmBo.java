package org.westford.compositor.drm.egl;

public interface GbmBo extends AutoCloseable {
    long getGbmBo();

    @Override
    void close();
}
