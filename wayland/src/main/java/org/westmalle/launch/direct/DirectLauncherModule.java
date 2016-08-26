package org.westmalle.launch.direct;

import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.Launcher;
import org.westmalle.nativ.NativeModule;
import org.westmalle.tty.TtyModule;

@Module(includes = {
        TtyModule.class,
        NativeModule.class
})
public class DirectLauncherModule {

    @Provides
    Launcher providesDirectLauncher() {
        return new DirectLauncher();
    }
}
