/*
 * PdlObject.java
 *
 * Created on 16.06.2005 13:37:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import de.marketmaker.istar.domain.data.PriceQuality;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class PdlObject implements Comparable<PdlObject> {

    /**
     * id for type TEXT => getContent will
     * return the text to be displayed
     */
    public final static int TYPE_TEXT = 1;

    /**
     * id for type PAGEPOINTER => getContent will
     * return the page to link to
     */
    public final static int TYPE_PAGEPOINTER = 2;

    /**
     * id for type DATA => getContent will
     * return object to request, field id to
     * display, the decimal places, and the
     * scaling factor; note that type == DATA
     * means the object is of type
     * PDLDataObject and this offers dedicated
     * access methods for the four additional
     * attributes
     */
    public final static int TYPE_DATA = 3;

    public final static int PAGE_ATTR_ALIGN_CENTER = 1;

    public final static int PAGE_ATTR_ALIGN_RIGHT = 2;

    public final static int PAGE_ATTR_DISPLAY_INVERSE = 4;

    public final static int PAGE_ATTR_OUTLINE_VISIBLE = 8;

    public final static int PAGE_ATTR_NEWS_UNDERLINE = 64;

    private int type;

    private int x;

    private int y;

    private int width;

    private int height;

    private int displayWidth;

    private int displayHeight;

    private int attribute;

    private String content = "";

    private int id;

    private PriceQuality priceQuality = PriceQuality.NONE;

    /**
     * Creates new PdlObject; requires to hand in all attributes of the page;
     * NOTE: do not use directly; create pdl objects with the factory
     *
     * @param type          attribute type
     * @param x             attribute x
     * @param y             attribute y
     * @param width         attribute width
     * @param height        attribute height
     * @param displayWidth  attribute displayWidth
     * @param displayHeight attribute displayHeight
     * @param attribute     attribute attribute
     */
    public PdlObject(int type, int x, int y, int width, int height,
                     int displayWidth, int displayHeight, int attribute) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.attribute = attribute;
    }

    public int getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public int getAttribute() {
        return attribute;
    }

    public String getContent() {
        return content;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(80);
        sb.append("PdlObject[");
        addToString(sb);
        sb.append("]");
        return sb.toString();
    }

    protected void addToString(StringBuilder sb) {
        sb.append("id=").append(this.id);
        sb.append(", ").append("type=").append(this.type);
        sb.append(", ").append("x=").append(this.x);
        sb.append(", ").append("y=").append(this.y);
        sb.append(", ").append("w=").append(this.width);
        sb.append(", ").append("h=").append(this.height);
        sb.append(", ").append("dw=").append(this.displayWidth);
        sb.append(", ").append("dh=").append(this.displayHeight);
        sb.append(", ").append("attr=");
        appendAttr(sb);
        sb.append(", ").append("cnt=").append(this.content);
    }

    private StringBuilder appendAttr(StringBuilder sb) {
        if (hasAttribute(PAGE_ATTR_ALIGN_CENTER)) {
            sb.append("C");
        }
        if (hasAttribute(PAGE_ATTR_ALIGN_RIGHT)) {
            sb.append("R");
        }
        if (hasAttribute(PAGE_ATTR_DISPLAY_INVERSE)) {
            sb.append("I");
        }
        if (hasAttribute(PAGE_ATTR_NEWS_UNDERLINE)) {
            sb.append("U");
        }
        if (hasAttribute(PAGE_ATTR_OUTLINE_VISIBLE)) {
            sb.append("O");
        }
        return sb;
    }


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public PriceQuality getPriceQuality() {
        return priceQuality;
    }

    public void setPriceQuality(PriceQuality priceQuality) {
        this.priceQuality = priceQuality;
    }

    public int compareTo(PdlObject po) {
        if (this.y != po.y) {
            return this.y - po.y;
        }
        else {
            return this.x - po.x;
        }
    }

    public void setContent(String content) {
        if (content.indexOf('\\') != -1) {
            content = content.replaceAll("\\\\>", ">").replaceAll("\\\\<", "<");
        }
        if (content.length() <= this.displayWidth) {
            this.content = content;
        }
        else {
            // this implies having the attributes BEFORE setting the content
            if (this.hasAttribute(PdlObject.PAGE_ATTR_ALIGN_RIGHT)) {
                this.content = content.substring(content.length() - this.displayWidth);
            }
            else {
                this.content = content.substring(0, this.displayWidth);
            }
        }
    }

    public boolean hasAttribute(int attr) {
        return (this.attribute & attr) == attr;
    }
}
