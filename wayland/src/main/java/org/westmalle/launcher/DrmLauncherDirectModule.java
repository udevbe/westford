package org.westmalle.launcher;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class DrmLauncherDirectModule {

    @Provides
    @Singleton
    DrmLauncher provideDrmLauncher(final DrmLauncherDirectFactory drmLauncherDirectFactory) {
        return drmLauncherDirectFactory.create();
    }
}
