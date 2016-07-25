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
package org.westmalle.wayland.drm.egl;

import dagger.Module;
import dagger.Provides;
import org.westmalle.wayland.core.Platform;
import org.westmalle.wayland.drm.DrmPlatform;
import org.westmalle.wayland.drm.DrmPlatformFactory;

import javax.inject.Singleton;

@Module
public class DrmEglPlatformModule {

    @Provides
    @Singleton
    DrmPlatform createDrmPlatform(final DrmPlatformFactory drmPlatformFactory) {
        return drmPlatformFactory.create();
    }

    @Provides
    @Singleton
    Platform createPlatform(final DrmEglPlatformFactory drmEglPlatformFactory) {
        return drmEglPlatformFactory.create();
    }
}
