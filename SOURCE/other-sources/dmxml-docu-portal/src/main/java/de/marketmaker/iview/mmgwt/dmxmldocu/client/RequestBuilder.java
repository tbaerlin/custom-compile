/*
 * RequestService.java
 *
 * Created on 29.03.12 09:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import java.util.ArrayList;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter.ParameterInputWidget;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class RequestBuilder {

    public static String INDENT = "  "; // $NON-NLS$

    public static String getRequestFromGui(String blockName, String authentication,
                                           String authenticationType, String locale,
                                           ArrayList<ParameterInputWidget> paramWidgets) {
        final StringBuilder xml = new StringBuilder(500);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); // $NON-NLS$
        xml.append("<request>\n"); // $NON-NLS$
        String indent = INDENT;
        if (authentication != null) {
            xml.append(indent).append("<authentication>").append(authentication).append("</authentication>\n"); // $NON-NLS$
        }
        if (authenticationType != null) {
            xml.append(indent).append("<authenticationType>").append(authenticationType).append("</authenticationType>\n"); // $NON-NLS$
        }
        if(locale != null && !locale.trim().isEmpty()) {
            xml.append(indent).append("<locale>").append(locale).append("</locale>\n"); // $NON-NLS$
        }
        xml.append(indent).append("<block key=\"").append(blockName).append("\">\n"); // $NON-NLS$
        indent = INDENT + INDENT;
        for (ParameterInputWidget widget : paramWidgets) {
            if (widget.isEnabled()) {
                final String name = widget.getParameterDocu().getName();
                final String[] values = widget.getParameterValues();

                // TODO currently, we cannot handle complex type parameters, e.g. in PM_MMTalk
                /*
                * Suggested solution: Add an annotation for Command fields that marks parameters
                * as complex-valued. This information should find its way into DmxmlBlockParameterDocumentation.
                * Then, we can adapt xml generation here.
                */

                for (String value : values) {
                    xml.append(indent).append("<parameter key=\"").append(name); // $NON-NLS$
                    xml.append("\" value=\"")  // $NON-NLS$
                            .append(SafeHtmlUtils.htmlEscapeAllowEntities(value))
                            .append("\"/>\n"); // $NON-NLS$
                }
            }
        }
        indent = INDENT;
        xml.append(indent).append("</block>\n"); // $NON-NLS$
        xml.append("</request>\n"); // $NON-NLS$
        return xml.toString();
    }

}
