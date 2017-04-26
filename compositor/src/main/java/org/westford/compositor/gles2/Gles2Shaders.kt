package org.westford.compositor.gles2


object Gles2Shaders {
    internal val VERTEX_SHADER =
            "uniform mat4 u_projection;\n" +
                    "uniform mat4 u_transform;\n" +
                    "attribute vec2 a_position;\n" +
                    "attribute vec2 a_texCoord;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "void main(){\n" +
                    "    v_texCoord = a_texCoord;\n" +
                    "    gl_Position = u_projection * u_transform * vec4(a_position, 0.0, 1.0) ;\n" +
                    "}"
    internal val FRAGMENT_SHADER_ARGB8888 =
            "precision mediump float;\n" +
                    "uniform sampler2D u_texture0;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "void main(){\n" +
                    "    gl_FragColor = texture2D(u_texture0, v_texCoord);\n" +
                    "}"
    internal val FRAGMENT_SHADER_XRGB8888 =
            "precision mediump float;\n" +
                    "uniform sampler2D u_texture0;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "void main(){\n" +
                    "    gl_FragColor = vec4(texture2D(u_texture0, v_texCoord).bgr, 1.0);\n" +
                    "}"
    internal val FRAGMENT_SHADER_EGL_EXTERNAL =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES u_texture0;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "void main(){\n" +
                    "   gl_FragColor = texture2D(u_texture0, v_texCoord)\n;" +
                    "}"
    private val FRAGMENT_CONVERT_YUV =
            "  gl_FragColor.r = y + 1.59602678 * v;\n" +
                    "  gl_FragColor.g = y - 0.39176229 * u - 0.81296764 * v;\n" +
                    "  gl_FragColor.b = y + 2.01723214 * u;\n" +
                    "  gl_FragColor.a = 1.0;\n" +
                    "}"
    internal val FRAGMENT_SHADER_EGL_Y_XUXV =
            "precision mediump float;\n" +
                    "uniform sampler2D u_texture0;\n" +
                    "uniform sampler2D u_texture1;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "void main() {\n" +
                    "  float y = 1.16438356 * (texture2D(u_texture0, v_texCoord).x - 0.0625);\n" +
                    "  float u = texture2D(u_texture1, v_texCoord).g - 0.5;\n" +
                    "  float v = texture2D(u_texture1, v_texCoord).a - 0.5;\n" +
                    FRAGMENT_CONVERT_YUV
    internal val FRAGMENT_SHADER_EGL_Y_U_V =
            "precision mediump float;\n" +
                    "uniform sampler2D u_texture0;\n" +
                    "uniform sampler2D u_texture1;\n" +
                    "uniform sampler2D u_texture2;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "void main() {\n" +
                    "  float y = 1.16438356 * (texture2D(u_texture0, v_texCoord).x - 0.0625);\n" +
                    "  float u = texture2D(u_texture1, v_texCoord).x - 0.5;\n" +
                    "  float v = texture2D(u_texture2, v_texCoord).x - 0.5;\n" +
                    FRAGMENT_CONVERT_YUV
    internal val FRAGMENT_SHADER_EGL_Y_UV =
            "precision mediump float;\n" +
                    "uniform sampler2D u_texture0;\n" +
                    "uniform sampler2D u_texture1;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "void main() {\n" +
                    "  float y = 1.16438356 * (texture2D(u_texture0, v_texCoord).x - 0.0625);\n" +
                    "  float u = texture2D(u_texture1, v_texCoord).r - 0.5;\n" +
                    "  float v = texture2D(u_texture1, v_texCoord).g - 0.5;\n" +
                    FRAGMENT_CONVERT_YUV
}
