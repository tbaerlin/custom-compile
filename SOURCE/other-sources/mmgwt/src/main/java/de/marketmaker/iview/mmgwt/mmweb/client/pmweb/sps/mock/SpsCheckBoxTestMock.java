/*
 * SpsCheckBoxTestMock.java
 *
 * Created on 17.10.2014 13:02
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ErrorSeverity;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addDecl;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEditWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addReadOnlyWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.booleanPti;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.errorMM;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.mmBool;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootSectionWidget;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsCheckBoxTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        addEditWidget(s, "threeWay", "Default (three way & no star)", "width-100");
        addEditWidget(s, "threeWayFalse", "False (three way & no star)");
        addEditWidget(s, "threeWayTrue", "True (three way & no star)");
        addEditWidget(s, "threeWayNull", "Null (three way & no star)");

        addReadOnlyWidget(s, "threeWay", "Readonly default (three way & no star)");
        addReadOnlyWidget(s, "threeWayFalse", "Readonly false (three way & no star)");
        addReadOnlyWidget(s, "threeWayTrue", "Readonly true (three way & no star)");
        addReadOnlyWidget(s, "threeWayNull", "Readonly null (three way & no star)");

        addEditWidget(s, "mandatoryThreeWay", "Mandatory default (three way & star)");
        addEditWidget(s, "mandatoryThreeWayFalse", "Mandatory false (three way & star)");
        addEditWidget(s, "mandatoryThreeWayTrue", "Mandatory true (three way & star)");
        addEditWidget(s, "mandatoryThreeWayNull", "Mandatory null (three way & star)");

        addReadOnlyWidget(s, "mandatoryThreeWay", "Readonly mandatory default (three way & star)");
        addReadOnlyWidget(s, "mandatoryThreeWayFalse", "Readonly mandatory false (three way & star)");
        addReadOnlyWidget(s, "mandatoryThreeWayTrue", "Readonly mandatory true (three way & star)");
        addReadOnlyWidget(s, "mandatoryThreeWayNull", "Readonly mandatory null (three way & star)");

        addEditWidget(s, "twoWay", "Default (two way & no star)");
        addEditWidget(s, "twoWayFalse", "false (two way & no star)");
        addEditWidget(s, "twoWayTrue", "true (two way & no star)");
        addEditWidget(s, "twoWayNull", "null (two way & no star)");

        addReadOnlyWidget(s, "twoWay", "Readonly default (two way & no star)");
        addReadOnlyWidget(s, "twoWayFalse", "Readonly false (two way & no star)");
        addReadOnlyWidget(s, "twoWayTrue", "Readonly true (two way & no star)");
        addReadOnlyWidget(s, "twoWayNull", "Readonly null (two way & no star)");

        addEditWidget(s, "mandatoryTwoWay", "Mandatory default (two way & star)");
        addEditWidget(s, "mandatoryTwoWayFalse", "Mandatory false (two way & star)");
        addEditWidget(s, "mandatoryTwoWayTrue", "Mandatory true (two way & star)");
        addEditWidget(s, "mandatoryTwoWayNull", "Mandatory null (two way & star)");

        addReadOnlyWidget(s, "mandatoryTwoWay", "Readonly mandatory default (two way & star)");
        addReadOnlyWidget(s, "mandatoryTwoWayFalse", "Readonly mandatory false (two way & star)");
        addReadOnlyWidget(s, "mandatoryTwoWayTrue", "Readonly mandatory true (two way & star)");
        addReadOnlyWidget(s, "mandatoryTwoWayNull", "Readonly mandatory null (two way & star)");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();

        addDecl(decl, parent, "threeWay", booleanPti(false, false));
        addDecl(decl, parent, "threeWayFalse", booleanPti(false, false), mmBool(false));
        addDecl(decl, parent, "threeWayTrue", booleanPti(false, false), mmBool(true));
        addDecl(decl, parent, "threeWayNull", booleanPti(false, false), new DefaultMM());

        addDecl(decl, parent, "mandatoryThreeWay", booleanPti(true, false));
        addDecl(decl, parent, "mandatoryThreeWayFalse", booleanPti(true, false), mmBool(false));
        addDecl(decl, parent, "mandatoryThreeWayTrue", booleanPti(true, false), mmBool(true));
        addDecl(decl, parent, "mandatoryThreeWayNull", booleanPti(true, false), new DefaultMM());

        addDecl(decl, parent, "twoWay", booleanPti(false, true));
        addDecl(decl, parent, "twoWayFalse", booleanPti(false, true), mmBool(false));
        addDecl(decl, parent, "twoWayTrue", booleanPti(false, true), mmBool(true));
        addDecl(decl, parent, "twoWayNull", booleanPti(false, true), new DefaultMM());

        addDecl(decl, parent, "mandatoryTwoWay", booleanPti(true, true));
        addDecl(decl, parent, "mandatoryTwoWayFalse", booleanPti(true, true), mmBool(false));
        addDecl(decl, parent, "mandatoryTwoWayTrue", booleanPti(true, true), mmBool(true));
        addDecl(decl, parent, "mandatoryTwoWayNull", booleanPti(true, true), new DefaultMM());
    }

    @Override
    public void preProcess(List<ErrorMM> errors) {
        errors.clear();
        errors.add(errorMM(ErrorSeverity.ESV_ERROR, "/threeWay", "/threeWay", "Check if the first Widget gets the focus"));
    }
}
