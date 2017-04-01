package org.westford.compositor.core;


import javax.inject.Inject;
import java.util.LinkedList;

public class SceneLayer {
    private final LinkedList<SurfaceView> surfaceViews = new LinkedList<>();

    @Inject
    public SceneLayer() {}

    public LinkedList<SurfaceView> getSurfaceViews() {
        return this.surfaceViews;
    }
}
