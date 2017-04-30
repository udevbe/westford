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
package org.westford.nativ.libxkbcommon

import org.freedesktop.jaccall.Lib
import org.freedesktop.jaccall.Ptr

import javax.inject.Singleton

@Singleton @Lib(value = "xkbcommon",
                version = 0) class Libxkbcommon {

    /**
     * Create a new context.

     * @param flags Optional flags for the context, or 0.
     * *
     * *
     * @return A new context, or null on failure.
     */
    @Ptr external fun xkb_context_new(flags: Int): Long

    /**
     * Create a keymap from RMLVO names.
     *
     *
     * The primary keymap entry point: creates a new XKB keymap from a set of RMLVO (Rules + Model + Layouts + Variants + Options) names.

     * @param context The context in which to create the keymap.
     * *
     * @param names   The RMLVO names to use.
     * *
     * @param flags   Optional flags for the keymap, or 0.
     * *
     * *
     * @return A keymap compiled according to the RMLVO names, or null if the compilation failed.
     */
    @Ptr external fun xkb_keymap_new_from_names(@Ptr context: Long,
                                                @Ptr names: Long,
                                                flags: Int): Long

    /**
     * Create a new keyboard state object.

     * @param keymap The keymap which the state will use.
     * *
     * *
     * @return A new keyboard state object, or null on failure.
     */
    @Ptr external fun xkb_state_new(@Ptr keymap: Long): Long

    /**
     * Update the keyboard state to reflect a given key being pressed or released.
     *
     *
     * This entry point is intended for programs which track the keyboard state explictly (like an evdev client).
     * If the state is serialized to you by a master process (like a Wayland compositor) using functions like
     * xkb_state_serialize_mods(), you should use xkb_state_update_mask() instead. The two functins should not
     * generally be used together.
     *
     *
     * A series of calls to this function should be consistent; that is, a call with XKB_KEY_DOWN for a key should be
     * matched by an XKB_KEY_UP; if a key is pressed twice, it should be released twice; etc. Otherwise (e.g. due to
     * missed input events), situations like "stuck modifiers" may occur.
     *
     *
     * This function is often used in conjunction with the function xkb_state_key_get_syms() (or
     * xkb_state_key_get_one_sym()), for example, when handling a key event. In this case, you should prefer to get
     * the keysyms before updating the key, such that the keysyms reported for the key event are not affected by the
     * event itself. This is the conventional behavior.

     * @param state
     * *
     * @param key
     * *
     * @param direction
     * *
     * *
     * @return A mask of state components that have changed as a result of the update. If nothing in the state has
     * * changed, returns 0.
     */
    external fun xkb_state_update_key(@Ptr state: Long,
                                      key: Int,
                                      direction: Int): Int

    /**
     * The counterpart to xkb_state_update_mask for layouts, to be used on the server side of serialization.

     * @param state      The keyboard state.
     * *
     * @param components A mask of the layout state components to serialize. State components other than
     * *                   XKB_STATE_LAYOUT_* are ignored. If XKB_STATE_LAYOUT_EFFECTIVE is included, all other state
     * *                   components are ignored.
     * *
     * *
     * @return A layout index representing the given components of the layout state.
     */
    external fun xkb_state_serialize_layout(@Ptr state: Long,
                                            components: Int): Int

    /**
     * The counterpart to xkb_state_update_mask for modifiers, to be used on the server side of serialization.

     * @param state      The keyboard state.
     * *
     * @param components A mask of the modifier state components to serialize. State components other than
     * *                   XKB_STATE_MODS_* are ignored. If XKB_STATE_MODS_EFFECTIVE is included, all other state
     * *                   components are ignored.
     * *
     * *
     * @return A xkb_mod_mask_t representing the given components of the modifier state.
     */
    external fun xkb_state_serialize_mods(@Ptr state: Long,
                                          components: Int): Int

    /**
     * Release a reference on a keyboard state object, and possibly free it.

     * @param state The state. If it is null, this function does nothing.
     */
    external fun xkb_state_unref(@Ptr state: Long)

    /**
     * Release a reference on a keymap, and possibly free it.

     * @param keymap The keymap. If it is null, this function does nothing.
     */
    external fun xkb_keymap_unref(@Ptr keymap: Long)

    /**
     * Release a reference on a context, and possibly free it.

     * @param context The context. If it is null, this function does nothing.
     */
    external fun xkb_context_unref(@Ptr context: Long)

    /**
     * Get the compiled keymap as a string.

     * @param keymap The keymap to get as a string.
     * *
     * @param format The keymap format to use for the string. You can pass in the special value
     * *               XKB_KEYMAP_USE_ORIGINAL_FORMAT to use the format from which the keymap was originally created.
     * *
     * *
     * @return The keymap as a NUL-terminated string, or NULL if unsuccessful. The returned string is dynamically
     * * allocated and should be freed by the caller.
     */
    @Ptr external fun xkb_keymap_get_as_string(@Ptr keymap: Long,
                                               format: Int): Long

    companion object {

        /**
         * Do not apply any context flags.
         */
        val XKB_CONTEXT_NO_FLAGS = 0
        /**
         * Create this context with an empty include path.
         */
        val XKB_CONTEXT_NO_DEFAULT_INCLUDES = 1 shl 0
        /**
         * Don't take RMLVO names from the environment.

         * @since 0.3.0
         */
        val XKB_CONTEXT_NO_ENVIRONMENT_NAMES = 1 shl 1

        /**
         * Do not apply any flags.
         */
        val XKB_KEYMAP_COMPILE_NO_FLAGS = 0

        /**
         * The key was released.
         */
        val XKB_KEY_UP = 0
        val XKB_KEY_DOWN = 1

        /**
         * Depressed modifiers, i.e. a key is physically holding them.
         */
        val XKB_STATE_MODS_DEPRESSED = 1 shl 0
        /**
         * Latched modifiers, i.e. will be unset after the next non-modifier key press.
         */
        val XKB_STATE_MODS_LATCHED = 1 shl 1
        /**
         * Locked modifiers, i.e. will be unset after the key provoking the lock has been pressed again.
         */
        val XKB_STATE_MODS_LOCKED = 1 shl 2
        /**
         * Effective modifiers, i.e. currently active and affect key processing (derived from the other state components).
         * Use this unless you explictly care how the state came about.
         */
        val XKB_STATE_MODS_EFFECTIVE = 1 shl 3
        /**
         * Depressed layout, i.e. a key is physically holding it.
         */
        val XKB_STATE_LAYOUT_DEPRESSED = 1 shl 4
        /**
         * Latched layout, i.e. will be unset after the next non-modifier key press.
         */
        val XKB_STATE_LAYOUT_LATCHED = 1 shl 5
        /**
         * Locked layout, i.e. will be unset after the key provoking the lock has been pressed again.
         */
        val XKB_STATE_LAYOUT_LOCKED = 1 shl 6
        /**
         * Effective layout, i.e. currently active and affects key processing (derived from the other state components). Use
         * this unless you explictly care how the state came about.
         */
        val XKB_STATE_LAYOUT_EFFECTIVE = 1 shl 7
        /**
         * LEDs (derived from the other state components).
         */
        val XKB_STATE_LEDS = 1 shl 8

        /**
         * The current/classic XKB text format, as generated by xkbcomp -xkb.
         */
        val XKB_KEYMAP_FORMAT_TEXT_V1 = 1
    }
}