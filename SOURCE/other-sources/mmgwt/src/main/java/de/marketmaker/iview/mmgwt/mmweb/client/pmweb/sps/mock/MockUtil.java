/*
 * MockUtil.java
 *
 * Created on 20.05.2014 09:08
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AnalysisControlDesc;
import de.marketmaker.iview.pmxml.BoundWidgetDesc;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDataItem;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.EditWidgetWithObjectDesc;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ErrorSeverity;
import de.marketmaker.iview.pmxml.LabelWidgetDesc;
import de.marketmaker.iview.pmxml.ListWidgetDesc;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMBool;
import de.marketmaker.iview.pmxml.MMDBRef;
import de.marketmaker.iview.pmxml.MMDateTime;
import de.marketmaker.iview.pmxml.MMIndexedString;
import de.marketmaker.iview.pmxml.MMNumber;
import de.marketmaker.iview.pmxml.MMString;
import de.marketmaker.iview.pmxml.OrderLimitType;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.PmxmlConstants;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SectionDescColumn;
import de.marketmaker.iview.pmxml.SectionListDesc;
import de.marketmaker.iview.pmxml.SelectionMode;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.SortMode;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.TiDateKind;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.pmxml.WidgetDescDependency;
import de.marketmaker.iview.pmxml.WidgetInputAction;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Arrays;
import java.util.List;

/**
 * @author Markus Dick
 */
@NonNLS
public class MockUtil {
    public static final ShellMMInfo BASF11 = shellMMInfo("0", ShellMMType.ST_AKTIE, "BASF11", "DE000BASF111", "BASF SE", "90577300");
    public static final ShellMMInfo BAY001 = shellMMInfo("1", ShellMMType.ST_AKTIE, "BAY001", "DE000BAY001", "Bayer AG", "55299629");
    public static final ShellMMInfo LED400 = shellMMInfo("2", ShellMMType.ST_AKTIE, "LED400", "DE000LED400", "OSRAM Licht AG", "169762312");
    public static final ShellMMInfo INVESTOR_TEST_HA = shellMMInfo("20473714", ShellMMType.ST_INHABER, "1234567890", null, "Test HA (pmweb)", null);
    public static final ShellMMInfo PORTFOLIO_TEST_HA = shellMMInfo("20473715", ShellMMType.ST_PORTFOLIO, "98765432123456789y", null, "Test HA (pmweb)", null);

    public static EditWidgetDesc addEditWidget(SectionDesc parent, String bind, String caption, String... style) {
        return addEditWidgetTooltip(parent, bind, caption, "", style);
    }

    public static EditWidgetWithObjectDesc addEditWidgetWithObject(SectionDesc parent, String bind, String objectBind, String caption, String... style) {
        return addEditWidgetWithObjectTooltip(parent, bind, objectBind, caption, "", style);
    }

    public static EditWidgetDesc addEditWidgetTooltip(SectionDesc parent, String bind, String caption, String tooltip, String... style) {
        return addToParent(createEditWidgetDesc(bind, caption, tooltip, style), parent);
    }

    public static EditWidgetDesc createEditWidgetDesc(String bind, String caption, String tooltip, String... style) {
        final EditWidgetDesc w = new EditWidgetDesc();
        initEditWidget(bind, caption, tooltip, w, style);
        return w;
    }

    public static EditWidgetDesc createEditWidgetDesc(String bind, String caption) {
        return createEditWidgetDesc(bind, caption, null);
    }

    public static EditWidgetWithObjectDesc addEditWidgetWithObjectTooltip(SectionDesc parent, String bind, String objectBind, String caption, String tooltip, String... style) {
        return addToParent(createEditWidgetWithObjectDesc(bind, objectBind, caption, tooltip, style), parent);
    }

    public static EditWidgetWithObjectDesc createEditWidgetWithObjectDesc(String bind, String objectBind, String caption) {
        return createEditWidgetWithObjectDesc(bind, objectBind, caption, null);
    }

    public static EditWidgetWithObjectDesc createEditWidgetWithObjectDesc(String bind, String objectBind, String caption, String tooltip, String... style) {
        final EditWidgetWithObjectDesc w = new EditWidgetWithObjectDesc();
        initEditWidget(bind, caption, tooltip, w, style);
        w.setObjectBind(objectBind);
        return w;
    }

    private static void initEditWidget(String bind, String caption, String tooltip, EditWidgetDesc w, String[] style) {
        w.setBind(bind);
        w.setCaption(caption);
        w.setTooltip(tooltip);
        if(style != null && style.length > 0) {
            final StringBuilder sb = new StringBuilder();
            for (String s : style) {
                sb.append(s);
                sb.append(' ');
            }
            sb.delete(sb.length() - 1, sb.length() - 1);
            w.setStyle(sb.toString());
        }
    }

