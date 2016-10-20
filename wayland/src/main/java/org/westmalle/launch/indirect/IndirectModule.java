package org.westmalle.launch.indirect;


import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.LifeCycleSignals;
import org.westmalle.launch.Privileges;
import org.westmalle.nativ.NativeModule;

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
