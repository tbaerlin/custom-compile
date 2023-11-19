/*
 * TextWithKeyItemMapper.java
 *
 * Created on 14.10.13 18:16
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.mappers;

import de.marketmaker.iview.mmgwt.mmweb.client.widgets.MappedListBox;
import de.marketmaker.iview.pmxml.TextWithKey;

/**
 * @author Markus Dick
 */
public class TextWithKeyItemMapper implements MappedListBox.ItemMapper<TextWithKey>{
    @Override
    public String getLabel(TextWithKey item) {
        if(item == null) return "null"; //$NON-NLS$
        return item.getText();
    }

    @Override
    public String getValue(TextWithKey item) {
        if(item == null) return "null"; //$NON-NLS$
        return item.getKey();
    }
}
