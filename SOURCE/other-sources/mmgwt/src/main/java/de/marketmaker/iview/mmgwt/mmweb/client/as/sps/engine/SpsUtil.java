/*
 * SpsUtil.java
 *
 * Created on 06.08.2014 17:08
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.history.EmptyContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.ErrorSeverity;
import de.marketmaker.iview.pmxml.HasCode;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMBool;
import de.marketmaker.iview.pmxml.MMDateTime;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.TiType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author mdick
 */
public final class SpsUtil {
    private SpsUtil() {
    }

    public static void transferDataToProperty(MM di, SpsLeafProperty lp, boolean setChangeIndicator, boolean fireEvent) {
        if (di == null || di instanceof DefaultMM) {
            if (TiType.TI_BOOLEAN == lp.getParsedTypeInfo().getTypeId()
                    && lp.getParsedTypeInfo().isBooleanIsKindOption()) {
                // DefaultMM is not a valid value if ParsedTypeInfo.BooleanIsKindOption is true!
                // Wolfgang specified that in this case the value must be interpreted as false.
                DebugUtil.showDeveloperNotification("BooleanIsKindOption (" + lp.getBindKey() + ") requires DIBoolean(true|false), but delivered value is DefaultMM!");
                final MMBool diFalse = new MMBool();
                diFalse.setValue(ThreeValueBoolean.TV_FALSE);
                lp.setValue(diFalse, true, false);
            }
            else {
                lp.setNullValue(setChangeIndicator, fireEvent);
            }
        }
        else if (di instanceof MMDateTime && MmTalkHelper.DI_DATE_NUll.equals(((MMDateTime) di).getValue())) {
            lp.setNullValue(setChangeIndicator, fireEvent);
        }
        else {
            lp.setValue(di, setChangeIndicator, fireEvent);
        }
    }

    public static SpsProperty clone(SpsProperty propertyToClone, SpsProperty clonedParent) {
        if (propertyToClone instanceof SpsLeafProperty) {
            final SpsLeafProperty leaf = new SpsLeafProperty(propertyToClone.getBindKey(), clonedParent,
                    ((SpsLeafProperty) propertyToClone).getParsedTypeInfo());

            final MM dataItem = ((SpsLeafProperty) propertyToClone).getDataItem();
            leaf.setValue(dataItem, false, false);
            return leaf;
        }
        else if (propertyToClone instanceof SpsGroupProperty) {
            final SpsGroupProperty group = new SpsGroupProperty(propertyToClone.getBindKey(), clonedParent);
            for (SpsProperty property : ((SpsGroupProperty) propertyToClone).getChildren()) {
                group.put(property.getBindKey(), clone(property, group), false);
            }
            return group;
        }
        else if (propertyToClone instanceof SpsListProperty) {
            final SpsListProperty list = new SpsListProperty(propertyToClone.getBindKey(), clonedParent);
            for (SpsProperty property : ((SpsListProperty) propertyToClone).getChildren()) {
                list.add(clone(property, list), false, false);
            }
            return list;
        }
        return null;
    }

    /**
     * @return false if no indexed bind key has been found in the bind key's ancestors
     */
    public static boolean consumeSameDescendantsOfNearestList(final SpsProperty spsProperty, final Consumer<SpsProperty> consumer) {
        if (spsProperty == null) {
            return false;
        }

        final BindToken token = BindToken.copy(spsProperty.getBindToken());
        final BindKeyIndexed indexedBindKey = findIndexedBindKey(token);

        if (indexedBindKey == null) {
            return false;
        }

        try {
            SpsProperty parent = spsProperty;
            while (parent != null) {
                if (parent instanceof SpsListProperty) {
                    consumeSameDescendantsOfList((SpsListProperty) parent, indexedBindKey.getNext(), consumer);
                    break;
                }
                parent = parent.getParent();
            }
        }
        catch (Exception e) {
            Firebug.error("<SpsUtil.consumeSameDescendantsOfNearestList> failed to find nearest list and to consume siblings of '" + spsProperty.getBindToken() + "'");
            return false;
        }

        return true;
    }

