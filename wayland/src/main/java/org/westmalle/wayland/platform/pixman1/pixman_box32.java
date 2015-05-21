package org.westmalle.wayland.platform.pixman1;

import com.sun.jna.Pointer;
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
    protected List<? > getFieldOrder() {
        return Arrays.asList("x1", "y1", "x2", "y2");
    }
    public pixman_box32(int x1, int y1, int x2, int y2) {
        super();
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    public pixman_box32(Pointer peer) {
        super(peer);
    }
    public static class ByReference extends pixman_box32 implements Structure.ByReference {

    };
    public static class ByValue extends pixman_box32 implements Structure.ByValue {

    };
}
