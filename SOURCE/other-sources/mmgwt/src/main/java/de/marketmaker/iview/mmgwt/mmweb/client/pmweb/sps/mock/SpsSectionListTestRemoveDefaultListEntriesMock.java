/*
 * SpsSectionListTestRemoveDefaultListEntriesMock.java
 *
 * Created on 29.06.2015 09:56
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.WidgetDesc;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addToParent;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.prepareRootSectionWidget;

/**
 * @author mdick
 */
public class SpsSectionListTestRemoveDefaultListEntriesMock extends SpsSectionListTestMock {
    @Override
    public void preProcess(WidgetDesc wd) {
        super.preProcess(wd);
        final SectionDesc sectionDesc = prepareRootSectionWidget(wd);
        addToParent(this.listSection, sectionDesc);
        this.listSection.setRemoveDefaultListEntries(true);
    }
}
