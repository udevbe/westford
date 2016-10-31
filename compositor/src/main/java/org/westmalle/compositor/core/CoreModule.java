/*
 * Westmalle Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westmalle.compositor.core;

import dagger.Module;
import dagger.Provides;
import org.freedesktop.jaccall.Pointer;
import org.freedesktop.wayland.server.Display;
import org.westmalle.compositor.core.FiniteRegionFactory;
import org.westmalle.nativ.NativeModule;
import org.westmalle.nativ.glibc.Libc;

import javax.inject.Singleton;

@Module(includes = NativeModule.class)
public class CoreModule {

    @Provides
    FiniteRegion provideFiniteRegion(final FiniteRegionFactory finiteRegionFactory) {
        return finiteRegionFactory.create();
    }

    @Provides
    @Singleton
    InfiniteRegion provideInfiniteRegion(final FiniteRegionFactory finiteRegionFactory) {
        return new InfiniteRegion(finiteRegionFactory);
    }

    @Provides
    PointerDevice providePointerDevice(final PointerDeviceFactory pointerDeviceFactory) {
        return pointerDeviceFactory.create();
    }

    @Provides
    @Singleton
    NullRegion provideNullRegion() {
        return new NullRegion();
    }

    @Provides
    @Singleton
    Display provideDisplay() {
        return Display.create();
    }

    @Singleton
    @Provides
    JobExecutor provideJobExecutor(final Display display,
                                   final Libc libc) {
        final Pointer<Integer> pipeFds = Pointer.nref(0,
                                                      0);
        libc.pipe(pipeFds.address);

        final int readFd  = pipeFds.dref(0);
        final int writeFd = pipeFds.dref(1);

        final int readFlags = libc.fcntl(readFd,
                                         Libc.F_GETFD,
                                         0);
        libc.fcntl(readFd,
                   Libc.F_SETFD,
                   readFlags | Libc.FD_CLOEXEC);

        final int writeFlags = libc.fcntl(writeFd,
                                          Libc.F_GETFD,
                                          0);
        libc.fcntl(writeFd,
                   Libc.F_SETFD,
                   writeFlags | Libc.FD_CLOEXEC);

        return new JobExecutor(display,
                               readFd,
                               writeFd,
                               libc);
    }
}
