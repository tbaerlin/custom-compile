/*
 * LazyIceCssResource.java
 *
 * Created on 25.11.2015 11:34
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.style;

import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public class LazyIceCssResource implements LazyCssResource {
    @Override
    public String getName() {
        return "ICE";
    }

    @Override
    public String generalViewStyle() {
        return "sps-taskView";
    }

    @Override
    public String generalFormStyle() {
        return "ice-form";
    }

    @Override
    public String textBox() {
        return "sps-edit";
    }

    @Override
    public String textBoxInvalid() {
        return "mm-form-invalid";
    }

    @Override
    public String textBoxDisabled() {
        return "sps-disabled";
    }

    @Override
    public String textBoxReadonly() {
        return textBoxDisabled(); //yes, we currently use the same style, but this may change in the future
    }

    @Override
    public String label() {
        return "gwt-Label";
    }

    @Override
    public String caption() {
        return "sps-caption";
    }

    @Override
    public String captionPanel() {
        return "sps-caption-panel";
    }

    @Override
    public String labelReadOnlyField() {
        return "sps-ro-field";
    }

    @Override
    public String labelNoWrap() {
        return "sps-nowrap";
    }

    @Override
    public String comboBox() {
        return textBox() + " combo";
    }

    @Override
    public String comboBoxWidth180() {
        return "w180";
    }

    @Override
    public String floatingRadio() {
        return "sps-radios-float";
    }

    @Override
    public String infoIconPanel() {
        return "sps-infoIcon-panel";
    }

    @Override
    public String configurationForm() {
        return "mm-configView-form";
    }
}
