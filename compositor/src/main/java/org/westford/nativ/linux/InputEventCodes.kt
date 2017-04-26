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
package org.westford.nativ.linux

/**
 * input-event-codes.h
 */
object InputEventCodes {
    val BTN_LEFT = 0x110
    val BTN_RIGHT = 0x111
    val BTN_MIDDLE = 0x112
    val BTN_SIDE = 0x113

    val KEY_RESERVED = 0
    val KEY_ESC = 1
    val KEY_1 = 2
    val KEY_2 = 3
    val KEY_3 = 4
    val KEY_4 = 5
    val KEY_5 = 6
    val KEY_6 = 7
    val KEY_7 = 8
    val KEY_8 = 9
    val KEY_9 = 10
    val KEY_0 = 11
    val KEY_MINUS = 12
    val KEY_EQUAL = 13
    val KEY_BACKSPACE = 14
    val KEY_TAB = 15
    val KEY_Q = 16
    val KEY_W = 17
    val KEY_E = 18
    val KEY_R = 19
    val KEY_T = 20
    val KEY_Y = 21
    val KEY_U = 22
    val KEY_I = 23
    val KEY_O = 24
    val KEY_P = 25
    val KEY_LEFTBRACE = 26
    val KEY_RIGHTBRACE = 27
    val KEY_ENTER = 28
    val KEY_LEFTCTRL = 29
    val KEY_A = 30
    val KEY_S = 31
    val KEY_D = 32
    val KEY_F = 33
    val KEY_G = 34
    val KEY_H = 35
    val KEY_J = 36
    val KEY_K = 37
    val KEY_L = 38
    val KEY_SEMICOLON = 39
    val KEY_APOSTROPHE = 40
    val KEY_GRAVE = 41
    val KEY_LEFTSHIFT = 42
    val KEY_BACKSLASH = 43
    val KEY_Z = 44
    val KEY_X = 45
    val KEY_C = 46
    val KEY_V = 47
    val KEY_B = 48
    val KEY_N = 49
    val KEY_M = 50
    val KEY_COMMA = 51
    val KEY_DOT = 52
    val KEY_SLASH = 53
    val KEY_RIGHTSHIFT = 54
    val KEY_KPASTERISK = 55
    val KEY_LEFTALT = 56
    val KEY_SPACE = 57
    val KEY_CAPSLOCK = 58
    val KEY_F1 = 59
    val KEY_F2 = 60
    val KEY_F3 = 61
    val KEY_F4 = 62
    val KEY_F5 = 63
    val KEY_F6 = 64
    val KEY_F7 = 65
    val KEY_F8 = 66
    val KEY_F9 = 67
    val KEY_F10 = 68
    val KEY_NUMLOCK = 69
    val KEY_SCROLLLOCK = 70
    val KEY_KP7 = 71
    val KEY_KP8 = 72
    val KEY_KP9 = 73
    val KEY_KPMINUS = 74
    val KEY_KP4 = 75
    val KEY_KP5 = 76
    val KEY_KP6 = 77
    val KEY_KPPLUS = 78
    val KEY_KP1 = 79
    val KEY_KP2 = 80
    val KEY_KP3 = 81
    val KEY_KP0 = 82
    val KEY_KPDOT = 83

    val KEY_ZENKAKUHANKAKU = 85
    val KEY_102ND = 86
    val KEY_F11 = 87
    val KEY_F12 = 88
    val KEY_RO = 89
    val KEY_KATAKANA = 90
    val KEY_HIRAGANA = 91
    val KEY_HENKAN = 92
    val KEY_KATAKANAHIRAGANA = 93
    val KEY_MUHENKAN = 94
    val KEY_KPJPCOMMA = 95
    val KEY_KPENTER = 96
    val KEY_RIGHTCTRL = 97
    val KEY_KPSLASH = 98
    val KEY_SYSRQ = 99
    val KEY_RIGHTALT = 100
    val KEY_LINEFEED = 101
    val KEY_HOME = 102
    val KEY_UP = 103
    val KEY_PAGEUP = 104
    val KEY_LEFT = 105
    val KEY_RIGHT = 106
    val KEY_END = 107
    val KEY_DOWN = 108
    val KEY_PAGEDOWN = 109
    val KEY_INSERT = 110
    val KEY_DELETE = 111
    val KEY_MACRO = 112
    val KEY_MUTE = 113
    val KEY_VOLUMEDOWN = 114
    val KEY_VOLUMEUP = 115
    val KEY_POWER = 116    /* SC System Power Down */
    val KEY_KPEQUAL = 117
    val KEY_KPPLUSMINUS = 118
    val KEY_PAUSE = 119
    val KEY_SCALE = 120    /* AL Compiz Scale (Expose) */

    val KEY_KPCOMMA = 121
    val KEY_HANGEUL = 122
    val KEY_HANGUEL = KEY_HANGEUL
    val KEY_HANJA = 123
    val KEY_YEN = 124
    val KEY_LEFTMETA = 125
    val KEY_RIGHTMETA = 126
    val KEY_COMPOSE = 127

