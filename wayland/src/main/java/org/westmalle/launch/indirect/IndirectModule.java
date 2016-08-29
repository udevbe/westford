package org.westmalle.launch.indirect;


import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.JvmLauncher;
import org.westmalle.launch.Launcher;
import org.westmalle.launch.Privileges;
import org.westmalle.nativ.NativeModule;
import org.westmalle.tty.TtyModule;

@Module(includes = {
        TtyModule.class,
        NativeModule.class
})
public class IndirectModule {
    @Provides
    Launcher provideIndirectLauncher(final IndirectLauncherFactory indirectLauncherFactory) {
        return indirectLauncherFactory.create();
    }

    @Provides
    Privileges provideIndirectPrivileges(final IndirectPrivilegesFactory indirectPrivilegesFactory) {
        return indirectPrivilegesFactory.create();
    }

    @Provides
    JvmLauncher provideJvmLauncher() {
        return new JvmLauncher();
    }
}
