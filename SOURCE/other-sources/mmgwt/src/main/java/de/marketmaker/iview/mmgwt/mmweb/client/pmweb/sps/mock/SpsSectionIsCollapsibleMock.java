/*
 * SpsSectionIsCollapsibleMock.java
 *
 * Created on 09.06.2015 14:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.SectionListDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;

/**
 * @author mdick
 */
@NonNLS
public class SpsSectionIsCollapsibleMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = MockUtil.prepareRootSectionWidget(wd);
        s.setStyle("sps-dummy");
        s.setDescription("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.");

        final SectionDesc section = addSectionWidget(s, null);

        final SectionDesc header = addSectionWidget(section, null, "sps-form-5", "sps-plainForm");
        readonly(addEditWidget(header, "aname", "A name"));

        final SectionDesc editSectionDesc = addSectionWidget(null, "$#. List Entry");
        addEditWidget(editSectionDesc, "string", "A string");
        addEditWidget(editSectionDesc, "memo", "A memo");
        final SectionListDesc sectionListDesc = addSectionListWidget(section, "list", editSectionDesc, null, "tableLayout", "sps-collapsible"/*, "sps-huddled"*/);
        sectionListDesc.setId("sectionListDescId");

        final SectionDesc sectionWithSomethingToEdit = addSectionWidget(s, "Section with Something to Edit");
        addEditWidget(sectionWithSomethingToEdit, "somethingToEdit", "The next edit field that gains focus if section list is collapsed");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode p = (DataContainerCompositeNode) dcn;
        final List<DataContainerNode> c = p.getChildren();
        c.clear();

        addDecl(decl, p, "somethingToEdit", mandatory(pti(TiType.TI_STRING)), mmString("something to edit"));
        addDecl(decl, p, "aname", mandatory(pti(TiType.TI_STRING)), mmString("a name"));
        final DataContainerListNode list = addList(p, "list");

        for (int i = 0; i < 20; i++) {
            addListEntry(decl, list, "string of entry " + i, "" + i + " Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.");
        }
    }

    private static DataContainerGroupNode addListEntry(boolean decl, DataContainerListNode parent, String string, String memo) {
        final DataContainerGroupNode group = addGroupToList(decl, parent);
        addDecl(decl, group, "string", pti(TiType.TI_STRING), mmString(string));
        addDecl(decl, group, "memo", pti(TiType.TI_STRING), mmString(memo));
        return group;
    }
}
