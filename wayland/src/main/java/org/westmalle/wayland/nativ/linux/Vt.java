package org.westmalle.wayland.nativ.linux;


public class Vt {
    public static final short VT_OPENQRY = 0x5600;
    public static final short VT_GETMODE = 0x5601;  /* get mode of active vt */
    public static final short VT_SETMODE = 0x5602;  /* set mode of active vt */

    public static final byte  VT_AUTO    = 0x00;    /* auto vt switching */
    public static final byte  VT_PROCESS = 0x01;    /* process controls switching */
    public static final byte  VT_ACKACQ  = 0x02;    /* acknowledge switch */

    public static final short VT_ACTIVATE   = 0x5606;  /* make vt active */
    public static final short VT_WAITACTIVE = 0x5607;  /* wait for vt active */

    public static final short VT_GETSTATE = 0x5603;

    public static final short VT_RELDISP = 0x5605;  /* release display */
}
