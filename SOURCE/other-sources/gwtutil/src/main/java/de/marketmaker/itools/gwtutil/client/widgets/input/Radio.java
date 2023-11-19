package de.marketmaker.itools.gwtutil.client.widgets.input;

/**
 * User: umaurer
 * Date: 04.06.13
 * Time: 17:26
 */
public class Radio<V> extends ClickWidget {
    private V value = null;
    private boolean uncheckEvent = false;

    public Radio(boolean checked) {
        super("mm-radio", checked);
    }

    @Override
    Boolean getCheckedAfterClick() {
        return Boolean.TRUE;
    }

    public Radio<V> withUncheckEvent() {
        this.uncheckEvent = true;
        return this;
    }

    @Override
    void fireValueChangeEvent(Boolean value) {
        if (value != null && value || this.uncheckEvent) {
            super.fireValueChangeEvent(value);
        }
    }

    public V getValue() {
        return this.value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public Radio<V> withValue(V value) {
        setValue(value);
        return this;
    }
}
