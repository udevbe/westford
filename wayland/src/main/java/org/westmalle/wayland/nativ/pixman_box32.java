package org.westmalle.wayland.nativ;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;


public class pixman_box32 extends Structure {
    public int x1;
    public int y1;
    public int x2;
    public int y2;

    public pixman_box32() {
        super();
    }

    protected List<?> getFieldOrder() {
        return Arrays.asList("x1",
                             "y1",
                             "x2",
                             "y2");
    }
}
