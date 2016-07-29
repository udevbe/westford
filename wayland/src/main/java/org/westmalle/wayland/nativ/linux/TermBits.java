package org.westmalle.wayland.nativ.linux;


public class TermBits {
    public static final int NCCS = 19;

    public static final int OPOST = 0x1;
    public static final int OLCUC = 0x2;
    public static final int ONLCR = 0x4;
    public static final int OCRNL = 0x8;

    public static final int TCSANOW   = 0;
    public static final int TCSADRAIN = 1;
    public static final int TCSAFLUSH = 2;

    public static final int TCIFLUSH  = 0;
    public static final int TCOFLUSH  = 1;
    public static final int TCIOFLUSH = 2;
}
