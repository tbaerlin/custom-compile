/*
 * Image.java
 *
 * Created on 15.03.12 16:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.io.Serializable;

/**
 * @author zzhao
 */
public class Image implements Serializable {

    private final String name;

    private final String caption;

    private final String url;

    public Image(String name, String caption, String url) {
        this.name = name;
        this.caption = caption;
        this.url = url;
    }

    public String getCaption() {
        return caption;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
