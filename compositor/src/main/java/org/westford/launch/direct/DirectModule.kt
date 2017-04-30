package org.westford.launch.direct


import dagger.Module
import dagger.Provides
import org.westford.launch.LifeCycleSignals
import org.westford.launch.Privileges
import org.westford.nativ.NativeModule

import javax.inject.Singleton

@Module(includes = arrayOf(NativeModule::class))
class DirectModule {

    @Singleton
    @Provides
    internal fun provideLifeCycleSignals(): LifeCycleSignals = DirectLifeCycleSignals()

    @Singleton
    @Provides
    internal fun providePrivileges(directPrivileges: DirectPrivileges): Privileges = directPrivileges
}
