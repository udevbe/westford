package org.westmalle.wayland.platform.eglkms.drm;


import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class drmModeConnector extends Structure{

    public int connector_id;
    public int encoder_id; /**< Encoder currently connected to */
    public int connector_type;
    public int connector_type_id;
    public int connection;
    public int mmWidth, mmHeight; /**< HxW in millimeters */
    public int subpixel;

    public int count_modes;
    public drmModeModeInfo.ByReference modes;

    public int count_props;
    public Pointer props; /**< List of property ids */
    public Pointer prop_values; /**< List of property values */

    public int count_encoders;
    public Pointer encoders; /**< List of encoder ids */

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("connector_id","encoder_id","connector_type","connector_type_id","connection",
                             "mmWidth","mmHeight","subpixel","count_modes","modes","count_props","props","prop_values",
                             "count_encoders","encoders");
    }
}
