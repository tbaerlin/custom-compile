/*
 * CustomTitleSuffixTextBoxForm.java
 *
 * Created on 20.06.2012 17:19:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import java.util.Map;

/**
 * @author Markus Dick
 */
public class CustomTitleSuffixTextBoxForm extends AbstractTextBoxForm {
    public final static String CUSTOM_TITLE_SUFFIX_PARAM_NAME = "customTitleSuffix"; // $NON-NLS-0$

    public CustomTitleSuffixTextBoxForm(Map<String, String> params, String label) {
        super(params, label, 300);
    }

    @Override
    protected String getDisplayParameterName() {
        return CUSTOM_TITLE_SUFFIX_PARAM_NAME;
    }

    @Override
    protected String getValueParameterName() {
        return CUSTOM_TITLE_SUFFIX_PARAM_NAME;
    }
}
