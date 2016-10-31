package org.westmalle.launch.direct;


import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.LifeCycleSignals;
import org.westmalle.launch.Privileges;
import org.westmalle.nativ.NativeModule;

import javax.inject.Singleton;

@Module(includes = {
        NativeModule.class
})
public class DirectModule {

    @Singleton
    @Provides
    LifeCycleSignals provideLifeCycleSignals() {
        return new DirectLifeCycleSignals();
    }

    @Singleton
    @Provides
    Privileges providePrivileges(final DirectPrivileges directPrivileges) {
        return directPrivileges;
    }
}
