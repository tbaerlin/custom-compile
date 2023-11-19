package de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz;

import de.marketmaker.iview.mmgwt.mmweb.client.SimpleHtmlController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Map;
import java.util.HashMap;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * Created on Oct 21, 2009 10:38:48 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class DzContentController extends SimpleHtmlController {

    enum Type {
        PDF("pdf", "mm-desktopIcon-pdf"), // $NON-NLS-0$ $NON-NLS-1$
        CSV("csv", ""), // $NON-NLS-0$ $NON-NLS-1$
        HTML("html", "mm-desktopIcon-html"); // $NON-NLS-0$ $NON-NLS-1$

        final String type;
        final String style;
        static final Map<String, Type> stringToEnum = new HashMap<String, Type>();

        static {
            for (Type t: values()) {
                stringToEnum.put(t.toString(), t);
            }
        }

        Type(String type, String style) {
            this.type = type;
            this.style = style;
        }

        public static Type fromString(String type) {
            return stringToEnum.get(type);
        }

        public String getType() {
            return type;
        }

        public String getStyle() {
            return style;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }


    private DzContentController(ContentContainer contentContainer, String html) {
        super(contentContainer, html);
    }

    private static String createHtml() {
        final StringBuilder sb = new StringBuilder();
        final JSONWrapper jsonWrapper = SessionData.INSTANCE.getGuiDef("dzcontent"); // $NON-NLS-0$
        final int docCount = Integer.valueOf(jsonWrapper.get("documentcount").stringValue()); // $NON-NLS-0$
        final String headLine = jsonWrapper.get("headline").stringValue(); // $NON-NLS-0$

        sb.append("<div class=\"external-tool-header\">").append(headLine).append("</div>"); // $NON-NLS-0$ $NON-NLS-1$
        sb.append("<div class=\"external-tool-text\">") // $NON-NLS-0$
                .append(I18n.I.clickFollowingLinkToOpenNewWindow()); 

        for (int i = 0; i < docCount; i++) {
            final JSONWrapper doc = jsonWrapper.get(String.valueOf(i));
            final String name = doc.get("name").stringValue(); // $NON-NLS-0$
            final String url = doc.get("url").stringValue(); // $NON-NLS-0$
            final Type type = Type.fromString(doc.get("type").stringValue()); // $NON-NLS-0$
            final String date = doc.get("date").stringValue(); // $NON-NLS-0$
            sb.append("<br/><br/>") // $NON-NLS-0$
                    .append("<a href=\"").append(url).append("\" target=\"dzcontent\" class=\"mm-simpleLink\">") // $NON-NLS-0$ $NON-NLS-1$
                    .append("<div class=\"").append(type.getStyle()).append("\"></div>") // $NON-NLS-0$ $NON-NLS-1$
                    .append("<span style=\"font-size:70%\">").append(date).append("</span>") // $NON-NLS-0$ $NON-NLS-1$
                    .append("<br>") // $NON-NLS-0$
                    .append(name)
                    .append("</a>"); // $NON-NLS-0$
        }
        sb.append("</div>"); // $NON-NLS-0$
        return sb.toString();
    }

    public static DzContentController createDzContent(ContentContainer cc) {
        return new DzContentController(cc, createHtml());
    }
}
