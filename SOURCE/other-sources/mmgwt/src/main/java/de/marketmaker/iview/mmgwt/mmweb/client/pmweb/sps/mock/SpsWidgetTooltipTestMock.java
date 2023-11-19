/*
 * SpsWidgetTooltipTestMock.java
 *
 * Created on 21.05.2014 15:18
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDataItem;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.ListWidgetDesc;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;
import de.marketmaker.iview.pmxml.MMString;
import de.marketmaker.iview.pmxml.OrderLimitType;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SelectionMode;
import de.marketmaker.iview.pmxml.TiDateKind;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsWidgetTooltipTestMock extends PreProcessHook {
    private final static TiType[] tiTypes = {TiType.TI_STRING, TiType.TI_NUMBER, TiType.TI_BOOLEAN,
            TiType.TI_MEMO, TiType.TI_DATE, TiType.TI_ENUMERATION, TiType.TI_SHELL_MM, TiType.TI_FOLDER};

    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        for (TiType type : tiTypes) {
            addWidgets(s, type);
        }
        addWidgets(s, TiType.TI_ENUMERATION, "combo");
        addListWidgets(s, TiType.TI_STRING, "aListKey", "aList");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();

        for (TiType type : tiTypes) {
            addDeclaration(decl, parent, type);
        }

        addDecl(decl, parent, "aListKey", TiType.TI_STRING, "A list key");
        addListDeclaration(decl, parent, "aList");
    }

    private void addWidgets(SectionDesc parent, TiType type, String... style) {
        final String typeValue = type.value();
        final String bind = "/" + typeValue.replaceFirst("ti", "a");

        if(type == TiType.TI_DATE) {
            addDateWidget(parent, typeValue, bind, style, TiDateKind.DK_DATE, false);
            addDateWidget(parent, typeValue, bind, style, TiDateKind.DK_DATE_TIME, false);
            addDateWidget(parent, typeValue, bind, style, TiDateKind.DK_DATE_TIME, true);
            addDateWidget(parent, typeValue, bind, style, TiDateKind.DK_TIME, false);
            addDateWidget(parent, typeValue, bind, style, TiDateKind.DK_TIME, true);
            return;
        }

        addTooltip(addEditWidget(parent, bind, "A " + typeValue, style), "A " + typeValue + " tooltip");
        addEditWidget(parent, bind, "A " + typeValue + " NO tooltip", style);
        addReadOnlyWidget(parent, bind, "A " + typeValue + " readonly", style);
    }

    private void addDateWidget(SectionDesc parent, String typeValue, String bindBaseName, String[] style, TiDateKind dateKind, boolean withSeconds) {
        final String bind = getDatePropNodeName(bindBaseName, dateKind, withSeconds);
        final String caption = getCaption(typeValue, dateKind, withSeconds);
        addTooltip(addEditWidget(parent, bind, caption, style), getTooltip(typeValue, dateKind, withSeconds));
        addEditWidget(parent, bind, caption + " NO tooltip", style);
        addReadOnlyWidget(parent, bind, caption + " readonly", style);
    }

    private String getTooltip(String typeValue, TiDateKind dateKind, boolean withSeconds) {
        return getCaption(typeValue, dateKind, withSeconds) + " tooltip";
    }

    private String getCaption(String typeValue, TiDateKind dateKind, boolean withSeconds) {
        return "A " + typeValue + " " + dateKind.value() + (withSeconds ? " with seconds " : "");
    }

    private void addListWidgets(SectionDesc parent, TiType type, String bind, String itemsBind, String... style) {
        final String typeValue = type.value();
        final String tooltip = "A " + typeValue + " tooltip";

        bind = "/" + bind;
        itemsBind = "/" + itemsBind;

        addListWidget(parent, bind, itemsBind, "key", "A " + typeValue, tooltip, false, false, style);
        addListWidget(parent, bind, itemsBind, "key", "A " + typeValue + " NO tooltip", null, false, false, style);
        addListWidget(parent, bind, itemsBind, "key", "A " + typeValue + " readonly", tooltip, false, true, style);
    }

    private void addListWidget(SectionDesc parent, String bind, String itemsBind, String keyField, String caption, String tooltip, boolean multiSelect, boolean readonly, String... style) {
        final ListWidgetDesc w = new ListWidgetDesc();
        w.setBind(bind);
        w.setItemsBind(itemsBind);
        w.setKeyField(keyField);
        w.setCaption(caption);
        w.setTooltip(tooltip);
        w.setIsReadonly(readonly);
        w.setSelectionMode(multiSelect ? SelectionMode.SM_ORDERED_MULTI_SELECT : SelectionMode.SM_SINGLE_SELECT);

        final ListWidgetDescColumn col = new ListWidgetDescColumn();
        col.setColumnName("Value");
        col.setFieldName("value");
        w.getColumns().add(col);

        if(style != null && style.length > 0) {
            final StringBuilder sb = new StringBuilder();
            for (String s : style) {
                sb.append(s);
                sb.append(' ');
            }
            sb.delete(sb.length() - 1, sb.length() - 1);
            w.setStyle(sb.toString());
        }
        addToParent(w, parent);
    }

    private void addDeclaration(boolean decl, DataContainerCompositeNode parent, TiType type) {
        final String nodeName = type.value().replaceFirst("ti", "a");

        if(type == TiType.TI_DATE) {
            addDateDecl(decl, parent, type, nodeName, TiDateKind.DK_DATE, false);
            addDateDecl(decl, parent, type, nodeName, TiDateKind.DK_DATE_TIME, false);
            addDateDecl(decl, parent, type, nodeName, TiDateKind.DK_DATE_TIME, true);
            addDateDecl(decl, parent, type, nodeName, TiDateKind.DK_TIME, false);
            addDateDecl(decl, parent, type, nodeName, TiDateKind.DK_TIME, true);
            return;
        }

        if(type == TiType.TI_NUMBER) {
            final ParsedTypeInfo parsedTypeInfo = new ParsedTypeInfo();
            parsedTypeInfo.setTypeId(type);
            parsedTypeInfo.setDescription("A fallback " + type.value() + " tooltip");
            parsedTypeInfo.setVUnit("100");

            addDecl(decl, parent, nodeName, parsedTypeInfo);
        }

        addDecl(decl, parent, nodeName, type, "A fallback " + type.value() + " tooltip");
    }

    private void addDateDecl(boolean decl, DataContainerCompositeNode parent, TiType type, String nodeBaseName, TiDateKind dateKind, boolean withSeconds) {
        final String nodeName;
        nodeName = getDatePropNodeName(nodeBaseName, dateKind, withSeconds);
        final String dateTimePropDescription = getDateTimePropDescription(type, dateKind, withSeconds);
        addDecl(decl, parent, nodeName, dateKind, withSeconds, dateTimePropDescription);
    }

    private String getDatePropNodeName(String nodeBaseName, TiDateKind dateKind, boolean withSeconds) {
        return nodeBaseName + dateKind.value() + Boolean.toString(withSeconds);
    }

    private String getDateTimePropDescription(TiType type, TiDateKind dateKind, boolean withSeconds) {
        return "A fallback " + type.value() + " " + dateKind.value() + (withSeconds ? " with seconds" : "") + " tooltip";
    }

    private void addListDeclaration(boolean decl, DataContainerCompositeNode parent, String listNodeName) {
        final List<DataContainerNode> children = parent.getChildren();

        final DataContainerListNode ln = new DataContainerListNode();
        final List<DataContainerNode> cs = ln.getChildren();
        ln.setNodeLevelName(listNodeName);
        children.add(ln);

        if(decl) {
            final DataContainerNode n = createElementNode(true, null, null);
            n.setNodeLevelName("[0]");
            cs.add(n);
        }
        else {
            int i = 0;
            for (OrderLimitType type : OrderLimitType.values()) {
                final DataContainerNode n = createElementNode(false, type.value(), type.value().replaceFirst("olt", ""));
                n.setNodeLevelName("[" + i++ + "]");
                cs.add(n);
            }
        }
    }

    private DataContainerNode createElementNode(boolean decl, String key, String value) {
        final DataContainerCompositeNode g = new DataContainerGroupNode();
        final List<DataContainerNode> cs = g.getChildren();

        if(decl) {
            addListGroupLeafNodeDecl(cs, "key");
            addListGroupLeafNodeDecl(cs, "value");
        }
        else {
            addListGroupLeafNode(cs, "key", key);
            addListGroupLeafNode(cs, "value", value);
        }

        return g;
    }

    private void addListGroupLeafNode(List<DataContainerNode> cs, String nodeName, String value) {
        final DataContainerLeafNodeDataItem l = new DataContainerLeafNodeDataItem();
        l.setNodeLevelName(nodeName);
        final MMString di = new MMString();
        di.setValue(value);
        l.setDataItem(di);
        cs.add(l);
    }

    private void addListGroupLeafNodeDecl(List<DataContainerNode> cs, String name) {
        final DataContainerLeafNodeDeclaration vd = new DataContainerLeafNodeDeclaration();
        vd.setDescription(createParsedTypeInfo(name, "The " + name));
        vd.setNodeLevelName(name);
        cs.add(vd);
    }

    private ParsedTypeInfo createParsedTypeInfo(String displayName, String description) {
        final ParsedTypeInfo pti = new ParsedTypeInfo();
        pti.setTypeId(TiType.TI_STRING);
        pti.setDisplayName(displayName);
        pti.setDescription(description);
        return pti;
    }
}
