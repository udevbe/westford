package org.westmalle.wayland.bootstrap;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.westmalle.wayland.core.Transforms;
import org.westmalle.wayland.core.calc.Mat4;
import org.westmalle.wayland.nativ.NativeString;
import org.westmalle.wayland.nativ.libEGL.LibEGL;
import org.westmalle.wayland.nativ.libGLESv2.LibGLESv2;
import org.westmalle.wayland.nativ.libbcm_host.EGL_DISPMANX_WINDOW_T;
import org.westmalle.wayland.nativ.libbcm_host.Libbcm_host;
import org.westmalle.wayland.nativ.libbcm_host.VC_DISPMANX_ALPHA_T;
import org.westmalle.wayland.nativ.libbcm_host.VC_RECT_T;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_ALPHA_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_BLUE_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_CONTEXT_CLIENT_VERSION;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_GREEN_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NONE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_CONTEXT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_DISPLAY;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_NO_SURFACE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES2_BIT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_OPENGL_ES_API;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RED_SIZE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_RENDERABLE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SURFACE_TYPE;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_SWAP_BEHAVIOR_PRESERVED_BIT;
import static org.westmalle.wayland.nativ.libEGL.LibEGL.EGL_WINDOW_BIT;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_BLEND;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_COMPILE_STATUS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_FLOAT;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_FRAGMENT_SHADER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_INFO_LOG_LENGTH;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_LINK_STATUS;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_NEAREST;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_ONE;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_ONE_MINUS_SRC_ALPHA;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_RGBA;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE0;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_2D;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_MAG_FILTER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TEXTURE_MIN_FILTER;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_TRIANGLES;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_UNSIGNED_BYTE;
import static org.westmalle.wayland.nativ.libGLESv2.LibGLESv2.GL_VERTEX_SHADER;
import static org.westmalle.wayland.nativ.libbcm_host.Libbcm_host.DISPMANX_PROTECTION_NONE;

public class Dispmanx {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    static {
        Native.register(Libbcm_host.class,
                        "bcm_host");
        Native.register(LibEGL.class,
                        "EGL");
        Native.register(LibGLESv2.class,
                        "GLESv2");
    }

    //libs
    private final Libbcm_host libbcm_host = new Libbcm_host();

    private final LibGLESv2 libGLESv2 = new LibGLESv2();
    private final LibEGL    libEGL    = new LibEGL();

    //shaders
    private int projectionArg;
    private int positionArg;
    private int textureCoordinateArg;
    private int transformCol0Arg;
    private int transformCol1Arg;
    private int transformCol2Arg;
    private int transformCol3Arg;

    private static final String vertex_shader_code =
            "uniform mat4 u_projection;\n" +
            "\n" +
            "attribute vec2 a_position;\n" +
            "attribute vec2 a_texCoord;\n" +
            "attribute mat4 a_transform;\n" +
            "\n" +
            "varying vec2 v_texCoord;\n" +
            "\n" +
            "void main(){\n" +
            "    v_texCoord = a_texCoord;\n" +
            "    gl_Position = u_projection * a_transform * vec4(a_position, 0.0, 1.0) ;\n" +
            "}";

    private int textureArg;

    private static final String fragment_shader_code =
            "precision mediump float;\n" +
            "\n" +
            "uniform sampler2D u_texture;\n" +
            "\n" +
            "varying vec2 v_texCoord;\n" +
            "\n" +
            "void main(){\n" +
            "    gl_FragColor = texture2D(u_texture, v_texCoord);\n" +
            "}";

    public static void main(final String[] args) throws InterruptedException {
        new Dispmanx();
    }