    public static void addReadOnlyWidget(SectionDesc parent, String bind, String caption, String... style) {
        addToParent(readonly(createEditWidgetDesc(bind, caption, null, style)), parent);
    }

    public static <T extends BoundWidgetDesc> T readonly(T widgetDesc) {
        widgetDesc.setIsReadonly(true);
        return widgetDesc;
    }

    public static <T extends BoundWidgetDesc> T bind(String bind, T widgetDesc) {
        widgetDesc.setBind(bind);
        return widgetDesc;
    }

    public static <T extends BoundWidgetDesc> T addTooltip(T widgetDesc, String tooltip) {
        widgetDesc.setTooltip(tooltip);
        return widgetDesc;
    }

    public static <T extends ParsedTypeInfo> T mandatory(T pti) {
        pti.setDemanded(true);
        return pti;
    }

    public static void addEnum(boolean decl, DataContainerCompositeNode parent, String nodeName, MMIndexedString... enums) {
        final DataContainerLeafNode leafNode;

        if(decl) {
            final ParsedTypeInfo p = new ParsedTypeInfo();
            p.setTypeId(TiType.TI_ENUMERATION);
            p.getEnumElements().addAll(Arrays.asList(enums));

            final DataContainerLeafNodeDeclaration d = new DataContainerLeafNodeDeclaration();
            d.setNodeLevelName(nodeName);
            d.setDescription(p);

            leafNode = d;
        }
        else {
            final DataContainerLeafNodeDataItem leafData = new DataContainerLeafNodeDataItem();
            leafData.setNodeLevelName(nodeName);
            leafData.setDataItem(new DefaultMM());
            leafNode = leafData;
        }

        parent.getChildren().add(leafNode);
    }

    public static MMIndexedString e(String value, String code) {
        final MMIndexedString enumElement = new MMIndexedString();
        enumElement.setValue(value);
        enumElement.setCode(code);
        return enumElement;
    }

    public static ParsedTypeInfo pti(TiType typeId) {
        return pti(typeId, null);
    }

    public static ParsedTypeInfo pti(boolean perCent, String vUnit) {
        final ParsedTypeInfo pti = pti(TiType.TI_NUMBER, null);
        pti.setNumberProcent(perCent);
        pti.setVUnit(vUnit);
        return pti;
    }

    public static ParsedTypeInfo pti(TiType typeId, String description) {
        final ParsedTypeInfo pti = new ParsedTypeInfo();
        pti.setTypeId(typeId);
        pti.setDescription(description);
        return pti;
    }

    public static ParsedTypeInfo pti(MMIndexedString... enumElements) {
        return pti(null, null, enumElements);
    }

    public static ParsedTypeInfo pti(String enumNullValue, MMIndexedString... enumElements) {
        return pti(null, enumNullValue, enumElements);
    }

    public static ParsedTypeInfo pti(String description, String enumNullValue, MMIndexedString... enumElements) {
        final ParsedTypeInfo pti = pti(TiType.TI_ENUMERATION, description);
        pti.getEnumElements().addAll(Arrays.asList(enumElements));
        pti.setEnumerationNullValue(enumNullValue);
        return pti;
    }

    public static void addDecl(boolean decl, DataContainerCompositeNode parent, String nodeName, TiType type) {
        if(parent == null) {
            return;
        }

        addDecl(decl, parent, nodeName, type, "");
    }

    public static void addDecl(boolean decl, DataContainerCompositeNode parent, String nodeName, TiType type, String description) {
        if(parent == null) {
            return;
        }

        final ParsedTypeInfo parsedTypeInfo = new ParsedTypeInfo();
        parsedTypeInfo.setTypeId(type);
        parsedTypeInfo.setDescription(description);

        addDecl(decl, parent, nodeName, parsedTypeInfo);
    }

    public static void addDecl(boolean decl, DataContainerCompositeNode parent, String nodeName, TiDateKind dateKind, boolean timeWithSeconds, String description) {
        if(parent == null) {
            return;
        }

        final ParsedTypeInfo parsedTypeInfo = new ParsedTypeInfo();
        parsedTypeInfo.setTypeId(TiType.TI_DATE);
        parsedTypeInfo.setDescription(description);
        parsedTypeInfo.setDateKind(dateKind);
        parsedTypeInfo.setIsTimeSeconds(timeWithSeconds);

        addDecl(decl, parent, nodeName, parsedTypeInfo);
    }

