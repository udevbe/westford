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
package org.westmalle.wayland.html5.egl;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import org.westmalle.wayland.core.EglPlatform;
import org.westmalle.wayland.core.Renderer;
import org.westmalle.wayland.html5.Html5Platform;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@AutoFactory(allowSubclasses = true,
             className = "PrivateHtml5EglPlatformFactory")
public class Html5EglPlatform implements EglPlatform {

    @Nonnull
    private final EglPlatform                       eglPlatform;
    @Nonnull
    private final List<Optional<Html5EglConnector>> eglConnectors;

    @Inject
    Html5EglPlatform(@Nonnull final EglPlatform eglPlatform,
                     @Nonnull final List<Optional<Html5EglConnector>> eglConnectors) {
        this.eglPlatform = eglPlatform;
        this.eglConnectors = eglConnectors;
    }

    @Override
    public long getEglDisplay() {
        return this.eglPlatform.getEglDisplay();
    }

    @Override
    public long getEglContext() {
        return this.eglPlatform.getEglContext();
    }

    @Nonnull
    @Override
    public List<Optional<Html5EglConnector>> getConnectors() {
        return this.eglConnectors;
    }

    @Nonnull
    @Override
    public String getEglExtensions() {
        return this.eglPlatform.getEglExtensions();
    }
}
