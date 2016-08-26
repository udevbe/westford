package org.westmalle.launch;

import dagger.Subcomponent;
import org.westmalle.launch.direct.DirectLauncherModule;

@Subcomponent(modules = {
        DirectLauncherModule.class
})
public interface DirectLauncherComponent {
    Launcher launcher();
}
