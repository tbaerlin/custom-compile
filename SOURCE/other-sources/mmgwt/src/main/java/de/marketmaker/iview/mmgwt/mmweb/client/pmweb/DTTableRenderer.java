/*
 * DTTableRenderer.java
 *
 * Created on 12.04.13 13:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.DTCell;
import de.marketmaker.iview.pmxml.DTColumnGroup;
import de.marketmaker.iview.pmxml.DTColumnSpec;
import de.marketmaker.iview.pmxml.DTColumnStack;
import de.marketmaker.iview.pmxml.DTGroupSpec;
import de.marketmaker.iview.pmxml.DTRowBlock;
import de.marketmaker.iview.pmxml.DTRowGroup;
import de.marketmaker.iview.pmxml.DTSingleRow;
import de.marketmaker.iview.pmxml.DTSortSpec;
import de.marketmaker.iview.pmxml.DTSubColumn;
import de.marketmaker.iview.pmxml.DTTable;
import de.marketmaker.iview.pmxml.HasCode;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * creates plain html to render a DTTable
 *
 * @author oflege
 */
@NonNLS
public class DTTableRenderer {
    public static final String PM_COL_INDEX_ATTRIBUTE_NAME = "pm-colindex";
    public static final String PM_ROW_MARKER_ATTRIBUTE_NAME = "pm-row";
    public static final String PM_SHELLTYPE_ATTRIBUTE_NAME = "pm-shelltype";
    public static final String PM_SHELLID_ATTRIBUTE_NAME = "pm-shellid";
    public static final String PM_SHELLMMSID_ATTRIBUTE_NAME = "pm-shellmmsid";
    public static final String PM_ACTIVITYID_ATTRIBUTE_NAME = "pm-activityid";
    public static final String PM_ACTIVITYNAME_ATTRIBUTE_NAME = "pm-activityname";

    private static final int CHAR_WIDTH_FACTOR = 7;
    private static final int PADDING = 4;
    private static final int BORDER_WIDTH = 1;

    private final CellRenderer rowLinkRenderer;

    public interface ColumnFilter {
        boolean isAcceptable(DTSingleRow row);
    }

    public static class Options {
        private boolean withAggregations = true;
        private boolean prefiltered = false;

        private List<DTSortSpec> customSort;

        private ColumnFilter filter;

        public Options() {
        }

        public Options withAggregations(boolean on) {
            this.withAggregations = on;
            return this;
        }

        public boolean isWithAggregations() {
            return withAggregations;
        }

        public ColumnFilter getFilter() {
            return filter;
        }


        @SuppressWarnings("UnusedDeclaration")
        public Options withFilter(ColumnFilter filter) {
            this.filter = filter;
            return this;
        }

        public Options withCustomSort(List<DTSortSpec> spec) {
            this.customSort = spec;
            return this;
        }

        public Options withPrefiltered(boolean prefiltered) {
            this.prefiltered = prefiltered;
            return this;
        }

        public boolean isPrefiltered() {
            return this.prefiltered;
        }

        public List<DTSortSpec> getCustomSort() {
            return customSort;
        }

        public boolean isWithCustomSort() {
            return this.customSort != null;
        }

        @Override
        public String toString() {
            return "Options{withAggregations=" + this.withAggregations +
                    ", customSort=" + toString(this.customSort) +
                    ", filter=" + this.filter +
                    '}';
        }

        public static String toString(List<DTSortSpec> specs) {
            if(specs == null) {
                return "null";
            }

            final StringBuilder sb = new StringBuilder("List{");
            for (DTSortSpec spec : specs) {
                sb.append(toString(spec)).append(", ");
            }
            return sb.append("}").toString();
        }

        private static String toString(DTSortSpec spec) {
            return "DTSortSpec{" +
                    "colIndex='" + spec.getColIndex() + '\'' +
                    ", ascending=" + spec.isAscending() +
                    '}';
        }
    }

