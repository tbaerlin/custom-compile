package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.user.client.ui.Label;
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
public class SpsReadonlyListSingleSelect<P extends SpsProperty> extends SpsBoundWidget<Label, P> implements NoValidationPopup {
    private final String columnsKeyField;
    private final List<ListWidgetDescColumn> columns;
    private final SpsListBindFeature spsListBindFeature;

    public SpsReadonlyListSingleSelect(Context context, BindToken parentToken, BindToken itemsBindToken, String columnsKeyField, List<ListWidgetDescColumn> columns) {
        this.spsListBindFeature = new SpsListBindFeature(context, parentToken, itemsBindToken) {
            @Override
            public void onChange() {
                onPropertyChange();
            }
        };
        this.columnsKeyField = columnsKeyField;
        this.columns = columns;
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
        final String selectedKey = getPropertyValue();
        final SpsGroupProperty gp = this.spsListBindFeature.getProperty(this.columnsKeyField, selectedKey);
        if (gp == null) {
            getWidget().setText("");
            return;
        }
        getWidget().setText(SpsListSingleSelect.getText(gp, this.columns));
    }

    public String getPropertyValue() {
        final P p = getBindFeature().getSpsProperty();
        if (p instanceof SpsLeafProperty) {
            return ((SpsLeafProperty) p).getStringValue();
        }
        else if (p instanceof SpsListProperty) {
            final SpsListProperty lp = (SpsListProperty) p;
            if (lp.getChildCount() < 1) {
                return null;
            }
            return ((SpsLeafProperty) lp.get(0)).getStringValue();
        }
        return null;
    }

    @Override
    protected Label createWidget() {
        final Label label = new Label();
        label.setStyleName("sps-ro-field");
        return label;
    }
}
