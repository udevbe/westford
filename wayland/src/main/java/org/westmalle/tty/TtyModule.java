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
package org.westmalle.tty;

import dagger.Module;
import dagger.Provides;
import org.westmalle.launch.indirect.NativeConstants;

import javax.inject.Singleton;

@Module
public class TtyModule {
    @Provides
    @Singleton
    Tty provideTty(final TtyFactory ttyFactory) {
        final String westmalleTtyFd = NativeConstants.ENV_WESTMALLE_TTY_FD;
        if (westmalleTtyFd == null) {
            return ttyFactory.create();
        }
        else {
            final int ttyFd = Integer.parseInt(System.getenv(westmalleTtyFd));
            return ttyFactory.create(ttyFd);
        }
    }
}
