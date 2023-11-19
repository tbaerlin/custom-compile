/*
 * CreatePortfolioVersionPageController.java
 *
 * Created on 27.03.2015 10:09
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.PortfolioPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.CreatePortfolioVersionRequest;
import de.marketmaker.iview.pmxml.CreatePortfolioVersionResponse;

/**
 * @author mdick
 */
public class CreatePortfolioVersionPageController extends AbstractPageController {
    public static final String CLONE_SOURCE_KEY = "cloneSource";  // $NON-NLS$

    private final PortfolioPortraitController portfolioController;
    private final DmxmlContext.Block<CreatePortfolioVersionResponse> block;
    private final CreatePortfolioVersionRequest createPortfolioVersionRequest;

    private CreatePortfolioVersionView view;

    public CreatePortfolioVersionPageController(PortfolioPortraitController portfolioController) {
        this.portfolioController = portfolioController;

        this.context.setCancellable(false);
        this.block = this.context.addBlock("PM_CreatePortfolioVersion"); // $NON-NLS$
        this.createPortfolioVersionRequest = new CreatePortfolioVersionRequest();
        this.block.setParameter(this.createPortfolioVersionRequest);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        this.block.setEnabled(false);
        final HistoryToken historyToken = event.getHistoryToken();

        final String portfolioId = historyToken.get(AbstractDepotObjectPortraitController.OBJECTID_KEY);
        this.createPortfolioVersionRequest.setPortfolioId(portfolioId);

        final String cloneSource = historyToken.get(CLONE_SOURCE_KEY);
        final boolean hasCloneSource = StringUtil.hasText(cloneSource);
        this.createPortfolioVersionRequest.setClone(hasCloneSource);
        this.createPortfolioVersionRequest.setCloneSourceId(hasCloneSource ? cloneSource : null);
        if(hasCloneSource) {
            this.createPortfolioVersionRequest.setName(null);
        }

        this.view = new CreatePortfolioVersionView(this, hasCloneSource);
        getContentContainer().setContent(this.view.asWidget());

        AbstractMainController.INSTANCE.getView().setContentHeader(hasCloneSource
                ? I18n.I.portfolioVersionCreateClone()
                : I18n.I.portfolioVersionCreate());
    }

    @Override
    public void refresh() {
        /* do nothing */
    }

    public void onSubmit(String name, final String effectiveFrom) {
        setParams(name, effectiveFrom);

        this.block.setEnabled(true);
        this.block.setToBeRequested();

        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onFailure(Throwable throwable) {
                CreatePortfolioVersionPageController.this.onError(throwable);
            }

            @Override
            public void onSuccess(ResponseType responseType) {
                CreatePortfolioVersionPageController.this.onSuccess(effectiveFrom);
            }
        });
    }

    public void onSuccess(String effectiveFrom) {
        if(this.block.isResponseOk()) {
            final CreatePortfolioVersionResponse response = this.block.getResult();
            this.block.setEnabled(false);

            switch(response.getResponseType()) {
                case CPVRT_OK:
                    this.portfolioController.onSuccessfullyCreatedOrClonedPortfolioVersion(response.getId(), effectiveFrom);
                    break;
                case CPVRT_DUPLICATE_EFFECTIVE_FROM:
                    this.view.setEffectiveFromRecommendation(response.getEffectiveFromRecommendation(), effectiveFrom);
                    break;
                case CPVRT_INSUFFICIENT_RIGHTS:
                    AbstractMainController.INSTANCE.showError(I18n.I.portfolioVersionCreateErrorInsufficientRights());
                    break;
                case CPVRT_UNKNOWN:
                default:
                    AbstractMainController.INSTANCE.showError(I18n.I.portfolioVersionCreateErrorUnknown());
            }
        }
        else {
            this.block.setEnabled(false);
            setParams(null, null);
        }
    }

    private void onError(Throwable throwable) {
        String message = I18n.I.portfolioVersionCreateErrorUnknown();
        if(throwable != null && StringUtil.hasText(throwable.getMessage())) {
            message += SafeHtmlUtils.htmlEscape(throwable.getMessage());
        }
        AbstractMainController.INSTANCE.showError(message);
    }

    public void onCancel() {
        this.portfolioController.onAfterEditSubmit();
    }

    private void setParams(String name, String effectiveFrom) {
        this.createPortfolioVersionRequest.setName(name);
        this.createPortfolioVersionRequest.setEffectiveFrom(effectiveFrom);
    }

    @Override
    public boolean isPrintable() {
        return false;
    }
}
