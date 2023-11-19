package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SimpleStandaloneEngine;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.AbstractViewFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskViewPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * Created on 28.08.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class CreateProspectView implements IsWidget {
    private final TaskViewPanel tvp = SimpleStandaloneEngine.createTaskViewPanel();
    private final CreateProspectViewFactory factory;

    public CreateProspectView(final CreateProspectPageController c) {
        this.factory = new CreateProspectViewFactory(c.getZones(), new AbstractViewFactory.TaskViewPanelView(this.tvp));

        final Widget buttonSubmit = Button.text(I18n.I.proceed())
                .icon("sps-task-proceed", Button.IconPosition.RIGHT) // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        factory.getSpsRootWidget().updateProperties();
                        final String zoneId = factory.getZone();
                        final String prospectName = factory.getProspectName();
                        if (prospectName == null) {
                            factory.visualizeProspectNameError(I18n.I.spsErrorNoEntry(I18n.I.prospectName()));
                            return;
                        }
                        c.configureProspect(zoneId, prospectName);
                        c.configurePerson(zoneId);
                        c.create();
                    }
                }).build();

        SimpleStandaloneEngine.createAndSetTaskToolbar(this.tvp, null, buttonSubmit);
    }

    @Override
    public Widget asWidget() {
        return this.tvp;
    }

    public void setProspectRecommendation(String recommendation) {
        final String prospectName = this.factory.getProspectName();
        this.factory.setProspectName(recommendation);
        this.factory.resetPropertyChanged();
        this.factory.visualizeProspectNameError(I18n.I.spsCreateProspectErrorDuplicate(prospectName));
    }

    public void hide() {
        PlaceUtil.goTo("PM_EX"); // $NON-NLS$
    }
}