/*
 * SpsEdit.java
 *
 * Created on 14.01.14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.user.client.ui.TextBox;

/**
 * Author: umaurer
 */
public class SpsEdit extends SpsEditBase<SpsEdit, TextBox> {
    public SpsEdit() {
    }

    @Override
    protected TextBox createGwtWidget() {
        final TextBox textBox = new TextBox();
        if(getMaxLength() > 0) {
            textBox.setMaxLength(getMaxLength());
        }
        return textBox;
    }
}
