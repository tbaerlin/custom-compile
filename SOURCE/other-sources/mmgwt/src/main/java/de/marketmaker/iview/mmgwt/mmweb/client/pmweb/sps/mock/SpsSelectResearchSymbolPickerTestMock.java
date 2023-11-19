/*
 * SpsSelectResearchSymbolPickerTestMock.java
 *
 * Created on 18.07.2014 13:12
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addReadOnlyWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addToParent;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootSectionWidget;

/**
 * @author mdick
 */
@NonNLS
public class SpsSelectResearchSymbolPickerTestMock  extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        final EditWidgetDesc picker = new EditWidgetDesc();
        picker.setCaption("Instrument");
        picker.setBind("/instrument");
        picker.setStyle("pickResearchSymbol");
        picker.setLayoutGUID("A99F9E9B56994CF8B143A10D4886B794");
        addToParent(picker, s);

        addReadOnlyWidget(s, "/instrument", "Description", "description");
        addReadOnlyWidget(s, "/instrument", "Type", "type");
        addReadOnlyWidget(s, "/instrument", "WKN", "number");
        addReadOnlyWidget(s, "/instrument", "ISIN", "isin");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();

        MockUtil.addDecl(decl, parent, "instrument", TiType.TI_SHELL_MM);
    }
}
