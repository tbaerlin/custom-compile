/*
 * Separator.java
 *
 * Created on 30.10.2008 18:01:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * Toolbar separator. Usually displays a vertical line.
 *  
 * @author Ulrich Maurer
 */
public class Separator extends Composite {
    public Separator() {
        final Label label = new Label();
        label.setStyleName("mm-tbsep");
        initWidget(label);
    }
}

