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
package org.westmalle.wayland.nativ.libGLESv2;

import com.sun.jna.LastErrorException;
import com.sun.jna.Pointer;

public class LibGLESv2 {
    public static final int GL_VERTEX_SHADER        = 0x8B31;
    public static final int GL_FRAGMENT_SHADER      = 0x8B30;
    public static final int GL_COMPILE_STATUS       = 0x8B81;
    public static final int GL_TRUE                 = 1;
    public static final int GL_INFO_LOG_LENGTH      = 0x8B84;
    public static final int GL_ARRAY_BUFFER         = 0x8892;
    public static final int GL_DYNAMIC_DRAW         = 0x88E8;
    public static final int GL_TRIANGLES            = 0x0004;
    public static final int GL_ELEMENT_ARRAY_BUFFER = 0x8893;
    public static final int GL_COLOR_BUFFER_BIT     = 0x00004000;
    public static final int GL_TEXTURE_2D           = 0x0DE1;
    public static final int GL_TEXTURE_WRAP_S       = 0x2802;
    public static final int GL_CLAMP_TO_EDGE        = 0x812F;
    public static final int GL_TEXTURE_WRAP_T       = 0x2803;
    public static final int GL_TEXTURE_MIN_FILTER   = 0x2801;
    public static final int GL_NEAREST              = 0x2600;
    public static final int GL_TEXTURE_MAG_FILTER   = 0x2800;

    public static final int GL_BYTE            = 0x1400;
    public static final int GL_UNSIGNED_BYTE   = 0x1401;
    public static final int GL_SHORT           = 0x1402;
    public static final int GL_UNSIGNED_SHORT  = 0x1403;
    public static final int GL_INT             = 0x1404;
    public static final int GL_UNSIGNED_INT    = 0x1405;
    public static final int GL_FLOAT           = 0x1406;
    public static final int GL_FIXED           = 0x140C;
    public static final int GL_DEPTH_COMPONENT = 0x1902;
    public static final int GL_ALPHA           = 0x1906;
    public static final int GL_RGB             = 0x1907;
    public static final int GL_RGBA            = 0x1908;

    public native void glViewport(int x,
                                  int y,
                                  int width,
                                  int height);

    public native void glClear(int mask);

    public native void glBindBuffer(int target,
                                    int buffer);

    public native void glBufferData(int target,
                                    long size,
                                    Pointer data,
                                    int usage);

    public native void glUseProgram(int program);

    public native void glDrawElements(int mode,
                                      int count,
                                      int type,
                                      Pointer indices);

    public native int glCreateShader(int type);

    public native int glCreateProgram();

    public native void glAttachShader(int program,
                                      int shader);

    public native void glLinkProgram(int program);

    public native void glShaderSource(int shader,
                                      int count,
                                      Pointer string,
                                      Pointer length);

    public native void glCompileShader(int shader);

    public native void glGetShaderiv(int shader,
                                     int pname,
                                     Pointer params);

    public native void glGetShaderInfoLog(int shader,
                                          int bufSize,
                                          Pointer length,
                                          Pointer infoLog);

    public native int glGetUniformLocation(int program,
                                           Pointer name);

    public native void glUniformMatrix4fv(int location,
                                          int count,
                                          boolean transpose,
                                          Pointer value);

    public native int glGetAttribLocation(int program,
                                          Pointer name);

    public native void glEnableVertexAttribArray(int index);

    public native void glVertexAttribPointer(int index,
                                             int size,
                                             int type,
                                             boolean normalized,
                                             int stride,
                                             Pointer pointer) throws LastErrorException;

    public native void glGenBuffers(int n,
                                    Pointer buffers);

    public native void glGenTextures(int n,
                                     Pointer textures);

    public native void glBindTexture(int target,
                                     int texture);

    public native void glTexImage2D(int target,
                                    int level,
                                    int internalformat,
                                    int width,
                                    int height,
                                    int border,
                                    int format,
                                    int type,
                                    Pointer pixels);

    public native void glTexParameteri(int target,
                                       int pname,
                                       int param);

    public native void glTexSubImage2D(int target,
                                       int level,
                                       int xoffset,
                                       int yoffset,
                                       int width,
                                       int height,
                                       int format,
                                       int type,
                                       Pointer pixels);

    public native void glDeleteTextures(int n,
                                        Pointer textures);


    public native void glClearColor(final float v,
                                    final float v1,
                                    final float v2,
                                    final float v3);

    public native void glDrawArrays(int mode,
                                    int first,
                                    int count);
}
