package org.westmalle.wayland.nativ.libxkbcommon;


import com.sun.jna.Pointer;

public class Libxkbcommon {

    /**
     * Do not apply any context flags.
     */
    public static final int XKB_CONTEXT_NO_FLAGS             = 0;
    /**
     * Create this context with an empty include path.
     */
    public static final int XKB_CONTEXT_NO_DEFAULT_INCLUDES  = (1 << 0);
    /**
     * Don't take RMLVO names from the environment.
     *
     * @since 0.3.0
     */
    public static final int XKB_CONTEXT_NO_ENVIRONMENT_NAMES = (1 << 1);

    /**
     * Do not apply any flags.
     */
    public static final int XKB_KEYMAP_COMPILE_NO_FLAGS = 0;

    /**
     * The key was released.
     */
    public static final int XKB_KEY_UP   = 0;
    public static final int XKB_KEY_DOWN = 1;

    /**
     * Depressed modifiers, i.e. a key is physically holding them.
     */
    public static final int XKB_STATE_MODS_DEPRESSED   = (1 << 0);
    /**
     * Latched modifiers, i.e. will be unset after the next non-modifier key press.
     */
    public static final int XKB_STATE_MODS_LATCHED     = (1 << 1);
    /**
     * Locked modifiers, i.e. will be unset after the key provoking the lock has been pressed again.
     */
    public static final int XKB_STATE_MODS_LOCKED      = (1 << 2);
    /**
     * Effective modifiers, i.e. currently active and affect key processing (derived from the other state components).
     * Use this unless you explictly care how the state came about.
     */
    public static final int XKB_STATE_MODS_EFFECTIVE   = (1 << 3);
    /**
     * Depressed layout, i.e. a key is physically holding it.
     */
    public static final int XKB_STATE_LAYOUT_DEPRESSED = (1 << 4);
    /**
     * Latched layout, i.e. will be unset after the next non-modifier key press.
     */
    public static final int XKB_STATE_LAYOUT_LATCHED   = (1 << 5);
    /**
     * Locked layout, i.e. will be unset after the key provoking the lock has been pressed again.
     */
    public static final int XKB_STATE_LAYOUT_LOCKED    = (1 << 6);
    /**
     * Effective layout, i.e. currently active and affects key processing (derived from the other state components). Use
     * this unless you explictly care how the state came about.
     */
    public static final int XKB_STATE_LAYOUT_EFFECTIVE = (1 << 7);
    /**
     * LEDs (derived from the other state components).
     */
    public static final int XKB_STATE_LEDS             = (1 << 8);


    public native Pointer xkb_context_new(int flags);

    public native Pointer xkb_keymap_new_from_names(Pointer context,
                                                    Pointer names,
                                                    int flags);

    public native Pointer xkb_state_new(Pointer keymap);

    public native int xkb_state_update_key(Pointer state,
                                           int key,
                                           int direction);

    public native int xkb_keymap_key_repeats(Pointer keymap,
                                             int key);

    public native int xkb_state_update_mask(Pointer state,
                                            int depressed_mods,
                                            int latched_mods,
                                            int locked_mods,
                                            int depressed_layout,
                                            int latched_layout,
                                            int locked_layout);

    public native void xkb_state_unref(Pointer state);

    public native void xkb_keymap_unref(Pointer keymap);

    public native void xkb_context_unref(Pointer context);
}