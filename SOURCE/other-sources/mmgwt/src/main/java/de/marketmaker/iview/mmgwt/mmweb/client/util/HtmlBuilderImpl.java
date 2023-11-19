/*
 * HtmlBuilderImpl.java
 *
 * Created on 18.02.2010 10:19:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author oflege
 */
public class HtmlBuilderImpl implements HtmlBuilder {
    private final StringBuffer sb = new StringBuffer();

    private HashSet<String> classes = new HashSet<String>();

    private ArrayList<String> tagsToClose = new ArrayList<String>();

    private boolean tagOpen = false;

    public HtmlBuilder append(String s) {
        sb.append(s);
        return this;
    }

    public HtmlBuilder addClass(String s) {
        if (s != null && s.length() > 0) {
            this.classes.add(s);
        }
        return this;
    }

    public HtmlBuilder startTag(String name) {
        closeTag();
        sb.append("<").append(name); // $NON-NLS-0$
        this.tagOpen = true;
        this.tagsToClose.add(name);
        return this;
    }

    public HtmlBuilder setContent(String c) {
        closeTag();
        this.sb.append(c);
        closeLast();
        return this;
    }

    public void closeLast() {
        if (this.tagOpen) {
            appendClasses();
            sb.append("/>");
            this.tagOpen = false;
            tagsToClose.remove(tagsToClose.size() - 1);
        }
        else {
            sb.append("</").append(tagsToClose.remove(tagsToClose.size() - 1)).append(">"); // $NON-NLS$
        }
    }

    private void closeTag() {
        if (this.tagOpen) {
            appendClasses();
            sb.append(">"); // $NON-NLS-0$
            this.tagOpen = false;
        }
    }

    private void appendClasses() {
        if (!this.classes.isEmpty()) {
            this.sb.append(" class="); // $NON-NLS-0$
            String sep = "\""; // $NON-NLS-0$
            for (String aClass : this.classes) {
                sb.append(sep).append(aClass);
                sep = " "; // $NON-NLS-0$
            }
            sb.append("\""); // $NON-NLS-0$
            this.classes.clear();
        }
    }

    public HtmlBuilder addAttribute(String name, int value) {
        if (this.tagOpen) {
            sb.append(" ").append(name).append("=").append(value); // $NON-NLS-0$ $NON-NLS-1$
        }
        return this;
    }

    public HtmlBuilder addAttribute(String name, String value) {
        if (value != null && this.tagOpen) {
            sb.append(" ").append(name).append("=\"").append(value).append("\""); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        }
        return this;
    }

    public String build() {
        closeTag();
        closeAllTags();
        return sb.toString();
    }

    public void closeAllTags() {
        while(!this.tagsToClose.isEmpty()) {
            closeLast();
        }
    }

    public static void main(String[] args) {
        final HtmlBuilderImpl b = new HtmlBuilderImpl();
        b.startTag("table"); // $NON-NLS-0$
        b.addAttribute("cellpadding", 8); // $NON-NLS-0$
        b.addAttribute("cellspacing", "1px"); // $NON-NLS-0$ $NON-NLS-1$
        b.addClass("c1"); // $NON-NLS-0$
        b.addClass(null);
        b.addClass("c2"); // $NON-NLS-0$
        b.startTag("tr"); // $NON-NLS-0$
        b.startTag("td"); // $NON-NLS-0$
        b.setContent("foo"); // $NON-NLS-0$
        System.out.println(b.toString());
    }
}