    public Dispmanx() throws InterruptedException {

        this.libbcm_host.bcm_host_init();

        final int screenWidth  = 1280;
        final int screenHeight = 800;

        //platform specific init
        final Pointer nativewindow  = createDispmanxWindow().getPointer();
        final Pointer nativeDisplay = LibEGL.EGL_DEFAULT_DISPLAY;

        final Pointer display = createEglDisplay(nativeDisplay);

        final Pointer config = createDispmanxConfig(display);

        //init
        final Pointer surface = createEglSurface(nativewindow,
                                                 display,
                                                 config);
        final int program = init();

        //begin draw

        begin(screenWidth,
              screenHeight);

        //@formatter:off
        final Mat4 projection = Mat4.create(2.0f / screenWidth, 0,                     0, -1,
                                            0,                   2.0f / -screenHeight, 0,  1,
                                            0,                   0,                     1,  0,
                                            0,                   0,                     0,  1);
        //@formatter:on

        //perform 2 draws

        //setup green semi transparent buffer
        final Mat4       greenTransform    = Mat4.IDENTITY;
        final int        greenBufferWidth  = 100;
        final int        greenBufferHeight = 100;
        final ByteBuffer greenBuffer       = ByteBuffer.allocateDirect(greenBufferWidth * greenBufferHeight * Integer.BYTES);
        greenBuffer.order(ByteOrder.nativeOrder());
        for (int i = 0; i < greenBufferWidth * greenBufferHeight; i++) {
            //semi transparent green (RGBA)
            greenBuffer.put((byte) 0x00);//R
            greenBuffer.put((byte) 0xFF);//G
            greenBuffer.put((byte) 0x00);//B
            greenBuffer.put((byte) 0x88);//A
        }
        draw(program,
             projection,
             greenTransform,
             100,
             100,
             greenBuffer);

        //setup red semi transparent buffer
        final Mat4 redTransform = Transforms.TRANSLATE(50,
                                                       50);
        final int        redBufferWidth  = 100;
        final int        redBufferHeight = 100;
        final ByteBuffer redBuffer       = ByteBuffer.allocateDirect(redBufferWidth * redBufferHeight * Integer.BYTES);
        for (int i = 0; i < redBufferWidth * redBufferHeight; i++) {
            //semi transparent red (ARGB)
            redBuffer.put((byte) 0xFF);//R
            redBuffer.put((byte) 0x00);//G
            redBuffer.put((byte) 0x00);//B
            redBuffer.put((byte) 0x88);//A
        }
        draw(program,
             projection,
             redTransform,
             100,
             100,
             redBuffer);


        //end draw
        end(display,
            surface);

        Thread.sleep(50000);
    }

    private EGL_DISPMANX_WINDOW_T createDispmanxWindow() {
        final int       dispman_element;
        final int       dispman_display;
        final int       dispman_update;
        final VC_RECT_T dst_rect = new VC_RECT_T();
        final VC_RECT_T src_rect = new VC_RECT_T();

        final Pointer display_width  = new Memory(Integer.BYTES);
        final Pointer display_height = new Memory(Integer.BYTES);

        final int success = this.libbcm_host.graphics_get_display_size(Libbcm_host.DISPMANX_ID_HDMI,
                                                                       display_width,
                                                                       display_height);

        if (success < 0) {
            throw new RuntimeException("couldn't get display size");
        }

        dst_rect.x = 0;
        dst_rect.y = 0;
        dst_rect.width = display_width.getInt(0);
        dst_rect.height = display_height.getInt(0);
        dst_rect.write();

        src_rect.x = 0;
        src_rect.y = 0;
        src_rect.width = display_width.getInt(0) << 16;
        src_rect.height = display_height.getInt(0) << 16;
        src_rect.write();

        dispman_display = this.libbcm_host.vc_dispmanx_display_open(Libbcm_host.DISPMANX_ID_HDMI);
        dispman_update = this.libbcm_host.vc_dispmanx_update_start(0);

        // set alpha to prevent surfaces beneath GL context to show through when GL context uses alpha channel
        final VC_DISPMANX_ALPHA_T alpha = new VC_DISPMANX_ALPHA_T();
        alpha.flags = Libbcm_host.DISPMANX_FLAGS_ALPHA_FIXED_ALL_PIXELS;
        alpha.opacity = 255;
        alpha.mask = 0;
        alpha.write();

        dispman_element = this.libbcm_host.vc_dispmanx_element_add(dispman_update,
                                                                   dispman_display,
                                                                   0/*layer*/,
                                                                   dst_rect.getPointer(),
                                                                   0/*src*/,
                                                                   src_rect.getPointer(),
                                                                   DISPMANX_PROTECTION_NONE,
                                                                   alpha.getPointer(),
                                                                   null/*clamp*/,
                                                                   0/*transform*/);
        final EGL_DISPMANX_WINDOW_T nativewindow = new EGL_DISPMANX_WINDOW_T();

        nativewindow.element = dispman_element;
        nativewindow.width = display_width.getInt(0);
        nativewindow.height = display_height.getInt(0);
        nativewindow.write();

        this.libbcm_host.vc_dispmanx_update_submit_sync(dispman_update);

        return nativewindow;
    }

