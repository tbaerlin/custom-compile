/*
 * ActivityLogUtil.java
 *
 * Created on 13.11.2014 15:16
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCompositeProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ListUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ActivityInstanceResponse;
import de.marketmaker.iview.pmxml.ActivityTask;
import de.marketmaker.iview.pmxml.AnalysisControlDesc;
import de.marketmaker.iview.pmxml.BoundWidgetDesc;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDataItem;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.EditWidgetWithObjectDesc;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.FormDesc;
import de.marketmaker.iview.pmxml.LabelWidgetDesc;
import de.marketmaker.iview.pmxml.ListWidgetDesc;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SectionDescColumn;
import de.marketmaker.iview.pmxml.SectionListDesc;
import de.marketmaker.iview.pmxml.SelectionMode;
import de.marketmaker.iview.pmxml.SubmitAction;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.pmxml.WidgetDescDependency;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Collection;
import java.util.List;

/**
 * @author mdick
 */
@SuppressWarnings({"Convert2streamapi"}) // due to source compatibility with 5_30/as 1.30 do not introduce diamonds, lambdas, or streams here. TODO: remove annotation after cherry pick into 1.30
@NonNLS
public class ActivityLogUtil {
    private ActivityLogUtil() {
        //nothing to do
    }

    public static void logForm(String title, String taskId, FormDesc formDesc, List<SubmitAction> submitCapabilities, DataContainerCompositeNode dataDeclRoot, DataContainerCompositeNode dataRoot, List<ErrorMM> taskErrors) {
        final SectionDesc rootSection = formDesc.getRoot();
        Firebug.groupStart(title + " \"" + rootSection.getCaption() + "\"  taskId: " + taskId);
        logAsGroup("Form Desc", formDesc); // $NON-NLS$
        logAsGroup("Decl " + formatSubmitCapabiblities(submitCapabilities), dataDeclRoot); // $NON-NLS$
        logAsGroup("Data", dataRoot); // $NON-NLS$
        if (!ListUtil.isEmpty(taskErrors)) {
            logAsGroup("Errors", taskErrors); // $NON-NLS$
        }
        Firebug.groupEnd();
    }

    private static String formatSubmitCapabiblities(List<SubmitAction> submitCapabilities) {
        final StringBuilder sb = new StringBuilder();
        String divider = "(";
        for (SubmitAction action : submitCapabilities) {
            sb.append(divider).append(action.value());
            divider = ", ";
        }
        sb.append(')');
        return sb.toString();
    }


    public static void logAsGroup(String groupName, DataContainerNode node) {
        Firebug.groupStart(groupName);
        addToLog(null, -1, node, true);
        Firebug.groupEnd();
    }

    public static void logAsGroup(String groupName, FormDesc fd) {
        Firebug.groupStart(groupName);
        addToLog(fd.getRoot(), 0, null);
        Firebug.groupEnd();
    }

    public static void logAsGroup(String groupName, SpsProperty p) {
        Firebug.groupStart(groupName);
        addToLog(p);
        Firebug.groupEnd();
    }

    public static void logAsGroup(String groupName, Collection<? extends MM> mms) {
        try {
            Firebug.groupStart(groupName);
            for (MM mm : mms) {
                Firebug.log(MmTalkHelper.toLogString(mm));
            }
        }
        finally {
            Firebug.groupEnd();
        }
    }

    @SuppressWarnings("unused")
    public static void logErrors(ActivityInstanceResponse air) {
        boolean groupAdded = false;
        try {
            for (ActivityTask activityTask : air.getTasks()) {
                final List<ErrorMM> errors = activityTask.getErrors();
                if (errors != null && !errors.isEmpty()) {
                    if (!groupAdded) {
                        groupAdded = true;
                        Firebug.groupStart("SPS Activity Errors (" + air.getInfo().getDefinition().getName() + ")");
                    }
                    logAsGroup(activityTask.getName(), activityTask.getErrors());
                }
            }
        }
        finally {
            if(groupAdded) {
                Firebug.groupEnd();
            }
        }
    }

