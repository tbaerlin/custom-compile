/*
 * HtmlBuilder.java
 *
 * Created on 18.02.2010 10:19:25
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author oflege
 */
public interface HtmlBuilder {
    HtmlBuilder append(String s);

    HtmlBuilder addClass(String s);

    HtmlBuilder startTag(String name);

    HtmlBuilder setContent(String c);

    HtmlBuilder addAttribute(String name, int value);

    HtmlBuilder addAttribute(String name, String value);        

    String build();

    void closeAllTags();

    void closeLast();
}
