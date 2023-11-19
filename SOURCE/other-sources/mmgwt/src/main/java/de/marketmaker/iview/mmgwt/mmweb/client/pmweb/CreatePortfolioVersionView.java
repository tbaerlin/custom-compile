/*
 * CreatePortfolioVersionView.java
 *
 * Created on 27.03.2015 10:07
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
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
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author mdick
 */
public class CreatePortfolioVersionView implements IsWidget {
    private final TaskViewPanel tvp = SimpleStandaloneEngine.createTaskViewPanel();
    private final CreatePortfolioVersionViewFactory factory;

    public CreatePortfolioVersionView(final CreatePortfolioVersionPageController presenter, boolean cloneMode) {
        this.factory = new CreatePortfolioVersionViewFactory(new AbstractViewFactory.TaskViewPanelView(this.tvp), cloneMode);

        final Widget cancelButton = Button.text(I18n.I.cancel())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        presenter.onCancel();
                    }
                }).build();

        final Widget submitButton = SimpleStandaloneEngine.createTaskViewPanelSubmitButtonFactory(
                cloneMode ? I18n.I.portfolioVersionCreateClone() : I18n.I.portfolioVersionCreate(),
                new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.onSubmit(factory.getName(), factory.getEffectiveFrom());
            }
        }).build();

        SimpleStandaloneEngine.createAndSetTaskToolbar(this.tvp, cancelButton, submitButton);
    }

    public void setEffectiveFromRecommendation(String effectiveFromRecommendation, String oldEffectiveFrom) {
        this.factory.setEffectiveFrom(effectiveFromRecommendation);
        final String entered = PmRenderers.DATE_TIME_STRING.render(oldEffectiveFrom);
        final String message = StringUtil.hasText(entered)
                ? I18n.I.portfolioVersionCreateErrorDuplicateEffectiveFrom(entered, I18n.I.portfolioVersionCreate())
                : I18n.I.portfolioVersionCreateErrorDuplicateEffectiveFromNoDate(I18n.I.portfolioVersionCreate());
        this.factory.visualizeEffectiveFromError(message);
    }

    @Override
    public Widget asWidget() {
        return this.tvp;
    }
}
