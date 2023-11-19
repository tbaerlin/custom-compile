/*
 * MmTalkHelper.java
 *
 * Created on 23.03.11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk;

import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDate;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.HistoryItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorItem;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.HasCode;
import de.marketmaker.iview.pmxml.HasId;
import de.marketmaker.iview.pmxml.HasValue;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMBool;
import de.marketmaker.iview.pmxml.MMDBRef;
import de.marketmaker.iview.pmxml.MMDateTime;
import de.marketmaker.iview.pmxml.MMIndexedString;
import de.marketmaker.iview.pmxml.MMNumber;
import de.marketmaker.iview.pmxml.MMString;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTableRow;
import de.marketmaker.iview.pmxml.MMTypRef;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.Parameter;
import de.marketmaker.iview.pmxml.ParameterDesc;
import de.marketmaker.iview.pmxml.ParameterEnumDesc;
import de.marketmaker.iview.pmxml.ParameterShellMM;
import de.marketmaker.iview.pmxml.ParameterShellMMDesc;
import de.marketmaker.iview.pmxml.ParameterSimple;
import de.marketmaker.iview.pmxml.ParameterSimpleDesc;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellMMTypeDesc;
import de.marketmaker.iview.pmxml.SimpleMM;
import de.marketmaker.iview.pmxml.TableTreeElement;
import de.marketmaker.iview.pmxml.TableTreeFormula;
import de.marketmaker.iview.pmxml.TableTreeTable;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ulrich Maurer
 * @author Markus Dick
 */
@NonNLS
public class MmTalkHelper {
    public static final String ID_NAME_SUFFIX = ".id";
    public static final String DI_DATE_NUll = "1899-12-30T00:00:00.000+01:00";
    private static GwtClientServerProxy dateFormatProxy = GwtClientServerProxy.createDateFormatInstance();

    public static void setDateFormatProxy(GwtClientServerProxy _dateFormatProxy) {
        dateFormatProxy = _dateFormatProxy;
    }

    public static GwtClientServerProxy getClientServerProxy() {
        return dateFormatProxy;
    }

    /**
     * Converts simple MM types to strings.
     * Types qualifying for TI_ENUMERATION and TI_DYNAMIC should not be handled here.
     * To handle those types, use special handling methods, e.g. {@linkplain #asCode(MM)}.
     *
     * @see #createMMType(de.marketmaker.iview.pmxml.TiType, String)
     */
    public static String asString(MM mm) {
        if (mm == null || mm instanceof DefaultMM) {
            return null;
        }
        else if (mm instanceof HasValue) {
            return asString((HasValue) mm);
        }
        else if (mm instanceof MMBool) {
            if (((MMBool) mm).getValue() == ThreeValueBoolean.TV_NULL) {
                return null;
            }
            return String.valueOf(((MMBool) mm).getValue() == ThreeValueBoolean.TV_TRUE);
        }
        else if (mm instanceof ErrorMM) {
            final ErrorMM error = (ErrorMM) mm;
            Firebug.warn("<MmTalkHelper.asString> dataItem is an ErrorMM: " + error.getErrorString());
            return null;
        }
        Firebug.warn("<MmTalkHelper.asString> unhandled MM subclass: " + mm.getClass().getSimpleName());
        return null;
    }

    /**
     * @see #createMMType(de.marketmaker.iview.pmxml.TiType, String)
     */
    public static String asCode(MM mm) {
        if (mm == null || mm instanceof DefaultMM) {
            return null;
        }
        else if(mm instanceof HasCode) {
            return ((HasCode) mm).getCode();
        }
        else {
            // All MM enum types implement the HasCode interface. So there is no need to fall back to
            // #asValue(MM). To enforce stricter typing, do not add asValue or any other string magic again!
            throw new IllegalArgumentException("Given MM does not implement HasCode: " + toLogString(mm));
        }
    }

    private static String asString(HasValue item) {
        return item == null ? null : item.getValue();
    }

    public static String asString(List<MM> cells, int index) {
        return asString(cells.get(index));
    }

    @SuppressWarnings("unchecked")
    private static <T extends MM> T asValue(MM item) {
        if (item == null || item instanceof DefaultMM || item instanceof ErrorMM) {
            return null;
        }
        return (T) item;
    }

    public static MMTypRef asMMTypRef(MM item) {
        return asValue(item);
    }

