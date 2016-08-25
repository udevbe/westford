package org.westmalle.launcher.direct;

import dagger.Module;
import dagger.Provides;
import org.westmalle.launcher.Launcher;
import org.westmalle.tty.TtyModule;

import javax.inject.Singleton;

@Module(includes = {TtyModule.class})
public class LauncherDirectModule {

    @Provides
    @Singleton
    Launcher provideDrmLauncher(final LauncherDirectFactory launcherDirectFactory) {
        return launcherDirectFactory.create();
    }
}
