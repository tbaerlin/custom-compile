/*
 * SpsDateTimeEditTestMock.java
 *
 * Created on 20.05.2014 09:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiDateKind;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsDateTimeEditTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        addEditWidget(s, "/date", "Widget auf Datum");
        addEditWidget(s, "/date", "Zweites Widget auf Datum");

        addEditWidget(s, "/time", "Widget auf Zeit");
        addEditWidget(s, "/time", "Zweites Widget auf Zeit");

        addEditWidget(s, "/timeHHMM", "Widget auf Zeit ohne Sekunden");
        addEditWidget(s, "/timeHHMM", "Zweites Widget auf Zeit ohne Sekunden");

        addEditWidget(s, "/dateTime", "Widget auf Datum mit Zeit");
        addEditWidget(s, "/dateTime", "Zweites Widget Datum mit Zeit");

        addEditWidget(s, "/dateTimeHHMM", "Widget auf Datum mit Zeit ohne Sekunden");
        addEditWidget(s, "/dateTimeHHMM", "Zweites Widget auf Datum mit Zeit ohne Sekunden");

        addReadOnlyWidget(s, "/date", "Widget auf Datum");
        addReadOnlyWidget(s, "/date", "Zweites Widget auf Datum");

        addReadOnlyWidget(s, "/time", "Widget auf Zeit");
        addReadOnlyWidget(s, "/time", "Zweites Widget auf Zeit");

        addReadOnlyWidget(s, "/timeHHMM", "Widget auf Zeit ohne Sekunden");
        addReadOnlyWidget(s, "/timeHHMM", "Zweites Widget auf Zeit ohne Sekunden");

        addReadOnlyWidget(s, "/dateTime", "Widget auf Datum mit Zeit");
        addReadOnlyWidget(s, "/dateTime", "Zweites Widget Datum mit Zeit");

        addReadOnlyWidget(s, "/dateTimeHHMM", "Widget auf Datum mit Zeit ohne Sekunden");
        addReadOnlyWidget(s, "/dateTimeHHMM", "Zweites Widget auf Datum mit Zeit ohne Sekunden");
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();
        addDecl(decl, parent, "date", TiDateKind.DK_DATE, false, "Date");
        addDecl(decl, parent, "time", TiDateKind.DK_TIME, true, "Time with seconds");
        addDecl(decl, parent, "timeHHMM", TiDateKind.DK_TIME, false, "Time without seconds");
        addDecl(decl, parent, "dateTime", TiDateKind.DK_DATE_TIME, true, "Date and Time with seconds");
        addDecl(decl, parent, "dateTimeHHMM", TiDateKind.DK_DATE_TIME, false, "Date and Time without seconds");
    }
}
