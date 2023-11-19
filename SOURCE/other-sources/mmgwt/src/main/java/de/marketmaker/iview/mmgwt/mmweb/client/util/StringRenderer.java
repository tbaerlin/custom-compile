package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * Created on 13.03.13 08:23
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class StringRenderer implements Renderer<String> {
    protected final String nullText;

    public StringRenderer(String nullText) {
        this.nullText = nullText;
    }


    @Override
    public String render(String s) {
        return StringUtil.hasText(s)
                ? s
                : this.nullText;
    }
}
