package org.westmalle.wayland.core;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class KeyboardMapping {

    public static KeyboardMapping create(String model,
                                         String layout,
                                         String variant,
                                         String options){
        return new AutoValue_KeyboardMapping(model,
                                             layout,
                                             variant,
                                             options);
    }

    public abstract String getModel();
    public abstract String getLayout();
    public abstract String getVariant();
    public abstract String getOptions();
}