    private static void addToLog(WidgetDesc wd, int level) {
        if (wd instanceof SectionDesc) {
            addToLog((SectionDesc) wd, level, null);
        }
        else if (wd instanceof SectionListDesc) {
            addToLog((SectionListDesc) wd, level);
        }
        else if (wd instanceof ListWidgetDesc) {
            addToLog((ListWidgetDesc) wd, level);
        }
        else {
            if(!wd.getDependencies().isEmpty()) {
                Firebug.groupStart(getDescription(wd, level));
                logDependencies(wd);
                Firebug.groupEnd();
            }
            else {
                Firebug.log(getDescription(wd, level));
            }
        }
    }

    private static void addToLog(SectionDesc sd, int level, String descriptionPrefix) {
        if(sd == null) {
            if(descriptionPrefix != null) {
                Firebug.log(descriptionPrefix + "--");
            }
            return;
        }
        Firebug.groupStart(descriptionPrefix != null ? descriptionPrefix + getDescription(sd, level) : getDescription(sd, level));
        if (sd.getColumns() != null && !sd.getColumns().isEmpty()) {
            Firebug.groupStart("columns");
            for (SectionDescColumn column : sd.getColumns()) {
                Firebug.log("caption=\"" + column.getCaption() + "\" style=\"" + column.getStyle() + "\"");
            }
            Firebug.groupEnd();
        }
        for (WidgetDesc childWd : sd.getItems()) {
            addToLog(childWd, level + 1);
        }
        logDependencies(sd);
        Firebug.groupEnd();
    }

    private static void addToLog(SectionListDesc wd, int level) {
        Firebug.groupStart(getDescription(wd, level));
        addToLog(wd.getEditSection(), level + 1, "editSection: ");
        addToLog(wd.getFooterSection(), level + 1, "footerSection: ");
        if(wd.getGroupOrder() != null && !wd.getGroupOrder().isEmpty()) {
            logAsGroup("groupOrder", wd.getGroupOrder());
        }
        logDependencies(wd);
        Firebug.groupEnd();
    }

    private static void addToLog(ListWidgetDesc wd, int level) {
        Firebug.groupStart(getDescription(wd, level));
        if (wd.getColumns() != null && !wd.getColumns().isEmpty()) {
            for (ListWidgetDescColumn column : wd.getColumns()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("columnName=\"").append(column.getColumnName()) 
                        .append("\" fieldName=\"").append(column.getFieldName()) 
                        .append("\" sortMode=\"").append(column.getSortMode())
                        .append("\" sortIndex=\"").append(column.getSortIndex());
                if (column.getColumnStyle() != null) {
                    sb.append("\" columnStyle=\"").append(column.getColumnStyle());
                }
                sb.append("\"");
                Firebug.log(sb.toString());
            }
        }
        logDependencies(wd);
        Firebug.groupEnd();
    }