    public static void addDecl(boolean decl, DataContainerCompositeNode parent, String nodeName, ParsedTypeInfo parsedTypeInfo) {
        if(parent == null) {
            return;
        }

        switch(parsedTypeInfo.getTypeId()) {
            case TI_DATE:
                final MMDateTime dt = new MMDateTime();
                dt.setValue(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(PmxmlConstants.ZERO_DATE));
                addDecl(decl, parent, nodeName, parsedTypeInfo, dt);
                break;

            default:
                addDecl(decl, parent, nodeName, parsedTypeInfo, new DefaultMM());
        }
    }

    public static void addDecl(boolean decl, DataContainerCompositeNode parent, String nodeName, ParsedTypeInfo parsedTypeInfo, MM dataItem) {
        if(parent == null) {
            return;
        }

        final DataContainerLeafNode leafNode;

        if(decl) {
            switch(parsedTypeInfo.getTypeId()) {
                case TI_ENUMERATION:
                    if(parsedTypeInfo.getEnumElements().isEmpty()) {
                        createEnumElements(parsedTypeInfo.getEnumElements());
                    }
                    break;
            }

            final DataContainerLeafNodeDeclaration d = new DataContainerLeafNodeDeclaration();
            d.setNodeLevelName(nodeName);
            d.setDescription(parsedTypeInfo);

            leafNode = d;
        }
        else {
            final DataContainerLeafNodeDataItem leafData = new DataContainerLeafNodeDataItem();
            leafData.setNodeLevelName(nodeName);
            leafData.setDataItem(dataItem);

            leafNode = leafData;
        }

        parent.getChildren().add(leafNode);
    }

    private static void createEnumElements(List<MM> elements) {
        for (OrderLimitType type : OrderLimitType.values()) {
            final String value = type.value();
            final MMIndexedString element = new MMIndexedString();
            element.setCode(value);
            element.setValue(value.replaceFirst("olt", ""));
            elements.add(element);
        }
    }

    public static void addShellMMInfo(boolean decl, String nodeName, TiType type, ShellMMType[] shellMMTypes, DataContainerCompositeNode parent) {
        if(parent == null) {
            return;
        }
        addShellMMInfo(decl, nodeName, type, shellMMTypes, parent, new DefaultMM());
    }

    public static void addShellMMInfo(boolean decl, String nodeName, TiType type, ShellMMType[] shellMMTypes, DataContainerCompositeNode parent, MM dataItem) {
        if(parent == null) {
            return;
        }
        final DataContainerLeafNode leafNode;
        if(decl) {
            final ParsedTypeInfo info = new ParsedTypeInfo();
            info.setTypeId(type);
            info.getFolderTypes().addAll(Arrays.asList(shellMMTypes));

            final DataContainerLeafNodeDeclaration leafDecl = new DataContainerLeafNodeDeclaration();
            leafDecl.setNodeLevelName(nodeName);
            leafDecl.setDescription(info);
            leafNode = leafDecl;
        }
        else {
            final DataContainerLeafNodeDataItem leafData = new DataContainerLeafNodeDataItem();
            leafData.setNodeLevelName(nodeName);
            leafData.setDataItem(dataItem);
            leafNode = leafData;
        }

        parent.getChildren().add(leafNode);
    }

    public static LabelWidgetDesc addLabelWidget(SectionDesc parent, String staticText) {
        return addToParent(createLabelWidgetDesc(staticText, null, null), parent);
    }

    public static LabelWidgetDesc addLabelWidget(SectionDesc parent, String staticText, String bind) {
        return addToParent(createLabelWidgetDesc(staticText, bind, null), parent);
    }

    public static LabelWidgetDesc addLabelWidget(SectionDesc parent, String s, String bind, String caption, String... styles) {
        return addToParent(createLabelWidgetDesc(s, bind, caption, styles), parent);
    }

    public static LabelWidgetDesc createLabelWidgetDesc(String staticText, String bind, String caption, String... styles) {
        final LabelWidgetDesc w = new LabelWidgetDesc();
        w.setText(staticText);
        w.setBind(bind);
        w.setStyle(StringUtil.join(' ', styles));
        w.setCaption(caption);
        return w;
    }

    public static SectionDesc addSectionWidget(SectionDesc parent, String caption, String... styles) {
        final SectionDesc sectionDesc = new SectionDesc();
        sectionDesc.setCaption(caption);
        sectionDesc.setStyle(StringUtil.join(' ', styles));

        return addToParent(sectionDesc, parent);
    }

    public static <T extends WidgetDesc> T addToParent(T widgetDesc, SectionDesc parent) {
        if(parent != null) {
            parent.getItems().add(widgetDesc);
            widgetDesc.setId("" + parent.getId() + "-" + parent.getItems().size());
        }
        return widgetDesc;
    }

