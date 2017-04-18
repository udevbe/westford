/*
 * Westford Wayland Compositor.
 * Copyright (C) 2016  Erik De Rijcke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.westford.nativ.libgbm;

import org.freedesktop.jaccall.Lib;
import org.freedesktop.jaccall.Ptr;
import org.freedesktop.jaccall.Unsigned;

@Lib(value = "gbm",
     version = 1)
public class Libgbm {

    public static final int GBM_BO_IMPORT_WL_BUFFER = 0x5501;
    public static final int GBM_BO_IMPORT_EGL_IMAGE = 0x5502;

    /**
     * RGB with 8 bits per channel in a 32 bit value
     */
    public static final int GBM_BO_FORMAT_XRGB8888 = 0;
    /**
     * ARGB with 8 bits per channel in a 32 bit value
     */
    public static final int GBM_BO_FORMAT_ARGB8888 = 1;

    public static final int GBM_FORMAT_ARGB8888     = __gbm_fourcc_code((byte) 'A',
                                                                        (byte) 'R',
                                                                        (byte) '2',
                                                                        (byte) '4'); /* [31:0] A:R:G:B 8:8:8:8 little endian */
    public static final int GBM_FORMAT_XRGB8888     = __gbm_fourcc_code((byte) 'X',
                                                                        (byte) 'R',
                                                                        (byte) '2',
                                                                        (byte) '4'); /* [31:0] x:R:G:B 8:8:8:8 little endian */
    /**
     * Buffer is going to be presented to the screen using an API such as KMS
     */
    public static final int GBM_BO_USE_SCANOUT      = (1 << 0);
    /**
     * Buffer is going to be used as cursor
     */
    public static final int GBM_BO_USE_CURSOR       = (1 << 1);
    /**
     * Deprecated
     */
    public static final int GBM_BO_USE_CURSOR_64X64 = GBM_BO_USE_CURSOR;
    /**
     * Buffer is to be used for rendering - for example it is going to be used
     * as the storage for a color buffer
     */
    public static final int GBM_BO_USE_RENDERING    = (1 << 2);
    /**
     * Buffer can be used for gbm_bo_write.  This is guaranteed to work
     * with GBM_BO_USE_CURSOR. but may not work for other combinations.
     */
    public static final int GBM_BO_USE_WRITE        = (1 << 3);
    /**
     * Buffer is linear, i.e. not tiled.
     */
    public static final int GBM_BO_USE_LINEAR       = (1 << 4);

    private static int __gbm_fourcc_code(final byte a,
                                         final byte b,
                                         final byte c,
                                         final byte d) {
        return ((int) (a) | ((int) (b) << 8) | ((int) (c) << 16) | ((int) (d) << 24));
    }

    /**
     * Create a gbm device for allocating buffers
     * <p>
     * The file descriptor passed in is used by the backend to communicate with
     * platform for allocating the memory. For allocations using DRI this would be
     * the file descriptor returned when opening a device such as {@code /dev/dri/card0}
     * <p>
     *
     * @param fd The file descriptor for an backend specific device
     *
     * @return The newly created struct gbm_device. The resources associated with
     * the device should be freed with gbm_device_destroy() when it is no longer
     * needed. If the creation of the device failed NULL will be returned.
     */
    @Ptr
    public native long gbm_create_device(int fd);

    /**
     * Allocate a surface object
     *
     * @param gbm    The gbm device returned from gbm_create_device()
     * @param width  The width for the surface
     * @param height The height for the surface
     * @param format The format to use for the surface
     *
     * @return A newly allocated surface that should be freed with
     * gbm_surface_destroy() when no longer needed. If an error occurs
     * during allocation %NULL will be returned.
     *
     * @see gbm_bo_format constants for the list of formats
     */
    @Ptr
    public native long gbm_surface_create(@Ptr long gbm,
                                          @Unsigned int width,
                                          @Unsigned int height,
                                          @Unsigned int format,
                                          @Unsigned int flags);

    /**
     * Get the user data associated with a buffer object
     *
     * @param bo The buffer object
     *
     * @return Returns the user data associated with the buffer object or %NULL
     * if no data was associated with it
     *
     * @see #gbm_bo_get_user_data(long)
     */
    @Ptr
    public native long gbm_bo_get_user_data(@Ptr long bo);

    /**
     * Set the user data associated with a buffer object
     *
     * @param bo                The buffer object
     * @param data              The data to associate to the buffer object
     * @param destroy_user_data A callback (which may be null) that will be
     *                          called prior to the buffer destruction
     */
    public native void gbm_bo_set_user_data(@Ptr long bo,
                                            @Ptr long data,
                                            @Ptr(destroy_user_data.class) long destroy_user_data);

    /**
     * Get the gbm device used to create the buffer object
     *
     * @param bo The buffer object
     *
     * @return Returns the gbm device with which the buffer object was created
     */
    @Ptr
    public native long gbm_bo_get_device(@Ptr long bo);

    /**
     * Get the width of the buffer object
     *
     * @param bo The buffer object
     *
     * @return The width of the allocated buffer object
     */
    @Unsigned
    public native int gbm_bo_get_width(@Ptr long bo);

    /**
     * Get the height of the buffer object
     *
     * @param bo The buffer object
     *
     * @return The height of the allocated buffer object
     */
    @Unsigned
    public native int gbm_bo_get_height(@Ptr long bo);

    /**
     * Get the stride of the buffer object
     * <p>
     * This is calculated by the backend when it does the allocation in
     * gbm_bo_create()
     *
     * @param bo The buffer object
     *
     * @return The stride of the allocated buffer object in bytes
     */
    @Unsigned
    public native int gbm_bo_get_stride(@Ptr long bo);

    /**
     * Get the format of the buffer object
     * <p>
     * The format of the pixels in the buffer.
     *
     * @param bo The buffer object
     *
     * @return The format of buffer object, on of the GBM_FORMAT_* codes
     */
    @Unsigned
    public native int gbm_bo_get_format(@Ptr long bo);

    /**
     * Get the handle of the buffer object
     * <p>
     * This is stored in the platform generic union gbm_bo_handle type. However
     * the format of this handle is platform specific.
     *
     * @param bo The buffer object
     *
     * @return Returns the handle of the allocated buffer object
     */
    public native long gbm_bo_get_handle(@Ptr long bo);

    /**
     * Lock the surface's current front buffer
     * <p>
     * Lock rendering to the surface's current front buffer until it is
     * released with gbm_surface_release_buffer().
     * <p>
     * This function must be called exactly once after calling
     * eglSwapBuffers.  Calling it before any eglSwapBuffer has happened
     * on the surface or two or more times after eglSwapBuffers is an
     * error.  A new bo representing the new front buffer is returned.  On
     * multiple invocations, all the returned bos must be released in
     * order to release the actual surface buffer.
     *
     * @param surface The surface
     *
     * @return A buffer object that should be released with
     * {@link #gbm_surface_release_buffer(long, long)} when no longer needed.  The implementation
     * is free to reuse buffers released with gbm_surface_release_buffer() so
     * this bo should not be destroyed using gbm_bo_destroy().  If an error
     * occurs this function returns {@code 0L}.
     */
    @Ptr
    public native long gbm_surface_lock_front_buffer(@Ptr long surface);

    public native void gbm_surface_release_buffer(@Ptr long surface,
                                                  @Ptr long bo);

    /**
     * Create a gbm buffer object from an foreign object
     * <p>
     * This function imports a foreign object and creates a new gbm bo for it.
     * This enabled using the foreign object with a display API such as KMS.
     * Currently two types of foreign objects are supported, indicated by the type
     * argument:
     * <p>
     * GBM_BO_IMPORT_WL_BUFFER
     * GBM_BO_IMPORT_EGL_IMAGE
     * <p>
     * The the gbm bo shares the underlying pixels but its life-time is
     * independent of the foreign object.
     *
     * @param gbm    The gbm device returned from gbm_create_device()
     * @param type   The type of object we're importing
     * @param buffer Pointer to the external object
     * @param usage  The union of the usage flags for this buffer
     *
     * @return A newly allocated buffer object that should be freed with
     * {@link #gbm_bo_destroy(long)} when no longer needed.
     * <p>
     *
     * @see enum gbm_bo_flags for the list of usage flags
     */
    @Ptr
    public native long gbm_bo_import(@Ptr long gbm,
                                     @Unsigned int type,
                                     @Ptr long buffer,
                                     @Unsigned int usage);

    /**
     * Destroys the given buffer object and frees all resources associated with
     * it.
     *
     * @param bo The buffer object
     */
    public native void gbm_bo_destroy(@Ptr long gbm);
}
