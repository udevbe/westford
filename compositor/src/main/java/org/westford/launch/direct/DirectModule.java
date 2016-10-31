package org.westford.launch.direct;


import dagger.Module;
import dagger.Provides;
import org.westford.launch.LifeCycleSignals;
import org.westford.launch.Privileges;
import org.westford.nativ.NativeModule;

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
