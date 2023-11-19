package de.marketmaker.itools.gwtutil.client.widgets.input;

/**
 * User: umaurer
 * Date: 04.06.13
 * Time: 17:26
 */
public class CheckBox extends ClickWidget {
    public enum Mode {
        TRUE_FALSE(Boolean.TRUE, Boolean.FALSE),
        TRUE_FALSE_NULL(Boolean.TRUE, Boolean.FALSE, null),
        TRUE_NULL(Boolean.TRUE, null);

        private final Boolean[] values;

        Mode(Boolean... values) {
            this.values = values;
        }

        public Boolean next(Boolean current) {
            final int pos = indexOf(current);
            if (pos == -1) {
                return this.values[0];
            }
            else {
                return this.values[(pos + 1) % this.values.length];
            }
        }

        private int indexOf(Boolean current) {
            if (current == null) {
                for (int i = 0; i < values.length; i++) {
                    if (values[i] == null) {
                        return i;
                    }
                }
            }
            else {
                final boolean c = current;
                for (int i = 0; i < this.values.length; i++) {
                    if (this.values[i] != null && this.values[i] == c) {
                        return i;
                    }
                }
            }
            return -1;
        }
    }
    private Mode mode = Mode.TRUE_FALSE;

    public CheckBox(Boolean checked) {
        super("mm-checkbox", checked);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    Boolean getCheckedAfterClick() {
        return this.mode.next(getChecked());
    }
}
