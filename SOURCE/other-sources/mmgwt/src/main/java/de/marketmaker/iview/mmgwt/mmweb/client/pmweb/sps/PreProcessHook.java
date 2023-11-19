/*
 * PreProcessHook.java
 *
 * Created on 17.04.2014 15:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.WidgetDesc;

import java.util.List;

/**
 * Allows easy mocking and pre-processing of decls and descs.
 * The GWT compiler will remove and inline the calls to these empty methods.
 * So, there should be no overhead in production mode.
 *
 * To use it, just add a "replace with" rule to your DevMmweb.gwt.xml, e.g.:
 * <replace-with class="de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.AnlageplanungslisteMock">
 *   <when-type-is class="de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook"/>
 * </replace-with>
 *
 * @author Markus Dick
 */
public class PreProcessHook {
    public void preProcess(SectionDesc formDescRoot, DataContainerCompositeNode dataDeclRoot, DataContainerCompositeNode dataRoot, List<ErrorMM> errors) {
        preProcess(formDescRoot);
        preProcess(dataDeclRoot, true);
        preProcess(dataRoot, false);
        preProcess(errors);
    }

    public void preProcess(WidgetDesc wd) {
        /* do nothing */
    }

    public void preProcess(DataContainerNode dcn, final boolean decl) {
        /* do nothing */
    }

    public void preProcess(List<ErrorMM> errors) {
        /* do nothing */
    }
}
