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
package org.westford.compositor.core

import javax.inject.Inject
import javax.inject.Singleton
import java.util.Collections

@Singleton class NullRegion @Inject internal constructor() : Region {

    override fun asList(): List<Rectangle> = emptyList()

    override fun add(rectangle: Rectangle) {}

    override fun subtract(rectangle: Rectangle) {}

    override fun contains(point: Point): Boolean = false

    override fun contains(clipping: Rectangle,
                          point: Point): Boolean = false

    override fun contains(rectangle: Rectangle): Boolean = false

    override fun intersect(rectangle: Rectangle): Region = this

    override fun copy(): Region = this

    override fun isEmpty(): Boolean = true

    //TODO hash & equals?
}
