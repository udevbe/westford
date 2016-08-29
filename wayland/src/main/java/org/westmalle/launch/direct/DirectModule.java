package org.westmalle.launch.direct;


import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.Privileges;
import org.westmalle.nativ.NativeModule;
import org.westmalle.tty.TtyModule;

@Module(includes = {
        TtyModule.class,
        NativeModule.class
})
public class DirectModule {

    @Provides
    Privileges providePrivileges(final DirectPrivilegesFactory directPrivilegesFactory) {
        return directPrivilegesFactory.create();
    }
}
