/*
 * SpsSelectDepotSymbolPickerTestMock.java
 *
 * Created on 28.07.2014 11:45
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.EditWidgetWithObjectDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addDecl;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEditWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addShellMMInfo;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addReadOnlyWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addToParent;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootSectionWidget;

/**
 * @author mdick
 */
@NonNLS
public class SpsSelectDepotSymbolPickerTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        addEditWidget(s, "/depot", "Depot");

        final EditWidgetWithObjectDesc picker1 = new EditWidgetWithObjectDesc();
        picker1.setBind("/instrument");
        picker1.setObjectBind("/depot");
        picker1.setStyle("pickDepotSymbol");
        addToParent(picker1, s);

        final EditWidgetWithObjectDesc picker2 = new EditWidgetWithObjectDesc();
        picker2.setBind("/instrument");
        picker2.setObjectBind("/depot");
        picker2.setStyle("pickDepotSymbol");
        picker2.setCaption("with Button Caption");
        addToParent(picker2, s);

        final EditWidgetWithObjectDesc picker3 = new EditWidgetWithObjectDesc();
        picker3.setBind("/instrument");
        picker3.setObjectBind(""); //empty bind or not bound. should use the main object of the activity as fallback
        picker3.setStyle("pickDepotSymbol");
        picker3.setCaption("Fallback to main object");
        addToParent(picker3, s);

        addEditWidget(s, "/depotId", "Depot-ID");
        final EditWidgetWithObjectDesc picker4 = new EditWidgetWithObjectDesc();
        picker4.setBind("/instrument");
        picker4.setObjectBind("/depotId");
        picker4.setStyle("pickDepotSymbol");
        picker4.setCaption("Bound object is depot ID");
        addToParent(picker4, s);

        addReadOnlyWidget(s, "/instrument", "Description", "description");
        addReadOnlyWidget(s, "/instrument", "Type", "type");
        addReadOnlyWidget(s, "/instrument", "WKN", "number");
        addReadOnlyWidget(s, "/instrument", "ISIN", "isin");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();
        addShellMMInfo(decl, "depot", TiType.TI_FOLDER, new ShellMMType[]{ShellMMType.ST_DEPOT}, parent, new DefaultMM());
        addDecl(decl, parent, "depotId", TiType.TI_NUMBER);
        addDecl(decl, parent, "instrument", TiType.TI_SHELL_MM);
    }
}
