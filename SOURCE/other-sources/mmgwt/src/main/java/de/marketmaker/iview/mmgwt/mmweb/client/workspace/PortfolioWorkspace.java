/*
 * InstrumentWorkspace.java
 *
 * Created on 05.04.2008 14:55:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import de.marketmaker.iview.dmxml.PFEvaluation;
import de.marketmaker.iview.dmxml.PortfolioElement;
import de.marketmaker.iview.dmxml.PortfolioPositionElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PortfolioUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortfolioWorkspace extends MultiListWorkspace<PFEvaluation, PortfolioPositionElement> {
    public static final PortfolioWorkspace INSTANCE = new PortfolioWorkspace();

    public PortfolioWorkspace() {
        super(I18n.I.portfolio());
        this.block = this.context.addBlock("PF_Evaluation"); // $NON-NLS-0$
        this.block.setParameter("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("sortBy", "name"); // $NON-NLS-0$ $NON-NLS-1$

        addListsChangeListener(false);

        IconImage.setIconStyle(this.button, "mm-portfolio"); // $NON-NLS-0$
        IconImage.setIconStyle(this.buttonOpenList, "mm-goto-portfolio"); // $NON-NLS-0$

        this.buttonOpenList.setTitle(I18n.I.toPortfolio()); 

        reorganizeMenu();
    }

    @Override
    public String getStateKey() {
        return StateSupport.PORTFOLIO;
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            if (numAdded == this.block.getResult().getElements().getPosition().size()) {
                event.addComponentToReload(this.block, this.pushReloadCallback);
            }
            if (numAdded > 0) {
                return getRenderItems();
            }
        }
        return null;
    }

    protected void openCurrentList() {
        PlaceUtil.goTo("B_P/" + this.block.getParameter("portfolioid")); // $NON-NLS-0$ $NON-NLS-1$
    }

    protected String getCurrentListId() {
        return this.block.getResult().getPortfolio().getPortfolioid();
    }

    protected String getCurrentName() {
        return this.block.getResult().getPortfolio().getName();
    }

    protected List<PortfolioPositionElement> getCurrentList() {
        return this.block.getResult().getElements().getPosition();
    }

    protected Price getPrice(PortfolioPositionElement data) {
        return Price.create(data);
    }

    @Override
    protected QuoteWithInstrument getQuoteWithInstrument(PortfolioPositionElement e) {
        return new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }

    protected void reorganizeMenu() {
        final List<PortfolioElement> elements = SessionData.INSTANCE.getPortfolios();
        if (this.currentListId != null
                && !PortfolioUtil.isPortfolioIdOk(elements, this.currentListId)) {
            this.currentListId = null;
        }

        if (!elements.isEmpty()) {
            if (this.currentListId == null) {
                final PortfolioElement e = elements.get(0);
                this.currentListId = e.getPortfolioid();
                this.block.setParameter("portfolioid", this.currentListId); // $NON-NLS-0$
            }
        }

        this.menu.removeAll();
        for (final PortfolioElement e : elements) {
            this.menu.add(createMenuItem(e));
        }
    }

    private CheckMenuItem createMenuItem(final PortfolioElement e) {
        final CheckMenuItem result = new CheckMenuItem(e.getName());
        result.setChecked(e.getPortfolioid().equals(this.currentListId));
        result.setGroup("pfws"); // $NON-NLS-0$
        result.addListener(Events.CheckChange, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                if (result.isChecked()) {
                    block.setParameter("portfolioid", e.getPortfolioid()); // $NON-NLS-0$
                    refresh();
                }
            }
        });
        return result;
    }
}
