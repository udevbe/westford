package org.westmalle.wayland.nativ.linux;


public class Kd {
    public static final short KDGKBMODE   = 0x4B44;
    public static final short KDSKBMODE   = 0x4B45;
    public static final byte  K_RAW       = 0x00;
    public static final byte  K_XLATE     = 0x01;
    public static final byte  K_MEDIUMRAW = 0x02;
    public static final byte  K_UNICODE   = 0x03;
    public static final byte  K_OFF       = 0x04;

    public static final short KDSETMODE   = 0x4B3A;  /* set text/graphics mode */
    public static final byte  KD_TEXT     = 0x00;
    public static final byte  KD_GRAPHICS = 0x01;
    public static final byte  KD_TEXT0    = 0x02;   /* obsolete */
    public static final byte  KD_TEXT1    = 0x03;  /* obsolete */
    public static final short KDGETMODE   = 0x4B3B; /* get current mode */
}
