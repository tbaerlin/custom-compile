/*
 * AbstractSpsListMultiBase.java
 *
 * Created on 23.04.2015 12:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsUtil;

/**
 * @author mdick
 */
public abstract class AbstractSpsListMultiBase<W extends Widget> extends AbstractSpsListBase<W, SpsListProperty>
        implements PopupTableSelectionHelper.Callback {

    public AbstractSpsListMultiBase(Context context, BindToken parentToken, BindToken itemsBindToken, String keyField) {
        super(context, parentToken, itemsBindToken, keyField);
    }

    public void addSelection(String key) {
        final SpsGroupProperty entry = getEntry(key);
        if(entry == null) {
            return;
        }

        final SpsListProperty listProperty = getBindFeature().getSpsProperty();
        final SpsLeafProperty leafProperty = new SpsLeafProperty("", listProperty, SpsUtil.getChildParsedTypeInfo(this.context, listProperty));
        leafProperty.setValue(((SpsLeafProperty)entry.get(this.keyField)).getDataItem(), true, false);
        listProperty.add(leafProperty, true);
    }

    public void removeProperty(int index) {
        getBindFeature().getSpsProperty().remove(index, true, true);
    }
}
