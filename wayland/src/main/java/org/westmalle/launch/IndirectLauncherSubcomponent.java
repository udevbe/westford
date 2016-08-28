package org.westmalle.launch;

import dagger.Subcomponent;
import org.westmalle.launch.indirect.IndirectLauncherModule;

import javax.inject.Singleton;

@Singleton
@Subcomponent(modules = {
        IndirectLauncherModule.class
})
public interface IndirectLauncherSubcomponent {
    Launcher launcher();
}
