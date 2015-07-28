package org.westmalle.wayland.nativ.libxkbcommon;


import com.sun.jna.Pointer;

public class Libxkbcommon {

    /**
     * Do not apply any context flags.
     */
    public static final int XKB_CONTEXT_NO_FLAGS = 0;
    /**
     * Create this context with an empty include path.
     */
    public static final int XKB_CONTEXT_NO_DEFAULT_INCLUDES = (1 << 0);
    /**
     * Don't take RMLVO names from the environment.
     *
     * @since 0.3.0
     */
    public static final int XKB_CONTEXT_NO_ENVIRONMENT_NAMES = (1 << 1);

    /** Do not apply any flags. */
    public static final int XKB_KEYMAP_COMPILE_NO_FLAGS = 0;

    public native Pointer xkb_context_new(int flags);

    public native Pointer xkb_keymap_new_from_names(Pointer context,
                                                    Pointer names,
                                                    int flags);

}
