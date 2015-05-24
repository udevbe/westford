package org.westmalle.wayland.nativ;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class Libgles2 {
    static{
        Native.register("gles2");
    }

    Libgles2() {
    }

    public final int GL_VERTEX_SHADER = 0x8B31;
    public final int GL_FRAGMENT_SHADER = 0x8B30;
    public final int GL_COMPILE_STATUS = 0x8B81;
    public final int GL_TRUE = 1;
    public final int GL_INFO_LOG_LENGTH = 0x8B84;
    public final int GL_ARRAY_BUFFER = 0x8892;
    public final int GL_DYNAMIC_DRAW = 0x88E8;
    public final int GL_FLOAT = 0x1406;
    public final int GL_UNSIGNED_INT = 0x1405;
    public final int GL_TRIANGLES = 0x0004;
    public final int GL_ELEMENT_ARRAY_BUFFER = 0x8893;
    public final int GL_COLOR_BUFFER_BIT = 0x00004000;
    public final int GL_TEXTURE_2D = 0x0DE1;
    public final int GL_UNSIGNED_BYTE = 0x1401;
    public final int GL_TEXTURE_WRAP_S = 0x2802;
    public final int GL_CLAMP_TO_EDGE = 0x812F;
    public final int GL_TEXTURE_WRAP_T = 0x2803;
    public final int GL_TEXTURE_MIN_FILTER = 0x2801;
    public final int GL_NEAREST = 0x2600;
    public final int GL_TEXTURE_MAG_FILTER = 0x2800;
    public final int GL_RGBA = 0x1908;


    public native void glViewport(int x, int y, int width, int height);

    public native void glClear(int mask);

    public native void glBindBuffer(int target, int buffer);

    public native void glBufferData(int target, long size, Pointer data, int usage);

    public native void glUseProgram(int program);

    public native void glDrawElements(int mode, int count, int type, Pointer indices);

    public native int glCreateShader(int type);

    public native int glCreateProgram();

    public native void glAttachShader(int program, int shader);

    public native void glLinkProgram(int program);

    public native void glShaderSource(int shader, int count, Pointer string, Pointer length);

    public native void glCompileShader(int shader);

    public native void glGetShaderiv(int shader, int pname, Pointer params);

    public native void glGetShaderInfoLog(int shader, int bufSize, Pointer length, Pointer infoLog);

    public native int glGetUniformLocation(int program, Pointer name);

    public native void glUniformMatrix4fv(int location, int count, boolean transpose, Pointer value);

    public native int glGetAttribLocation(int program, Pointer name);

    public native void glEnableVertexAttribArray(int index);

    public native void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, Pointer pointer);
}
