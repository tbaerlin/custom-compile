package de.marketmaker.iview.mmgwt.mmweb.client.push;

import java.util.ArrayList;

import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.Settings;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

/**
 * Created on 10.03.2010 08:55:25
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
class PushBrowserTitle extends ResponseTypeCallback implements PushRegisterHandler {

    private final PriceSupport priceSupport;
    private boolean isActive;
    private DmxmlContext.Block<MSCPriceData> block;

    PushBrowserTitle() {
        this.priceSupport = new PriceSupport(this);
        final DmxmlContext context = new DmxmlContext();
        this.block = context.addBlock("MSC_PriceData"); // $NON-NLS-0$
        this.block.setParameter("symbol", Settings.INSTANCE.mainWindowPushQuote()); // $NON-NLS-0$
        context.setCancellable(false);
        context.issueRequest(this);
    }

    @Override
    protected void onResult() {
        if (this.block.isResponseOk()) {
            this.priceSupport.activate();
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (isPushTitle()) {
            if (!event.addVwdcode(this.block.getResult())) {
                this.priceSupport.deactivate();
                this.isActive = false;
            }
            this.isActive = true;
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (isPushTitle() && event.isPushedUpdate() && !this.priceSupport.isLatestPriceGeneration() && this.isActive) {
            AbstractMainController.INSTANCE.setWindowTitlePrefix(renderTitlePrefix());
        }
    }

    private String renderTitlePrefix() {
        final Price p = Price.create(this.block.getResult());
        final String change = Renderer.PERCENT.render(p.getChangePercent());
        final StringBuilder sb = new StringBuilder();
        return sb.append(Settings.INSTANCE.mainWindowPushTitle()).append(" ") // $NON-NLS-0$
                .append(Renderer.PRICE23.render(p.getLastPrice().getPrice())).append(" ") // $NON-NLS-0$
                .append("(").append(change.startsWith("-") ? change : "+" + change).append(")") // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
                .toString();
    }

    void onPushStop() {
        this.isActive = false;
        AbstractMainController.INSTANCE.setDefaultWindowTitle();
    }

    private boolean isPushTitle() {
        return SessionData.INSTANCE.getUser()
                .getAppConfig().getBooleanProperty(AppConfig.SHOW_PUSH_IN_TITLE, false);
    }
}
