package org.westmalle.launch.direct;


import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.Launcher;
import org.westmalle.launch.Privileges;
import org.westmalle.nativ.NativeModule;

import javax.inject.Singleton;

@Module(includes = {
        NativeModule.class
})
public class DirectModule {

    @Singleton
    @Provides
    Launcher provideLauncher(final DirectLauncher directLauncher) {
        return directLauncher;
    }

    @Singleton
    @Provides
    Privileges providePrivileges(final DirectPrivileges directPrivileges) {
        return directPrivileges;
    }
}
