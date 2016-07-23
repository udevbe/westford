package org.westmalle.wayland.html5;


import java.util.HashMap;
import java.util.Map;

public class Html5ToLinuxKeycode {

    private static final Map<Integer, Integer> JS_KEY_CODE_TO_LINUX = new HashMap<Integer, Integer>(250) {{
        //backspace
        put(8,
            14);
        //space
        put(32,57);
        //tab
        put(9,
            15);
        //enter
        put(13,
            28);
        //shift (left)
        put(16,
            42);
        //ctrl (left)
        put(17,
            29);
        //alt (left)
        put(18,
            56);
        //pause/break
        put(19,
            119);
        //caps lock
        put(20,
            58);
        //escape
        put(27,
            1);
        //page up
        put(33,
            104);
        //page down
        put(34,
            109);
        //end
        put(35,
            107);
        //home
        put(36,
            102);
        //left arrow
        put(37,
            105);
        //up arrow
        put(38,
            103);
        //right arrow
        put(39,
            106);
        //down arrow
        put(40,
            108);
        //insert
        put(45,
            110);
        //delete
        put(46,
            111);
        //0
        put(48,
            11);
        //1
        put(49,
            2);
        //2
        put(50,
            3);
        //3
        put(51,
            4);
        //4
        put(52,
            5);
        //5
        put(53,
            6);
        //6
        put(54,
            7);
        //7
        put(55,
            8);
        //8
        put(56,
            9);
        //9
        put(57,
            10);
        //a
        put(65,
            30);
        //b
        put(66,
            48);
        //c
        put(67,
            46);
        //d
        put(68,
            32);
        //e
        put(69,
            18);
        //f
        put(70,
            33);
        //g
        put(71,
            34);
        //h
        put(72,
            35);
        //i
        put(73,
            23);
        //j
        put(74,
            36);
        //k
        put(75,
            37);
        //l
        put(76,
            38);
        //m
        put(77,
            50);
        //n
        put(78,
            49);
        //o
        put(79,
            24);
        //p
        put(80,
            25);
        //q
        put(81,
            16);
        //r
        put(82,
            19);
        //s
        put(83,
            31);
        //t
        put(84,
            20);
        //u
        put(85,
            22);
        //v
        put(86,
            47);
        //w
        put(87,
            17);
        //x
        put(88,
            45);
        //y
        put(89,
            21);
        //z
        put(90,
            44);
        //left window key
        put(91,
            125);
        //right window key
        put(92,
            126);
        //select key//TODO(?)
        //93,
        //numpad 0
        put(96,
            82);
        //numpad 1
        put(97,
            79);
        //numpad 2
        put(98,
            80);
        //numpad 3
        put(99,
            81);
        //numpad 4
        put(100,
            75);
        //numpad 5
        put(101,
            76);
        //numpad 6
        put(102,
            77);
        //numpad 7
        put(103,
            71);
        //numpad 8
        put(104,
            72);
        //numpad 9
        put(105,
            73);
        //multiply
        put(106,
            55);
        //add
        put(107,
            78);
        //subtract
        put(109,
            74);
        //decimal point
        put(110,
            83);
        //divide
        put(111,
            98);
        //f1
        put(112,
            59);
        //f2
        put(113,
            60);
        //f3
        put(114,
            61);
        //f4
        put(115,
            62);
        //f5
        put(116,
            63);
        //f6
        put(117,
            64);
        //f7
        put(118,
            65);
        //f8
        put(119,
            66);
        //f9
        put(120,
            67);
        //f10
        put(121,
            68);
        //f11
        put(122,
            87);
        //f12
        put(123,
            88);
        //num lock
        put(144,
            69);
        //scroll lock
        put(145,
            70);
        //semi-colon
        put(186,
            39);
        //equal sign
        put(187,
            117);
        //comma
        put(188,
            121);
        //dash
        put(189,
            12);
        //period
        put(190,
            52);
        //forward slash
        put(191,
            53);
        //grave accent
        put(192,
            41);
        //open bracket
        put(219,
            26);
        //back slash
        put(220,
            43);
        //close braket
        put(221,
            27);
        //single quote
        put(222,
            40);
    }};

    public static int toLinuxInputEvent(int javaScriptKeycode) {
        return JS_KEY_CODE_TO_LINUX.get(javaScriptKeycode);
    }
}
