/*
 * Westford Wayland Compositor.
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
package org.westford.compositor.core;


import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class Compositor {

    @Nonnull
    private final RenderPlatform renderPlatform;

    @Inject
    Compositor(@Nonnull final RenderPlatform renderPlatform) {
        this.renderPlatform = renderPlatform;
    }

    public void requestRender() {
        //TODO optimize by only requesting a render for specific damaged render outputs
        this.renderPlatform.getWlOutputs()
                           .forEach((wlOutput) -> wlOutput.getOutput()
                                                          .getRenderOutput()
                                                          .render(wlOutput));
    }

    @Nonnegative
    public int getTime() {
        return (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }
}
