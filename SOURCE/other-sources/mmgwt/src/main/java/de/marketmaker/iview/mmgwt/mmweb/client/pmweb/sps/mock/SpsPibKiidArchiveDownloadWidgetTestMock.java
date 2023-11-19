/*
 * SpsPibKiidArchiveDownloadWidgetTestMock.java
 *
 * Created on 16.09.2014 11:14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ShellMMTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addDecl;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEditWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEditWidgetWithObject;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.mmString;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.pti;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootSectionWidget;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsPibKiidArchiveDownloadWidgetTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        addEditWidget(s, "/instrument", "Instrument");
        addEditWidgetWithObject(s, "/archiveId", "/instrument", "Archive PIB/KIID", "pibKiidArchiveDownload");
        addEditWidget(s, "/archiveId", "Archive-Id");

        addEditWidgetWithObject(s, "/existingArchiveIdStock", "", "Archived PIB/KIID Stock", "pibKiidArchiveDownload");
        addEditWidgetWithObject(s, "/existingArchiveIdFond", "", "Archived PIB/KIID Fond", "pibKiidArchiveDownload");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();

        final ParsedTypeInfo pti = pti(TiType.TI_SHELL_MM, "Instrument", false, false, "", "", "", "", 0, "");
        pti.getFolderTypes().addAll(ShellMMTypeUtil.getSecurityTypes());
        addDecl(decl, parent, "instrument", pti);

        addDecl(decl, parent, "archiveId", TiType.TI_STRING);

        addDecl(decl, parent, "existingArchiveIdStock", pti(TiType.TI_STRING), mmString("F54C.9E5D.41ER.S701"));
        addDecl(decl, parent, "existingArchiveIdFond", pti(TiType.TI_STRING), mmString("HHH9.KFTS.HIF0.43LT"));
    }
}