    static MMString mmString(String s) {
        final MMString mm = new MMString();
        mm.setValue(s);
        return mm;
    }

    static MMBool mmBool(boolean b) {
        final MMBool mm = new MMBool();
        mm.setValue(b ? ThreeValueBoolean.TV_TRUE : ThreeValueBoolean.TV_FALSE);
        return mm;
    }

    public static MM mmDbRef(String name) {
        final MMDBRef mm = new MMDBRef();
        mm.setValue(name);
        mm.setCode(name);
        return mm;
    }

    public static MM mmIndexedString(String name, String code) {
        return e(name, code);
    }

    public static MMNumber mmNumber(String s) {
        final MMNumber mmNumber = new MMNumber();
        mmNumber.setValue(s);
        return mmNumber;
    }

    public static MMNumber mmNumber(Number n) {
        final MMNumber mmNumber = new MMNumber();
        mmNumber.setValue(n.toString());
        return mmNumber;
    }

    static ShellMMInfo shellMMInfo(String objectId, ShellMMType type, String number, String isin, String name, String iidWithoutSuffix) {        final ShellMMInfo shellMMInfo = new ShellMMInfo();
        shellMMInfo.setId(objectId);
        shellMMInfo.setNumber(number);
        shellMMInfo.setISIN(isin);
        shellMMInfo.setBezeichnung(name);
        shellMMInfo.setTyp(type);
        shellMMInfo.setMMSecurityID(iidWithoutSuffix);

        return shellMMInfo;
    }

    public static ParsedTypeInfo pti(TiType typeId, String displayName, boolean percent, boolean mandatory, String min, String max, String vUnit, String numberSpin, int memoCharacterLimit, String description) {
        final ParsedTypeInfo pti = new ParsedTypeInfo();
        pti.setTypeId(typeId);
        pti.setDisplayName(displayName);
        pti.setDescription(description);
        pti.setNumberProcent(percent);
        pti.setDemanded(mandatory);
        pti.setMin(min);
        pti.setMax(max);
        pti.setVUnit(vUnit);
        pti.setNumberSpin(numberSpin);
        pti.setMemoCharacterLimit(Integer.toString(memoCharacterLimit));
        return pti;
    }

    public static ParsedTypeInfo booleanPti(boolean demanded, boolean booleanIsKindOption) {
        final ParsedTypeInfo pti = pti(TiType.TI_BOOLEAN);
        pti.setDemanded(demanded);
        pti.setBooleanIsKindOption(booleanIsKindOption);
        return pti;
    }

    public static ListWidgetDesc addListWidget(SectionDesc parent, String bind, String itemsBind, String key, String caption, String... style) {
        return addListWidget(parent, bind, itemsBind, key, SelectionMode.SM_SINGLE_SELECT, caption, style);
    }

    public static ListWidgetDesc addListWidget(SectionDesc parent, String bind, String itemsBind, String keyColumn, SelectionMode smSingleSelect, String caption, String... style) {
        final ListWidgetDesc list = new ListWidgetDesc();
        list.setSelectionMode(smSingleSelect);
        list.setKeyField(keyColumn);
        list.setBind(bind);
        list.setItemsBind(itemsBind);
        list.setCaption(caption);
        list.setStyle(StringUtil.join(' ', style));
        return addToParent(list, parent);
    }

    public static ListWidgetDescColumn addListWidgetColumn(ListWidgetDesc listWidgetDesc, String fieldName, String columnName, String... columnStyle) {
        return addListWidgetColumn(listWidgetDesc, fieldName, "0", SortMode.SM_ASCENDING, columnName, columnStyle);
    }

    public static ListWidgetDescColumn addListWidgetColumn(ListWidgetDesc listWidgetDesc, String fieldName, String sortIndex, SortMode sortMode, String columnName, String... columnStyle) {
        final ListWidgetDescColumn c = new ListWidgetDescColumn();
        c.setColumnName(columnName);
        c.setColumnStyle(StringUtil.join(' ', columnStyle));
        c.setFieldName(fieldName);
        c.setSortIndex(sortIndex);
        c.setSortMode(sortMode);
        listWidgetDesc.getColumns().add(c);
        return c;
    }

    public static WidgetDescDependency addDependency(WidgetDesc targetWidget, String bind, WidgetInputAction action) {
        return addDependency(targetWidget, bind, action, null);
    }

    public static WidgetDescDependency addDependency(WidgetDesc targetWidget, String bind, WidgetInputAction action, String condition) {
        final WidgetDescDependency wdd = new WidgetDescDependency();
        wdd.setBind(bind);
        wdd.setAction(action);
        wdd.setCondition(condition);

        targetWidget.getDependencies().add(wdd);
        return wdd;
    }

