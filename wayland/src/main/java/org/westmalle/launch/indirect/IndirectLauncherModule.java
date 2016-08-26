package org.westmalle.launch.indirect;

import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.Launcher;
import org.westmalle.nativ.NativeModule;
import org.westmalle.tty.TtyModule;

@Module(includes = {
        TtyModule.class,
        NativeModule.class
})
public class IndirectLauncherModule {

    @Provides
    Launcher provideIndirectLauncher(final IndirectLauncherFactory indirectLauncherFactory) {
        return indirectLauncherFactory.create();
    }
}