    val KEY_STOP = 128    /* AC Stop */
    val KEY_AGAIN = 129
    val KEY_PROPS = 130/* AC Properties */
    val KEY_UNDO = 131    /* AC Undo */
    val KEY_FRONT = 132
    val KEY_COPY = 133    /* AC Copy */
    val KEY_OPEN = 134    /* AC Open */
    val KEY_PASTE = 135    /* AC Paste */
    val KEY_FIND = 136    /* AC Search */
    val KEY_CUT = 137    /* AC Cut */
    val KEY_HELP = 138    /* AL Integrated Help Center */
    val KEY_MENU = 139    /* Menu (show menu) */
    val KEY_CALC = 140    /* AL Calculator */
    val KEY_SETUP = 141
    val KEY_SLEEP = 142    /* SC System Sleep */
    val KEY_WAKEUP = 143    /* System Wake Up */
    val KEY_FILE = 144    /* AL Local Machine Browser */
    val KEY_SENDFILE = 145
    val KEY_DELETEFILE = 146
    val KEY_XFER = 147
    val KEY_PROG1 = 148
    val KEY_PROG2 = 149
    val KEY_WWW = 150    /* AL Internet Browser */
    val KEY_MSDOS = 151
    val KEY_COFFEE = 152    /* AL Terminal Lock/Screensaver */
    val KEY_SCREENLOCK = KEY_COFFEE
    val KEY_ROTATE_DISPLAY = 153    /* Display orientation for e.g. tablets */
    val KEY_DIRECTION = KEY_ROTATE_DISPLAY
    val KEY_CYCLEWINDOWS = 154
    val KEY_MAIL = 155
    val KEY_BOOKMARKS = 156    /* AC Bookmarks */
    val KEY_COMPUTER = 157
    val KEY_BACK = 158    /* AC Back */
    val KEY_FORWARD = 159    /* AC Forward */
    val KEY_CLOSECD = 160
    val KEY_EJECTCD = 161
    val KEY_EJECTCLOSECD = 162
    val KEY_NEXTSONG = 163
    val KEY_PLAYPAUSE = 164
    val KEY_PREVIOUSSONG = 165
    val KEY_STOPCD = 166
    val KEY_RECORD = 167
    val KEY_REWIND = 168
    val KEY_PHONE = 169    /* Media Select Telephone */
    val KEY_ISO = 170
    val KEY_CONFIG = 171    /* AL Consumer Control Configuration */
    val KEY_HOMEPAGE = 172    /* AC Home */
    val KEY_REFRESH = 173    /* AC Refresh */
    val KEY_EXIT = 174    /* AC Exit */
    val KEY_MOVE = 175
    val KEY_EDIT = 176
    val KEY_SCROLLUP = 177
    val KEY_SCROLLDOWN = 178
    val KEY_KPLEFTPAREN = 179
    val KEY_KPRIGHTPAREN = 180
    val KEY_NEW = 181    /* AC New */
    val KEY_REDO = 182    /* AC Redo/Repeat */

    val KEY_F13 = 183
    val KEY_F14 = 184
    val KEY_F15 = 185
    val KEY_F16 = 186
    val KEY_F17 = 187
    val KEY_F18 = 188
    val KEY_F19 = 189
    val KEY_F20 = 190
    val KEY_F21 = 191
    val KEY_F22 = 192
    val KEY_F23 = 193
    val KEY_F24 = 194

    val KEY_PLAYCD = 200
    val KEY_PAUSECD = 201
    val KEY_PROG3 = 202
    val KEY_PROG4 = 203
    val KEY_DASHBOARD = 204    /* AL Dashboard */
    val KEY_SUSPEND = 205
    val KEY_CLOSE = 206    /* AC Close */
    val KEY_PLAY = 207
    val KEY_FASTFORWARD = 208
    val KEY_BASSBOOST = 209
    val KEY_PRINT = 210    /* AC Print */
    val KEY_HP = 211
    val KEY_CAMERA = 212
    val KEY_SOUND = 213
    val KEY_QUESTION = 214
    val KEY_EMAIL = 215
    val KEY_CHAT = 216
    val KEY_SEARCH = 217
    val KEY_CONNECT = 218
    val KEY_FINANCE = 219    /* AL Checkbook/Finance */
    val KEY_SPORT = 220
    val KEY_SHOP = 221
    val KEY_ALTERASE = 222
    val KEY_CANCEL = 223    /* AC Cancel */
    val KEY_BRIGHTNESSDOWN = 224
    val KEY_BRIGHTNESSUP = 225
    val KEY_MEDIA = 226

    val KEY_SWITCHVIDEOMODE = 227    /* Cycle between available video
                       outputs (Monitor/LCD/TV-out/etc) */
    val KEY_KBDILLUMTOGGLE = 228
    val KEY_KBDILLUMDOWN = 229
    val KEY_KBDILLUMUP = 230

    val KEY_SEND = 231    /* AC Send */
    val KEY_REPLY = 232    /* AC Reply */
    val KEY_FORWARDMAIL = 233    /* AC Forward Msg */
    val KEY_SAVE = 234    /* AC Save */
    val KEY_DOCUMENTS = 235

    val KEY_BATTERY = 236

    val KEY_BLUETOOTH = 237
    val KEY_WLAN = 238
    val KEY_UWB = 239

    val KEY_UNKNOWN = 240

    val KEY_VIDEO_NEXT = 241    /* drive next video source */
    val KEY_VIDEO_PREV = 242    /* drive previous video source */
    val KEY_BRIGHTNESS_CYCLE = 243    /* brightness up, after max is min */
    val KEY_BRIGHTNESS_AUTO = 244    /* Set Auto Brightness: manual
                      brightness control is off,
					  rely on ambient */
    val KEY_BRIGHTNESS_ZERO = KEY_BRIGHTNESS_AUTO
    val KEY_DISPLAY_OFF = 245    /* display device to off state */

    val KEY_WWAN = 246    /* Wireless WAN (LTE, UMTS, GSM, etc.) */
    val KEY_WIMAX = KEY_WWAN
    val KEY_RFKILL = 247    /* Key that controls all radios */

    val KEY_MICMUTE = 248    /* Mute / unmute the microphone */


}
