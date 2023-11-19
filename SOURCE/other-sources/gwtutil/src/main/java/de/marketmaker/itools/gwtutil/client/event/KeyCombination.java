package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.dom.client.NativeEvent;
import de.marketmaker.itools.gwtutil.client.util.Firebug;

/**
 * Author: umaurer
 * Created: 29.10.14
 */
public class KeyCombination {
    public static final KeyCombination CTRL_A = new Factory().ctrl().a();
    public static final KeyCombination CTRL_C = new Factory().ctrl().c();
    public static final KeyCombination CTRL_V = new Factory().ctrl().v();
    public static final KeyCombination CTRL_X = new Factory().ctrl().x();

    private boolean ctrl;
    private boolean shift;
    private boolean alt;
    private boolean meta;
    private final int keyCode;
    private final int charCode;

    static class Factory {
        private boolean ctrl = false;
        private boolean shift = false;
        private boolean alt = false;
        private boolean meta = false;

        public Factory ctrl() {
            this.ctrl = true;
            return this;
        }

        public Factory alt() {
            this.alt = true;
            return this;
        }

        public Factory shift() {
            this.shift = true;
            return this;
        }

        public Factory meta() {
            this.meta = true;
            return this;
        }

        public KeyCombination a() {
            return charCode('a');
        }

        public KeyCombination c() {
            return charCode('c');
        }

        public KeyCombination v() {
            return charCode('v');
        }

        public KeyCombination x() {
            return charCode('x');
        }

        public KeyCombination keyCode(int keyCode) {
            return new KeyCombination(this.ctrl, this.shift, this.alt, this.meta, keyCode, 0);
        }

        public KeyCombination charCode(int charCode) {
            return new KeyCombination(this.ctrl, this.shift, this.alt, this.meta, 0, charCode);
        }
    }

    public KeyCombination(boolean ctrl, boolean shift, boolean alt, boolean meta, int keyCode, int charCode) {
        this.ctrl = ctrl;
        this.shift = shift;
        this.alt = alt;
        this.meta = meta;
        this.keyCode = keyCode;
        this.charCode = charCode;
    }

    public boolean matches(NativeEvent event) {
//        firebug(event);
        return event.getCtrlKey() == this.ctrl
                && event.getShiftKey() == this.shift
                && event.getAltKey() == this.alt
                && event.getMetaKey() == this.meta
                && event.getCharCode() == this.charCode
                && event.getKeyCode() == this.keyCode;
    }

    @SuppressWarnings("UnusedDeclaration")
    private void firebug(NativeEvent event) {
        Firebug.debug("NativeEvent - ctrl: " + event.getCtrlKey()
                + "  shift: " + event.getShiftKey()
                + "  alt: " + event.getAltKey()
                + "  meta: " + event.getMetaKey()
                + "  charCode: " + event.getCharCode()
                + "  keyCode: " + event.getKeyCode()
        );
    }
}
