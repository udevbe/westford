package org.westmalle.launcher;

import dagger.Module;
import dagger.Provides;
import org.westmalle.tty.TtyModule;

import javax.inject.Singleton;

@Module(includes = {TtyModule.class})
public class DrmLauncherDirectModule {

    @Provides
    @Singleton
    DrmLauncher provideDrmLauncher(final DrmLauncherDirectFactory drmLauncherDirectFactory) {
        return drmLauncherDirectFactory.create();
    }
}
