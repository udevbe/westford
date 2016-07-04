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
import org.freedesktop.wayland.server.WlBufferResource;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

@AutoValue
public abstract class UnsupportedBuffer implements Buffer {

    public static UnsupportedBuffer create(@Nonnull WlBufferResource wlBufferResource) {
        return new AutoValue_UnsupportedBuffer(wlBufferResource);
    }

    @Nonnegative
    @Override
    public int getWidth() {
        return 0;
    }

    @Nonnegative
    @Override
    public int getHeight() {
        return 0;
    }

    @Nonnull
    @Override
    public abstract WlBufferResource getWlBufferResource();

    @Override
    public void accept(@Nonnegative final BufferVisitor bufferVisitor) {
        bufferVisitor.visit(this);
    }
}