    private static void consumeSameDescendantsOfList(SpsListProperty listProperty, BindKey nextOfIndexedBindKey, Consumer<SpsProperty> consumer) {
        final int size = listProperty.getChildren().size();

        for (int i = 0; i < size; i++) {
            final BindKeyIndexed newIndexedBindKey = new BindKeyIndexed(i);
            newIndexedBindKey.withNext(nextOfIndexedBindKey);

            final SpsProperty targetProperty = listProperty.get(newIndexedBindKey);
            if (targetProperty != null) {
                consumer.accept(targetProperty);
            }
        }
    }

    /**
     * Recursively finds the nearest indexed bind key starting from the last bind key of the token.
     */
    private static BindKeyIndexed findIndexedBindKey(BindToken token) {
        if (token == null) {
            return null;
        }

        BindKey previous = token.getLast();
        while (previous != null) {
            if (previous instanceof BindKeyIndexed) {
                return (BindKeyIndexed) previous;
            }
            previous = findPrevious(token.getHead(), previous);
        }
        return null;
    }

    /**
     * Recursively finds the previous bind key for a given starting bind key.
     * This is necessary, because BindKey's prev property is mostly not properly set, but next is.
     */
    private static BindKey findPrevious(BindKey first, BindKey previousOf) {
        if (first == null || previousOf == null) {
            return null;
        }

        BindKey previous = null;
        BindKey next = first;
        while (next != null) {
            if (next == previousOf) {
                return previous;
            }
            previous = next;
            next = next.getNext();
        }

        return null;
    }

    // TODO: maybe we should use only the distinct version?!?
    public static HistoryContext extractShellMMInfoHistoryContext(String historyContext, SpsLeafProperty leaf) {
        final ArrayList<ShellMMInfo> shellMMInfos = new ArrayList<>();

        final Consumer<SpsProperty> consumer = spsProperty -> {
            if (spsProperty instanceof SpsLeafProperty) {
                final SpsLeafProperty leafProperty = (SpsLeafProperty) spsProperty;
                if (leafProperty.isShellMMInfo()) {
                    shellMMInfos.add(leafProperty.getShellMMInfo());
                }
            }
        };
        return extractShellMMInfoHistoryContext(historyContext, leaf, shellMMInfos, consumer);
    }

    public static HistoryContext extractDistinctShellMMInfoHistoryContext(String historyContext, SpsLeafProperty leaf) {
        final HashSet<String> shellMMInfoIds = new HashSet<>();
        final ArrayList<ShellMMInfo> shellMMInfos = new ArrayList<>();

        final Consumer<SpsProperty> consumer = property -> {
            if (property instanceof SpsLeafProperty) {
                final SpsLeafProperty leafProperty = (SpsLeafProperty) property;
                if (leafProperty.isShellMMInfo()) {
                    final ShellMMInfo shellMMInfo = leafProperty.getShellMMInfo();
                    if(!shellMMInfoIds.contains(shellMMInfo.getId())) {
                        shellMMInfos.add(shellMMInfo);
                        shellMMInfoIds.add(shellMMInfo.getId());
                    }
                }
            }
        };

        return extractShellMMInfoHistoryContext(historyContext, leaf, shellMMInfos, consumer);
    }

    private static HistoryContext extractShellMMInfoHistoryContext(String historyContext, SpsLeafProperty leaf, ArrayList<ShellMMInfo> shellMMInfos, Consumer<SpsProperty> consumer) {
        if (!consumeSameDescendantsOfNearestList(leaf, consumer)) {
            return EmptyContext.create(historyContext);
        }

        return PmItemListContext.createForShellMMInfo(historyContext, leaf.getShellMMInfo(), shellMMInfos);
    }

    public static ParsedTypeInfo getChildParsedTypeInfo(Context context, SpsCompositeProperty spsPropertyParent) {
        final BindToken bindToken = spsPropertyParent.getBindToken();
        final DataContainerCompositeNode declParent = (DataContainerCompositeNode) context.getDeclaration(bindToken);
        final DataContainerLeafNodeDeclaration declChild = (DataContainerLeafNodeDeclaration) declParent.getChildren().get(0);
        return declChild.getDescription();
    }