    public interface CellRenderer {
        int getWidth();

        void appendStyle(StringBuilder sb1);

        void appendSortStyle(StringBuilder sb1);

        void appendHeader(StringBuilder sb1);

        void appendRow(DTSingleRow row, boolean aggregated, StringBuilder sb1);
    }

    public static class InclusiveMultiColumnFilter implements ColumnFilter {
        private final List<ColumnFilter> filters;

        public InclusiveMultiColumnFilter(List<ColumnFilter> filters) {
            this.filters = filters;
        }

        @Override
        public boolean isAcceptable(DTSingleRow row) {
            for (ColumnFilter filter : this.filters) {
                if (filter.isAcceptable(row)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "InclusiveMultiColumnFilter{" +
                    "filters=" + filters +
                    '}';
        }

        @SuppressWarnings("RedundantIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InclusiveMultiColumnFilter)) return false;

            final InclusiveMultiColumnFilter that = (InclusiveMultiColumnFilter) o;

            if (filters != null ? !filters.equals(that.filters) : that.filters != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return filters != null ? filters.hashCode() : 0;
        }
    }

    public static class ExclusiveMultiColumnFilter implements ColumnFilter {
        private final List<ColumnFilter> filters;

        public ExclusiveMultiColumnFilter(List<ColumnFilter> filters) {
            this.filters = filters;
        }

        @Override
        public boolean isAcceptable(DTSingleRow row) {
            for (ColumnFilter filter : this.filters) {
                if(!filter.isAcceptable(row)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "ExclusiveMultiColumnFilter{" +
                    "filters=" + filters +
                    '}';
        }

        @SuppressWarnings("RedundantIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExclusiveMultiColumnFilter)) return false;

            final ExclusiveMultiColumnFilter that = (ExclusiveMultiColumnFilter) o;

            if (filters != null ? !filters.equals(that.filters) : that.filters != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return filters != null ? filters.hashCode() : 0;
        }
    }

    public static class EqualsColumnFilter implements ColumnFilter {
        final int colIndex;
        final String content;

        public EqualsColumnFilter(int colIndex, String content) {
            this.colIndex = colIndex;
            this.content = content;
        }

        @Override
        public boolean isAcceptable(DTSingleRow row) {
            return this.content.equalsIgnoreCase(row.getCells().get(this.colIndex).getContent());
        }

        @Override
        public String toString() {
            return "EqualsColumnFilter{" +
                    "colIndex=" + colIndex +
                    ", content='" + content + '\'' +
                    '}';
        }

        @SuppressWarnings("RedundantIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EqualsColumnFilter)) return false;

            final EqualsColumnFilter that = (EqualsColumnFilter) o;

            if (colIndex != that.colIndex) return false;
            if (content != null ? !content.equals(that.content) : that.content != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = colIndex;
            result = 31 * result + (content != null ? content.hashCode() : 0);
            return result;
        }
    }

    public static class SubstringColumnFilter implements ColumnFilter {
        final int colIndex;
        final String content;

        public SubstringColumnFilter(int colIndex, String content) {
            this.colIndex = colIndex;
            this.content = content != null ? content.toLowerCase() : null;
        }

        @Override
        public boolean isAcceptable(DTSingleRow row) {
            final String value = row.getCells().get(this.colIndex).getContent();
            return this.content != null && value != null && value.toLowerCase().contains(this.content);
        }

        @SuppressWarnings("RedundantIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SubstringColumnFilter)) return false;

            final SubstringColumnFilter that = (SubstringColumnFilter) o;

            if (colIndex != that.colIndex) return false;
            if (content != null ? !content.equals(that.content) : that.content != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = colIndex;
            result = 31 * result + (content != null ? content.hashCode() : 0);
            return result;
        }
    }

    public static class HasCodeColumnFilter implements ColumnFilter {
        final int colIndex;
        final String code;

        @SuppressWarnings("UnusedDeclaration")
        public HasCodeColumnFilter(int colIndex, HasCode hasCode) {
            this(colIndex, hasCode.getCode());
        }

        public HasCodeColumnFilter(int colIndex, String code) {
            this.colIndex = colIndex;
            this.code = code;
        }

        @Override
        public boolean isAcceptable(DTSingleRow row) {
            final MM item = row.getCells().get(this.colIndex).getItem();
            return item instanceof HasCode && StringUtil.equals(this.code, ((HasCode) item).getCode());
        }

        @SuppressWarnings("RedundantIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HasCodeColumnFilter)) return false;

            final HasCodeColumnFilter that = (HasCodeColumnFilter) o;

            if (colIndex != that.colIndex) return false;
            if (code != null ? !code.equals(that.code) : that.code != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = colIndex;
            result = 31 * result + (code != null ? code.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "HasCodeColumnFilter{" +
                    "colIndex=" + colIndex +
                    ", code='" + code + '\'' +
                    '}';
        }
    }

    private static class RowLinkRenderer implements CellRenderer {
        @Override
        public int getWidth() {
            return 16;
        }

        @Override
        public void appendStyle(StringBuilder sb1) {
            //nothing to do here
        }

        @Override
        public void appendSortStyle(StringBuilder sb1) {
            //nothing to do here
        }

        @Override
        public void appendHeader(StringBuilder sb1) {
            //nothing to do here
        }

        @Override
        public void appendRow(DTSingleRow row, boolean aggregated, StringBuilder sb) {
            final boolean rowWithShellMMInfoLink = isRowWithShellMMInfoLink(row);
            final boolean rowWithActivityLink = isRowWithActivityLink(row);

            if (rowWithShellMMInfoLink || rowWithActivityLink) {
                sb.append("<a class=\"mm-shellType-link\"");
                if(rowWithShellMMInfoLink) {
                    appendRowLinkShellMM(row.getObjectInfo(), sb);
                }
                if(rowWithActivityLink) {
                    appendRowWithActivityLink(row.getActivity(), sb);
                }
                sb.append('>');
                sb.append(IconImage.get("jump-to-url-12").getHTML());
                sb.append("</a>");
            }
            else {
                sb.append("&nbsp;");
            }
        }

        public void appendRowLinkShellMM(ShellMMInfo info, StringBuilder sb) {
            final ShellMMType type = info.getTyp();
            final boolean isSecurity = ShellMMTypeUtil.isSecurity(type);

            if (isSecurity && !StringUtil.hasText(info.getMMSecurityID())) {
                return;
            }

            sb.append(PM_SHELLTYPE_ATTRIBUTE_NAME).append("=\"").append(type.name()).append("\" ")
                    .append(PM_SHELLID_ATTRIBUTE_NAME).append("=\"").append(info.getId()).append("\" ");

            if (isSecurity) {
                sb.append(PM_SHELLMMSID_ATTRIBUTE_NAME).append("=\"").append(info.getMMSecurityID()).append("\"");
            }
        }

        public void appendRowWithActivityLink(ActivityInstanceInfo aii, StringBuilder sb) {
            sb.append(PM_ACTIVITYID_ATTRIBUTE_NAME).append("=\"").append(aii.getId()).append("\" ")
                    .append(PM_ACTIVITYNAME_ATTRIBUTE_NAME).append("=\"").append(aii.getDefinition().getName()).append("\" ");
        }
    }

    /**
     * Renders a complex (i.e., with more than one SubColum) column stack
     */
    private static class StackRenderer implements CellRenderer {
        private final CellRenderer[] renderers;

        private StackRenderer(DTColumnStack stack, DTTable table, int[] columnLengths) {
            final int lcd = getLeastCommonMultipleColspan(stack);
            this.renderers = new ComplexGroupRenderer[stack.getGroups().size()];
            for (int i = 0; i < this.renderers.length; i++) {
                final DTColumnGroup group = stack.getGroups().get(i);
                this.renderers[i] = new ComplexGroupRenderer(group, lcd, table, columnLengths);
            }
        }

        @Override
        public void appendStyle(StringBuilder sb1) {
            // empty
        }

        @Override
        public void appendSortStyle(StringBuilder sb1) {
            // empty
        }

        public void appendHeader(StringBuilder sb) {
            sb.append("<table style=\"width:100%\">");
            for (CellRenderer renderer : this.renderers) {
                sb.append("<tr>");
                renderer.appendHeader(sb);
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        @Override
        public void appendRow(DTSingleRow row, boolean aggregated, StringBuilder sb) {
            sb.append("<table style=\"width:100%\">");
            for (CellRenderer renderer : this.renderers) {
                sb.append("<tr>");
                renderer.appendRow(row, aggregated, sb);
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        @Override
        public int getWidth() {
            int result = 0;
            for (CellRenderer renderer : this.renderers) {
                result = Math.max(result, renderer.getWidth());
            }
            return result;
        }
    }

    /**
     * Renders a row in a complex column stack
     */
    private static class ComplexGroupRenderer implements CellRenderer {
        private final int colSpan;

        private final ColumnRenderer[] renderers;

        private final String columnWidthStr;

        private ComplexGroupRenderer(DTColumnGroup group, int lcd, DTTable table, int[] columnLengths) {
            this.colSpan = lcd / group.getSubColumns().size();
            this.renderers = new ColumnRenderer[group.getSubColumns().size()];
            for (int i = 0; i < this.renderers.length; i++) {
                this.renderers[i] = new ColumnRenderer(table, columnLengths, group.getSubColumns().get(i));
            }
            this.columnWidthStr = Float.toString(100 / this.renderers.length);
        }

        @Override
        public void appendStyle(StringBuilder sb1) {
            // empty
        }

        @Override
        public void appendSortStyle(StringBuilder sb1) {
            // empty
        }

        @Override
        public void appendHeader(StringBuilder sb) {
            for (ColumnRenderer renderer : this.renderers) {
                /*sb.append("<th align=\"center\"");*/
                sb.append("<th ");
                renderer.appendStyle(sb);
                appendWidthStyle(sb);
                renderer.appendSortStyle(sb);

                if (colSpan > 1) {
                    sb.append(" colspan=").append(this.colSpan);
                }
                sb.append(">");
                renderer.appendHeader(sb);
                sb.append("</th>");
            }
        }

        private void appendWidthStyle(StringBuilder sb) {
            if (sb.charAt(sb.length() - 1) == '"') {
                sb.deleteCharAt(sb.length() - 1);
            }
            else {
                sb.append("style=\"");
            }
            sb.append("width:").append(this.columnWidthStr).append("%;\"");
        }

        @Override
        public void appendRow(DTSingleRow row, boolean aggregated, StringBuilder sb) {
            for (ColumnRenderer renderer : this.renderers) {
                sb.append("<td ");
                renderer.appendStyle(sb);
                appendWidthStyle(sb);

                if (colSpan > 1) {
                    sb.append(" colspan=").append(this.colSpan);
                }
                sb.append(">");
                renderer.appendRow(row, aggregated, sb);
                sb.append("</td>");
            }
        }

        @Override
        public int getWidth() {
            int result = 0;
            for (ColumnRenderer renderer : this.renderers) {
                result += renderer.getWidth();
            }
            return result;
        }
    }

    /**
     * Renders a single cell in a complex column stack or is alone responsible for
     * rendering a simple column stack
     */
    private static class ColumnRenderer implements CellRenderer {
        private final int colIndex;
        private final DTColumnSpec spec;

        private int columnLength;

        public ColumnRenderer(DTTable table, int[] columnLengths, DTSubColumn dtSubColumn) {
            this.colIndex = Integer.parseInt(dtSubColumn.getColIndex());
            this.columnLength = columnLengths[this.colIndex];
            this.spec = table.getColumnSpecs().get(this.colIndex);
        }

        @Override
        public void appendStyle(StringBuilder sb) {
            appendAlign(this.spec, sb);
        }

        @Override
        public void appendSortStyle(StringBuilder sb) {
            sb.append(' ');
            sb.append(PM_COL_INDEX_ATTRIBUTE_NAME).append("=\"").append(this.colIndex).append('\"');
        }

        @Override
        public void appendHeader(StringBuilder sb) {
            append(this.spec.getCaption(), sb);
        }

        @Override
        public void appendRow(DTSingleRow row, boolean aggregated, StringBuilder sb) {
            final DTCell cell = row.getCells().get(this.colIndex);
            append(cell.getContent(), sb);
        }

        @Override
        public int getWidth() {
            return this.columnLength * CHAR_WIDTH_FACTOR + PADDING;
        }

        private void append(String text, StringBuilder sb) {
            sb.append(StringUtil.hasText(text) ? toEscapedLines(text) : "&nbsp;");
        }
    }

    private static final Set<ShellMMType> SHELL_MM_LINK_TYPES;

    private final DTTable table;

    private final ColumnFilter filter;

    private final StringBuilder sb = new StringBuilder();

    private final CellRenderer[] cellRenderers;

    private int[] columnLengths;

    private final int[] stackWidths;

    private final int tableWidth;

    private final boolean withAggregations;

    private final boolean prefiltered;

    private final String headTable;

    private final String bodyTable;

    private final Comparator<DTRowBlock> rowComparator;

    static {
        SHELL_MM_LINK_TYPES = new HashSet<>(ShellMMTypeUtil.getDepotObjectTypes());
        SHELL_MM_LINK_TYPES.addAll(ShellMMTypeUtil.getSecurityTypes());
    }

    public DTTableRenderer(DTTable table, Options options) {
        this(table, options, new RowLinkRenderer());
    }

    public DTTableRenderer(DTTable table, Options options, CellRenderer rowLinkRenderer) {
        this.table = table;
        this.withAggregations = options.isWithAggregations();
        this.prefiltered = options.isPrefiltered();
        final List<DTSortSpec> customSort = options.getCustomSort();
        this.filter = options.getFilter();
        this.rowLinkRenderer = rowLinkRenderer;

        if (customSort != null) {
            this.rowComparator = new DTTableUtils.RowComparator(customSort);
        }
        else {
            this.rowComparator = null;
        }

        this.columnLengths = new int[table.getColumnSpecs().size()];
        initColumnLengths();

        int numStacks = 0;
        for (DTColumnStack stack : table.getColumnStacks()) {
            if (isVisible(stack)) {
                numStacks++;
            }
        }

        this.cellRenderers = new CellRenderer[numStacks];
        int c = 0;
        for (DTColumnStack stack : table.getColumnStacks()) {
            if (isVisible(stack)) {
                if (isComplex(stack)) {
                    this.cellRenderers[c++] = new StackRenderer(stack, table, columnLengths);
                }
                else {
                    this.cellRenderers[c++] = new ColumnRenderer(table, columnLengths, stack.getGroups().get(0).getSubColumns().get(0));
                }
            }
        }

        int widthSum = 0;
        this.stackWidths = new int[numStacks];
        for (int i = 0; i < stackWidths.length; i++) {
            stackWidths[i] = this.cellRenderers[i].getWidth();
            widthSum += stackWidths[i];
        }
        widthSum += this.rowLinkRenderer.getWidth();
        this.tableWidth = (numStacks + 1) * BORDER_WIDTH + widthSum;

        final long start = System.currentTimeMillis();
        final String colgroup = renderColgroup();

        this.headTable = renderHead(colgroup);
        final long afterHeader = System.currentTimeMillis();

        this.bodyTable = renderBody(colgroup);
        final long end = System.currentTimeMillis();

        Firebug.debug("<DTTableRenderer> render header: " + (afterHeader - start) + "ms render body: " + (end - afterHeader) + "ms total: " + (end - start) + "ms");
    }

    public SafeHtml getCaption() {
        return SafeHtmlUtils.fromString(this.table.getCaption());
    }

    public SafeHtml getHeadTable() {
        return SafeHtmlUtils.fromTrustedString(this.headTable);
    }

    public SafeHtml getBodyTable() {
        return SafeHtmlUtils.fromTrustedString(this.bodyTable);
    }

    private void initColumnLengths() {
        for (int i = 0; i < columnLengths.length; i++) {
            columnLengths[i] = maxLineLength(table.getColumnSpecs().get(i).getCaption());
        }
        adjustColumnLengths(table.getToplevelGroup());
    }

    private void adjustColumnLengths(DTRowBlock row) {
        if (row instanceof DTSingleRow) {
            adjustColumnLengths((DTSingleRow) row);
        }
        else if (row instanceof DTRowGroup) {
            adjustColumnLengths((DTRowGroup) row);
        }
    }

    private void adjustColumnLengths(DTSingleRow row) {
        if (!isAcceptable(row)) {
            return;
        }
        for (int i = 0; i < this.columnLengths.length; i++) {
            int maxLineLength = maxLineLength(row.getCells().get(i).getContent());
            // for retests use a table that contains the custom security AQ000WWWWWW6 (WKN: WWWWWW)
            if(maxLineLength < 7) { //should also apply for columns that contain a WKN (6 chars)
                maxLineLength = (maxLineLength * 165) / 100;
            }
            else if(maxLineLength < 13) { //should also apply for columns that contain an ISIN (12 chars)
                maxLineLength = (maxLineLength * 130) / 100;
            }
            this.columnLengths[i] = Math.max(this.columnLengths[i],
                    maxLineLength);
        }
    }

    private boolean hasAcceptableRows(DTRowGroup rowGroup) {
        return !getFilteredRows(rowGroup).isEmpty();
    }

    private boolean isAcceptable(DTSingleRow row) {
        return this.filter == null || this.filter.isAcceptable(row);
    }

    private void adjustColumnLengths(DTRowGroup rowGroup) {
        for (DTRowBlock grouped : rowGroup.getGroupedRows()) {
            adjustColumnLengths(grouped);
        }
        if (this.withAggregations && rowGroup.getAggregatedRow() != null) {
            adjustColumnLengths(rowGroup.getAggregatedRow());
        }
    }

    private boolean isVisible(DTColumnStack stack) {
        return !this.withAggregations
                || isComplex(stack)
                || !isGroupColumn(stack.getGroups().get(0).getSubColumns().get(0).getColIndex());
    }

    private boolean isGroupColumn(String idx) {
        for (DTGroupSpec spec : table.getGroupSpecs()) {
            if (idx.equals(spec.getColIndex())) {
                return true;
            }
        }
        return false;
    }

    private boolean isComplex(DTColumnStack stack) {
        return stack.getGroups().size() > 1 || stack.getGroups().get(0).getSubColumns().size() > 1;
    }

    private String renderColgroup() {
        this.sb.setLength(0);
        this.sb.append("<colgroup>");
        this.sb.append("<col style=\"width: ").append(this.rowLinkRenderer.getWidth()).append("px\">");
        for (int stackWidth : this.stackWidths) {
            this.sb.append("<col style=\"width: ").append(stackWidth).append("px\">");
        }
        this.sb.append("</colgroup>\n");
        return sb.toString();
    }

    private String renderHead(String colgroup) {
        this.sb.setLength(0);
        this.sb.append("<table class=\"dt-table\" style=\"width: ").append(this.tableWidth).append("px\">")
                .append(colgroup)
                .append("<thead>");
        sb.append("<tr>");

        sb.append("<th>");
        rowLinkRenderer.appendHeader(sb);
        sb.append("</th>");

        for (CellRenderer renderer : cellRenderers) {
            sb.append("<th");
            renderer.appendSortStyle(sb);
            sb.append(">");
            renderer.appendHeader(sb);
            sb.append("</th>");
        }
        sb.append("</tr>\n");
        sb.append("</thead></table>");
        return sb.toString();
    }

    private String renderBody(String colgroup) {
        this.sb.setLength(0);
        this.sb.append("<table class=\"dt-table\" style=\"width: ").append(this.tableWidth).append("px\">")
                .append(colgroup)
                .append("<tbody>");
        addBody();
        sb.append("</tbody></table>");
        return sb.toString();
    }

    private void addBody() {
        final int topGroupLevelClassSuffix = this.table.getGroupSpecs().size() - 1;
        if (this.withAggregations || this.rowComparator == null) {
            addRow(this.table.getToplevelGroup(), 0, topGroupLevelClassSuffix);
        }
        else {
            for (DTRowBlock block : getSortedRows(this.table.getToplevelGroup())) {
                addRow(block, 0, topGroupLevelClassSuffix);
            }
        }
    }

    private List<DTRowBlock> getFilteredRows(DTRowGroup group) {
        if (this.filter == null) {
            return group.getGroupedRows();
        }
        List<DTRowBlock> result = new ArrayList<>();
        addFilteredRows(group, result);
        return result;
    }

    private void addFilteredRows(DTRowGroup group, List<DTRowBlock> result) {
        for (DTRowBlock block : group.getGroupedRows()) {
            if (block instanceof DTSingleRow) {
                if (isAcceptable((DTSingleRow) block)) {
                    result.add(block);
                }
            }
            else {
                addFilteredRows((DTRowGroup) block, result);
            }
        }
    }

    private List<DTRowBlock> getSortedRows(final DTRowGroup group) {
        final List<DTRowBlock> result;
        if (this.filter != null) {
            result = getFilteredRows(group);
        }
        else {
            if (this.withAggregations) {
                result = new ArrayList<>(group.getGroupedRows());
            }
            else {
                //Avoid class cast exception in comparator if DTRowBlock is of type DTRowGroup
                result = expandAllDTRowGroups(group.getGroupedRows());
            }
        }

        Collections.sort(result, this.rowComparator);
        return result;
    }

    private List<DTRowBlock> expandAllDTRowGroups(List<DTRowBlock> in) {
        return DTTableUtils.expandAllDTRowGroups(in);
    }

    private void addRow(DTRowBlock row, int groupLevel, int groupLevelClassSuffix) {
        if (row instanceof DTSingleRow) {
            add((DTSingleRow) row, false, groupLevelClassSuffix);
            return;
        }

        final DTRowGroup group = (DTRowGroup) row;
        if (this.withAggregations && group.getHead() != null) {
            if (!hasAcceptableRows(group)) {
                return;
            }
            onBeforeAggregation(group, groupLevel, groupLevelClassSuffix);
        }

        final int nextGroupLevel = groupLevel + 1;
        final int nextGroupLevelClassSuffix = groupLevelClassSuffix > 0 ? groupLevelClassSuffix - 1 : 0;

        List<DTRowBlock> groupedRows = getRowBlocks(group, nextGroupLevel);
        for (DTRowBlock grouped : groupedRows) {
            addRow(grouped, nextGroupLevel, nextGroupLevelClassSuffix);
        }

        if (this.withAggregations && this.filter == null && group.getAggregatedRow() != null && !this.prefiltered) {
            onAfterAggregation(group, groupLevelClassSuffix);
        }
    }

    private List<DTRowBlock> getRowBlocks(DTRowGroup rowGroup, int rowGroupLevel) {
        if (this.withAggregations && isLeafLevel(rowGroupLevel)) {
            return (this.rowComparator != null) ? getSortedRows(rowGroup) : getFilteredRows(rowGroup);
        }
        return rowGroup.getGroupedRows();
    }

    private boolean isLeafLevel(int level) {
        return level == this.table.getGroupSpecs().size();
    }

    private void onBeforeAggregation(DTRowGroup rowGroup, int groupLevel, int groupLevelClassSuffix) {
        if (rowGroup.getHead() != null && StringUtil.hasText(rowGroup.getHead().getContent())) {
            sb.append("<tr class=\"aggr before-").append(groupLevelClassSuffix);
            if (groupLevel > 1) {
                sb.append(" header-indent-").append(groupLevel - 1);
            }
            sb.append("\">")
                    .append("<td style=\"text-align:left;\" ")
                    .append("colspan=\"").append(this.cellRenderers.length + 1).append("\">")  //+1 bec. of. first col with link icon
                    .append(toEscapedLines(rowGroup.getHead().getContent()))
                    .append("</td></tr>\n");
        }
    }

    private void onAfterAggregation(DTRowGroup rowGroup, int groupLevelClassSuffix) {
        add(rowGroup.getAggregatedRow(), true, groupLevelClassSuffix);
    }

    private void add(DTSingleRow row, boolean aggregated, int groupLevelClassSuffix) {
        sb.append("<tr ").append(PM_ROW_MARKER_ATTRIBUTE_NAME).append("=\"").append(PM_ROW_MARKER_ATTRIBUTE_NAME).append("\"");

        if (aggregated) {
            sb.append(" class=\"")
                    .append("aggr after-").append(groupLevelClassSuffix)
                    .append("\"");
        }

        sb.append(">");

        sb.append("<td ");
        rowLinkRenderer.appendStyle(sb);
        sb.append(">");
        rowLinkRenderer.appendRow(row, aggregated, sb);
        sb.append("</td>");

        for (CellRenderer renderer : cellRenderers) {
            sb.append("<td ");
            renderer.appendStyle(sb);
            sb.append(">");
            renderer.appendRow(row, aggregated, sb);
            sb.append("</td>");
        }
        sb.append("</tr>\n");
    }

    private static boolean isRowWithActivityLink(DTSingleRow row) {
        final ActivityInstanceInfo aii = row.getActivity();
        return aii != null &&
                aii.getId() != null &&
                !"-1".equals(aii.getId());  // $NON-NLS$
    }

    private static boolean isRowWithShellMMInfoLink(DTSingleRow row) {
        final ShellMMInfo info = row.getObjectInfo();
        return info != null &&
                SHELL_MM_LINK_TYPES.contains(info.getTyp()) &&
                info.getId() != null &&
                !"-1".equals(info.getId()); // $NON-NLS$
    }

    private static String toEscapedLines(String text) {
        return SafeHtmlUtils.htmlEscape(text).replaceAll("\n", "<br/>");
    }

    private static void appendAlign(DTColumnSpec spec, StringBuilder sb) {
        switch (spec.getAlignment()) {
            case AT_LEFT:
                sb.append(" style=\"text-align:left;\"");
                break;
            case AT_CENTER:
                sb.append(" style=\"text-align:center;\"");
                break;
            case AT_RIGHT:
// right is default, no need for style
                break;
        }
    }

    private static int getLeastCommonMultipleColspan(DTColumnStack stack) {
        int lcd = 0;
        for (DTColumnGroup group : stack.getGroups()) {
            if (lcd == 0) {
                lcd = group.getSubColumns().size();
            }
            else {
                lcd = lcd(lcd, group.getSubColumns().size());
            }
        }
        return lcd;
    }

    private static int maxLineLength(String s) {
        if (!StringUtil.hasText(s)) {
            return 0;
        }
        int n = 0;
        int from = 0;
        int to;
        while ((to = s.indexOf('\n', from)) >= 0) {
            n = Math.max(n, to - from);
            from = to + 1;
        }
        return Math.max(n, s.length() - from);
    }

    private static int lcd(int a, int b) {
        return a * b / gcd(a, b);
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            if (a > b) {
                a = a - b;
            }
            else {
                b = b - a;
            }
        }
        return a;
    }
}
