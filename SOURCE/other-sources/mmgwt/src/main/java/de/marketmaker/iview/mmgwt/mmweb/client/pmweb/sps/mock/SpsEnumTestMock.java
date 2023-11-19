/*
 * SpsEnumTestMock.java
 *
 * Created on 20.10.2014 09:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addDecl;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEditWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addReadOnlyWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.mmDbRef;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.e;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.mandatory;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootSectionWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.pti;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsEnumTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        addEditWidget(s, "enumWithNull", "Enum with defined Null");
        addEditWidget(s, "enumWithNull", "Enum with defined Null", "combo");
        addReadOnlyWidget(s, "enumWithNull", "Enum with defined Null (readonly)");

        addEditWidget(s, "defaultInitEnumWithNull", "Default init enum with defined Null");
        addEditWidget(s, "defaultInitEnumWithNull", "Default init enum with defined Null", "combo");
        addReadOnlyWidget(s, "defaultInitEnumWithNull", "Default init enum with defined Null (readonly)");

        addEditWidget(s, "enumWithoutNull", "Enum without defined Null");
        addEditWidget(s, "enumWithoutNull", "Enum without defined Null", "combo");
        addReadOnlyWidget(s, "enumWithoutNull", "Enum without defined Null (readonly)");

        addEditWidget(s, "enumWithNullMandatory", "Mandatory enum with defined Null");
        addEditWidget(s, "enumWithNullMandatory", "Mandatory enum with defined Null", "combo");
        addReadOnlyWidget(s, "enumWithNullMandatory", "Mandatory enum with defined Null (readonly)");

        addEditWidget(s, "defaultInitEnumWithNullMandatory", "Default init enum with defined Null");
        addEditWidget(s, "defaultInitEnumWithNullMandatory", "Default init enum with defined Null", "combo");
        addReadOnlyWidget(s, "defaultInitEnumWithNullMandatory", "Default init enum with defined Null (readonly)");

        addEditWidget(s, "enumWithoutNullMandatory", "Mandatory enum without defined Null");
        addEditWidget(s, "enumWithoutNullMandatory", "Mandatory enum without defined Null", "combo");
        addReadOnlyWidget(s, "enumWithoutNullMandatory", "Mandatory enum without defined Null (readonly)");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();

        addDecl(decl, parent, "enumWithNull", pti("myNA", e("A-Thing", "myAThing"), e("n/a", "myNA"), e("B-Thing", "myBThing"), e("C-Thing", "myCThing")));
        addDecl(decl, parent, "defaultInitEnumWithNull", pti("myNA", e("A-Thing", "myAThing"), e("n/a", "myNA"), e("B-Thing", "myBThing"), e("C-Thing", "myCThing")), mmDbRef("myNA"));
        addDecl(decl, parent, "enumWithoutNull", pti("", e("A-Thing", "myAThing"), e("n/a", "myNA"), e("B-Thing", "myBThing"), e("C-Thing", "myCThing")));
        addDecl(decl, parent, "enumWithNullMandatory", mandatory(pti("myNA", e("A-Thing", "myAThing"), e("n/a", "myNA"), e("B-Thing", "myBThing"), e("C-Thing", "myCThing"))));
        addDecl(decl, parent, "defaultInitEnumWithNullMandatory", mandatory(pti("myNA", e("A-Thing", "myAThing"), e("n/a", "myNA"), e("B-Thing", "myBThing"), e("C-Thing", "myCThing"))), mmDbRef("myNA"));
        addDecl(decl, parent, "enumWithoutNullMandatory", mandatory(pti("", e("A-Thing", "myAThing"), e("n/a", "myNA"), e("B-Thing", "myBThing"), e("C-Thing", "myCThing"))));
    }
}