    private Pointer createEglDisplay(final Pointer nativeDisplay) {
        final boolean result;
        final Pointer display = this.libEGL.eglGetDisplay(nativeDisplay);
        if (display == EGL_NO_DISPLAY) {
            this.libEGL.throwError("eglGetDisplay");
        }

        this.libGLESv2.check("eglGetDisplay");

        // initialize the EGL display connection
        result = this.libEGL.eglInitialize(display,
                                           null,
                                           null);
        if (!result) {
            this.libEGL.throwError("eglInitialize");
        }

        this.libGLESv2.check("eglInitialize");

        final String eglClientApis = this.libEGL.eglQueryString(display,
                                                                LibEGL.EGL_CLIENT_APIS)
                                                .getString(0);
        final String eglVendor = this.libEGL.eglQueryString(display,
                                                            LibEGL.EGL_VENDOR)
                                            .getString(0);
        final String eglVersion = this.libEGL.eglQueryString(display,
                                                             LibEGL.EGL_VERSION)
                                             .getString(0);

        LOGGER.info(format("Creating X11 EGL output:\n"
                           + "\tEGL client apis: %s\n"
                           + "\tEGL vendor: %s\n"
                           + "\tEGL version: %s\n",
                           eglClientApis,
                           eglVendor,
                           eglVersion));

        return display;
    }

    private Pointer createDispmanxConfig(final Pointer display) {
        final boolean result;
        final Pointer num_config = new Memory(Integer.BYTES);

        final int[] attribute_list_values =
                {
                        EGL_SURFACE_TYPE, EGL_WINDOW_BIT | EGL_SWAP_BEHAVIOR_PRESERVED_BIT,
                        EGL_RED_SIZE, 1,
                        EGL_GREEN_SIZE, 1,
                        EGL_BLUE_SIZE, 1,
                        EGL_ALPHA_SIZE, 0,
                        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                        EGL_NONE

                };
        final Pointer attribute_list = new Memory(Integer.BYTES * attribute_list_values.length);
        attribute_list.write(0,
                             attribute_list_values,
                             0,
                             attribute_list_values.length);

        final Pointer configs = new Memory(Pointer.SIZE);
        // get an appropriate EGL frame buffer configuration
        result = this.libEGL.eglChooseConfig(display,
                                             attribute_list,
                                             configs,
                                             1,
                                             num_config);
        if (!result)

        {
            this.libEGL.throwError("eglChooseConfig");
        }

        this.libGLESv2.check("eglChooseConfig");

        return configs.getPointer(0);
    }


    private Pointer createEglSurface(final Pointer nativewindow,
                                     final Pointer display,
                                     final Pointer config) {
        boolean result;

        final int[] context_attributes_values = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        };
        final Pointer context_attributes = new Memory(Integer.BYTES * context_attributes_values.length);
        context_attributes.write(0,
                                 context_attributes_values,
                                 0,
                                 context_attributes_values.length);


