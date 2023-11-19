/*
 * PdlObjectFactory.java
 *
 * Created on 16.06.2005 13:38:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class PdlObjectFactory {
    /**
     * static method to create a Pdl object; analyze type string and call other
     * createPdlObject(int type, ...)
     * @param type respective attribute of Pdl object
     * @param x respective attribute of Pdl object
     * @param y respective attribute of Pdl object
     * @param width respective attribute of Pdl object
     * @param height respective attribute of Pdl object
     * @param displayWidth respective attribute of Pdl object
     * @param displayHeight respective attribute of Pdl object
     * @param attribute respective attribute of Pdl object
     * @param content respective attribute of Pdl object
     * @return PdlObject created with the given attributes;
     *         object type depends on type attribute
     */
    static PdlObject createPdlObject(String type, int x, int y,
            int width, int height, int displayWidth, int displayHeight, int attribute,
            String content) {

        if ("T".equals(type.toUpperCase())) {
            PdlObject result = new PdlObject(PdlObject.TYPE_TEXT, x, y, width, height,
                    displayWidth, displayHeight, attribute);
            result.setContent(content);
            return result;
        }
        else if ("P".equals(type.toUpperCase())) {
            PdlObject result = new PdlObject(PdlObject.TYPE_PAGEPOINTER, x, y, width, height,
                    displayWidth, displayHeight, attribute);
            result.setContent(content);
            return result;
        }
        else if ("D".equals(type.toUpperCase())) {
            return new PdlDataObject(PdlObject.TYPE_DATA, x, y, width, height, displayWidth, displayHeight, attribute, content);
        }

        return null;
    }


}