    private static String getDescription(WidgetDesc wd, int level) {
        final StringBuilder sb = new StringBuilder();
        sb.append(wd.getClass().getSimpleName());
        appendParameter(sb, "id", wd.getId()); 
        appendParameter(sb, "style", wd.getStyle()); 
        appendParameter(sb, "tag", wd.getTag());
        if (wd instanceof BoundWidgetDesc) {
            final BoundWidgetDesc bwd = (BoundWidgetDesc) wd;
            appendParameter(sb, "bind", bwd.getBind()); 
            if (bwd instanceof EditWidgetWithObjectDesc) {
                appendParameter(sb, "objectBind", ((EditWidgetWithObjectDesc) bwd).getObjectBind()); 
            }
            if (bwd instanceof AnalysisControlDesc) {
                appendParameter(sb, "useAsyncHandle", Boolean.toString(((AnalysisControlDesc) bwd).isUseAsyncHandle()));
            }
            if (bwd.isIsReadonly()) {
                appendParameter(sb, "readonly", "true"); 
            }
            appendParameter(sb, "tooltip", bwd.getTooltip());  
        }
        if (wd instanceof SectionDesc) {
            final SectionDesc sd = (SectionDesc) wd;
            appendParameter(sb, "caption", sd.getCaption()); 
            appendParameter(sb, "description", sd.getDescription()); 
            appendParameter(sb, "descriptionIcon", sd.getDescriptionIcon());
            // As negotiated with PM core, only the child sections (level 1) of the root section (level 0) may have
            // exactly one Section marked as fixedSection.
            if(level == 1) {
                appendParameter(sb, "isFixedSection", Boolean.toString(sd.isIsFixedSection()));
            }
        }
        if (wd instanceof SectionListDesc) {
            final SectionListDesc sld = (SectionListDesc) wd;
            appendParameter(sb, "caption", sld.getCaption()); 
            appendParameter(sb, "description", sld.getDescription()); 
            appendParameter(sb, "descriptionIcon", sld.getDescriptionIcon()); 
            appendParameter(sb, "addTooltip", sld.getTooltipAdd());  
            appendParameter(sb, "deleteTooltip", sld.getTooltipDelete());
            appendParameter(sb, "removeDefaultListEntries", Boolean.toString(sld.isRemoveDefaultListEntries()));
        }
        if (wd instanceof EditWidgetDesc) {
            appendParameter(sb, "caption", ((EditWidgetDesc) wd).getCaption()); 
        }
        if (wd instanceof ListWidgetDesc) {
            appendParameter(sb, "itemsBind", ((ListWidgetDesc) wd).getItemsBind()); 
            appendParameter(sb, "keyField", ((ListWidgetDesc) wd).getKeyField());
            final SelectionMode selectionMode = ((ListWidgetDesc) wd).getSelectionMode();
            appendParameter(sb, "selectionMode", selectionMode != null ? selectionMode.value() : null);
            appendParameter(sb, "caption", ((ListWidgetDesc) wd).getCaption()); 
            appendParameter(sb, "description", ((ListWidgetDesc) wd).getDescription());
        }
        if (wd instanceof LabelWidgetDesc) {
            final LabelWidgetDesc lwd = (LabelWidgetDesc) wd;
            appendParameter(sb, "caption", lwd.getCaption()); 
            appendParameter(sb, "text", lwd.getText());  
            appendParameter(sb, "tooltip", lwd.getTooltip());  
            appendParameter(sb, "iconNameBind", lwd.getIconNameBind());  
        }
        return sb.toString();
    }

    private static void logDependencies(WidgetDesc wd) {
        if(wd.getDependencies().isEmpty()) {
            return;
        }
        for (WidgetDescDependency dependency : wd.getDependencies()) {
            final StringBuilder sb = new StringBuilder("WidgetDescDependency ");  
            appendParameter(sb, "action", "" + dependency.getAction());  
            appendParameter(sb, "bind", dependency.getBind());
            if(StringUtil.hasText(dependency.getCondition())) {
                appendParameter(sb, "condition", dependency.getCondition());
            }
            Firebug.log(sb.toString());
        }
    }

    private static void appendParameter(StringBuilder sb, String key, String value) {
        if (StringUtil.hasText(value)) {
            sb.append(' ').append(key).append("=\"").append(value).append("\"");
        }
    }

    private static void addToLog(SpsProperty p) {
        if (p instanceof SpsLeafProperty) {
            Firebug.log(p.getBindKey() + "=" + ((SpsLeafProperty) p).getStringValue() + " " + (p.hasChanged() ? "!" : "-"));
        }
        else if (p instanceof SpsCompositeProperty) {
            final String nodeGUID = renderNodeGUID(p);
            Firebug.groupStart((StringUtil.hasText(p.getBindKey()) ? p.getBindKey() : "") +
                    (p instanceof SpsListProperty ? "[]" : "") +
                    (StringUtil.hasText(nodeGUID) ? nodeGUID : "") +
                    " " + (p.hasChanged() ? "!" : "-"));
            for (SpsProperty child : ((SpsCompositeProperty) p).getChildren()) {
                addToLog(child);
            }
            Firebug.groupEnd();
        }
    }

    private static String renderNodeGUID(SpsProperty p) {
        if(!(p instanceof SpsGroupProperty)) {
            return "";
        }
        final String nodeGUID = ((SpsGroupProperty) p).getNodeGUID();
        return StringUtil.hasText(nodeGUID) ? "(nodeGUID=" + nodeGUID + ")" : "";
    }

