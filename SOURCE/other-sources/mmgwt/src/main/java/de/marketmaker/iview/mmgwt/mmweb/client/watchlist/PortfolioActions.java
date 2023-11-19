/*
 * PortfolioActions.java
 *
 * Created on 07.09.2009 11:18:53
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.PFEvaluation;
import de.marketmaker.iview.dmxml.PFOrder;
import de.marketmaker.iview.dmxml.PFOrderResult;
import de.marketmaker.iview.dmxml.PFPortfolioResult;
import de.marketmaker.iview.dmxml.PortfolioElement;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortfolioActions {
    public static final PortfolioActions INSTANCE = new PortfolioActions();

    private final DmxmlContext.Block<PFPortfolioResult> pfCreateBlock
            = createBlock("PF_Portfolio_Create"); // $NON-NLS-0$

    private final DmxmlContext.Block<PFPortfolioResult> pfUpdateBlock
            = createBlock("PF_Portfolio_Update"); // $NON-NLS-0$

    private final DmxmlContext.Block<PFPortfolioResult> pfDeleteBlock
            = createBlock("PF_Portfolio_Delete"); // $NON-NLS-0$

    private final DmxmlContext.Block<PFOrderResult> pfOrderCreateBlock
            = createBlock("PF_Order_Create"); // $NON-NLS-0$

    private final DmxmlContext.Block<PFPortfolioResult> pfOrderDeleteBlock
            = createBlock("PF_Order_Delete"); // $NON-NLS-0$

    private final DmxmlContext.Block<PFOrderResult> pfOrderUpdateBlock
            = createBlock("PF_Order_Update"); // $NON-NLS-0$

    private final ResponseTypeCallback pfCreatedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            portfolioCreated();
        }
    };

    private final ResponseTypeCallback pfUpdatedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            portfolioUpdated();
        }
    };

    private final ResponseTypeCallback pfDeletedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            portfolioDeleted();
        }
    };

    private final ResponseTypeCallback orderCreatedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            orderCreated();
        }
    };

    private final ResponseTypeCallback orderUpdatedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            orderUpdated();
        }
    };

    private final ResponseTypeCallback orderDeletedCallback = new ResponseTypeCallback() {
        protected void onResult() {
            orderDeleted();
        }
    };


    private PortfolioActions() {
    }

    public void createPortfolio(String name, String liquidity, String currency) {
        createPortfolio(name, liquidity, currency, this.pfCreatedCallback);
    }

    public void createPortfolio(String name, String liquidity, String currency, ResponseTypeCallback callback) {
        this.pfCreateBlock.setParameter("name", name); // $NON-NLS-0$
        this.pfCreateBlock.setParameter("liquiditaet", liquidity); // $NON-NLS-0$
        this.pfCreateBlock.setParameter("currencyCode", currency); // $NON-NLS-0$
        this.pfCreateBlock.issueRequest(callback);
        ActionPerformedEvent.fire("X_PF_INS"); // $NON-NLS-0$
    }

    public void deletePortfolio(String id) {
        this.pfDeleteBlock.setParameter("portfolioid", id); // $NON-NLS-0$
        this.pfDeleteBlock.issueRequest(this.pfDeletedCallback);
        ActionPerformedEvent.fire("X_PF_DEL"); // $NON-NLS-0$
    }

    public void updatePortfolio(String id, String name, String liquidity) {
        this.pfUpdateBlock.setParameter("portfolioid", id); // $NON-NLS-0$
        this.pfUpdateBlock.setParameter("name", name); // $NON-NLS-0$
        this.pfUpdateBlock.setParameter("liquiditaet", liquidity); // $NON-NLS-0$
        this.pfUpdateBlock.issueRequest(this.pfUpdatedCallback);
        ActionPerformedEvent.fire("X_PF_UPD"); // $NON-NLS-0$
    }

    public void deleteOrder(final String portfolioId, String orderId) {
        this.pfOrderDeleteBlock.setParameter("portfolioid", portfolioId); // $NON-NLS-0$
        this.pfOrderDeleteBlock.setParameter("orderid", orderId); // $NON-NLS-0$
        this.pfOrderDeleteBlock.issueRequest(this.orderDeletedCallback);
        ActionPerformedEvent.fire("X_PF_O_DEL"); // $NON-NLS-0$
    }

    public void createOrder(final String portfolioId, String symbol, PFOrder order, boolean sellFromOtherMarkets) {
        this.pfOrderCreateBlock.setParameter("portfolioid", portfolioId); // $NON-NLS-0$
        this.pfOrderCreateBlock.setParameter("symbol", symbol); // $NON-NLS-0$
        this.pfOrderCreateBlock.setParameter("amount", order.getAmount()); // $NON-NLS-0$
        this.pfOrderCreateBlock.setParameter("price", order.getPrice()); // $NON-NLS-0$
        this.pfOrderCreateBlock.setParameter("exchangeRate", order.getExchangeRate()); // $NON-NLS-0$
        this.pfOrderCreateBlock.setParameter("date", getFormattedDate(order)); // $NON-NLS-0$
        this.pfOrderCreateBlock.setParameter("ordertype", order.getOrdertype()); // $NON-NLS-0$
        this.pfOrderCreateBlock.setParameter("charge", order.getCharge()); // $NON-NLS-0$
        this.pfOrderCreateBlock.setParameter("sellFromOtherMarkets", sellFromOtherMarkets); // $NON-NLS-0$

        this.pfOrderCreateBlock.issueRequest(this.orderCreatedCallback);
        ActionPerformedEvent.fire("X_PF_O_INS"); // $NON-NLS-0$
    }

    public void updateOrder(final String portfolioId, PFOrder order) {
        this.pfOrderUpdateBlock.setParameter("portfolioid", portfolioId); // $NON-NLS-0$
        this.pfOrderUpdateBlock.setParameter("orderid", order.getOrderid()); // $NON-NLS-0$
        this.pfOrderUpdateBlock.setParameter("amount", order.getAmount()); // $NON-NLS-0$
        this.pfOrderUpdateBlock.setParameter("price", order.getPrice()); // $NON-NLS-0$
        this.pfOrderUpdateBlock.setParameter("exchangeRate", order.getExchangeRate()); // $NON-NLS-0$
        this.pfOrderUpdateBlock.setParameter("date", getFormattedDate(order)); // $NON-NLS-0$
        this.pfOrderUpdateBlock.setParameter("ordertype", order.getOrdertype()); // $NON-NLS-0$
        this.pfOrderUpdateBlock.setParameter("charge", order.getCharge()); // $NON-NLS-0$

        this.pfOrderUpdateBlock.issueRequest(this.orderUpdatedCallback);
        ActionPerformedEvent.fire("X_PF_O_UPD"); // $NON-NLS-0$
    }

    private String getFormattedDate(PFOrder order) {
        final Date date = Formatter.parseISODate(order.getDate());
        return Formatter.formatDateAsISODay(date);
    }

    private <V extends BlockType> DmxmlContext.Block<V> createBlock(final String key) {
        final DmxmlContext.Block<V> result = new DmxmlContext().addBlock(key);
        result.setParameter("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        return result;
    }

    private void portfolioCreated() {
        onResult(this.pfCreateBlock);
    }

    private void portfolioUpdated() {
        onResult(this.pfUpdateBlock);
    }

    private void portfolioDeleted() {
        onResult(this.pfDeleteBlock);
    }

    private void orderCreated() {
        onResult(this.pfOrderCreateBlock);
    }

    private void orderUpdated() {
        onResult(this.pfOrderUpdateBlock);
    }

    private void orderDeleted() {
        onResult(this.pfOrderDeleteBlock);
    }

    private void onResult(DmxmlContext.Block<? extends BlockType> block) {
        if (block.isResponseOk()) {
            onSuccess(block);
        }
        else {

            if("user.portfolio.positions.limit.exceeded".equals(block.getError().getCode())) {// $NON-NLS$
                AbstractMainController.INSTANCE.showError(I18n.I.portfolioPositionsLimitExceeded());
            }
            else {
                AbstractMainController.INSTANCE.showError(I18n.I.internalError());
            }
        }
    }

    private void onSuccess(DmxmlContext.Block<? extends BlockType> block) {
        if (!SessionData.isAsDesign()) {
            doLegacyStuffOnSuccess(block);
            return;
        }

        new LoadPortfoliosMethod(new AsyncCallback<PFEvaluation>() {
            @Override
            public void onFailure(Throwable throwable) {
                if (throwable != null && StringUtil.hasText(throwable.getMessage())) {
                    AbstractMainController.INSTANCE.showError(I18n.I.internalError() + ": " + throwable.getMessage()); // $NON-NLS$
                }
                else {
                    AbstractMainController.INSTANCE.showError(I18n.I.internalError() + ": loading portfolios failed"); // $NON-NLS$
                }
            }

            @Override
            public void onSuccess(PFEvaluation pfEvaluation) {
                final List<PortfolioElement> element = pfEvaluation.getPortfolios().getElement();
                new UpdatePortfolioMenuMethod().withElements(element).execute();

                doOnSuccess(block, pfEvaluation);
            }
        }).execute();
    }

    private void doOnSuccess(DmxmlContext.Block<? extends BlockType> block, PFEvaluation pfEvaluation) {
        if (!SessionData.isAsDesign()) {
            throw new UnsupportedOperationException("only supported in ICE/AS design"); // $NON-NLS$
        }
        PortfolioController.INSTANCE.setToBeRequested();
        PlaceUtil.goTo(getToken(block, pfEvaluation));
    }

    private String getToken(DmxmlContext.Block<? extends BlockType> block, PFEvaluation pfEvaluation) {
        if (!SessionData.isAsDesign()) {
            throw new UnsupportedOperationException("only supported in ICE/AS design"); // $NON-NLS$
        }

        if (block == this.pfDeleteBlock) {
            final List<PortfolioElement> element = pfEvaluation.getPortfolios().getElement();
            if (element.size() < 1) {
                return "B_P"; // $NON-NLS$
            }
            return "B_P_" + element.get(0).getPortfolioid(); // $NON-NLS$
        }

        final BlockType result = block.getResult();
        final String portfolioId;
        if(result instanceof PFPortfolioResult) {
            portfolioId = ((PFPortfolioResult) result).getPortfolioid();
        }
        else if(result instanceof PFOrderResult) {
            portfolioId = ((PFOrderResult) result).getPortfolioid();
        }
        else {
            return "B_P"; // $NON-NLS$
        }
        return "B_P_" + portfolioId; // $NON-NLS$
    }

    private void doLegacyStuffOnSuccess(DmxmlContext.Block<? extends BlockType> block) {
        if (SessionData.isAsDesign()) {
            throw new UnsupportedOperationException("not supported in ICE/AS design"); // $NON-NLS$
        }

        PortfolioController.INSTANCE.setToBeRequested();
        PlaceUtil.goTo(getLegacyToken(block));
    }


    private String getLegacyToken(DmxmlContext.Block<? extends BlockType> block) {
        if (SessionData.isAsDesign()) {
            throw new UnsupportedOperationException("not supported in ICE/AS design"); // $NON-NLS$
        }

        if (block == this.pfDeleteBlock) {
            return "B_P"; // $NON-NLS$
        }

        final BlockType result = block.getResult();
        if(result instanceof PFPortfolioResult) {
            return getLegacyToken(((PFPortfolioResult) result).getPortfolioid());
        }
        else if(result instanceof PFOrderResult) {
            return getLegacyToken(((PFOrderResult) result).getPortfolioid());
        }
        else {
            return "B_P"; // $NON-NLS$
        }
    }

    private String getLegacyToken(String portfolioId) {
        if (SessionData.isAsDesign()) {
            throw new UnsupportedOperationException("not supported in ICE/AS design"); // $NON-NLS$
        }

        return "B_P/" + portfolioId; // $NON-NLS$
    }
}