    /**
     * Converts a MMTypRef to the corresponding Java enum.
     * Usually, the Java enum results from an XSD export of an enum present in pm code.
     * The same order of the pm enum values and the resulting Java enum is assumed.
     *
     * @param mmTypRefType The type of the enum for type safety reasons.
     * @param values       The values array of the target enum.
     * @param item         The SimpleMM; may be null or of any type, but works only as expected with instances of DiEnumEx
     * @param <E>          The target enum
     * @return The enum instance that corresponds to the given SimpleMM
     */
    public static <E extends Enum<E>> E asEnum(MMTypRefType mmTypRefType, E[] values, MM item) {
        assert mmTypRefType != null && values != null;

        final MMTypRef tr = MmTalkHelper.asMMTypRef(item);

        if (tr == null || !mmTypRefType.equals(tr.getTyp())) {
            Firebug.debug("No corresponding enum type " + mmTypRefType.name() + " for " + tr);
            return null;
        }

        for (E e : values) {
            try {
                if (e.ordinal() == Integer.parseInt(tr.getOrdValue())) {
                    return e;
                }
            }
            catch (NumberFormatException nfe) {
                Firebug.error("Cannot parse ordinal of enum", nfe);
            }
        }

        return null;
    }

    public static MMNumber asMMNumber(MM item) {
        return asValue(item);
    }

    public static MMNumber asMMNumber(List<MM> cells, int index) {
        return asMMNumber(cells.get(index));
    }

    public static BigDecimal asBigDecimal(MM item) {
        final MMNumber mmNumber = asValue(item);
        return new BigDecimal(mmNumber.getValue());
    }

    public static int getBigDecimalScale(String vUnit) {
        return vUnit == null || "0".equals(vUnit) ? -1 : vUnit.length() - 1;
    }

    public static Boolean asBoolean(MM item) {
        final MMBool value = asValue(item);
        return (value == null || value.getValue() == ThreeValueBoolean.TV_NULL) ? null : value.getValue() == ThreeValueBoolean.TV_TRUE;
    }

    public static boolean isTrue(MM item) {
        final Boolean value = asBoolean(item);
        return value != null && value;
    }

    public static String[] concat(String[] s1, String[] s2) {
        String[] result = new String[s1.length + s2.length];
        System.arraycopy(s1, 0, result, 0, s1.length);
        System.arraycopy(s2, 0, result, s1.length, s2.length);
        return result;
    }

    public static String toLogString(MM mm) {
        if (mm == null) {
            return "null";
        }
        else if (mm instanceof MMIndexedString) {
            final MMIndexedString mmIndexedString = (MMIndexedString) mm;
            return " MMIndexedString { value=" + mmIndexedString.getValue() + ", code=" + mmIndexedString.getCode() + " }";
        }
        else if (mm instanceof SimpleMM) {
            return mm.getClass().getSimpleName() + " { " + asString(mm) + " }";
        }
        else if (mm instanceof ShellMMInfo) {
            ShellMMInfo shellMMInfo = (ShellMMInfo) mm;
            return "ShellMMInfo { id=" + shellMMInfo.getId()
                    + ", number=\"" + shellMMInfo.getNumber()
                    + "\", bezeichnung=\"" + shellMMInfo.getBezeichnung()
                    + "\", typ=" + PmRenderers.SHELL_MM_TYPE.render(shellMMInfo.getTyp())
                    + " }";
        }
        else if (mm instanceof ErrorMM) {
            final ErrorMM errorMM = (ErrorMM) mm;
            return "ErrorMM { message='" + errorMM.getErrorString() + '\'' +
                    ", severity=" + errorMM.getErrorSeverity() +
                    ", correlationSource='" + errorMM.getCorrelationSource() + '\'' +
                    ", correlationTarget='" + errorMM.getCorrelationTarget() + "' }";
        }
        else if(mm instanceof MMDBRef) {
            final MMDBRef mmdbRef = (MMDBRef) mm;
            return mm.getClass().getSimpleName() + " (is MMDBRef) { value='" + mmdbRef.getValue() + '\'' +
                    ", code=" + mmdbRef.getCode() +
                    ", id=" + mmdbRef.getId() + " }";
        }
        else if(mm instanceof MMTypRef) {
            final MMTypRef mmTypRef = (MMTypRef) mm;
            return mmTypRef.getClass().getSimpleName() + " (is MMTypRef) { value='" + mmTypRef.getValue() + '\'' +
                    ", code=" + mmTypRef.getCode() +
                    ", ordValue=" + mmTypRef.getOrdValue() +
                    ", typ=" + (mmTypRef.getTyp() != null ? mmTypRef.getTyp().name() : "null") + " }";
        }
        else if(mm instanceof HasId) {
            return mm.getClass().getSimpleName() + " (is HasId) { id=" + ((HasId) mm).getId() + " }";
        }
        else {
            return mm.getClass().getSimpleName() + " { not handled by toLogString }";
        }
    }

