//Copyright 2015 Erik De Rijcke
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


import javax.annotation.Nonnull;
import java.util.List;

public interface Region {

    @Nonnull
    List<Rectangle> asList();

    @Nonnull
    Region add(@Nonnull Rectangle rectangle);

    @Nonnull
    Region subtract(@Nonnull Rectangle rectangle);

    boolean contains(@Nonnull Point point);

    boolean contains(@Nonnull Rectangle clipping,
                     @Nonnull Point point);
}

