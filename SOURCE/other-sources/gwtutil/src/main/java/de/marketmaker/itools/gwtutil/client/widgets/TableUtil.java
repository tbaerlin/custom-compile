package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;

/**
 * Author: umaurer
 * Created: 21.03.14
 */
public class TableUtil {
    public static void setTableHeaders(FlexTable flexTable, String... headerTexts) {
        addTableHeaders(flexTable, headerTexts, null);
    }

    public static void setTableHeaders(FlexTable flexTable, String[] headerTexts, String[] headerStyles) {
        final Element tr = clearOrCreateHeaderRow(flexTable);
        for (int i = 0; i < headerTexts.length; i++) {
            final Element th = DOM.createTH();
            th.setInnerSafeHtml(StringUtility.toHtmlLines(headerTexts[i]));
            if (headerStyles != null && headerStyles[i] != null) {
                th.setClassName(headerStyles[i]);
            }
            tr.appendChild(th);
        }
    }

    public static void addTableHeaders(FlexTable flexTable, String[] headerTexts, String[] headerStyles) {
        final Element tr = createHeaderRow(flexTable);
        for (int i = 0; i < headerTexts.length; i++) {
            final Element th = DOM.createTH();
            th.setInnerSafeHtml(StringUtility.toHtmlLines(headerTexts[i]));
            if (headerStyles != null && headerStyles[i] != null) {
                th.setClassName(headerStyles[i]);
            }
            tr.appendChild(th);
        }
    }

    public static void addTableHeaders(FlexTable flexTable, SafeHtml[] headerTexts, String[] headerStyles) {
        final Element tr = createHeaderRow(flexTable);
        for (int i = 0; i < headerTexts.length; i++) {
            final Element th = DOM.createTH();
            th.setInnerSafeHtml(headerTexts[i]);
            if (headerStyles != null && headerStyles[i] != null) {
                th.setClassName(headerStyles[i]);
            }
            tr.appendChild(th);
        }
    }

    private static Element createHeaderRow(FlexTable flexTable) {
        final Element table = flexTable.getElement();
        final NodeList<Element> theads = table.getElementsByTagName("THEAD");
        final Element tHead;
        if (theads != null && theads.getLength() > 0) {
            tHead = theads.getItem(0);
        } else {
            tHead = DOM.createTHead();
            table.insertFirst(tHead);
        }
        final Element tr = DOM.createTR();
        tHead.appendChild(tr);
        return tr;
    }

    private static Element clearOrCreateHeaderRow(FlexTable flexTable) {
        final Element table = flexTable.getElement();
        final NodeList<Element> theads = table.getElementsByTagName("THEAD");
        final Element tHead;
        if (theads != null && theads.getLength() > 0) {
            tHead = theads.getItem(0);
            final NodeList<Element> trs = tHead.getElementsByTagName("TR");
            if (trs != null && trs.getLength() > 0) {
                final Element tr = trs.getItem(0);
                tr.removeAllChildren();
                return tr;
            }
        } else {
            tHead = DOM.createTHead();
            table.insertFirst(tHead);
        }
        final Element tr = DOM.createTR();
        tHead.appendChild(tr);
        return tr;
    }

    public static class CellIndex {
        private final int row;
        private final int column;

        public CellIndex(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return "CellIndex{" + row + ", " + column + '}';
        }
    }

    public static CellIndex getCellIndex(HTMLTable table, Element td) {
        if (!td.getNodeName().toLowerCase().equals("td")) {
            return null;
        }
        int column = 0;
        Node tdNode = td;
        while (tdNode.getPreviousSibling() != null) {
            tdNode = tdNode.getPreviousSibling();
            if (tdNode.getNodeName().toLowerCase().equals("td")) {
                column++;
            }
        }

        int row = 0;
        Node trNode = tdNode.getParentNode();
        while (trNode.getPreviousSibling() != null) {
            trNode = trNode.getPreviousSibling();
            if (trNode.getNodeName().toLowerCase().equals("tr")) {
                row++;
            }
        }
        return table.getCellFormatter().getElement(row, column) == td
                ? new CellIndex(row, column)
                : null;
    }

    public static TableCellElement getCellElement(HTMLTable table, DomEvent event) {
        final EventTarget eventTarget = event.getNativeEvent().getEventTarget();
        if (eventTarget == null || !Node.is(eventTarget)) {
            return null;
        }
        return getCellElement(table.getElement(), Node.as(eventTarget));
    }

    public static TableCellElement getCellElement(Node tableNode, Node node) {
        if (node == null) {
            return null;
        }
        final Node parentNode = node.getParentNode();
        if (TableCellElement.is(node)) {
            final TableElement table = getParentTableElement(node.getParentNode());
            if (table == null) {
                return null;
            }
            if (table == tableNode) {
                return TableCellElement.as(Element.as(node));
            }
            return getCellElement(tableNode, table.getParentNode());
        }
        return getCellElement(tableNode, parentNode);
    }

    public static TableElement getParentTableElement(Node node) {
        if (node == null) {
            return null;
        }
        if (TableElement.is(node)) {
            return TableElement.as(Element.as(node));
        }
        return getParentTableElement(node.getParentNode());
    }

    public static TableRowElement getRowElement(TableCellElement cellElement) {
        return TableRowElement.as(cellElement.getParentElement());
    }
}
