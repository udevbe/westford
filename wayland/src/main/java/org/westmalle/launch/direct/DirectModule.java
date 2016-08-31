package org.westmalle.launch.direct;


import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.Privileges;
import org.westmalle.nativ.NativeModule;
import org.westmalle.tty.TtyModule;

import javax.inject.Singleton;

@Module(includes = {
        TtyModule.class,
        NativeModule.class
})
public class DirectModule {

    @Singleton
    @Provides
    Privileges providePrivileges(final DirectPrivilegesFactory directPrivilegesFactory) {
        return directPrivilegesFactory.create();
    }
}
