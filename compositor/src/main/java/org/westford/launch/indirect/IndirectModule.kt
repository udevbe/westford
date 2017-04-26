package org.westford.launch.indirect


import dagger.Module
import dagger.Provides
import org.westford.launch.LifeCycleSignals
import org.westford.launch.Privileges
import org.westford.nativ.NativeModule

import javax.inject.Singleton

@Module(includes = arrayOf(NativeModule::class))
class IndirectModule {

    @Singleton
    @Provides
    internal fun provideIndirectLifeCycleSignals(indirectLifeCycleSignalsFactory: IndirectLifeCycleSignalsFactory): LifeCycleSignals {
        return indirectLifeCycleSignalsFactory.create()
    }

    @Singleton
    @Provides
    internal fun provideIndirectPrivileges(indirectPrivilegesFactory: IndirectPrivilegesFactory): Privileges {
        return indirectPrivilegesFactory.create()
    }
}
