/*
 * SpsYieldRiskPickerMock.java
 *
 * Created on 19.05.2015 06:58
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
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.*;

/**
 * @author mdick
 */
@NonNLS
public class SpsYieldRiskPickerMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = prepareRootSectionWidget(wd);

        addEditWidgetWithObject(s, "riskClass", "config", "Yield Risk Picker", "yieldRiskPicker");
        addEditWidgetWithObject(s, "riskClass", "config", "Yield Risk Picker", "yieldRiskPicker", "size-400x250");
        addEditWidgetWithObject(s, "riskClass", "config", "Yield Risk Picker", "yieldRiskPicker", "size-400");
        readonly(addEditWidgetWithObject(s, "riskClass", "config", "Yield Risk Picker Readonly", "yieldRiskPicker"));
    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode p = (DataContainerCompositeNode) dcn;
        final List<DataContainerNode> c = p.getChildren();
        c.clear();
        addDecl(decl, p, "riskClass", pti(TiType.TI_STRING), mmString("3"));

        final DataContainerGroupNode config = addGroup(p, "config");
        addDecl(decl, config, "showXGrid", pti(TiType.TI_BOOLEAN), mmBool(false));
        addDecl(decl, config, "showYGrid", pti(TiType.TI_BOOLEAN), mmBool(false));
        addDecl(decl, config, "xAxisLabel", pti(TiType.TI_STRING), mmString("Risiko"));
        addDecl(decl, config, "yAxisLabel", pti(TiType.TI_STRING), mmString("Rendite"));

        final DataContainerListNode riskClasses = addList(config, "riskClasses");

        final String lorem =  "stet clita kasd gubergren, no sea takimata sanctus est lorem ipsum dolor sit amet ";

        addRiskClass(decl, riskClasses, "", "", 0, 0, "", "");
        addRiskClass(decl, riskClasses, "Sicherheits-\norientiert", "1", 1, 1.5, "Sicherheitso" + lorem, "Sicherheitso" + lorem + lorem + lorem);
        addRiskClass(decl, riskClasses, "Begrenzt risikobereit", "2", 2, 2.6, "Begrenzt risi" + lorem, "Begrenzt risi" + lorem + lorem + lorem);
        addRiskClass(decl, riskClasses, "Risikobereit", "3", 3, 3.35, "Risi" + lorem, "Risi" + lorem + lorem + lorem);
        addRiskClass(decl, riskClasses, "Vermehrt\nrisikobereit", "4", 4, 3.82, "Vermehrt risi" + lorem, "Vermehrt risi" + lorem + lorem + lorem);
        addRiskClass(decl, riskClasses, "Spekulativ", "5", 5, 4.1, "Speku" + lorem, "Speku" + lorem + lorem + lorem);
        addRiskClass(decl, riskClasses, "", "", 6, 4.3, "", "");
    }

    private DataContainerGroupNode addRiskClass(boolean decl, DataContainerListNode riskClasses, String label, String riskClass, double x, double y, String shortDescription, String longDescription) {
        final DataContainerGroupNode entry = addGroupToList(decl, riskClasses);
        if (entry == null) {
            return null;
        }

        addDecl(decl, entry, "label", pti(TiType.TI_STRING), mmString(label));
        addDecl(decl, entry, "riskClass", pti(TiType.TI_STRING), mmString(riskClass));
        addDecl(decl, entry, "x", pti(TiType.TI_NUMBER), mmNumber(x));
        addDecl(decl, entry, "y", pti(TiType.TI_NUMBER), mmNumber(y));
        addDecl(decl, entry, "shortDescription", pti(TiType.TI_STRING), mmString(shortDescription));
        addDecl(decl, entry, "longDescription", pti(TiType.TI_STRING), mmString(longDescription));

        return entry;
    }
}
