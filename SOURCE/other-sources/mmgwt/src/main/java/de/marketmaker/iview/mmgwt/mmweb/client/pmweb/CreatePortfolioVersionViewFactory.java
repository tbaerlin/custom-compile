/*
 * CreatePortfolioVersionViewFactory.java
 *
 * Created on 27.03.2015 10:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SimpleStandaloneEngine;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.AbstractViewFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.SpsBaseDisplay;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.EditWidgetDesc;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiDateKind;
import de.marketmaker.iview.pmxml.TiType;

/**
 * @author mdick
 */
public class CreatePortfolioVersionViewFactory extends AbstractViewFactory {
    private static final String NAME = "name"; // $NON-NLS$
    private static final String EFFECTIVE_FROM = "effectiveFrom"; // $NON-NLS$
    private final SpsWidget effectiveFrom;

    private final boolean cloneMode;

    public CreatePortfolioVersionViewFactory(SpsBaseDisplay hasWidgets, boolean cloneMode) {
        this.cloneMode = cloneMode;

        init(hasWidgets);

        this.effectiveFrom = getSpsRootWidget().findWidget(BindToken.create(BindToken.EMPTY_ROOT_TOKEN, EFFECTIVE_FROM));
        final SpsLeafProperty property = getProperty(EFFECTIVE_FROM);
        property.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                resetEffectiveFromError();
            }
        });
    }

    @Override
    protected SectionDesc createFormDesc() {
        final SectionDesc formDescRoot = new SectionDesc();
        formDescRoot.setId("root"); // $NON-NLS$
        formDescRoot.setDescription(this.cloneMode ? I18n.I.portfolioVersionCreateCloneHint() : I18n.I.portfolioVersionCreateHint());
        formDescRoot.setDescriptionIcon("pm-portfolio-32"); // $NON-NLS$

        final SectionDesc sectionForm = new SectionDesc();
        sectionForm.setId("form"); // $NON-NLS$
        sectionForm.setStyle("sps-form"); // $NON-NLS$
        formDescRoot.getItems().add(sectionForm);

        if(!this.cloneMode) {
            final EditWidgetDesc editName = new EditWidgetDesc();
            editName.setId(NAME);
            editName.setBind(NAME);
            editName.setCaption(I18n.I.portfolioVersionName());
            sectionForm.getItems().add(editName);
        }

        final EditWidgetDesc editEffectiveFromDate = new EditWidgetDesc();
        editEffectiveFromDate.setId(EFFECTIVE_FROM);
        editEffectiveFromDate.setBind(EFFECTIVE_FROM);
        editEffectiveFromDate.setCaption(I18n.I.portfolioVersionValidFrom());
        sectionForm.getItems().add(editEffectiveFromDate);

        return formDescRoot;
    }

    @Override
    protected void addDeclAndData(DataContainerCompositeNode declRoot, DataContainerCompositeNode dataRoot) {
        addLeaf(declRoot, dataRoot, TiType.TI_STRING, false, I18n.I.portfolioVersionName(), NAME, null);
        final ParsedTypeInfo pti = addLeaf(declRoot, dataRoot, TiType.TI_DATE, I18n.I.portfolioVersionValidFrom(),
                EFFECTIVE_FROM, null);
        pti.setDateKind(TiDateKind.DK_DATE);
    }

    public String getName() {
        return MmTalkHelper.asString(getPropertyDataItem(NAME));
    }

    public String getEffectiveFrom() {
        return MmTalkHelper.asString(getPropertyDataItem(EFFECTIVE_FROM));
    }

    public void setName(String name) {
        getProperty(NAME).setValue(name, false, true);
    }

    public void setEffectiveFrom(String date) {
        getProperty(EFFECTIVE_FROM).setValue(date, false, true);
    }

    public void visualizeEffectiveFromError(String errorMessage) {
        final ErrorMM error = SimpleStandaloneEngine.toErrorMM(errorMessage);
        error.setCorrelationSource(EFFECTIVE_FROM);

        this.effectiveFrom.focusFirst();
        this.effectiveFrom.visualizeError(error, true);
        this.effectiveFrom.selectAll();
    }

    public void resetEffectiveFromError() {
        this.effectiveFrom.visualizeError(null, true);
    }
}
