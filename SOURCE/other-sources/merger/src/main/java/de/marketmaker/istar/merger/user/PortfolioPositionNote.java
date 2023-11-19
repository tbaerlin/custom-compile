/*
 * PortfolioPositionNote.java
 *
 * Created on 11/24/14 1:03 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.io.Serializable;

/**
* @author kmilyut
*/
public class PortfolioPositionNote implements Serializable {

    private String itemId;

    private String itemName;

    private String content;

    public PortfolioPositionNote(String itemId, String content) {
        this.itemId = itemId;
        this.content = content;
    }

    public String getItemId() {
        return itemId;
    }

    public String getContent() {
        return content;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public PortfolioPositionNote deepCopy() {
        PortfolioPositionNote result = new PortfolioPositionNote(this.itemId, this.content);
        result.itemName = this.itemName;
        return result;
    }
}