    public static DataContainerNode getDeclChild(DataContainerGroupNode groupNode, String nodeLevelName) {
        for (DataContainerNode node : groupNode.getChildren()) {
            if (nodeLevelName.equals(node.getNodeLevelName())) {
                return node;
            }
        }
        return null;
    }

    public static DataContainerLeafNodeDeclaration getListCellDecl(Context context, BindToken itemsBindToken, String fieldName) {
        final BindToken entryBindToken = itemsBindToken.append("[0]/" + fieldName); // $NON-NLS$
        final DataContainerNode fieldDeclNode = context.getDeclaration(entryBindToken);
        if (!(fieldDeclNode instanceof DataContainerLeafNodeDeclaration)) {
            throw new RuntimeException("DataContainerLeafNodeDeclaration expected (is " + fieldDeclNode.getClass().getSimpleName() + "): " + entryBindToken); // $NON-NLS$
        }
        return (DataContainerLeafNodeDeclaration) fieldDeclNode;
    }

    /**
     * MMTalk name values of
     * sSLTAnlageplanungsTransaktionsTypNA = 'n/a';
     * sSLTAnlageplanungsTransaktionsTypAnkauf = 'Kaufen';
     * sSLTAnlageplanungsTransaktionsTypZeichnung = 'Zeichnen';
     * sSLTAnlageplanungsTransaktionsTypVerkauf = 'Verkaufen';
     * sSLTAnlageplanungsTransaktionsTypHalten = 'Halten';
     * sSLTAnlageplanungsTransaktionsTypNichtKaufen = 'Nicht kaufen';
     */
    public static OrderActionType toOrderActionType(String value) {
        if (!StringUtil.hasText(value)) {
            return null;
        }

        // the MMTalk usable names of enums are constant, only the display names are translated.
        switch (value) {
            case "Kaufen":  // $NON-NLS$
                return OrderActionType.AT_BUY;
            case "Verkaufen": // $NON-NLS$
                return OrderActionType.AT_SELL;
            case "Zeichnen":  // $NON-NLS$
                return OrderActionType.AT_SUBSCRIBE;
            case "Halten":  // $NON-NLS$
            case "Nicht kaufen": // $NON-NLS$
            case "n/a": // $NON-NLS$
                Firebug.debug("<SpsUtil.toOrderActionType> given transaction type value \"" + value + "\" has no correspondence in OrderActionType.");
                return null;
            default:
                final String s = "<SpsUtil.toOrderActionType> given transaction type value \"" + value + "\" cannot be mapped to OrderActionType!";  // $NON-NLS$
                Firebug.warn(s);
                DebugUtil.showDeveloperNotification(s);
                return null;
        }
    }

    public static String getSeverityIconStyle(ErrorSeverity severity) {
        if (severity == null) {
            return null;
        }
        return "pmSeverity-" + severity.value(); // $NON-NLS$
    }

    public static Map<String, String> createEnumMap(List<MM> mms) {
        final Map<String, String> map = new LinkedHashMap<>();
        for (MM mm : mms) {
            if(mm instanceof HasCode) {
                final HasCode mmEnum = (HasCode) mm;
                map.put(mmEnum.getCode(), mmEnum.getValue());
            }
            else {
                Firebug.warn("<SpsUtil.createEnumMap> enum element does not implement HasCode: " + MmTalkHelper.toLogString(mm));
            }
        }
        return map;
    }

    public static String getLeafStringValue(SpsGroupProperty group, String bindKeyOfLeaf) {
        if(group == null) {
            return null;
        }
        final SpsProperty spsProperty = group.get(bindKeyOfLeaf);
        if(!(spsProperty instanceof SpsLeafProperty)) {
            return null;
        }
        return ((SpsLeafProperty) spsProperty).getStringValue();
    }
}
