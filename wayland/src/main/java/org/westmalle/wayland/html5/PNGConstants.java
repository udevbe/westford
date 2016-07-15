package org.westmalle.wayland.html5;


public class PNGConstants {

    /**
     * first bytes of all PNG file
     */
    public static final byte[] SIGNATURE = new byte[]{(byte) 137, (byte) 80, (byte) 78, (byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10};
    /**
     * last bytes of all PNG file : IEND chunk, size+chunkName+crc32
     */
    public static final byte[] IENDCHUNK = new byte[]{0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};

    public static final String CHUNK_IHDR      = "IHDR";
    public static final String CHUNK_PLTE      = "PLTE";
    public static final String CHUNK_IDAT      = "IDAT";
    public static final String CHUNK_IEND      = "IEND";
    public static final String CHUNK_cHRM      = "cHRM";
    public static final String CHUNK_gAMA      = "gAMA";
    public static final String CHUNK_iCCP      = "iCCP";
    public static final String CHUNK_sBIT      = "sBIT";
    public static final String CHUNK_sRGB      = "sRGB";
    public static final String CHUNK_bKGD      = "bKGD";
    public static final String CHUNK_hIST      = "hIST";
    public static final String CHUNK_tRNS      = "tRNS";
    public static final String CHUNK_pHYs      = "pHYs";
    public static final String CHUNK_sPLT      = "sPLT";
    public static final String CHUNK_tIME      = "tIME";
    public static final String CHUNK_iTXt      = "iTXt";
    public static final String CHUNK_tEXt      = "tEXt";
    public static final String CHUNK_zTXt      = "zTXt";
    public static final String CHUNK_UNKNOWNED = "uwkd";

    public static final int COLOR_GREYSCALE        = 0;        //allowed bit depth : 1, 2, 4, 8, 16
    public static final int COLOR_TRUECOLOUR       = 2;       //allowed bit depth : 8, 16
    public static final int COLOR_INDEXED          = 3;          //allowed bit depth : 1, 2, 4, 8
    public static final int COLOR_GREYSCALE_ALPHA  = 4;  //allowed bit depth : 8, 16
    public static final int COLOR_TRUECOLOUR_ALPHA = 6; //allowed bit depth : 8, 16

    public static final int COMPRESSION_DEFAULT = 0;    // compression method 0, deflate
    public static final int FILTER_DEFAULT      = 0;         // filter method 0, only type reconized.
    public static final int INTERLACE_NONE      = 0;         // No interlace
    public static final int INTERLACE_ADAM7     = 1;        // Adam7 interlaced
    public static final int FILTER_TYPE_NONE    = 0;       // None
    public static final int FILTER_TYPE_SUB     = 1;        // Sub
    public static final int FILTER_TYPE_UP      = 2;         // Up
    public static final int FILTER_TYPE_AVERAGE = 3;    // Average
    public static final int FILTER_TYPE_PAETH   = 4;      // Paeth


}
