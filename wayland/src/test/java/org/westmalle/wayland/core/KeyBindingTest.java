package org.westmalle.wayland.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.westmalle.Signal;
import org.westmalle.Slot;
import org.westmalle.wayland.core.events.Key;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyBindingTest {

    private final Signal<Key, Slot<Key>> keySignal   = new Signal<>();
    private final Set<Integer>           bindingKeys = new HashSet<>();
    @Mock
    private KeyboardDevice keyboardDevice;
    @Mock
    private Runnable binding;

    private KeyBinding keyBinding;

    @Before
    public void setUp() {
        when(this.keyboardDevice.getKeySignal()).thenReturn(this.keySignal);

        this.keyBinding = new KeyBinding(this.keyboardDevice,
                                         this.bindingKeys,
                                         this.binding);
    }

    @Test
    public void enable() throws Exception {
        //given a keybinding
        //when keybinding is enabled
        this.keyBinding.enable();
        //then keybinding is fired when exactly the bound keys are pressed

    }

    @Test
    public void disable() throws Exception {
        //given a keybinding
        //when keybinding is disabled
        //then keybinding is not fired when exactly the bound keys are pressed
    }

}