/*
 * ProspectView.java
 *
 * Created on 05.06.2014 08:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Advisor;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Prospect;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

/**
 * @author Markus Dick
 */
public class ProspectView extends AbstractOwnerView<Prospect, Prospect>{
    private final Button editButton;

    public ProspectView() {
        super();

        final FloatingToolbar toolbar = getToolbar();

        this.editButton = Button.icon("x-tool-btn-edit") // $NON-NLS$
                .tooltip(I18n.I.prospectEdit())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        getPresenter().onEditButtonClicked();
                    }
                }).build();
        this.editButton.setVisible(false);
        toolbar.add(this.editButton);

        setToolbarVisible(true);
    }

    protected void doUpdateView(Prospect t) {
        addStaticData(t);
        addContactData(t);
        addLinkedPersons(t);
        addReportingDetails(t);
        addAdvisorWidget(t != null ? t.getAdvisor() : null);
        addTaxDetails(t);
        addUserDefinedFields(t);
    }

    @Override
    protected void addStaticData(Prospect prospect) {
        doAddStaticData(prospect, I18n.I.prospectNumberAbbr());
    }

    @Override
    protected Prospect getOwner(Prospect h) {
        return h;
    }

    @Override
    protected void doAddStaticData(Prospect t, final String numberLabel) {
        Panel p = addSection(SC_STATIC, I18n.I.staticData());
        setSelectedSection(SC_STATIC);

        addField(p, I18n.I.prospect(), t.getName());
        final String investorFullName = PmRenderers.ADDRESS_FULL_NAME_WITH_SALUTATION.render(t.getAddress());
        addField(p, I18n.I.name(), investorFullName);
        addField(p, I18n.I.organization(), t.getAddress().getOrganization());
        addField(p, numberLabel, t.getNumber());
        addField(p, I18n.I.customerCategory(), t.getCustomerCategory());
        addCreationDeactivationDates(p, t.getCreationDate(), t.getDeactivationDate());
        addMultilineField(p, I18n.I.comment(), t.getComment());
        addField(p, I18n.I.prospectStatus(), t.getStatus());
        addField(p, I18n.I.lastContact(), PmRenderers.DATE_TIME_STRING.render(t.getLastContactAt()));
        addField(p, I18n.I.prospectValuation(), t.getValuation());
        addField(p,
                PmRenderers.DATA_STATUS_LABEL.render(I18n.I.dataStatus(), t.getDataStatusDate()),
                PmRenderers.DATA_STATUS.render(t.getDataStatus()));

        addField(p, I18n.I.zone(), t.getZone());
    }

    private void addAdvisorWidget(Advisor advisor) {
        final Panel p = addSection(SC_ADVISOR, I18n.I.advisor());
        AdvisorWidget av = new AdvisorWidget();
        p.add(av.asWidget());
        av.setValue(advisor);
    }

    @Override
    public void setEditButtonVisible(boolean visible) {
        this.editButton.setVisible(visible);
        this.setToolbarVisible(visible);  //only necessary as long as the toolbar has only one button
    }
}
