/*
 * SpsAttachmentsWidgetTestMock.java
 *
 * Created on 01.09.2014 11:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
 * @author mdick
 */
@NonNLS
public class SpsAttachmentsWidgetTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = (SectionDesc) wd;
        s.setBind(null);
        final List<WidgetDesc> i = s.getItems();
        i.clear();

        final EditWidgetDesc fileAttacher = new EditWidgetDesc();
        fileAttacher.setCaption("Attach a file");
        fileAttacher.setStyle("fileAttachments");
        i.add(fileAttacher);
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();
    }
}
