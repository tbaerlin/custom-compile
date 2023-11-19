package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import de.marketmaker.itools.gwtutil.client.widgets.chart.SelectableChart;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.event.SpsHoverEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.event.SpsHoverHandler;

import java.util.List;

/**
 * Author: umaurer
 * Created: 09.12.14
 */
public class SpsListHoverHandler implements SpsHoverHandler {
    private final SpsWidget spsWidget;
    private final String itemsBindTokenPrefix;
    private final int entryIndexPos;
    private final List<String> listBindKeyValue;
    private SelectableChart selectable;

    public SpsListHoverHandler(SpsWidget spsWidget, BindToken itemsBindToken, List<String> listBindKeyValue) {
        this.spsWidget = spsWidget;
        this.listBindKeyValue = listBindKeyValue;
        this.itemsBindTokenPrefix = itemsBindToken.toString() + "[";
        this.entryIndexPos = this.itemsBindTokenPrefix.length();
    }

    public void setSelectable(SelectableChart selectable) {
        this.selectable = selectable;
    }

    @Override
    public void onHover(SpsHoverEvent event) {
        if (this.spsWidget == event.getWidgetSource()) {
            return;
        }
        if (this.selectable == null) {
            return;
        }
        final String bindKeys = event.getBindKeys();
        if (bindKeys.startsWith(this.itemsBindTokenPrefix)) {
            final int pos = bindKeys.indexOf(']', this.entryIndexPos);
            final int entryIndex = Integer.parseInt(bindKeys.substring(this.entryIndexPos, pos));
            final int valueIndex = getValueIndex(bindKeys.substring(pos + 2)); // rest of the bindKeys after ']/'
            this.selectable.setSelectedValue(new SelectableChart.Index(entryIndex, valueIndex, event.isSelected()));
        }
    }

    private int getValueIndex(String valueFieldName) {
        for (int i = 0, listBindKeyValue1Size = this.listBindKeyValue.size(); i < listBindKeyValue1Size; i++) {
            if (valueFieldName.equals(this.listBindKeyValue.get(i))) {
                return i;
            }
        }
        return -1;
    }

}
