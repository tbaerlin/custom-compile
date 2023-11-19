/*
 * SpsDmsLinkWidgetTestMock.java
 *
 * Created on 06.06.2014 07:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SectionListDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;

/**
 * Replaces any task with decls and data to test the SpsDmsLinkWidget.
 *
 * @author Markus Dick
 */
@NonNLS
public class SpsDmsLinkWidgetTestMock extends PreProcessHook {
    public static final String DMS_HANDLE = "246208"; //Changes depending on DMS entries!

    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);
        s.setStyle("no-form");

        final SectionDesc singleSection = addSectionWidget(s, "Single DMS Documents");
        singleSection.getItems().add(createDmsLinkWidgetDesc("/documentData"));

        final SectionListDesc listSection = new SectionListDesc();
        listSection.setBind("/list");
        listSection.setIsReadonly(true);
        listSection.setCaption("List of DMS documents");

        final SectionDesc listEditSection = new SectionDesc();
        listEditSection.setCaption("Document $#");

        final SectionDesc wtf = new SectionDesc();
        listEditSection.getItems().add(wtf);
        wtf.getItems().add(createDmsLinkWidgetDesc("documentData"));

        listSection.setEditSection(listEditSection);
        addToParent(listSection, s);

        final SectionDesc incompleteSection = addSectionWidget(s, "Single DMS Document but invisible due to missing data");
        incompleteSection.getItems().add(createDmsLinkWidgetDesc("/incompleteData"));

        final SectionDesc readonlySection = addSectionWidget(s, "Readonly DMS Document links");
        readonlySection.getItems().add(createDmsLinkWidgetDesc("/documentDataInsufficientRights"));
    }

    private EditWidgetDesc createDmsLinkWidgetDesc(String bind) {
        final EditWidgetDesc edit = new EditWidgetDesc();
        edit.setIsReadonly(true); //set automatically by MMTalk framework because no editable property is bound.
        edit.setStyle("dmsDocumentLink");
        edit.setBind(bind);
        edit.setCaption("Document");
        return edit;
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final List<DataContainerNode> c = ((DataContainerCompositeNode) dcn).getChildren();
        c.clear();

        c.add(createDmsGroupNode(decl, "documentData", "", true, "A PDF file", DMS_HANDLE));
        c.add(createDmsGroupNode(decl, "documentDataInsufficientRights", "", false, "A PDF file with insufficient rights", DMS_HANDLE));

        final DataContainerListNode list = new DataContainerListNode();
        list.setNodeLevelName("list");
        c.add(list);

        final List<DataContainerNode> lc = list.getChildren();
        if(decl) {
            lc.add(listEntry(createDmsGroupNode(true, "documentData", " " + 0, true, "A PDF file in a section list", DMS_HANDLE)));
        }
        else {
            for (int i = 0; i < 3; i++) {
                lc.add(listEntry(createDmsGroupNode(false, "documentData", " " + i, true, "A PDF file in a section list", DMS_HANDLE)));
            }
        }

        c.add(createIncompleteDmsGroupNode(decl));
    }

    private DataContainerGroupNode createIncompleteDmsGroupNode(boolean decl) {
        final DataContainerGroupNode group = new DataContainerGroupNode();
        group.setNodeLevelName("incompleteData");

        addDecl(decl, group, "Handle", pti(TiType.TI_STRING, "The Handle"), new DefaultMM());

        addDecl(decl, group, "DisplayName", pti(TiType.TI_STRING, "The DisplayName"),
                mmString("A PDF file with an incomplete group node (missing HasSufficientRights)"));

        addDecl(decl, group, "DocumentType", pti(TiType.TI_STRING, "The DocumentType"), mmDbRef("pdf"));

        return group;
    }

    private DataContainerGroupNode listEntry(DataContainerGroupNode child) {
        final DataContainerGroupNode group = new DataContainerGroupNode();
        group.getChildren().add(child);
        return group;
    }
}
