/*
 * SpsDecimalEditTestMock.java
 *
 * Created on 26.06.2014 12:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addDecl;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEditWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.errorMM;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.pti;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootSectionWidget;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsDecimalEditTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        addEditWidget(s, "default", "Default");
        addEditWidget(s, "mandatory", "Mandatory");
        addEditWidget(s, "percent", "Percent");
        addEditWidget(s, "minMax", "Min-Max");
        addEditWidget(s, "vUnit0", "V-Unit 0");
        addEditWidget(s, "vUnit1", "V-Unit 1");
        addEditWidget(s, "vUnit10", "V-Unit 10");
        addEditWidget(s, "vUnit100", "V-Unit 100");
        addEditWidget(s, "vUnit1000", "V-Unit 1000");
        addEditWidget(s, "vUnit10000", "V-Unit 10000");
        addEditWidget(s, "spin", "Spin");
        addEditWidget(s, "mix1", "Min-Max V-Unit Spin");
        addEditWidget(s, "mix2", "Mandatory Min-Max V-Unit Spin");
        addEditWidget(s, "mix3", "Mandatory Percent Min-Max V-Unit Spin");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();

        addDecl(decl, parent, "default", pti(TiType.TI_NUMBER, "Default", false, false, "0", "0", "0", "0", 0, ""));
        addDecl(decl, parent, "mandatory", pti(TiType.TI_NUMBER, "Mandatory", false, true, "0", "0", "0", "0", 0, ""));
        addDecl(decl, parent, "percent", pti(TiType.TI_NUMBER, "Percent", true, false, "0", "0", "0", "0", 0, ""));
        addDecl(decl, parent, "minMax", pti(TiType.TI_NUMBER, "Min-Max", false, false, "10", "20", "0", "0", 0, "10-20"));
        addDecl(decl, parent, "vUnit0", pti(TiType.TI_NUMBER, "V-Unit = 0", false, false, "0", "0", "0", "0", 0, "vUnit 0"));
        addDecl(decl, parent, "vUnit1", pti(TiType.TI_NUMBER, "V-Unit = 1", false, false, "0", "0", "1", "0", 0, "vUnit 1"));
        addDecl(decl, parent, "vUnit10", pti(TiType.TI_NUMBER, "V-Unit = 10", false, false, "0", "0", "10", "0", 0, "vUnit 10"));
        addDecl(decl, parent, "vUnit100", pti(TiType.TI_NUMBER, "V-Unit = 100", false, false, "0", "0", "100", "0", 0, "vUnit 100"));
        addDecl(decl, parent, "vUnit1000", pti(TiType.TI_NUMBER, "V-Unit = 1000", false, false, "0", "0", "1000", "0", 0, "vUnit 1000"));
        addDecl(decl, parent, "vUnit10000", pti(TiType.TI_NUMBER, "V-Unit = 10000", false, false, "0", "0", "10000", "0", 0, "vUnit 10000"));
        addDecl(decl, parent, "spin", pti(TiType.TI_NUMBER, "Spin", false, false, "0", "0", "0", "128", 0, "Spin 128"));
        addDecl(decl, parent, "mix1", pti(TiType.TI_NUMBER, "Min-Max V-Unit Spin", false, false, "0", "1024", "1000", "128", 0, "false, false, \"0\", \"1024\", \"1000\", \"128\""));
        addDecl(decl, parent, "mix2", pti(TiType.TI_NUMBER, "Mandatory Min-Max V-Unit Spin", false, true, "0", "1024", "1000", "128", 0, "false, true, \"0\", \"1024\", \"1000\", \"128\""));
        addDecl(decl, parent, "mix3", pti(TiType.TI_NUMBER, "Mandatory Percent Min-Max V-Unit Spin", true, true, "0.16", "0.96", "100", "0.08", 0, "true, true, \"0.16\", \"0.96\", \"100\", \"0.08\""));
    }

    @Override
    public void preProcess(List<ErrorMM> errors) {
        errors.clear();
        errors.add(errorMM("/mandatory", "/mandatory", "A mandatory test error"));
        errors.add(errorMM("/minMax", "/minMax", "A minMax test error"));
        errors.add(errorMM("/mix3", "/mix3", "A mix3 test error"));
    }
}
