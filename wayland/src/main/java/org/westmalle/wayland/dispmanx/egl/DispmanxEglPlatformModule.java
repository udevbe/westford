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
package org.westmalle.wayland.dispmanx.egl;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.dispmanx.DispmanxPlatform;
import org.westmalle.wayland.dispmanx.DispmanxPlatformFactory;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_ID_HDMI;

@Module
public class DispmanxEglPlatformModule {

    @Provides
    @Singleton
    DispmanxPlatform createDispmanxPlatform(final DispmanxPlatformFactory dispmanxPlatformFactory) {
        //FIXME from config
        return dispmanxPlatformFactory.create(DISPMANX_ID_HDMI);
    }

    @Provides
    @Singleton
    Platform createPlatform(@Nonnull final DispmanxEglPlatformFactory dispmanxEglPlatformFactory) {
        return dispmanxEglPlatformFactory.create();
    }
}
