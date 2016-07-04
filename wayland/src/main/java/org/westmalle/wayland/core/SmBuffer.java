//Copyright 2016 Erik De Rijcke
//
//Licensed under the Apache License,Version2.0(the"License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing,software
//distributed under the License is distributed on an"AS IS"BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
package org.westmalle.wayland.core;

import com.google.auto.value.AutoValue;
import org.freedesktop.wayland.server.ShmBuffer;
import org.freedesktop.wayland.server.WlBufferResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoValue
public abstract class SmBuffer implements Buffer {

    @Nonnull
    public static SmBuffer create(@Nonnegative final int width,
                                  @Nonnegative final int height,
                                  @Nonnull final WlBufferResource wlBufferResource,
                                  @Nonnull final ShmBuffer shmBuffer) {
        return new AutoValue_SmBuffer(width,
                                      height,
                                      wlBufferResource,
                                      shmBuffer);
    }

    @Override
    public void accept(@Nonnull final BufferVisitor bufferVisitor) {
        bufferVisitor.visit(this);
    }

    @Override
    @Nonnegative
    public abstract int getWidth();

    @Override
    @Nonnegative
    public abstract int getHeight();

    @Override
    @Nonnull
    public abstract WlBufferResource getWlBufferResource();

    @Nonnull
    public abstract ShmBuffer getShmBuffer();
}