        // get an appropriate EGL frame buffer configuration
        result = this.libEGL.eglBindAPI(EGL_OPENGL_ES_API);
        if (!result) {
            this.libEGL.throwError("eglBindAPI");
        }

        this.libGLESv2.check("eglBindAPI");

        // create an EGL window surface
        final Pointer surface = this.libEGL.eglCreateWindowSurface(display,
                                                                   config,
                                                                   nativewindow,
                                                                   null);
        if (surface == EGL_NO_SURFACE) {
            this.libEGL.throwError("eglCreateWindowSurface");
        }

        this.libGLESv2.check("eglCreateWindowSurface");

        // create an EGL rendering context
        final Pointer context = this.libEGL.eglCreateContext(display,
                                                             config,
                                                             EGL_NO_CONTEXT,
                                                             context_attributes);
        if (context == EGL_NO_CONTEXT) {
            this.libEGL.throwError("eglCreateContext");
        }

        this.libGLESv2.check("eglCreateContext");

        // connect the context to the surface
        result = this.libEGL.eglMakeCurrent(display,
                                            surface,
                                            surface,
                                            context);
        if (!result) {
            this.libEGL.throwError("eglMakeCurrent");
        }

        this.libGLESv2.check("eglMakeCurrent");

        return surface;
    }

    private int init() {

        return initShader();
    }

    private int initShader() {
        //vertex shader
        final int     vertexShader       = this.libGLESv2.glCreateShader(GL_VERTEX_SHADER);
        final Pointer vertexShaderSource = new NativeString(vertex_shader_code).getPointer();
        final Pointer vertexShaders      = new Memory(Pointer.SIZE);
        vertexShaders.setPointer(0,
                                 vertexShaderSource);
        this.libGLESv2.glShaderSource(vertexShader,
                                      1,
                                      vertexShaders,
                                      null);
        this.libGLESv2.glCompileShader(vertexShader);

        checkShader(vertexShader);

        //fragment shader
        final int     fragmentShader       = this.libGLESv2.glCreateShader(GL_FRAGMENT_SHADER);
        final Pointer fragmentShaderSource = new NativeString(fragment_shader_code).getPointer();
        final Pointer fragmentShaders      = new Memory(Pointer.SIZE);
        fragmentShaders.setPointer(0,
                                   fragmentShaderSource);
        this.libGLESv2.glShaderSource(fragmentShader,
                                      1,
                                      fragmentShaders,
                                      null);
        this.libGLESv2.glCompileShader(fragmentShader);

        checkShader(fragmentShader);

        //shader program
        final int shaderProgram = this.libGLESv2.glCreateProgram();
        this.libGLESv2.glAttachShader(shaderProgram,
                                      vertexShader);
        this.libGLESv2.check("glAttachShader");

        this.libGLESv2.glAttachShader(shaderProgram,
                                      fragmentShader);
        this.libGLESv2.check("glAttachShader");

        this.libGLESv2.glLinkProgram(shaderProgram);
        this.libGLESv2.check("glLinkProgram");

        //check the link status
        final Pointer linked = new Memory(Integer.BYTES);
        this.libGLESv2.glGetProgramiv(shaderProgram,
                                      GL_LINK_STATUS,
                                      linked);
        if (linked.getInt(0) == 0) {
            final Pointer infoLen = new Memory(Integer.BYTES);
            this.libGLESv2.glGetProgramiv(shaderProgram,
                                          GL_INFO_LOG_LENGTH,
                                          infoLen);
            int logSize = infoLen.getInt(0);
            if (logSize <= 0) {
                //some drivers report incorrect log size
                logSize = 1024;
            }
            final Memory log = new Memory(logSize);
            this.libGLESv2.glGetProgramInfoLog(shaderProgram,
                                               logSize,
                                               null,
                                               log);
            this.libGLESv2.glDeleteProgram(shaderProgram);
            System.err.println("Error compiling the vertex shader: " + log.getString(0));
            System.exit(1);
        }

        //find shader arguments
        final Memory u_projection = new NativeString("u_projection").getPointer();
        this.projectionArg = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                                 u_projection);
        this.libGLESv2.check("glGetUniformLocation");

        final Memory a_position = new NativeString("a_position").getPointer();
        this.positionArg = this.libGLESv2.glGetAttribLocation(shaderProgram,
                                                              a_position);
        this.libGLESv2.check("glGetAttribLocation");

        final Memory a_texCoord = new NativeString("a_texCoord").getPointer();
        this.textureCoordinateArg = this.libGLESv2.glGetAttribLocation(shaderProgram,
                                                                       a_texCoord);
        this.libGLESv2.check("glGetAttribLocation");

        final Memory a_transform = new NativeString("a_transform").getPointer();
        this.transformCol0Arg = this.libGLESv2.glGetAttribLocation(shaderProgram,
                                                                   a_transform);
        this.libGLESv2.check("glGetAttribLocation");

        this.transformCol1Arg = this.transformCol0Arg + 1;
        this.transformCol2Arg = this.transformCol1Arg + 1;
        this.transformCol3Arg = this.transformCol2Arg + 1;

        final Memory u_texture = new NativeString("u_texture").getPointer();
        this.textureArg = this.libGLESv2.glGetUniformLocation(shaderProgram,
                                                              u_texture);
        this.libGLESv2.check("glGetUniformLocation");


        return shaderProgram;
    }

    private void checkShader(final int shader) {
        final Memory vstatus = new Memory(Integer.BYTES);
        this.libGLESv2.glGetShaderiv(shader,
                                     GL_COMPILE_STATUS,
                                     vstatus);
        if (vstatus.getInt(0) == 0) {
            //failure!
            //get log length
            final Memory logLength = new Memory(Integer.BYTES);
            this.libGLESv2.glGetShaderiv(shader,
                                         GL_INFO_LOG_LENGTH,
                                         logLength);
            //get log
            int logSize = logLength.getInt(0);
            if (logSize == 0) {
                //some drivers report incorrect log size
                logSize = 1024;
            }
            final Memory log = new Memory(logSize);
            this.libGLESv2.glGetShaderInfoLog(shader,
                                              logSize,
                                              null,
                                              log);
            System.err.println("Error compiling the vertex shader: " + log.getString(0));
            System.exit(1);
        }
    }


    private void begin(final int width,
                       final int height) {
        this.libGLESv2.glViewport(0,
                                  0,
                                  width,
                                  height);
        this.libGLESv2.check("glViewport");

        this.libGLESv2.glClearColor(0.0f,
                                    0.0f,
                                    0.0f,
                                    1.0f);
        this.libGLESv2.check("glClearColor");

        this.libGLESv2.glClear(LibGLESv2.GL_COLOR_BUFFER_BIT);
        this.libGLESv2.check("glClear");

    }

    private void draw(final int program,
                      final Mat4 projection,
                      final Mat4 transform,
                      final int bufferWidth,
                      final int bufferHeight,
                      final ByteBuffer buffer) {
        //define vertex data
        final float[] vertexDataValues = {
                //top left:
                //attribute vec2 a_position
                0f, 0f,

                //attribute vec2 a_texCoord
                0f, 0f,
                //attribute mat4 a_transform
                transform.getM00(), transform.getM01(), transform.getM02(), transform.getM03(),
                transform.getM10(), transform.getM11(), transform.getM12(), transform.getM13(),
                transform.getM20(), transform.getM21(), transform.getM22(), transform.getM23(),
                transform.getM30(), transform.getM31(), transform.getM32(), transform.getM33(),

                //top right:
                //attribute vec2 a_position
                bufferWidth, 0f,
                //attribute vec2 a_texCoord
                1f, 0f,
                //attribute mat4 a_transform
                transform.getM00(), transform.getM01(), transform.getM02(), transform.getM03(),
                transform.getM10(), transform.getM11(), transform.getM12(), transform.getM13(),
                transform.getM20(), transform.getM21(), transform.getM22(), transform.getM23(),
                transform.getM30(), transform.getM31(), transform.getM32(), transform.getM33(),

                //bottom right:
                //vec2 a_position
                bufferWidth, bufferHeight,
                //vec2 a_texCoord
                1f, 1f,
                //attribute mat4 a_transform
                transform.getM00(), transform.getM01(), transform.getM02(), transform.getM03(),
                transform.getM10(), transform.getM11(), transform.getM12(), transform.getM13(),
                transform.getM20(), transform.getM21(), transform.getM22(), transform.getM23(),
                transform.getM30(), transform.getM31(), transform.getM32(), transform.getM33(),

                //bottom right:
                //vec2 a_position
                bufferWidth, bufferHeight,
                //vec2 a_texCoord
                1f, 1f,
                //attribute mat4 a_transform
                transform.getM00(), transform.getM01(), transform.getM02(), transform.getM03(),
                transform.getM10(), transform.getM11(), transform.getM12(), transform.getM13(),
                transform.getM20(), transform.getM21(), transform.getM22(), transform.getM23(),
                transform.getM30(), transform.getM31(), transform.getM32(), transform.getM33(),

                //bottom left:
                //vec2 a_position
                0f, bufferHeight,
                //vec2 a_texCoord
                0f, 1f,
                //attribute mat4 a_transform
                transform.getM00(), transform.getM01(), transform.getM02(), transform.getM03(),
                transform.getM10(), transform.getM11(), transform.getM12(), transform.getM13(),
                transform.getM20(), transform.getM21(), transform.getM22(), transform.getM23(),
                transform.getM30(), transform.getM31(), transform.getM32(), transform.getM33(),

                //top left:
                //attribute vec2 a_position
                0f, 0f,

                //attribute vec2 a_texCoord
                0f, 0f,
                //attribute mat4 a_transform
                transform.getM00(), transform.getM01(), transform.getM02(), transform.getM03(),
                transform.getM10(), transform.getM11(), transform.getM12(), transform.getM13(),
                transform.getM20(), transform.getM21(), transform.getM22(), transform.getM23(),
                transform.getM30(), transform.getM31(), transform.getM32(), transform.getM33(),
        };
        final Memory vertexData = new Memory(Float.BYTES * vertexDataValues.length);
        vertexData.write(0,
                         vertexDataValues,
                         0,
                         vertexDataValues.length);

        //activate shader
        this.libGLESv2.glUseProgram(program);
        this.libGLESv2.check("glUseProgram");

        //upload uniform data
        final Pointer projectionBuffer = new Memory(Float.BYTES * 16);
        projectionBuffer.write(0,
                               projection.toArray(),
                               0,
                               16);
        this.libGLESv2.glUniformMatrix4fv(this.projectionArg,
                                          1,
                                          false,
                                          projectionBuffer);
        this.libGLESv2.check("glUniformMatrix4fv");

        //set vertex data in shader
        this.libGLESv2.glEnableVertexAttribArray(this.positionArg);
        this.libGLESv2.glVertexAttribPointer(this.positionArg,
                                             2,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             vertexData);
        this.libGLESv2.check("glVertexAttribPointer");

        this.libGLESv2.glEnableVertexAttribArray(this.textureCoordinateArg);
        this.libGLESv2.glVertexAttribPointer(this.textureCoordinateArg,
                                             2,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             vertexData.share(2 * Float.BYTES));
        this.libGLESv2.check("glVertexAttribPointer");

        this.libGLESv2.glEnableVertexAttribArray(this.transformCol0Arg);
        this.libGLESv2.glVertexAttribPointer(this.transformCol0Arg,
                                             4,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             vertexData.share(4 * Float.BYTES));
        this.libGLESv2.check("glVertexAttribPointer");

        this.libGLESv2.glEnableVertexAttribArray(this.transformCol1Arg);
        this.libGLESv2.glVertexAttribPointer(this.transformCol1Arg,
                                             4,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             vertexData.share(8 * Float.BYTES));
        this.libGLESv2.check("glVertexAttribPointer");

        this.libGLESv2.glEnableVertexAttribArray(this.transformCol2Arg);
        this.libGLESv2.glVertexAttribPointer(this.transformCol2Arg,
                                             4,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             vertexData.share(12 * Float.BYTES));
        this.libGLESv2.check("glVertexAttribPointer");

        this.libGLESv2.glEnableVertexAttribArray(this.transformCol3Arg);
        this.libGLESv2.glVertexAttribPointer(this.transformCol3Arg,
                                             4,
                                             GL_FLOAT,
                                             false,
                                             20 * Float.BYTES,
                                             vertexData.share(16 * Float.BYTES));
        this.libGLESv2.check("glVertexAttribPointer");

        //check for required texture extensions
        final String extensions = this.libGLESv2.glGetString(LibGLESv2.GL_EXTENSIONS)
                                                .getString(0);
        this.libGLESv2.check("glGetString");

        LOGGER.info("GLESv2 extensions: " + extensions);
        if (!extensions.contains("GL_EXT_texture_format_BGRA8888")) {
            throw new Error("Required extension GL_EXT_texture_format_BGRA8888 not available");
        }

        final Memory textureIdValue = new Memory(Integer.BYTES);
        this.libGLESv2.glGenTextures(1,
                                     textureIdValue);
        this.libGLESv2.check("glGenTextures");

        final int textureId = textureIdValue.getInt(0);

        //configure texture blending
        this.libGLESv2.glBlendFunc(GL_ONE,
                                   GL_ONE_MINUS_SRC_ALPHA);
        this.libGLESv2.check("glBlendFunc");


        //upload buffer to gpu
        this.libGLESv2.glBindTexture(GL_TEXTURE_2D,
                                     textureId);
        this.libGLESv2.glTexParameteri(GL_TEXTURE_2D,
                                       GL_TEXTURE_MIN_FILTER,
                                       GL_NEAREST);
        this.libGLESv2.glTexParameteri(GL_TEXTURE_2D,
                                       GL_TEXTURE_MAG_FILTER,
                                       GL_NEAREST);
        this.libGLESv2.glTexImage2D(GL_TEXTURE_2D,
                                    0,
                                    GL_RGBA /*glesv2 doesnt care what internal format we give it, it must however match the external format*/,
                                    bufferWidth,
                                    bufferHeight,
                                    0,
                                    GL_RGBA,
                                    GL_UNSIGNED_BYTE,
                                    Native.getDirectBufferPointer(buffer));
        this.libGLESv2.check("glTexImage2D");

        //set the buffer in the shader
        this.libGLESv2.glActiveTexture(GL_TEXTURE0);
        this.libGLESv2.glUniform1i(this.textureArg,
                                   0);

        //draw
        this.libGLESv2.glEnable(GL_BLEND);
        this.libGLESv2.glDrawArrays(GL_TRIANGLES,
                                    0,
                                    6);

        //cleanup
        this.libGLESv2.glUseProgram(0);
        this.libGLESv2.glDisableVertexAttribArray(this.positionArg);
        this.libGLESv2.glDisableVertexAttribArray(this.textureArg);
        this.libGLESv2.glDisableVertexAttribArray(this.transformCol0Arg);
        this.libGLESv2.glDisableVertexAttribArray(this.transformCol1Arg);
        this.libGLESv2.glDisableVertexAttribArray(this.transformCol2Arg);
        this.libGLESv2.glDisableVertexAttribArray(this.transformCol3Arg);

    }

    private void end(final Pointer display,
                     final Pointer surface) {
        this.libEGL.eglSwapBuffers(display,
                                   surface);
    }
}