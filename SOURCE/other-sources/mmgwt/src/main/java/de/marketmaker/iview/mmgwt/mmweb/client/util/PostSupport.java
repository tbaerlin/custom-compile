/*
 * PostSupport.java
 *
 * Created on 16.04.13 17:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.Map;

/**
 * @author Markus Dick
 */
public class PostSupport {
    final String formName;
    final boolean showSubmitButton;

    public PostSupport(String formName) {
        this(formName, false);
    }

    public PostSupport(String formName, boolean showSubmitButton) {
        this.formName = formName;
        this.showSubmitButton = showSubmitButton;
    }

    public String createForm(String actionUrl, Map<String, String> parameters) {
        final HtmlBuilderImpl f = new HtmlBuilderImpl();
        f.startTag("html"); //$NON-NLS$
        f.startTag("head").closeLast(); //$NON-NLS$
        f.startTag("body"); //$NON-NLS$
        f.startTag("form") //$NON-NLS$
                .addAttribute("name", this.formName) //$NON-NLS$
                .addAttribute("method", "post") //$NON-NLS$
                .addAttribute("target", "_blank") //$NON-NLS$
                .addAttribute("action", actionUrl); //$NON-NLS$

        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            final String id = entry.getKey();
            f.startTag("input") //$NON-NLS$
                    .addAttribute("type", "hidden") //$NON-NLS$
                    .addAttribute("name", id) //$NON-NLS$
                    .addAttribute("value", entry.getValue()) //$NON-NLS$
                    .closeLast();
        }

        if(this.showSubmitButton) {
            f.startTag("input") //$NON-NLS$
                    .addAttribute("type", "submit") //$NON-NLS$
                    .addAttribute("name", "go") //$NON-NLS$
                    .closeLast();
        }

        f.closeAllTags();

        return f.build();
    }

    public void createFormAndSubmit(String actionUrl, Map<String, String> parameters) {
        submitForm(createForm(actionUrl, parameters));
    }

    public void submitForm(String htmlWithForm) {
        submitForm("", htmlWithForm, this.formName); //$NON-NLS$
    }

    /**
     * This strange construction (to open a scripting window, submitting the form, and
     * closing it immediately) is necessary because from IE9 onwards, MS changed the
     * ActiveX plugin behaviour, so that the downloaded file does no longer replace the
     * page's contents. In fact, the new window remains empty.
     *
     * @link http://stackoverflow.com/questions/14822589/form-with-target-blank-behavior-changed-in-ie-9-and-10
     */
    private native void submitForm(String url, String htmlWithForm, String formNameToSubmit) /*-{
        var jso = $wnd.open(url, '_blank');
        jso.document.open('text/html', 'replace');
        jso.document.write(htmlWithForm);
        jso.document.close();
        jso.document.forms[formNameToSubmit].submit();
        jso.close();
    }-*/;
}
