/*
 * Block.java
 *
 * Created on 25.03.13 10:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import java.io.Serializable;

/**
 * @author zzhao
 */
public class Block implements Serializable {

    private static final long serialVersionUID = -3460937353142536850L;

    private final String key;

    private final String title;

    private final String text;

    private final String source;

    public Block(String key, String title, String text, String source) {
        this.key = key;
        this.title = title;
        this.text = text;
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }

    public String getKey() {
        return key;
    }

    public String getText() {
        return text;
    }
}
