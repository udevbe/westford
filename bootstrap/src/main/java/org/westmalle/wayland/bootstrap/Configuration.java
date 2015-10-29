package org.westmalle.wayland.bootstrap;

import com.beust.jcommander.Parameter;

public class Configuration {
    @Parameter(names = "--backend",
               arity = 1,
               forceNonOverwritable = true)
    public String backend = "";
}
