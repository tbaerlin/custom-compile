/*
 * LazyCssResource.java
 *
 * Created on 25.11.2015 11:42
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.style;

/**
 * Provides style names for widgets, views, etc.
 *
 * The concept is similar to that of GWT's CssResource. However, we cannot use it, because we do
 * not want to have an additional permutation for each style.
 *
 * Styles: use {@linkplain com.google.gwt.user.client.ui.Widget#setStyleName(String)}
 * except those annotated with &quot;additional style&quot;: use {@linkplain com.google.gwt.user.client.ui.Widget#addStyleName(String)}
 * @author mdick
 */
public interface LazyCssResource {
    String getName();

    /**
     * @return the additional style or null
     */
    String generalViewStyle();

    /**
     * @return the additional style or null
     */
    String generalFormStyle();

    /**
     * @return the primary style but never null.
     */
    String textBox();

    /**
     * @return the additional style or null
     */
    String textBoxInvalid();

    /**
     * @return the additional style or null
     */
    String textBoxDisabled();

    /**
     * @return the additional style or null
     */
    String textBoxReadonly();

    /**
     * @return the primary style but never null.
     */
    String label();

    /**
     * @return the primary style but never null.
     */
    String caption();

    /**
     * @return the primary style but never null.
     */
    String captionPanel();

    /**
     * @return the primary style but never null.
     */
    String labelReadOnlyField();

    /**
     * @return the additional style or null
     */
    String labelNoWrap();

    /**
     * @return the additional style or null
     */
    String comboBox();

    /**
     * @return the additional style or null
     */
    String comboBoxWidth180();

    /**
     * @return the primary style but never null.
     */
    String floatingRadio();

    /**
     * @return the primary style but never null
     */
    String infoIconPanel();

    /**
     * @return the additional style or null
     */
    String configurationForm();
}
