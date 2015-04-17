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
package org.westmalle.wayland.platform.newt.eglkms;

import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.nativewindow.egl.EGLGraphicsDevice;
import com.jogamp.opengl.egl.EGL;
import com.sun.jna.Pointer;
import jogamp.newt.DisplayImpl;
import jogamp.opengl.egl.EGLDisplayUtil;
import org.westmalle.wayland.platform.CLibrary;
import org.westmalle.wayland.platform.newt.eglkms.gbm.GbmLibrary;

public class DisplayDriver extends DisplayImpl {

    private int     fd;
    private Pointer gbmDevice;

    @Override
    protected void createNativeImpl() {
        this.fd = CLibrary.INSTANCE.open("/dev/dri/card0",
                                         CLibrary.O_RDWR);
        this.gbmDevice = GbmLibrary.INSTANCE.gbm_create_device(this.fd);

        final EGLGraphicsDevice eglGraphicsDevice = EGLDisplayUtil.eglCreateEGLGraphicsDevice(Pointer.nativeValue(this.gbmDevice),
                                                                                              AbstractGraphicsDevice.DEFAULT_CONNECTION,
                                                                                              AbstractGraphicsDevice.DEFAULT_UNIT);
        eglGraphicsDevice.open();

        final String extensions = EGL.eglQueryString(eglGraphicsDevice.getHandle(),
                                                     EGL.EGL_EXTENSIONS);

        if (!extensions.contains("EGL_KHR_surfaceless_opengl")) {
            System.err.println("no surfaceless support, cannot initialize\n");
            System.exit(1);
        }

        this.aDevice = eglGraphicsDevice;
    }

    @Override
    protected void closeNativeImpl(final AbstractGraphicsDevice aDevice) {
        aDevice.close();
        GbmLibrary.INSTANCE.gbm_device_destroy(this.gbmDevice);
        this.gbmDevice = null;
        CLibrary.INSTANCE.close(this.fd);
        this.fd = 0;
    }

    @Override
    protected void dispatchMessagesNative() {
        //TODO
    }

    public int getFd() {
        return this.fd;
    }

    public Pointer getGbmDevice() {
        return this.gbmDevice;
    }
}
