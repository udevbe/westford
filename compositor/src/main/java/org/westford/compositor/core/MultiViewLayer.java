package org.westford.compositor.core;


import javax.inject.Inject;
import java.util.LinkedList;

public class MultiViewLayer {
    private final LinkedList<SurfaceView> surfaceViews = new LinkedList<>();

    @Inject
    public MultiViewLayer() {}

    public LinkedList<SurfaceView> getSurfaceViews() {
        return this.surfaceViews;
    }
}
