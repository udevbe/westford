package org.westmalle.wayland.bootstrap;

import com.beust.jcommander.Parameter;

import javax.annotation.Nonnull;


public class CommandBackend {
    @Parameter(names = "-back_end")
    @Nonnull
    public String backend = "";
}
