/*
 * QwiMenu.java
 *
 * Created on 11.08.2008 17:09:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContentFlagsEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCER;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderWNT;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * @author Michael Lösch
 */
public class VRFinderLinkMenu {
    public static final VRFinderLinkMenu INSTANCE = new VRFinderLinkMenu();

    private final Menu menu;
    private QuoteWithInstrument qwi;
    private final MenuItem cerItem;
    private final MenuItem levItem;
    private final MenuItem wntItem;

    private VRFinderLinkMenu() {
        this.menu = new Menu();

        this.cerItem = new MenuItem(I18n.I.certificates(), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent menuEvent) {
                gotoCerFinder(qwi);
            }
        });
        this.menu.add(this.cerItem);

        this.levItem = new MenuItem(I18n.I.leverageProducts(), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent menuEvent) {
                gotoLevFinder(qwi);
            }
        });
        this.menu.add(this.levItem);

        this.wntItem = new MenuItem(I18n.I.warrants(), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent menuEvent) {
                gotoWntFinder(qwi);
            }
        });
        this.menu.add(this.wntItem);
    }

    public void show(QuoteWithInstrument qwi, Element anchor) {
        this.qwi = qwi;
        final QuoteData qd = qwi.getQuoteData();
        this.cerItem.setEnabled(ContentFlagsEnum.CerUnderlyingDzbank.isAvailableFor(qd));
        this.levItem.setEnabled(ContentFlagsEnum.LeverageProductUnderlyingDzbank.isAvailableFor(qd));
        this.wntItem.setEnabled(ContentFlagsEnum.WntUnderlyingDzbank.isAvailableFor(qd));
        this.menu.showAt(anchor.getAbsoluteLeft(), anchor.getAbsoluteTop() + anchor.getOffsetHeight());
    }

    public static void gotoWntFinder(QuoteWithInstrument qwi) {
        final FinderController controller = LiveFinderWNT.INSTANCE;
        final FinderFormConfig ffc = new FinderFormConfig("temp", LiveFinderWNT.INSTANCE.getId()); // $NON-NLS-0$
        ffc.put(LiveFinderWNT.UNDERLYING_ID, "true"); // $NON-NLS-0$
        handleUnderlying(qwi, ffc);
        ffc.put(LiveFinderWNT.BASE_ID, "true"); // $NON-NLS$
        ffc.put(FinderFormKeys.ONLY_CUSTOM_ISSUER, "true"); // $NON-NLS$
        controller.prepareFind(ffc);
        PlaceUtil.goTo("M_LF_WNT"); // $NON-NLS-0$
    }


    public static void gotoCerFinder(QuoteWithInstrument qwi) {
        final FinderController controller = LiveFinderCER.INSTANCE_CER;
        final FinderFormConfig ffc = new FinderFormConfig("temp", LiveFinderCER.INSTANCE_CER.getId()); // $NON-NLS-0$
        ffc.put(LiveFinderCER.UNDERLYING_ID, "true"); // $NON-NLS-0$
        handleUnderlying(qwi, ffc);
        ffc.put(LiveFinderCER.BASE_ID, "true"); // $NON-NLS-0$
        ffc.put(FinderFormKeys.ONLY_CUSTOM_ISSUER, "true"); // $NON-NLS-0$
        controller.prepareFind(ffc);
        PlaceUtil.goTo("M_LF_CER"); // $NON-NLS-0$
    }

    public static void gotoLevFinder(QuoteWithInstrument qwi) {
        final FinderController controller = LiveFinderCER.INSTANCE_LEV;
        final FinderFormConfig ffc = new FinderFormConfig("temp", LiveFinderCER.INSTANCE_LEV.getId()); // $NON-NLS-0$
        ffc.put(LiveFinderCER.UNDERLYING_ID, "true"); // $NON-NLS-0$
        handleUnderlying(qwi, ffc);
        ffc.put(LiveFinderCER.BASE_ID, "true"); // $NON-NLS$
        ffc.put(FinderFormKeys.ONLY_CUSTOM_ISSUER, "true"); // $NON-NLS$
        controller.prepareFind(ffc);
        PlaceUtil.goTo("M_LF_LEV"); // $NON-NLS-0$
    }

    private static void handleUnderlying(QuoteWithInstrument qwi, FinderFormConfig ffc) {
        ffc.put(FinderFormKeys.UNDERLYING, "true"); // $NON-NLS-0$
        ffc.put(FinderFormKeys.UNDERLYING + "-symbol", qwi.getIid(false)); // $NON-NLS$
        ffc.put(FinderFormKeys.UNDERLYING + "-name", qwi.getInstrumentData().getName()); // $NON-NLS$
    }

    public static void gotoFinder(QuoteWithInstrument qwi, ContentFlagsEnum singleFlagAllowed) {
        if (singleFlagAllowed == ContentFlagsEnum.CerUnderlyingDzbank ||
                singleFlagAllowed == ContentFlagsEnum.CerUnderlyingWgzbank) {
            gotoCerFinder(qwi);
        }
        if (singleFlagAllowed == ContentFlagsEnum.LeverageProductUnderlyingDzbank) {
            gotoLevFinder(qwi);
        }
        if (singleFlagAllowed == ContentFlagsEnum.WntUnderlyingDzbank) {
            gotoWntFinder(qwi);
        }
    }
}