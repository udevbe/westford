package org.westmalle.wayland.x11;

import dagger.Subcomponent;

@Subcomponent
public interface X11Component {
    X11OutputFactory outputFactory();
}
