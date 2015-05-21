package org.westmalle.wayland.platform;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class pixman_region32 extends Structure {
    /** C type : pixman_box32_t */
    public pixman_box32 extents;
    /** C type : pixman_region32_data_t* */
    public pixman_region32_data.ByReference data;
    public pixman_region32() {
        super();
    }
    protected List<? > getFieldOrder() {
        return Arrays.asList("extents", "data");
    }
    /**
     * @param extents C type : pixman_box32_t<br>
     * @param data C type : pixman_region32_data_t*
     */
    public pixman_region32(pixman_box32 extents, pixman_region32_data.ByReference data) {
        super();
        this.extents = extents;
        this.data = data;
    }
    public pixman_region32(Pointer peer) {
        super(peer);
    }
    public static class ByReference extends pixman_region32 implements Structure.ByReference {

    };
    public static class ByValue extends pixman_region32 implements Structure.ByValue {

    };
}
