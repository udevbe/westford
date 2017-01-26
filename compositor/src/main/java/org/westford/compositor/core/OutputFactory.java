package org.westford.compositor.core;


import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;

public class OutputFactory {
    @Nonnull
    private final PrivateOutputFactory privateOutputFactory;

    @Inject
    OutputFactory(@Nonnull final PrivateOutputFactory privateOutputFactory) {
        this.privateOutputFactory = privateOutputFactory;
    }

    public Output create(@Nonnull final RenderOutput renderOutput,
                         @Nonnull final String name,
                         @Nonnull final OutputGeometry outputGeometry,
                         @Nonnull final OutputMode outputMode) {
        final Output output = this.privateOutputFactory.create(renderOutput,
                                                               name);
        output.update(Collections.emptySet(),
                      outputGeometry);
        output.update(Collections.emptySet(),
                      outputMode);
        return output;
    }
}
