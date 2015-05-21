package org.westmalle.wayland.platform;


import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class pixman_region32_data extends Structure {
    public NativeLong size;
    public NativeLong numRects;
    public pixman_region32_data() {
        super();
    }
    protected List<? > getFieldOrder() {
        return Arrays.asList("size", "numRects");
    }
    public pixman_region32_data(NativeLong size, NativeLong numRects) {
        super();
        this.size = size;
        this.numRects = numRects;
    }
    public pixman_region32_data(Pointer peer) {
        super(peer);
    }
    public static class ByReference extends pixman_region32_data implements Structure.ByReference {

    };
    public static class ByValue extends pixman_region32_data implements Structure.ByValue {

    };
}
