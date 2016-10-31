package org.westford.launch.indirect;


import dagger.Module;
import dagger.Provides;
import org.westford.launch.LifeCycleSignals;
import org.westford.launch.Privileges;
import org.westford.nativ.NativeModule;

import javax.inject.Singleton;

@Module(includes = {
        NativeModule.class
})
public class IndirectModule {

    @Singleton
    @Provides
    LifeCycleSignals provideIndirectLifeCycleSignals(final IndirectLifeCycleSignalsFactory indirectLifeCycleSignalsFactory) {
        return indirectLifeCycleSignalsFactory.create();
    }

    @Singleton
    @Provides
    Privileges provideIndirectPrivileges(final IndirectPrivilegesFactory indirectPrivilegesFactory) {
        return indirectPrivilegesFactory.create();
    }
}
