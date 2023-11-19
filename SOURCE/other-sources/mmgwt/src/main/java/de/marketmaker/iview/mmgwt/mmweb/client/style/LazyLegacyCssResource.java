/*
 * LazyLegacyCssResource.java
 *
 * Created on 25.11.2015 11:41
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.style;

import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public class LazyLegacyCssResource implements LazyCssResource {
    @Override
    public String getName() {
        return "LEGACY";
    }

    @Override
    public String generalViewStyle() {
        return null;
    }

    @Override
    public String generalFormStyle() {
        return null;
    }

    @Override
    public String textBox() {
        return "mm-form-field";
    }

    @Override
    public String textBoxInvalid() {
        return "mm-form-invalid";
    }

    @Override
    public String textBoxDisabled() {
        return "mm-form-disabled";
    }

    @Override
    public String textBoxReadonly() {
        return null;
    }

    @Override
    public String label() {
        return "gwt-Label";
    }

    @Override
    public String caption() {
        return label();
    }

    @Override
    public String captionPanel() {
        return "mm-caption-panel";
    }

    @Override
    public String labelReadOnlyField() {
        return "gwt-Label";
    }

    @Override
    public String labelNoWrap() {
        return "mm-nobreak";
    }

    @Override
    public String comboBox() {
        return null;
    }

    @Override
    public String comboBoxWidth180() {
        return null;
    }

    @Override
    public String floatingRadio() {
        return "mm-radios-float";
    }

    @Override
    public String infoIconPanel() {
        return "mm-infoIcon-panel";
    }

    @Override
    public String configurationForm() {
        return "mm-configView-form";
    }
}