    public static String toLogString(Parameter parameter) {
        if (parameter == null) {
            return "null";
        }
        final String simpleName = parameter.getClass().getSimpleName();
        if (parameter instanceof ParameterSimple) {
            final ParameterSimple paramSimple = (ParameterSimple) parameter;
            final MM simpleMM = paramSimple.getValue();
            return simpleName + "{value=" + simpleMM.getClass().getSimpleName() + "{" + asString(simpleMM) + "}}";
        }
        if (parameter instanceof ParameterShellMM) {
            final ParameterShellMM paramShellMM = (ParameterShellMM) parameter;
            return simpleName + "{shellMMId=" + paramShellMM.getShellMMId() + "}";
        }
        throw new IllegalArgumentException("unknown subtype of Parameter: " + parameter.getClass().getName());
    }

    /**
     * @return a map with parameters or an empty map, but never null.
     */
    public static Map<String, String> paramAsStringMap(Parameter parameter) {
        if (parameter == null) {
            return Collections.emptyMap();
        }
        if (parameter instanceof ParameterSimple) {
            final ParameterSimple paramSimple = (ParameterSimple) parameter;
            final MM simpleMM = paramSimple.getValue();
            return Collections.singletonMap(parameter.getName(), asString(simpleMM));
        }
        if (parameter instanceof ParameterShellMM) {
            final ParameterShellMM paramShellMM = (ParameterShellMM) parameter;
            final HashMap<String, String> map = new HashMap<>();
            map.put(parameter.getName(), paramShellMM.getName());
            map.put(parameter.getName() + ID_NAME_SUFFIX, paramShellMM.getShellMMId());
            return Collections.unmodifiableMap(map);
        }
        throw new IllegalArgumentException("unknown subtype of Parameter: " + parameter.getClass().getName());
    }

    /**
     * Converts a string map to a parameter map.
     * Implements the symmetrical reverse function of {@link #toParameterMap(java.util.Map, de.marketmaker.iview.pmxml.LayoutDesc)}
     */
    public static HashMap<String, String> toStringMap(Map<String, Parameter> layoutParameters) {
        final HashMap<String, String> result = new HashMap<>();
        if (layoutParameters == null) {
            return result;
        }
        for (Map.Entry<String, Parameter> entry : layoutParameters.entrySet()) {
            final Parameter value = entry.getValue();
            if (value == null) {
                continue;
            }
            result.putAll(paramAsStringMap(value));
        }
        return result;
    }

    /**
     * Converts a parameter map to a string map.
     * The symmetrical reverse function is {@link #toStringMap(java.util.Map)}
     */
    public static Map<String, Parameter> toParameterMap(Map<String, String> layoutParameters, LayoutDesc metadata) {
        final HashMap<String, Parameter> result = new HashMap<>();
        for (final ParameterDesc parameterDesc : metadata.getParameters()) {
            // A key may be present but its value may be null then we have to explicitly add a DefaultMM
            // (i.e. to handle PM's three-state booleans)
            // If a key is absent, we simply set null which causes PM to use the default value.
            final String parameterName = parameterDesc.getName();
            if (!layoutParameters.containsKey(parameterName)) {
                result.put(parameterName, null);
                continue;
            }

            final String value = layoutParameters.get(parameterName);
            if (value == null) {
                final ParameterSimple ps = new ParameterSimple();
                ps.setName(parameterName);
                ps.setValue(new DefaultMM());
                result.put(parameterName, ps);
            }
            else if (parameterDesc instanceof ParameterSimpleDesc || parameterDesc instanceof ParameterEnumDesc) {
                final MM mm = createMMType(parameterDesc.getTypeInfo().getTypeId(), value);
                if (mm != null) {
                    final ParameterSimple ps = new ParameterSimple();
                    ps.setName(parameterName);
                    ps.setValue(mm);
                    result.put(parameterName, ps);
                }
                else {
                    Firebug.warn("<MmTalkHelper.toParameterMap> could not handle ParamType " + parameterDesc.getTypeInfo().getTypeId());
                }
            }
            else if (parameterDesc instanceof ParameterShellMMDesc) {
                final String id = layoutParameters.get(parameterName + ID_NAME_SUFFIX);
                if (id != null) {
                    final ParameterShellMM parameterShellMM = new ParameterShellMM();
                    parameterShellMM.setShellMMId(id);
                    parameterShellMM.setName(parameterName);
                    result.put(parameterName, parameterShellMM);
                }
            }
            else {
                throw new IllegalStateException("unknown subtype of ParameterDesc: " + parameterDesc.getClass().getName());
            }
        }
        return result;
    }