    private static void addToLog(String bindKey, DataContainerLeafNodeDeclaration leafNodeDecl) {
        final ParsedTypeInfo pti = leafNodeDecl.getDescription();
        final StringBuilder sb = new StringBuilder();
        sb.append(bindKey);
        sb.append(" (").append(pti.getTypeId());
        if (pti.isNumberProcent()) {
            sb.append(" %");
        }
        if (pti.isDemanded()) {
            sb.append(" !");
        }
        if (!"0".equals(pti.getMin())) { 
            sb.append(" >=").append(pti.getMin());
        }
        if (!"0".equals(pti.getMax())) { 
            sb.append(" <=").append(pti.getMax());
        }
        if (!"0".equals(pti.getVUnit())) { 
            sb.append(" .").append(pti.getVUnit());
        }
        if (!"0".equals(pti.getNumberSpin())) { 
            sb.append(" +").append(pti.getNumberSpin());
        }
        if (pti.getTypeId() == TiType.TI_DATE) {
            sb.append(' ').append(pti.getDateKind());
        }
        if (pti.getTypeId() == TiType.TI_ENUMERATION) {
            if(StringUtil.hasText(pti.getEnumerationNullValue())) {
                sb.append(" null='").append(pti.getEnumerationNullValue()).append("'");  
            }
        }
        if (pti.getTypeId() == TiType.TI_BOOLEAN) {
            sb.append(pti.isBooleanIsKindOption() ? " range={F|T}" : " range={N|F|T}");  
        }
        if((pti.getTypeId() == TiType.TI_FOLDER || pti.getTypeId() == TiType.TI_SHELL_MM) && !pti.getFolderTypes().isEmpty()) {
            sb.append(" folderTypes=").append(pti.getFolderTypes());
        }
        sb.append("): ").append(pti.getDisplayName());
        if (leafNodeDecl.getDefaultValue() != null) {
            final String defaultValue = MmTalkHelper.toLogString(leafNodeDecl.getDefaultValue());
            sb.append("\ndefault: ").append(defaultValue); 
        }
        Firebug.log(sb.toString());
    }

    private static void addToLog(String bindKeyPrefix, int listIndex, DataContainerNode node, boolean rootNode) {
        final String bindKey = createBindKey(bindKeyPrefix, listIndex, node, rootNode);
        if (node instanceof DataContainerLeafNodeDataItem) {
            Firebug.log(bindKey + ": " + MmTalkHelper.toLogString(((DataContainerLeafNodeDataItem) node).getDataItem()));
        }
        else if (node instanceof DataContainerLeafNodeDeclaration) {
            addToLog(bindKey, (DataContainerLeafNodeDeclaration) node);
        }
        else if (node instanceof DataContainerCompositeNode) {
            if (!rootNode) {
                if(node instanceof DataContainerGroupNode) {
                    ((DataContainerGroupNode) node).getNodeGUID();
                }
                Firebug.groupStart(appendNodeName(new StringBuilder(), listIndex, node, true).append(node instanceof DataContainerListNode ? "[]" : "").toString());
            }

            final String childPrefix = rootNode ? null : bindKey;
            int childIndex = -1;
            for (DataContainerNode child : ((DataContainerCompositeNode) node).getChildren()) {
                if (node instanceof DataContainerListNode) {
                    childIndex++;
                }
                addToLog(childPrefix, childIndex, child, false);
            }
            if (!rootNode) {
                Firebug.groupEnd();
            }
        }
        else {
            Firebug.warn(bindKey + ": unhandled node type " + node.getClass().getSimpleName());
        }
    }

    private static StringBuilder appendNodeName(StringBuilder sb, int listIndex, DataContainerNode node, boolean appendGuid) {
        if (listIndex == -1) {
            if (sb.length() != 0) {
                sb.append('/');
            }
            sb.append(node.getNodeLevelName());
            return sb;
        }
        sb.append('[').append(listIndex);
        if (StringUtil.hasText(node.getNodeLevelName())) {
            sb.append(':').append(node.getNodeLevelName());
        }
        sb.append(']');
        if(appendGuid && node instanceof DataContainerGroupNode) {
            final String nodeGUID = ((DataContainerGroupNode) node).getNodeGUID();
            if(StringUtil.hasText(nodeGUID)) {
                sb.append("(nodeGUID=").append(nodeGUID).append(')');
            }
        }
        return sb;
    }

    private static String createBindKey(String bindKeyPrefix, int listIndex, DataContainerNode node, boolean rootNode) {
        if (bindKeyPrefix == null) {
            return rootNode ? null : ("/" + node.getNodeLevelName());
        }
        else {
            final StringBuilder sb = new StringBuilder();
            sb.append(bindKeyPrefix);
            appendNodeName(sb, listIndex, node, false);
            return sb.toString();
        }
    }
}
