/*
 * SpsLabelWidgetTestMock.java
 *
 * Created on 02.06.2014 13:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.MMString;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiDateKind;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsLabelWidgetTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);
        s.setStyle("sps-dummy");

        addLabelWidget(s, "Sanctus sea sed takimata ut vero voluptua. est Lorem ipsum dolor sit amet.");

        final SectionDesc section1 = addSectionWidget(s, null);
        addLabelWidget(section1, "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");
        addEditWidget(section1, "/time", "A time");
        addEditWidget(section1, "/time", "Another time");

        addLabelWidget(s, "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum.");

        final SectionDesc section2 = addSectionWidget(s, null);
        addLabelWidget(section2, "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.");
        addEditWidget(section2, "/time", "Again");
        addEditWidget(section2, "/time", "And again");

        final SectionDesc section3 = addSectionWidget(s, "A Section with Bound Label Descs", "sps-dummy");
        //bound label descs without text
        addLabelWidget(section3, "", "/labelString");
        addLabelWidget(section3, "", "/labelString");
        //a bound label desc with text (it should always show the bound value)
        addLabelWidget(section3, "The bound label desc", "/labelString");
        //a bound label desc with text (it should always show the bound value)
        addLabelWidget(section3, "The bound memo label desc", "/memoString");

        final SectionDesc section4 = addSectionWidget(s, "An Edit Control for Bound Labels (to check onPropertyChanged)");
        addEditWidget(section4, "/memoString", "Edit the memo label");

        final SectionDesc section5 = addSectionWidget(s, "A Label Widget that has the styles width and sps-nowrap");
        addLabelWidget(section5, "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.", "", "The Label", "width-100", "sps-nowrap");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = prepareRootNode(dcn);

        addDecl(decl, parent, "time", TiDateKind.DK_TIME, false, "Time");

        addLabelDecl(decl, parent, TiType.TI_STRING, "labelString");
        addLabelDecl(decl, parent, TiType.TI_MEMO, "memoString");
    }

    private void addLabelDecl(boolean decl, DataContainerCompositeNode parent, TiType tiType, String nodeName) {
        final ParsedTypeInfo parsedTypeInfo = new ParsedTypeInfo();
        parsedTypeInfo.setTypeId(tiType);

        final MMString diString = new MMString();
        diString.setValue("This is an example for a bound " + tiType.value() + "... At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.");

        addDecl(decl, parent, nodeName, parsedTypeInfo, diString);
    }
}
