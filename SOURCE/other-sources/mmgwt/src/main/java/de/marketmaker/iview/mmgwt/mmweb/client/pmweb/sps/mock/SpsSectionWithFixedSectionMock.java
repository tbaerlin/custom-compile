/*
 * SpsSectionWithFixedSection.java
 *
 * Created on 03.06.2015 17:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsSection;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerListNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ListWidgetDesc;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addDecl;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEditWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addGroup;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addSectionWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.mmString;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.mandatory;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootSectionWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.pti;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.readonly;

/**
 * @see SpsSection#createNorthWidget()
 * @see de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskController#initTask
 *
 * @author mdick
 */
@NonNLS
public class SpsSectionWithFixedSectionMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);
        s.setBind("rootSectionBind");
        s.setCaption("Root Section");
        s.setDescription("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.");

        final SectionDesc fixedSection = addSectionWidget(s, "Fixed Section");
        fixedSection.setBind("fixedSectionBind");
        fixedSection.setDescription("At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");
        fixedSection.setIsFixedSection(true);

        readonly(addEditWidget(fixedSection, "/rootSectionBind/firstname", "Firstname of Root Section"));
        addEditWidget(fixedSection, "memo", "Memo of Fixed Section");

        // As long as the chart engine is loaded initially, the size of the chart is zero.
        // So we have to resize the north widget if the chart engine was loaded and the chart was rendered.
        // See also AS-1281
        final ListWidgetDesc listWidget = addListWidget(fixedSection, "dummy", "chart", "value", "Pie", "pie");
        addListWidgetColumn(listWidget, "asset", "Asset");
        addListWidgetColumn(listWidget, "value", "Value");

        s.setStyle("sps-form");

        addEditWidget(s, "firstname", "Firstname of Root Section");

        readonly(addEditWidget(s, "fixedSectionBind/memo", "Memo of Fixed Section"));
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode p = (DataContainerCompositeNode) dcn;
        final List<DataContainerNode> c = p.getChildren();
        c.clear();
        final DataContainerGroupNode rootSectionGroup = addGroup(p, "rootSectionBind");
        addDecl(decl, rootSectionGroup, "firstname", mandatory(pti(TiType.TI_STRING)), mmString("a first name"));
        addDecl(decl, rootSectionGroup, "lastname", pti(TiType.TI_STRING), mmString("a last name"));

        final DataContainerGroupNode fixedSectionGroup = addGroup(rootSectionGroup, "fixedSectionBind");
        addDecl(decl, fixedSectionGroup, "memo", mandatory(pti(TiType.TI_MEMO)), mmString("Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat."));

        addDecl(decl, fixedSectionGroup, "dummy", TiType.TI_STRING);

        final DataContainerListNode chart = addList(fixedSectionGroup, "chart");
        final DataContainerGroupNode groupNode = addGroupToList(decl, chart);
        addDecl(decl, groupNode, "asset", pti(TiType.TI_STRING), mmString("Asset 1"));
        addDecl(decl, groupNode, "value", pti(true, "100"), mmNumber(0.23));

        final DataContainerGroupNode groupNode2 = addGroupToList(decl, chart);
        addDecl(decl, groupNode2, "asset", pti(TiType.TI_STRING), mmString("Asset 2"));
        addDecl(decl, groupNode2, "value", pti(true, "100"), mmNumber(0.42));

        final DataContainerGroupNode groupNode3 = addGroupToList(decl, chart);
        addDecl(decl, groupNode3, "asset", pti(TiType.TI_STRING), mmString("Asset 3"));
        addDecl(decl, groupNode3, "value", pti(true, "100"), mmNumber(0.35));
    }

    @Override
    public void preProcess(List<ErrorMM> errors) {
        errors.clear();
        errors.add(errorMM("/rootSectionBind/fixedSectionBind/memo", "/rootSectionBind/fixedSectionBind/memo", "Something is wrong with this thing!"));
    }
}
