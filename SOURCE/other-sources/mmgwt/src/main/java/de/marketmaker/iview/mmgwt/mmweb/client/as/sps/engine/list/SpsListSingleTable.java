package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
 * Author: umaurer
 * Created: 03.04.14
 */
@NonNLS
public class SpsListSingleTable<P extends SpsProperty> extends AbstractSpsListSingleTable<FlowPanel, P> implements PopupTableSelectionHelper.Callback, HasFocusHandlers, HasBlurHandlers {
    private FlowPanel panel = new FlowPanel();
    private InlineHTML labelSelectedValue = new InlineHTML();

    public SpsListSingleTable(Context context, BindToken parentToken, BindToken itemsBindToken, String columnsKeyField, List<ListWidgetDescColumn> columns) {
        super(context, parentToken, itemsBindToken, columnsKeyField, columns);

        this.labelSelectedValue.setStyleName("label");
        this.panel.setStyleName("as-button as-button-inline sps-list-single");
        this.panel.add(this.labelSelectedValue);

        final Image imagePopupTrigger = new Image("clear.cache.gif");
        imagePopupTrigger.setStyleName("menu-trigger");
        this.panel.add(imagePopupTrigger);
        initFocusSupport(this.panel);
        this.panel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                showList();
            }
        }, ClickEvent.getType());
    }

    @Override
    public void onPropertyChange() {
        if (!this.spsListBindFeature.isPropertySet()) {
            return;
        }
        final String key = getPropertyValue();
        final SpsGroupProperty entry = getEntry(key);
        if (entry == null) {
            this.labelSelectedValue.setHTML(TextUtil.NO_SELECTION_TEXT);
        }
        else {
            this.labelSelectedValue.setText(SpsListSingleSelect.getText(entry, this.columns));
        }
    }

    @Override
    protected FlowPanel createWidget() {
        return this.panel;
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
}
