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
package org.westmalle.wayland.bootstrap.x11;


import org.westmalle.wayland.x11.config.X11ConnectorConfig;
import org.westmalle.wayland.x11.config.X11PlatformConfig;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class X11PlatformConfigSimple implements X11PlatformConfig {

    @Nonnull
    @Override
    public String getDisplay() {
        return ":0";
    }

    @Nonnull
    @Override
    public Iterable<X11ConnectorConfig> getX11ConnectorConfigs() {
        return Arrays.asList(new X11ConnectorConfig() {
                                 @Override
                                 public int getWidth() {
                                     return 800;
                                 }

                                 @Override
                                 public int getHeight() {
                                     return 600;
                                 }

                                 @Override
                                 public int getX() {
                                     return 0;
                                 }

                                 @Override
                                 public int getY() {
                                     return 0;
                                 }
                             }
                ,
                             new X11ConnectorConfig() {
                                 @Override
                                 public int getWidth() {
                                     return 800;
                                 }

                                 @Override
                                 public int getHeight() {
                                     return 600;
                                 }

                                 @Override
                                 public int getX() {
                                     return 800;
                                 }

                                 @Override
                                 public int getY() {
                                     return 0;
                                 }
                             }
                            );
    }
}