    /**
     * @see #asCode(de.marketmaker.iview.pmxml.MM)
     * @see #asString(de.marketmaker.iview.pmxml.MM)
     */
    private static MM createMMType(TiType type, String value) {
        if (!StringUtil.hasText(value)) {
            return null;
        }
        final MM dis;
        switch (type) {
            case TI_NUMBER:
                dis = new MMNumber();
                ((MMNumber) dis).setValue(value);
                break;
            case TI_BOOLEAN:
                dis = new MMBool();
                ((MMBool) dis).setValue("true".equals(value)
                        ? ThreeValueBoolean.TV_TRUE
                        : ThreeValueBoolean.TV_FALSE);
                break;
            case TI_DATE:
                dis = new MMDateTime();
                ((MMDateTime) dis).setValue(dateFormatProxy.formatIso8601(value));
                break;
            case TI_STRING:
            case TI_MEMO:
                dis = new MMString();
                ((MMString) dis).setValue(value);
                break;
            case TI_ENUMERATION:
                // Steffen says pm handles MMStrings when some enumeration is expected.
                // TI_ENUMERATION could be represented by a MMTypRef, MMDBRef or MMIndexedString *d'oh*
                // We are creating MMIndexedString instead, so that we can rely in subsequent processing
                // steps on HasCode to get the code of the enum.
                final MMIndexedString mmis = new MMIndexedString();
                mmis.setValue(value);
                mmis.setCode(value);
                dis = mmis;
                break;
            case TI_DYNAMIC:
                throw new IllegalArgumentException("Types of type tiDynamic cannot be created in GWT");
            default:
                return null;
        }
        return dis;
    }

    public static void addColumnFormulas(TableTreeTable rootnode, String... formulas) {
        final List<TableTreeElement> columns = rootnode.getColumns();
        for (String formula : formulas) {
            final TableTreeFormula ttf = new TableTreeFormula();
            ttf.setFormula(formula);
            columns.add(ttf);
        }
    }

    public static void addNode(TableTreeTable rootnode, TableTreeTable node) {
        final List<TableTreeElement> columns = rootnode.getColumns();
        columns.add(node);
    }

    public static List<InvestorItem> getInvestorItems(MMTable data) {
        final List<MMTableRow> listTableRows = data.getRow();
        final List<InvestorItem> listInvestorItems = new ArrayList<>(listTableRows.size());
        addInvestorItems(listInvestorItems, listTableRows);
        return listInvestorItems;
    }

    public static void addInvestorItems(List<InvestorItem> listInvestorItems, List<MMTableRow> listTableRows) {
        for (MMTableRow tableRow : listTableRows) {
            final List<MM> listCells = tableRow.getCell();
            int cellId = -1;
            final String uid = asMMNumber(listCells, ++cellId).getValue();
            if (uid == null) {
                continue;
            }
            final InvestorItem.Type type = getType(listCells, ++cellId);
            if (type == null) {
                continue;
            }
            final String name = asString(listCells, ++cellId);
            if (name == null) {
                continue;
            }
            final String zone = asString(listCells, ++cellId);
            if (zone == null) {
                continue;
            }
            final String id = asString(listCells, ++cellId);
            if (id == null) {
                continue;
            }
            ++cellId;
            final InvestorItem.HasChildren hasChildren = listCells.size() > cellId ? isTrue(listCells, cellId) : InvestorItem.HasChildren.UNKNOWN;

            final InvestorItem item = new InvestorItem(id, type, name, zone, false);
            item.setHasChildren(hasChildren);
            listInvestorItems.add(item);
        }
    }

