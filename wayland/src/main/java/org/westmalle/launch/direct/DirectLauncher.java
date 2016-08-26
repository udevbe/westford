package org.westmalle.launch.direct;

import org.westmalle.launch.Launcher;

public class DirectLauncher implements Launcher {

    DirectLauncher() {
    }

    @Override
    public void launch(final Class<?> main,
                       final String[] args) throws Exception {
        main.getMethod("main",
                       String[].class)
            .invoke(null,
                    (Object) args);
    }
}
