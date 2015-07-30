package org.westmalle.wayland.nativ.libxkbcommon;


import com.sun.jna.Pointer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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


    /**
     * Create a new context.
     *
     * @param flags Optional flags for the context, or 0.
     *
     * @return A new context, or null on failure.
     */
    @Nullable
    public native Pointer xkb_context_new(int flags);

    /**
     * Create a keymap from RMLVO names.
     * <p/>
     * The primary keymap entry point: creates a new XKB keymap from a set of RMLVO (Rules + Model + Layouts + Variants + Options) names.
     *
     * @param context The context in which to create the keymap.
     * @param names   The RMLVO names to use.
     * @param flags   Optional flags for the keymap, or 0.
     *
     * @return A keymap compiled according to the RMLVO names, or null if the compilation failed.
     */
    @Nullable
    public native Pointer xkb_keymap_new_from_names(@Nonnull Pointer context,
                                                    Pointer names,
                                                    int flags);

    /**
     * Create a new keyboard state object.
     *
     * @param keymap The keymap which the state will use.
     *
     * @return A new keyboard state object, or null on failure.
     */
    @Nullable
    public native Pointer xkb_state_new(@Nonnull Pointer keymap);

    /**
     * Update the keyboard state to reflect a given key being pressed or released.
     * <p/>
     * This entry point is intended for programs which track the keyboard state explictly (like an evdev client).
     * If the state is serialized to you by a master process (like a Wayland compositor) using functions like
     * xkb_state_serialize_mods(), you should use xkb_state_update_mask() instead. The two functins should not
     * generally be used together.
     * <p/>
     * A series of calls to this function should be consistent; that is, a call with XKB_KEY_DOWN for a key should be
     * matched by an XKB_KEY_UP; if a key is pressed twice, it should be released twice; etc. Otherwise (e.g. due to
     * missed input events), situations like "stuck modifiers" may occur.
     * <p/>
     * This function is often used in conjunction with the function xkb_state_key_get_syms() (or
     * xkb_state_key_get_one_sym()), for example, when handling a key event. In this case, you should prefer to get
     * the keysyms before updating the key, such that the keysyms reported for the key event are not affected by the
     * event itself. This is the conventional behavior.
     *
     * @param state
     * @param key
     * @param direction
     *
     * @return A mask of state components that have changed as a result of the update. If nothing in the state has
     * changed, returns 0.
     */
    public native int xkb_state_update_key(@Nonnull Pointer state,
                                           int key,
                                           int direction);

    /**
     * Determine whether a key should repeat or not.
     * <p/>
     * A keymap may specify different repeat behaviors for different keys. Most keys should generally exhibit repeat
     * behavior; for example, holding the 'a' key down in a text editor should normally insert a single 'a' character
     * every few milliseconds, until the key is released. However, there are keys which should not or do not need to be
     * repeated. For example, repeating modifier keys such as Left/Right Shift or Caps Lock is not generally useful or
     * desired.
     *
     * @param keymap
     * @param key
     *
     * @return 1 if the key should repeat, 0 otherwise.
     */
    public native int xkb_keymap_key_repeats(@Nonnull Pointer keymap,
                                             int key);

    /**
     * Update a keyboard state from a set of explicit masks.
     * <p/>
     * This entry point is intended for window systems and the like, where a master process holds an xkb_state, then
     * serializes it over a wire protocol, and clients then use the serialization to feed in to their own xkb_state.
     * <p/>
     * All parameters must always be passed, or the resulting state may be incoherent.
     * <p/>
     * The serialization is lossy and will not survive round trips; it must only be used to feed slave state objects,
     * and must not be used to update the master state.
     * <p/>
     * If you do not fit the description above, you should use {@link Libxkbcommon#xkb_state_update_key} instead. The
     * two functions should not generally be used together.
     *
     * @param state
     * @param depressed_mods
     * @param latched_mods
     * @param locked_mods
     * @param depressed_layout
     * @param latched_layout
     * @param locked_layout
     *
     * @return A mask of state components that have changed as a result of the update. If nothing in the state has
     * changed, returns 0.
     */
    public native int xkb_state_update_mask(@Nonnull Pointer state,
                                            int depressed_mods,
                                            int latched_mods,
                                            int locked_mods,
                                            int depressed_layout,
                                            int latched_layout,
                                            int locked_layout);

    /**
     * Release a reference on a keyboard state object, and possibly free it.
     *
     * @param state The state. If it is null, this function does nothing.
     */
    public native void xkb_state_unref(@Nullable Pointer state);

    /**
     * Release a reference on a keymap, and possibly free it.
     *
     * @param keymap The keymap. If it is null, this function does nothing.
     */
    public native void xkb_keymap_unref(@Nullable Pointer keymap);

    /**
     * Release a reference on a context, and possibly free it.
     *
     * @param context The context. If it is null, this function does nothing.
     */
    public native void xkb_context_unref(@Nullable Pointer context);
}