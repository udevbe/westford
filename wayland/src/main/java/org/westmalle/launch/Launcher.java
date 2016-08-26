package org.westmalle.launch;

public interface Launcher {
    void launch(Class<?> main,
                String[] args) throws Exception;
}
