/*
 * PdlDataObject.java
 *
 * Created on 16.06.2005 13:39:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class PdlDataObject extends PdlObject {
    private String requestObject;

    private int fieldId;

    private int decimalPlaces;

    private int scalingFactor;

    private boolean priceConverted = false;

    private String rawContent;

    /**
     * Creates new PdlDataObject; requires to hand in all attributes of the page;
     * NOTE: do not use directly; create Pdl objects with the factory
     * @param type attribute type
     * @param x attribute x
     * @param y attribute y
     * @param width attribute width
     * @param height attribute height
     * @param displayWidth attribute displayWidth
     * @param displayHeight attribute displayHeight
     * @param attribute attribute attribute
     * @param content content
     */
    public PdlDataObject(int type, int x, int y, int width, int height,
            int displayWidth, int displayHeight,
            int attribute, String content) {

        super(type, x, y, width, height, displayWidth, displayHeight, attribute);

        final String[] tokens = content.split(" ");
        this.requestObject = tokens[0];
        this.fieldId = parseInt(tokens[1]);
        this.decimalPlaces = parseInt(tokens[2]);
        this.scalingFactor = parseInt(tokens[3]);
    }

    public static int parseInt(String val) {
        if ("n/a".equals(val)) {
            return 0;
        }

        try {
            return Integer.parseInt(val);
        }
        catch (Exception e) {
            return 0;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(80);
        sb.append("PdlDataObject[");
        super.addToString(sb);
        sb.append(", ").append("object=").append(this.requestObject);
        sb.append(", ").append("field=").append(this.fieldId);
        sb.append(", ").append("dec=").append(this.decimalPlaces);
        sb.append(", ").append("scale=").append(this.scalingFactor);
        sb.append(", ").append("conv=").append(this.priceConverted);
        sb.append(", ").append("rawCnt=").append(this.rawContent);
        sb.append("]");
        return sb.toString();
    }


    public String getRequestObject() {
        return requestObject;
    }

    public int getFieldId() {
        return fieldId;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public int getScalingFactor() {
        return scalingFactor;
    }

    public void setContent(String content) {
        this.rawContent = content;
        super.setContent(content);
    }

    public boolean isPriceConverted() {
        return priceConverted;
    }

    public void setPriceConverted(boolean priceConverted) {
        this.priceConverted = priceConverted;
    }
}
