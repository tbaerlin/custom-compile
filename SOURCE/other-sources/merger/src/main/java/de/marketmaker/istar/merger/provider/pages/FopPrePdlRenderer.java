/*
 * HtmlPrePdlRenderer.java
 *
 * Created on 17.06.2005 10:17:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import java.util.Properties;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FopPrePdlRenderer extends PdlRenderer {
    private static final String EOL = "\n";

    FopPrePdlRenderer(Properties p) {
        super(p);
    }

    public String render(PdlPage pp) {
        StringBuilder sb = new StringBuilder(pp.getHeight() * pp.getWidth() * 4);

        int row = 0;
        int col = 0;

        for (PdlObject po : pp.getObjects()) {
            if (row < po.getY()) {
                while (row < po.getY()) {
                    while (col < pp.getWidth()) {
                        sb.append(' ');
                        col++;
                    }
                    sb.append(EOL);
                    row++;
                }
                col = 0;
            }

            while (col < po.getX()) {
                sb.append(' ');
                col++;
            }

            col += render(sb, po);
        }

        return sb.toString();
    }

    private int render(StringBuilder sb, PdlObject po) {
        final int preSpaceCount = getPreSpaceCount(po);
        final int postSpaceCount = getPostSpaceCount(po);

        final boolean isInverse = po.hasAttribute(PdlObject.PAGE_ATTR_DISPLAY_INVERSE);
        final String inverseAttribute = " background-color=\"#b2b2b2\"";
        final String dataColor = " color=\"#111144\"";

        if (po.getType() == PdlObject.TYPE_PAGEPOINTER) {
            addSpace(sb, preSpaceCount);
            sb.append("<fo:inline text-decoration=\"underline\"");
            if (isInverse) {
                sb.append(inverseAttribute);
            }
            sb.append(">");
            sb.append(po.getContent());
            sb.append("</fo:inline>");
            addSpace(sb, postSpaceCount);

        } else if (po.getType() == PdlObject.TYPE_TEXT) {
            if (isInverse) {
                sb.append("<fo:inline").append(inverseAttribute).append(">");
            }
            addSpace(sb, preSpaceCount);
            appendEncoded(sb, po.getContent());
            addSpace(sb, postSpaceCount);
            if (isInverse) {
                sb.append("</fo:inline>");
            }
        } else if (po.getType() == PdlObject.TYPE_DATA) {
            sb.append("<fo:inline").append(dataColor);
            if (isInverse) {
                sb.append(inverseAttribute);
            }
            sb.append(">");
            addSpace(sb, preSpaceCount);
            appendEncoded(sb, po.getContent());
            addSpace(sb, postSpaceCount);
            sb.append("</fo:inline>");
        }

        return po.getDisplayWidth();
    }
}
