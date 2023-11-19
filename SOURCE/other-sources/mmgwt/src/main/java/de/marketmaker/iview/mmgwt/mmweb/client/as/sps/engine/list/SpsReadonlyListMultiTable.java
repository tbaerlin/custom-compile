package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.user.client.ui.FlexTable;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;

import java.util.List;

/**
 * Author: umaurer
 * Created: 16.05.14
 */
public class SpsReadonlyListMultiTable extends SpsBoundWidget<FlexTable, SpsListProperty> implements NoValidationPopup {
    private final String keyField;
    private final String[] dataFields;
    private final SpsListBindFeature spsListBindFeature;

    public SpsReadonlyListMultiTable(Context context, BindToken parentToken, BindToken itemsBindToken, String keyField, List<ListWidgetDescColumn> columns) {
        this.spsListBindFeature = new SpsListBindFeature(context, parentToken, itemsBindToken) {
            @Override
            public void onChange() {
                onPropertyChange();
            }
        };
        this.keyField = keyField;
        this.dataFields = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            this.dataFields[i] = columns.get(i).getFieldName();
        }
    }

    @Override
    public boolean isReadonly() {
        return true;
    }

    @Override
    public void release() {
        super.release();
        this.spsListBindFeature.release();
    }

    @Override
    public void onPropertyChange() {
        assert !isReleased() : "already released!";

        final FlexTable table = getWidget();
        table.clear(true);
        final SpsListProperty listProperty = getBindFeature().getSpsProperty();
        int row = 0;
        for (SpsProperty spsProperty : listProperty.getChildren()) {
            final String key = ((SpsLeafProperty) spsProperty).getStringValue();
            final SpsGroupProperty gp = this.spsListBindFeature.getProperty(this.keyField, key);
            if (gp != null) {
                for (int i = 0; i < dataFields.length; i++) {
                    table.setText(row, i, ((SpsLeafProperty)gp.get(this.dataFields[i])).getStringValue());
                }
                row++;
            }
        }
        if (row == 0) {
            for (int i = 0; i < dataFields.length; i++) {
                table.setHTML(row, i, "&nbsp;"); // $NON-NLS$
            }
        }
    }

    @Override
    protected FlexTable createWidget() {
        final FlexTable table = new FlexTable();
        table.setStyleName("sps-list-ro-table");
        table.setCellSpacing(0);
        table.setCellPadding(0);
        return table;
    }
}
