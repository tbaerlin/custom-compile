package de.marketmaker.istar.merger.provider.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * @author Ulrich Maurer
 */
class HtmlTablePdlRenderer extends PdlRenderer {
    private String typeText;
    private String typeLink;
    private String typeData;
    private String typeDataConverted;
    private String typeDataQualityRt;
    private String typeDataQualityNt;
    private String typeAttrAlignCenter;
    private String typeAttrAlignRight;
    private String typeAttrDisplayInverse;

    HtmlTablePdlRenderer(Properties p) {
        super(p);
        this.typeText = p.getProperty("typeText", "text");
        this.typeLink = p.getProperty("typeLink", "link");
        this.typeData = p.getProperty("typeData", "data");
        this.typeDataConverted = p.getProperty("typeDataConverted", "converted");
        this.typeDataQualityRt = p.getProperty("typeDataQualityRt", "qualityRt");
        this.typeDataQualityNt = p.getProperty("typeDataQualityNt", "qualityNt");
        this.typeAttrAlignCenter = p.getProperty("typeAttrAlignCenter", "center");
        this.typeAttrAlignRight = p.getProperty("typeAttrAlignRight", "right");
        this.typeAttrDisplayInverse = p.getProperty("typeAttrDisplayInverse", "inverse");
    }

    public String render(PdlPage pp) {
        final StringBuilder sb = new StringBuilder(pp.getHeight() * pp.getWidth());
        sb.append("<table id=\"pageData\" class=\"pageTable\">\n");
        final PdlObject[][] pageMatrix = createPageMatrix(pp);
        final List<String> classes = new ArrayList<>(6);
        for (final PdlObject[] rows : pageMatrix) {
            sb.append("<tr>");
            for (final PdlObject po : rows) {
                if (po == null) {
                    sb.append("<td></td>");
                    continue;
                }

                sb.append("<td");
                setTdClass(classes, sb, po);
                sb.append(">");

                if (po.getType() == PdlObject.TYPE_PAGEPOINTER) {
                    sb.append("<a");
                    if (this.typeLink != null) {
                        sb.append(" class=\"").append(this.typeLink).append('\"');
                    }
                    sb.append(" href=\"").append(this.linkText).append(po.getContent()).append('\"');
                    sb.append('>');
                }
                appendEncoded(sb, po.getContent());
                if (po.getType() == PdlObject.TYPE_PAGEPOINTER) {
                    sb.append("</a>");
                }

                sb.append("</td>");
            }
            sb.append("</tr>\n");
        }
        sb.append("</table>");
        return sb.toString();
    }


    private void setTdClass(final List<String> classes, final StringBuilder sb, final PdlObject po) {
        classes.clear();
        switch (po.getType()) {
            case PdlObject.TYPE_TEXT:
                if (this.typeText != null) {
                    classes.add(this.typeText);
                }
                break;
            case PdlObject.TYPE_PAGEPOINTER:
                if (this.typeLink != null) {
                    classes.add(this.typeLink);
                }
                break;
            case PdlObject.TYPE_DATA:
                if (this.typeData != null) {
                    classes.add(this.typeData);

                    if (this.typeDataConverted != null) {
                        final PdlDataObject pdo = (PdlDataObject) po;
                        if (pdo.isPriceConverted()) {
                            classes.add(this.typeDataConverted);
                        }
                    }

                    final String priceQualityClass = getPriceQualityClass(po);
                    if (priceQualityClass != null) {
                        classes.add(priceQualityClass);
                    }
                }
                break;
        }

        if (po.hasAttribute(PdlObject.PAGE_ATTR_ALIGN_CENTER)) {
            classes.add(this.typeAttrAlignCenter);
        }
        else if (po.hasAttribute(PdlObject.PAGE_ATTR_ALIGN_RIGHT)) {
            classes.add(this.typeAttrAlignRight);
        }

        if (po.hasAttribute(PdlObject.PAGE_ATTR_DISPLAY_INVERSE)) {
            classes.add(this.typeAttrDisplayInverse);
        }

        if (classes.isEmpty()) {
            return;
        }

        sb.append(" class=\"");
        String space = "";
        for (final String tdClass : classes) {
            sb.append(space).append(tdClass);
            space = " ";
        }
        sb.append('\"');
    }


    private String getPriceQualityClass(PdlObject po) {
        switch(po.getPriceQuality()) {
            case REALTIME:
                return this.typeDataQualityRt;
            case DELAYED:
                return this.typeDataQualityNt;
            default:
                return null;
        }
    }


    private PdlObject[][] createPageMatrix(PdlPage page) {
        final Map<Integer, Integer> columnMapping = new HashMap<>(page.getWidth());
        final Map<Integer, Integer> rowMapping = new HashMap<>(page.getHeight());
        for (final PdlObject pdlObject : page.getObjects()) {
            columnMapping.put(pdlObject.getX(), 0);
            rowMapping.put(pdlObject.getY(), 0);
        }

        final List<Integer> columns = new ArrayList<>(columnMapping.keySet());
        columns.sort(null);
        for (int column = 0; column < columns.size(); column++) {
            columnMapping.put(columns.get(column), column);
        }

        final List<Integer> rows = new ArrayList<>(rowMapping.keySet());
        rows.sort(null);
        for (int row = 0; row < rows.size(); row++) {
            rowMapping.put(rows.get(row), row);
        }

        final PdlObject[][] result = new PdlObject[rows.size()][];
        for (int row = 0; row < rows.size(); row++) {
            result[row] = new PdlObject[columns.size()];
        }
        for (final PdlObject pdlObject : page.getObjects()) {
            result[rowMapping.get(pdlObject.getY())][columnMapping.get(pdlObject.getX())] = pdlObject;
        }

        return result;
    }
}
