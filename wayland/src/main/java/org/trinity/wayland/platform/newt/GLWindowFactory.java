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
package org.trinity.wayland.platform.newt;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;

import javax.inject.Inject;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

public class GLWindowFactory {

    @Inject
    GLWindowFactory() {
    }

    public GLWindow create() {
        return createDrawable(System.getenv("DISPLAY"),
                              getGLProfile(),
                              800,
                              600);
    }

    private GLWindow createDrawable(final String xDisplay,
                                    final GLProfile profile,
                                    final int width,
                                    final int height) {
        final Display display = NewtFactory.createDisplay(xDisplay);
        final Screen screen = NewtFactory.createScreen(display,
                                                       0);
        final GLWindow drawable = GLWindow.create(screen,
                                                  new GLCapabilities(profile));
        drawable.setSize(width,
                         height);
        drawable.setVisible(true,
                            true);
        return drawable;
    }

    private GLProfile getGLProfile() {
        return GLProfile.getGL2ES2();
    }
}
