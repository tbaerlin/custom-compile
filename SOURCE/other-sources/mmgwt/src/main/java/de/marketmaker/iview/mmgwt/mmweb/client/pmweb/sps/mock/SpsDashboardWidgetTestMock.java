/*
 * SpsDashboardWidgetTestMock.java
 *
 * Created on 02.10.2015 07:39
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addAnalysisWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addDecl;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addGroup;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.mmString;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootSectionWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.pti;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.readonly;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsDashboardWidgetTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);
        s.setStyle("SingleAnalysis");
        s.setCaption("A Caption"); // the caption of the section should not be shown.
        s.setDescription("A Description"); // the description should not be shown.
        readonly(addAnalysisWidget(s, "dashboard", "Dashboard", false));
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();

        final DataContainerGroupNode dashboard = addGroup(parent, "dashboard");
        addDecl(decl, dashboard, "Id", pti(TiType.TI_STRING), mmString("26466801"));
        addDecl(decl, dashboard, "AnalysisId", pti(TiType.TI_STRING), mmString("37935F3668984A8FAF26CFB93F32DE6A"));
    }
}
