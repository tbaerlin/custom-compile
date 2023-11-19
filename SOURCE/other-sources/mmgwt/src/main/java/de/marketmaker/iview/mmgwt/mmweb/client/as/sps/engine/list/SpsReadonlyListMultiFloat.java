package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;

/**
 * Author: umaurer
 * Created: 16.05.14
 */
public class SpsReadonlyListMultiFloat extends SpsBoundWidget<FlowPanel, SpsListProperty> implements NoValidationPopup {
    private final String keyField;
    private final String dataField;
    private final SpsListBindFeature spsListBindFeature;

    public SpsReadonlyListMultiFloat(Context context, BindToken parentToken, BindToken itemsBindToken, String keyField, ListWidgetDescColumn column) {
        this.spsListBindFeature = new SpsListBindFeature(context, parentToken, itemsBindToken) {
            @Override
            public void onChange() {
                onPropertyChange();
            }
        };
        this.keyField = keyField;
        this.dataField = column.getFieldName();
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
        final FlowPanel panel = getWidget();
        final SpsListProperty listProperty = getBindFeature().getSpsProperty();
        for (SpsProperty spsProperty : listProperty.getChildren()) {
            final String key = ((SpsLeafProperty) spsProperty).getStringValue();
            final SpsGroupProperty gp = this.spsListBindFeature.getProperty(this.keyField, key);
            if (gp != null) {
                final InlineLabel label = new InlineLabel(((SpsLeafProperty)gp.get(this.dataField)).getStringValue());
                label.setStyleName("sps-list-ro-entry");
                panel.add(label);
            }
        }
    }

    @Override
    protected FlowPanel createWidget() {
        return new FlowPanel();
    }
}