    public static DataContainerGroupNode createDmsGroupNode(boolean decl, String nodeLevelName, String displayNameSuffix, boolean sufficientRights, String fileName, String dmsHandle) {
        final DataContainerGroupNode group = new DataContainerGroupNode();
        group.setNodeLevelName(nodeLevelName);

        addDecl(decl, group, "Handle", pti(TiType.TI_STRING, "The Handle"), mmString(dmsHandle));

        addDecl(decl, group, "DisplayName", pti(TiType.TI_STRING, "The DisplayName"),
                mmString(fileName + displayNameSuffix));

        addDecl(decl, group, "DocumentType", pti(TiType.TI_STRING, "The DocumentType"),
                mmString("pdf"));

        addDecl(decl, group, "HasSufficientRights", pti(TiType.TI_BOOLEAN, "The HasSufficientRights"),
                mmBool(sufficientRights));

        return group;
    }

    public static DataContainerGroupNode addGroup(DataContainerCompositeNode parent, String nodeLevelName) {
        if(parent == null) {
            return null;
        }

        final DataContainerGroupNode group = new DataContainerGroupNode();
        group.setNodeLevelName(nodeLevelName);
        parent.getChildren().add(group);
        return group;
    }

    public static DataContainerListNode addList(DataContainerCompositeNode parent, String nodeLevelName) {
        if(parent == null) {
            return null;
        }

        final DataContainerListNode list = new DataContainerListNode();
        list.setNodeLevelName(nodeLevelName);
        parent.getChildren().add(list);
        return list;
    }

    public static DataContainerGroupNode addGroupToList(boolean decl, DataContainerListNode parent) {
        if(decl && parent.getChildren().size() >= 1) {
            return null;
        }

        final DataContainerGroupNode entry = new DataContainerGroupNode();
        parent.getChildren().add(entry);
        return entry;
    }

    public static SectionListDesc addSectionListWidget(SectionDesc parent, String listBind, SectionDesc editSection, String caption, String... styles) {
        final SectionListDesc sectionListDesc = new SectionListDesc();
        sectionListDesc.setCaption(caption);
        sectionListDesc.setBind(listBind);
        sectionListDesc.setEditSection(editSection);
        sectionListDesc.setStyle(StringUtil.join(' ', styles));

        addToParent(sectionListDesc, parent);

        return sectionListDesc;
    }

    @SuppressWarnings("unused")
    public static SectionDescColumn addSectionWidgetColumn(SectionDesc section, String... styles) {
        final SectionDescColumn sectionDescColumn = new SectionDescColumn();
        sectionDescColumn.setStyle(StringUtil.join(' ', styles));
        section.getColumns().add(sectionDescColumn);
        return sectionDescColumn;
    }

    public static AnalysisControlDesc addAnalysisWidget(SectionDesc parent, String bind, String caption, boolean useAsyncHandle, String... styles) {
        final AnalysisControlDesc desc = new AnalysisControlDesc();
        desc.setBind(bind);
        desc.setCaption(caption);
        desc.setStyle(StringUtil.join(' ', styles));
        desc.setUseAsyncHandle(useAsyncHandle);
        parent.getItems().add(desc);
        return desc;
    }

    public static SectionDesc prepareRootSectionWidget(WidgetDesc wd) {
        final SectionDesc s = (SectionDesc)wd;
        s.setId("root");
        s.setBind(null);
        s.setCaption(null);
        s.setDescription(null);
        s.setTooltip(null);
        s.setStyle(null);
        s.getItems().clear();
        return s;
    }

    public static DataContainerCompositeNode prepareRootNode(DataContainerNode dcn) {
        final DataContainerCompositeNode dccn = (DataContainerCompositeNode) dcn;
        dccn.getChildren().clear();
        return dccn;
    }

    public static ErrorMM errorMM(String correlationSource, String correlationTarget, String errorString) {
        return errorMM(ErrorSeverity.ESV_ERROR, correlationSource, correlationTarget, errorString);
    }

    public static ErrorMM errorMM(ErrorSeverity errorSeverity, String correlationSource, String correlationTarget, String errorString) {
        final ErrorMM errorMM = new ErrorMM();
        errorMM.setCorrelationSource(correlationSource);
        errorMM.setCorrelationTarget(correlationTarget);
        errorMM.setErrorCode("mockErrorCode");
        errorMM.setErrorSeverity(errorSeverity);
        errorMM.setErrorString(errorString);
        return errorMM;
    }
}
