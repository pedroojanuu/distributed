package utils;

import java.awt.*;
import java.awt.event.KeyEvent;

/*
 * Adapted from Stack Overflow
 * https://stackoverflow.com/questions/18037576/how-do-i-check-if-the-user-is-pressing-a-key
 */

public class Keyboard {
    private static volatile int pressed = -1;
    
    public static int pressedKey() {
        synchronized (Keyboard.class) {
            return pressed;
        }
    }

    public static void main(String[] args) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent ke) {
                synchronized (Keyboard.class) {
                    switch (ke.getID()) {
                        case KeyEvent.KEY_PRESSED:
                            pressed = ke.getKeyCode();
                            break;
                        case KeyEvent.KEY_RELEASED:
                            pressed = -1;
                            break;
                    }
                    return false;
                }
            }
        });
    }
}
