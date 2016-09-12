package org.westmalle.launch.indirect;


import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.JvmLauncher;
import org.westmalle.launch.Launcher;
import org.westmalle.launch.Privileges;
import org.westmalle.nativ.NativeModule;

import javax.inject.Singleton;

@Module(includes = {
        NativeModule.class
})
public class IndirectModule {
    @Singleton
    @Provides
    Launcher provideIndirectLauncher(final IndirectLauncherFactory indirectLauncherFactory) {
        return indirectLauncherFactory.create();
    }

    @Singleton
    @Provides
    Privileges provideIndirectPrivileges(final IndirectPrivilegesFactory indirectPrivilegesFactory) {
        return indirectPrivilegesFactory.create();
    }

    @Singleton
    @Provides
    JvmLauncher provideJvmLauncher() {
        return new JvmLauncher();
    }
}
