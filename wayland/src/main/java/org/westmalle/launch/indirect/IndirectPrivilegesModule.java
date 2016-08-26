package org.westmalle.launch.indirect;

import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.Privileges;
import org.westmalle.nativ.NativeModule;

@Module(includes = {
        NativeModule.class
})
public class IndirectPrivilegesModule {

    @Provides
    Privileges provideIndirectPrivileges(final IndirectPrivilegesFactory indirectPrivilegesFactory) {
        return indirectPrivilegesFactory.create();
    }
}
