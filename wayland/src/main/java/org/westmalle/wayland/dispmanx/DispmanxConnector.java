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
package org.westmalle.wayland.dispmanx;


import com.google.auto.factory.AutoFactory;
import org.westmalle.wayland.core.Connector;
import org.westmalle.wayland.protocol.WlOutput;

import javax.annotation.Nonnull;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "DispmanxConnectorFactory")
public class DispmanxConnector implements Connector {

    private final Optional<WlOutput> wlOutput;
    private final int                dispmanxElement;


    DispmanxConnector(final Optional<WlOutput> wlOutput,
                      final int dispmanxElement) {
        this.wlOutput = wlOutput;
        this.dispmanxElement = dispmanxElement;
    }

    @Nonnull
    @Override
    public Optional<WlOutput> getWlOutput() {
        return this.wlOutput;
    }


    public int getDispmanxElement() {
        return this.dispmanxElement;
    }
}
