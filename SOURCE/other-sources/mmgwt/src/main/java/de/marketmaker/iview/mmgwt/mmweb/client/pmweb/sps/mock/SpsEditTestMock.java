/*
 * SpsEditTestMock.java
 *
 * Created on 26.09.2014 10:09
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ErrorSeverity;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addDecl;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEditWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.mmString;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.pti;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootNode;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.readonly;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsEditTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        addEditWidget(s, "default", "Default");
        addEditWidget(s, "maxLength", "Max-Length 4");
        readonly(addEditWidget(s, "readonlyWidthAndSpsNowrap", "readonlyWidthAndSpsNowrap", "width-100", "sps-nowrap"));

        readonly(addEditWidget(s, "security", "Security with link"));
        readonly(addEditWidget(s, "investor", "Investor iff main input then no link"));
        readonly(addEditWidget(s, "portfolio", "Portfolio iff main input then no link"));

        addEditWidget(s, "esvError", "esvError");
        addEditWidget(s, "esvWarning2", "esvWarning2");
        addEditWidget(s, "esvWarning", "esvWarning");
        addEditWidget(s, "esvHint", "esvHint");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = prepareRootNode(dcn);

        addDecl(decl, parent, "default", pti(TiType.TI_STRING, "Default", false, false, "0", "0", "0", "0", 0, ""));
        addDecl(decl, parent, "maxLength", pti(TiType.TI_STRING, "Max-Length 4", false, false, "0", "0", "0", "0", 4, ""));
        addDecl(decl, parent, "readonlyWidthAndSpsNowrap", pti(TiType.TI_STRING), mmString("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."));

        addDecl(decl, parent, "security", pti(TiType.TI_SHELL_MM), LED400);
        addDecl(decl, parent, "investor", pti(TiType.TI_SHELL_MM), INVESTOR_TEST_HA);
        addDecl(decl, parent, "portfolio", pti(TiType.TI_SHELL_MM), PORTFOLIO_TEST_HA);

        addDecl(decl, parent, "esvError", pti(TiType.TI_STRING, "Default", false, true, "0", "0", "0", "0", 0, ""));
        addDecl(decl, parent, "esvWarning2", pti(TiType.TI_STRING, "Default", false, true, "0", "0", "0", "0", 0, ""));
        addDecl(decl, parent, "esvWarning", pti(TiType.TI_STRING, "Default", false, true, "0", "0", "0", "0", 0, ""));
        addDecl(decl, parent, "esvHint", pti(TiType.TI_STRING, "Default", false, true, "0", "0", "0", "0", 0, ""));
    }

    @Override
    public void preProcess(List<ErrorMM> errors) {
        errors.clear();
        errors.add(errorMM("/esvError", "/esvError", "An esvError"));
        errors.add(errorMM(ErrorSeverity.ESV_WARNING_2, "/esvWarning2", "/esvWarning2", "An esvWarning2"));
        errors.add(errorMM(ErrorSeverity.ESV_WARNING, "/esvWarning", "/esvWarning", "An esvWarning"));
        errors.add(errorMM(ErrorSeverity.ESV_HINT, "/esvHint", "/esvHint", "An esvHint"));
    }
}
