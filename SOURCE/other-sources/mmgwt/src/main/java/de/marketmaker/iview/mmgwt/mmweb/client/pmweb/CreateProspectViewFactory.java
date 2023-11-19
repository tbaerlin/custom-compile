package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SimpleStandaloneEngine;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.AbstractViewFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.SpsBaseDisplay;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ErrorSeverity;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.*;

/**
 * Author: umaurer
 * Created: 25.11.14
 */
public class CreateProspectViewFactory extends AbstractViewFactory {
    public static final String BIND_ZONE = "zone"; // $NON-NLS$
    public static final String BIND_PROSPECT_NAME = "prospectName"; // $NON-NLS$

    private final List<ZoneDesc> listZones;

    private final SpsWidget spsProspectName;

    public CreateProspectViewFactory(List<ZoneDesc> listZones, SpsBaseDisplay view) {
        if (listZones.isEmpty()) {
            throw new IllegalStateException("no zones defined"); // $NON-NLS$
        }
        this.listZones = listZones;
        init(view);
        this.spsProspectName = getSpsRootWidget().findWidget(BindToken.create(BindToken.EMPTY_ROOT_TOKEN, BIND_PROSPECT_NAME));
    }

    @Override
    protected SectionDesc createFormDesc() {
        final SectionDesc formDescRoot = new SectionDesc();
        formDescRoot.setId("root"); // $NON-NLS$
        formDescRoot.setDescription(I18n.I.prospectCreateTaskDescription());
        formDescRoot.setDescriptionIcon("pm-investor-prospect-32"); // $NON-NLS$

        final SectionDesc sectionForm = new SectionDesc();
        sectionForm.setId("form"); // $NON-NLS$
        sectionForm.setStyle("sps-form"); // $NON-NLS$

        final EditWidgetDesc editZone = new EditWidgetDesc();
        editZone.setId("zone"); // $NON-NLS$
        editZone.setStyle("combo"); // $NON-NLS$
        editZone.setBind(BIND_ZONE);
        editZone.setCaption(I18n.I.zone());

        final EditWidgetDesc editProspectName = new EditWidgetDesc();
        editProspectName.setId("prospectName"); // $NON-NLS$
        editProspectName.setBind(BIND_PROSPECT_NAME);
        editProspectName.setCaption(I18n.I.prospectName());

        sectionForm.getItems().add(editZone);
        sectionForm.getItems().add(editProspectName);
        formDescRoot.getItems().add(sectionForm);
        return formDescRoot;
    }

    @Override
    protected void addDeclAndData(DataContainerCompositeNode declRoot, DataContainerCompositeNode dataRoot) {
        final ParsedTypeInfo ptiZone = addLeaf(declRoot, dataRoot, TiType.TI_ENUMERATION, I18n.I.zone(), BIND_ZONE, this.listZones.get(0).getId());
        for (ZoneDesc zoneDesc : this.listZones) {
            ptiZone.getEnumElements().add(createEnumElement(zoneDesc));
        }

        addLeaf(declRoot, dataRoot, TiType.TI_STRING, I18n.I.prospectName(), BIND_PROSPECT_NAME, null);
    }

    public String getZone() {
        final MM di = getPropertyDataItem(BIND_ZONE);
        return di == null
                ? null
                : asString(di);
    }

    public String getProspectName() {
        final MM di = getPropertyDataItem(BIND_PROSPECT_NAME);
        return di == null
                ? null
                : asString(di);
    }

    public void setProspectName(String prospectName) {
        getProperty(BIND_PROSPECT_NAME).setValue(prospectName);
    }

    public void visualizeProspectNameError(String errorMessage) {
        Firebug.debug("prospect name error: " + errorMessage);
        final ErrorMM error = SimpleStandaloneEngine.toErrorMM(errorMessage);
        error.setCorrelationSource(BIND_PROSPECT_NAME);
        this.spsProspectName.focusFirst();
        this.spsProspectName.visualizeError(error, true);
        this.spsProspectName.selectAll();
    }
}
