/*
 * HtmlPrePdlRenderer.java
 *
 * Created on 17.06.2005 10:17:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import java.util.BitSet;
import java.util.Properties;

import org.springframework.util.StringUtils;

import static de.marketmaker.istar.common.featureflags.FeatureFlags.Flag.VWD_RELEASE_2014;
import static de.marketmaker.istar.merger.context.RequestContextHolder.getRequestContext;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class HtmlPrePdlRenderer extends PdlRenderer {

    private static final String ALL_FIELDS = "*";

    private static BitSet toBitSet(String str) {
        if (str == null || ALL_FIELDS.equals(str)) {
            return null;
        }
        final String[] list = StringUtils.delimitedListToStringArray(str, " ");
        final BitSet result = new BitSet();
        for (String s : list) {
            result.set(Integer.parseInt(s));
        }
        return result;
    }

    private final String startTagConverted;

    private final String endTagConverted;

    private final boolean usePriceQualityStyle;

    private final String typeTextClass;

    private final String typeDataClass;

    private final String typeInverseClass;

    private final boolean addTitlePopup;

    private final String linkClass;

    private final String fieldLinkClass;

    private final String dataObjectLinkPrefix;

    private final BitSet dataObjectLinkFields;

    private final boolean linkAllFields;

    HtmlPrePdlRenderer(Properties p) {
        super(p);
        final boolean vwdRelease2014 = getRequestContext().isEnabledAndNotForbidden(VWD_RELEASE_2014);

        this.startTagConverted = p.getProperty("startTagConverted", "<em>");
        this.endTagConverted = p.getProperty("endTagConverted", "</em>");
        this.usePriceQualityStyle = Boolean.valueOf(p.getProperty("usePriceQualityStyle"));
        this.typeTextClass = p.getProperty("typeTextClass", null);
        this.typeDataClass = p.getProperty("typeDataClass", null);
        this.typeInverseClass = p.getProperty("typeInverseClass", null);
        this.addTitlePopup = Boolean.valueOf(p.getProperty("addTitlePopup"));
        this.linkClass = p.getProperty("linkClass", null);
        this.fieldLinkClass = p.getProperty("fieldLinkClass", vwdRelease2014 ? "mm-page-hover" : null);
        this.dataObjectLinkPrefix = p.getProperty("dataObjectLinkPrefix", null);

        String dataObjectLinkFieldsStr = p.getProperty("dataObjectLinkFields");
        this.dataObjectLinkFields = toBitSet(dataObjectLinkFieldsStr);
        this.linkAllFields = vwdRelease2014 || ALL_FIELDS.equals(dataObjectLinkFieldsStr);
    }

    public String render(PdlPage pp) {
        StringBuilder sb = new StringBuilder(pp.getHeight() * pp.getWidth() * 4);

        int row = 0;
        int col = 0;

        for (PdlObject po : pp.getObjects()) {
            if (row < po.getY()) {
                while (row < po.getY()) {
                    sb.append('\n');
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

        if (po.hasAttribute(PdlObject.PAGE_ATTR_DISPLAY_INVERSE)) {
            if (this.typeInverseClass != null) {
                sb.append("<span class=\"").append(this.typeInverseClass).append("\">");
            }
            else {
                sb.append("<strong>");
            }
        }

        if (po.getType() == PdlObject.TYPE_PAGEPOINTER) {
            addSpace(sb, preSpaceCount);
            sb.append("<a");
            if (this.linkClass != null) {
                sb.append(" class=\"").append(this.linkClass).append('\"');
            }
            sb.append(" href=\"");
            sb.append(this.linkText);
            sb.append(po.getContent());
            sb.append("\">");
            sb.append(po.getContent());
            sb.append("</a>");
            addSpace(sb, postSpaceCount);
        }
        else if (po.getType() == PdlObject.TYPE_TEXT) {
            if (this.typeTextClass != null) {
                sb.append("<span class=\"").append(this.typeTextClass).append("\">");
            }
            addSpace(sb, preSpaceCount);
            appendEncoded(sb, po.getContent());
            addSpace(sb, postSpaceCount);
            if (this.typeTextClass != null) {
                sb.append("</span>");
            }
        }
        else if (po.getType() == PdlObject.TYPE_DATA) {
            final PdlDataObject pdo = (PdlDataObject) po;
            if (pdo.isPriceConverted()) {
                sb.append(this.startTagConverted);
            }
            if (this.typeDataClass != null) {
                sb.append("<span");
                sb.append(" class=\"").append(this.typeDataClass).append("\"");
                if (this.addTitlePopup) {
                    sb.append(" title=\"").append(pdo.getRequestObject()).append(" #").append(pdo.getFieldId()).append("\"");
                }
                sb.append(">");
            }
            final String pqClass = getPriceQualityClass(po);
            if (this.usePriceQualityStyle && pqClass != null) {
                sb.append("<span class=\"").append(pqClass).append("\">");
            }
            addSpace(sb, preSpaceCount);
            appendField(sb, po, pdo);
            addSpace(sb, postSpaceCount);
            if (this.usePriceQualityStyle && pqClass != null) {
                sb.append("</span>");
            }
            if (this.typeDataClass != null) {
                sb.append("</span>");
            }
            if (pdo.isPriceConverted()) {
                sb.append(this.endTagConverted);
            }
        }

        if (po.hasAttribute(PdlObject.PAGE_ATTR_DISPLAY_INVERSE)) {
            if (this.typeInverseClass != null) {
                sb.append("</span>");
            }
            else {
                sb.append("</strong>");
            }
        }

        return po.getDisplayWidth();
    }

    private void appendField(StringBuilder sb, PdlObject po, PdlDataObject pdo) {
        final String content = po.getContent();
        final boolean wrapInLink = isWrapFieldInLink(pdo, content);

        if (wrapInLink) {
            sb.append("<a href=\"").append(this.dataObjectLinkPrefix).append(pdo.getRequestObject()).append("\"");
            if (!StringUtils.isEmpty(this.fieldLinkClass)) {
                sb.append(" class=\"").append(this.fieldLinkClass).append("\"");
            }
            sb.append(">");
        }
        appendEncoded(sb, content);
        if (wrapInLink) {
            sb.append("</a>");
        }
    }

    private boolean isWrapFieldInLink(PdlDataObject pdo, String content) {
        if (StringUtils.isEmpty(content) || this.dataObjectLinkPrefix == null) {
            return false;
        }
        if (this.dataObjectLinkFields != null) {
            return this.dataObjectLinkFields.get(pdo.getFieldId());
        }
        return this.linkAllFields;
    }

    private String getPriceQualityClass(PdlObject po) {
        switch (po.getPriceQuality()) {
            case REALTIME:
                return "rt";
            case DELAYED:
                return "nt";
            default:
                return null;
        }
    }

}
