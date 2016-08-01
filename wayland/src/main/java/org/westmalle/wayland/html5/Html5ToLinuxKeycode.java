//Copyright 2016 Erik De Rijcke
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
package org.westmalle.wayland.html5;


import org.westmalle.wayland.nativ.linux.InputEventCodes;

import java.util.HashMap;
import java.util.Map;

public class Html5ToLinuxKeycode {

    //TODO all keys are js key codes, add them to their own class -> https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/keyCode
    private static final Map<Integer, Integer> JS_KEY_CODE_TO_LINUX = new HashMap<Integer, Integer>(250) {{
        //backspace
        put(8,
            InputEventCodes.KEY_BACKSPACE);
        //space
        put(32,
            InputEventCodes.KEY_SPACE);
        //tab
        put(9,
            InputEventCodes.KEY_TAB);
        //enter
        put(13,
            InputEventCodes.KEY_ENTER);
        //shift (left)
        put(16,
            InputEventCodes.KEY_LEFTSHIFT);
        //ctrl (left)
        put(17,
            InputEventCodes.KEY_LEFTCTRL);
        //alt (left)
        put(18,
            InputEventCodes.KEY_LEFTALT);

        //pause/break
        put(19,
            InputEventCodes.KEY_PAUSE);
        //caps lock
        put(20,
            InputEventCodes.KEY_CAPSLOCK);
        //escape
        put(27,
            InputEventCodes.KEY_ESC);

        //page up
        put(33,
            InputEventCodes.KEY_PAGEUP);
        //page down
        put(34,
            InputEventCodes.KEY_PAGEDOWN);
        //end
        put(35,
            InputEventCodes.KEY_END);
        //home
        put(36,
            InputEventCodes.KEY_HOME);
        //left arrow
        put(37,
            InputEventCodes.KEY_LEFT);
        //up arrow
        put(38,
            InputEventCodes.KEY_UP);
        //right arrow
        put(39,
            InputEventCodes.KEY_RIGHT);
        //down arrow
        put(40,
            InputEventCodes.KEY_DOWN);
        //insert
        put(45,
            InputEventCodes.KEY_INSERT);
        //delete
        put(46,
            InputEventCodes.KEY_DELETE);

        //0
        put(48,
            InputEventCodes.KEY_0);
        //1
        put(49,
            InputEventCodes.KEY_1);
        //2
        put(50,
            InputEventCodes.KEY_2);
        //3
        put(51,
            InputEventCodes.KEY_3);
        //4
        put(52,
            InputEventCodes.KEY_4);
        //5
        put(53,
            InputEventCodes.KEY_5);
        //6
        put(54,
            InputEventCodes.KEY_6);
        //7
        put(55,
            InputEventCodes.KEY_7);
        //8
        put(56,
            InputEventCodes.KEY_8);
        //9
        put(57,
            InputEventCodes.KEY_9);
        //a
        put(65,
            InputEventCodes.KEY_A);
        //b
        put(66,
            InputEventCodes.KEY_B);
        //c
        put(67,
            InputEventCodes.KEY_C);
        //d
        put(68,
            InputEventCodes.KEY_D);
        //e
        put(69,
            InputEventCodes.KEY_E);
        //f
        put(70,
            InputEventCodes.KEY_F);
        //g
        put(71,
            InputEventCodes.KEY_G);
        //h
        put(72,
            InputEventCodes.KEY_H);
        //i
        put(73,
            InputEventCodes.KEY_I);
        //j
        put(74,
            InputEventCodes.KEY_J);
        //k
        put(75,
            InputEventCodes.KEY_K);
        //l
        put(76,
            InputEventCodes.KEY_L);
        //m
        put(77,
            InputEventCodes.KEY_M);
        //n
        put(78,
            InputEventCodes.KEY_N);
        //o
        put(79,
            InputEventCodes.KEY_O);
        //p
        put(80,
            InputEventCodes.KEY_P);
        //q
        put(81,
            InputEventCodes.KEY_Q);
        //r
        put(82,
            InputEventCodes.KEY_R);
        //s
        put(83,
            InputEventCodes.KEY_S);
        //t
        put(84,
            InputEventCodes.KEY_T);
        //u
        put(85,
            InputEventCodes.KEY_U);
        //v
        put(86,
            InputEventCodes.KEY_V);
        //w
        put(87,
            InputEventCodes.KEY_W);
        //x
        put(88,
            InputEventCodes.KEY_X);
        //y
        put(89,
            InputEventCodes.KEY_Y);
        //z
        put(90,
            InputEventCodes.KEY_Z);

        //left window key
        put(91,
            InputEventCodes.KEY_LEFTMETA);
        //right window key
        put(92,
            InputEventCodes.KEY_RIGHTMETA);
        //context menu
        put(93,
            InputEventCodes.KEY_MENU);

        //numpad 0
        put(96,
            InputEventCodes.KEY_KP0);
        //numpad 1
        put(97,
            InputEventCodes.KEY_KP1);
        //numpad 2
        put(98,
            InputEventCodes.KEY_KP2);
        //numpad 3
        put(99,
            InputEventCodes.KEY_KP3);
        //numpad 4
        put(100,
            InputEventCodes.KEY_KP4);
        //numpad 5
        put(101,
            InputEventCodes.KEY_KP5);
        //numpad 6
        put(102,
            InputEventCodes.KEY_KP6);
        //numpad 7
        put(103,
            InputEventCodes.KEY_KP7);
        //numpad 8
        put(104,
            InputEventCodes.KEY_KP8);
        //numpad 9
        put(105,
            InputEventCodes.KEY_KP9);
        //multiply
        put(106,
            InputEventCodes.KEY_KPASTERISK);
        //add
        put(107,
            InputEventCodes.KEY_KPPLUS);
        //subtract
        put(109,
            InputEventCodes.KEY_KPMINUS);
        //decimal point
        put(110,
            InputEventCodes.KEY_KPDOT);
        //divide
        put(111,
            InputEventCodes.KEY_KPSLASH);

        //f1
        put(112,
            InputEventCodes.KEY_F1);
        //f2
        put(113,
            InputEventCodes.KEY_F2);
        //f3
        put(114,
            InputEventCodes.KEY_F3);
        //f4
        put(115,
            InputEventCodes.KEY_F4);
        //f5
        put(116,
            InputEventCodes.KEY_F5);
        //f6
        put(117,
            InputEventCodes.KEY_F6);
        //f7
        put(118,
            InputEventCodes.KEY_F7);
        //f8
        put(119,
            InputEventCodes.KEY_F8);
        //f9
        put(120,
            InputEventCodes.KEY_F9);
        //f10
        put(121,
            InputEventCodes.KEY_F10);
        //f11
        put(122,
            InputEventCodes.KEY_F11);
        //f12
        put(123,
            InputEventCodes.KEY_F12);
        //num lock
        put(144,
            InputEventCodes.KEY_NUMLOCK);
        //scroll lock
        put(145,
            InputEventCodes.KEY_SCROLLLOCK);

        //semi-colon
        put(186,
            InputEventCodes.KEY_SEMICOLON);
        //semi-colon (gecko)
        put(59,
            InputEventCodes.KEY_SEMICOLON);

        //equal sign
        put(187,
            InputEventCodes.KEY_EQUAL);
        //equal sign (gecko)
        put(61,
            InputEventCodes.KEY_EQUAL);

        //comma
        put(188,
            InputEventCodes.KEY_COMMA);

        //dash
        put(189,
            InputEventCodes.KEY_MINUS);
        //dash (gecko)
        put(173,
            InputEventCodes.KEY_MINUS);

        //period
        put(190,
            InputEventCodes.KEY_DOT);
        //forward slash
        put(191,
            InputEventCodes.KEY_SLASH);
        //backtick
        put(192,
            InputEventCodes.KEY_GRAVE);
        //open bracket
        put(219,
            InputEventCodes.KEY_LEFTBRACE);
        //back slash
        put(220,
            InputEventCodes.KEY_BACKSLASH);
        //close braket
        put(221,
            InputEventCodes.KEY_RIGHTBRACE);
        //single quote
        put(222,
            InputEventCodes.KEY_APOSTROPHE);
    }};

    public static int toLinuxInputEvent(final int javaScriptKeycode) {
        final Integer eventCode = JS_KEY_CODE_TO_LINUX.get(javaScriptKeycode);
        if (eventCode == null) {
            return 0;
        }
        return eventCode;
    }
}
