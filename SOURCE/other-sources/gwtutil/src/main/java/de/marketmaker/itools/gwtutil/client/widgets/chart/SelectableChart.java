package de.marketmaker.itools.gwtutil.client.widgets.chart;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * a chart with elements that can be selected/highlighted by an external component
 */
public interface SelectableChart {

    void setSelectedValue(Index index);

    class Util {
        public static HTML createLegendElement(final SelectableChart chart, final int row, String style, String label, String value) {
            final SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.appendHtmlConstant("<div class=\"" + style + "\">&nbsp;</div><div class=\"name\">");
            sb.appendEscaped(label);
            sb.appendHtmlConstant("</div>");
            sb.appendHtmlConstant("<div class=\"value\">");
            sb.appendEscaped(value);
            sb.appendHtmlConstant("</div>");
            final HTML html = new HTML(sb.toSafeHtml());
            html.setStyleName("legendEntry");

            html.addMouseOverHandler(event -> {
                chart.setSelectedValue(new Index(row, 0, true));
                html.addStyleName("hover"); // $NON-NLS$
            });
            html.addMouseOutHandler(event -> {
                chart.setSelectedValue(new Index(row, 0, false));
                html.removeStyleName("hover"); // $NON-NLS$
            });
            return html;
        }
    }

    class Index {
        private final int entryIndex;
        private final int valueIndex;
        private final boolean selected;

        public Index(int entryIndex, int valueIndex, boolean selected) {
            this.entryIndex = entryIndex;
            this.valueIndex = valueIndex;
            this.selected = selected;
        }

        public int getEntryIndex() {
            return entryIndex;
        }

        public int getValueIndex() {
            return valueIndex;
        }

        public boolean isSelected() {
            return selected;
        }

        public Index withSelected(boolean selected) {
            return new Index(this.entryIndex, this.valueIndex, selected);
        }
    }

    abstract class LegendHighlighter implements SelectionHandler<Index> {
        private Index selectedIndex = null;

        @Override
        public void onSelection(SelectionEvent<Index> event) {
            if (this.selectedIndex != null) {
                getWidget(this.selectedIndex).removeStyleName("hover"); // $NON-NLS$
            }
            final Index selectedItem = event.getSelectedItem();
            this.selectedIndex = selectedItem.isSelected() ? selectedItem : null;
            if (this.selectedIndex != null) {
                getWidget(this.selectedIndex).addStyleName("hover"); // $NON-NLS$
            }
        }

        protected abstract Widget getWidget(Index row);
    }

    @SuppressWarnings("unused")
    class DefaultLegendHighlighter extends LegendHighlighter {
        private final FlowPanel legendContainer;

        public DefaultLegendHighlighter(FlowPanel legendContainer) {
            this.legendContainer = legendContainer;
        }

        @Override
        protected Widget getWidget(Index index) {
            return this.legendContainer.getWidget(index.getEntryIndex());
        }
    }
}