    private static InvestorItem.Type getType(final List<MM> listCells, int id) {
        final MM di = listCells.get(id);
        if (di instanceof ErrorMM) {
            final ErrorMM error = (ErrorMM) di;
            Firebug.debug("getType(" + id + ") error: " + error.getErrorString());
            return null;
        }
        try {
            return InvestorItem.Type.valueOf(((MMDBRef) di).getValue());
        }
        catch (IllegalArgumentException e) {
            Firebug.debug("getType(" + id + ") error: " + e.getMessage());
            return null;
        }
    }

    private static InvestorItem.HasChildren isTrue(List<MM> listCells, int id) {
        final MM di = listCells.get(id);
        if (di instanceof ErrorMM) {
            final ErrorMM error = (ErrorMM) di;
            Firebug.debug("isTrue(" + id + ") error: " + error.getErrorString());
            return InvestorItem.HasChildren.UNKNOWN;
        }
        return ((MMBool) di).getValue() == ThreeValueBoolean.TV_TRUE
                ? InvestorItem.HasChildren.YES
                : InvestorItem.HasChildren.NO;
    }

    public static MMDateTime nowAsDIDateTime() {
        final MMDateTime diDateTime = new MMDateTime();
        final String now = Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(new Date());
        diDateTime.setValue(now);
        return diDateTime;
    }

    public static MMDateTime asMMDateTime(JsDate date) {
        final MMDateTime diDateTime = new MMDateTime();
        diDateTime.setValue(JsDateFormatter.formatIso8601(date));
        return diDateTime;
    }

    public static List<ShellMMTypeDesc> toShellMmTypeDescList(ShellMMType[] shellMMTypes) {
        final ArrayList<ShellMMTypeDesc> result = new ArrayList<>();
        for (ShellMMType shellMMType : shellMMTypes) {
            final ShellMMTypeDesc desc = new ShellMMTypeDesc();
            desc.setT(shellMMType);
            result.add(desc);
        }
        return result;
    }

    public static <T> List<T> cloneWithoutNulls(List<T> source) {
        if (source == null) {
            return null;
        }

        final ArrayList<T> target = new ArrayList<>();

        for (final T t : source) {
            if (t != null) {
                target.add(t);
            }
        }

        return target;
    }

    public static void sortHistoryItems(List<HistoryItem> historyItems) {
        if (historyItems == null || historyItems.isEmpty()) {
            return;
        }
        Collections.sort(historyItems, HISTORY_ITEM_COMPARATOR);
    }

    private static final Comparator<HistoryItem> HISTORY_ITEM_COMPARATOR = new HistoryItemComparator(true);

    public static List<String> toStringList(List<MMString> mmStrings) {
        if(mmStrings == null) {
            return null;
        }
        if(mmStrings.isEmpty()) {
            return Collections.emptyList();
        }
        final ArrayList<String> stringList = new ArrayList<>();
        for (MMString mmString : mmStrings) {
            stringList.add(mmString.getValue());
        }
        return stringList;
    }

    private static final class HistoryItemComparator implements Comparator<HistoryItem> {
        private final boolean reverse;

        private HistoryItemComparator(boolean reverse) {
            this.reverse = reverse;
        }

        @Override
        public int compare(HistoryItem o1, HistoryItem o2) {
            return this.reverse ? doCompare(o1, o2) * -1 : doCompare(o1, o2);
        }

        private int doCompare(HistoryItem o1, HistoryItem o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }

            final String date1 = o1.getDate();
            final String date2 = o2.getDate();
            if (date1 == null) {
                return -1;
            }
            if (date2 == null) {
                return 1;
            }
            return date1.compareTo(date2);
        }
    }


    public static MM asMMType(String value, TiType type) {
        return createMMType(type, value);
    }

    public static boolean isSameDataType(TiType tiType, MM value) {
        if (value == null || value instanceof DefaultMM) {
            return true;
        }
        switch (tiType) {
            case TI_NUMBER:
                return value instanceof MMNumber;
            case TI_BOOLEAN:
                return value instanceof MMBool;
            case TI_DATE:
                return value instanceof MMDateTime;
            case TI_STRING:
            case TI_MEMO:
                return value instanceof MMString;
            case TI_ENUMERATION:
                return value instanceof MMTypRef || value instanceof MMDBRef || value instanceof MMString; //MMString means MMIndexString as well
            case TI_SHELL_MM:
            case TI_FOLDER:
                return value instanceof ShellMMInfo;
            case TI_DYNAMIC:
                return value instanceof HasId;
        }
        throw new IllegalArgumentException("type not handled: " + tiType);
    }

    /**
     * Checks, in case both MM objects are of type ShellMMInfo, if their ShellMMTypes are equal.
     * If any of the MM objects is not of type ShellMMInfo or null, returns false.
     * Returns also false, if both MM objects are null.
     */
    public static boolean isSameShellMMType(MM me, MM other) {
        return me instanceof ShellMMInfo && other instanceof ShellMMInfo && ((ShellMMInfo) me).getTyp() == ((ShellMMInfo) other).getTyp();
    }

    /**
     * Compares MMs based on their tiType.
     * In contrast to asString, this method uses strict type conversions.
     * This means that any other value than the expected type, null or DefaultMM will not be converted to null.
     * Instead, an IllegalArgumentException is thrown.
     */
    public static boolean equals(TiType tiType, MM me, MM other) {
        if (me == other) return true;
        if (isSameDataType(tiType, me) && isSameDataType(tiType, other)) {
            switch(tiType) {
                case TI_STRING:
                case TI_MEMO:
                case TI_NUMBER:
                case TI_DATE:
                    return CompareUtil.equals(toTiStringMemoNumberDateCompareString(me),
                            toTiStringMemoNumberDateCompareString(other));
                case TI_BOOLEAN:
                    return CompareUtil.equals(toTiBooleanCompareObject(me), toTiBooleanCompareObject(other));
                case TI_ENUMERATION:
                    return CompareUtil.equals(asCode(me), asCode(other));
                case TI_SHELL_MM:
                case TI_FOLDER:
                    return CompareUtil.equals(toTiShellMMOrTiFolderCompareString(me),
                            toTiShellMMOrTiFolderCompareString(other));
                case TI_DYNAMIC:
                    return CompareUtil.equals(toTiDynamicCompareString(me), toTiDynamicCompareString(other));
            }
        }
        else {
            final String s = "<MmTalkHelper.equals> comparing different MM types: " + me.getClass().getSimpleName() + " and " + other.getClass().getSimpleName();
            Firebug.warn(s);
            DebugUtil.showDeveloperNotification(s);
            return false;
        }

        return CompareUtil.equals(me, other);
    }

    private static String toTiStringMemoNumberDateCompareString(MM mm) {
        if (mm == null || mm instanceof DefaultMM) {
            return null;
        }
        else if (mm instanceof HasValue) {
            return ((HasValue) mm).getValue();
        }
        else {
            throw new IllegalArgumentException("MM " + mm.getClass().getSimpleName() + " does not qualify for TiString, TiMemo, TiNumber or TiDate. It is not null, an instance of DefaultMM, and it is not an instance of SimpleMM.");
        }
    }

    private static Boolean toTiBooleanCompareObject(MM mm) {
        if (mm == null || mm instanceof DefaultMM) {
            return null;
        }
        else if (mm instanceof MMBool) {
            final ThreeValueBoolean threeValueBoolean = ((MMBool) mm).getValue();
            if (threeValueBoolean == ThreeValueBoolean.TV_NULL) {
                return null;
            }
            return threeValueBoolean == ThreeValueBoolean.TV_TRUE;
        }
        else {
            throw new IllegalArgumentException("MM " + mm.getClass().getSimpleName() + " does not qualify for TiBoolean. It is not null, an instance of DefaultMM, and it is not an instance of MMBoolean.");
        }
    }

    private static String toTiDynamicCompareString(MM mm) {
        if(mm == null || mm instanceof DefaultMM) {
            return null;
        }
        else if(mm instanceof HasId) {
            return ((HasId) mm).getId();
        }
        else {
            throw new IllegalArgumentException("MM " + mm.getClass().getSimpleName() + " does not qualify for TiDynamic. It is not null, an instance of DefaultMM, and it does not implement HasId.");
        }
    }

    private static String toTiShellMMOrTiFolderCompareString(MM mm) {
        if(mm == null || mm instanceof DefaultMM) {
            return null;
        }
        else if(mm instanceof ShellMMInfo) {
            return ((ShellMMInfo) mm).getId();
        }
        else {
            throw new IllegalArgumentException("MM " + mm.getClass().getSimpleName() + " does not qualify for TiShellMM or TiFolder. It is not null, an instance of DefaultMM, and it is not an instance of ShellMMInfo");
        }
    }
}