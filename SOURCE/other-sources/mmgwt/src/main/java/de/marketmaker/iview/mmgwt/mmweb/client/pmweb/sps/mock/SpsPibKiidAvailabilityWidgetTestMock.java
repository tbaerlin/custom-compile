/*
 * SpsPibKiidWidgetTestMock.java
 *
 * Created on 02.06.2014 13:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ShellMMTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.PreProcessHook;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerNode;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.WidgetDesc;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addDecl;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEditWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEditWidgetWithObject;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addEnum;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.addLabelWidget;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.e;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil.pti;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsPibKiidAvailabilityWidgetTestMock extends PreProcessHook {
    @Override
    public void preProcess(WidgetDesc wd) {
        final SectionDesc s = MockUtil.prepareRootSectionWidget(wd);

        addEditWidget(s, "/instrument", "Instrument");
        addLabelWidget(s, "Instrument", "/instrument", "vwd document manager", "pibKiidAvailability");
        addLabelWidget(s, "Instrument", "/instrument", "vwd document manager", "pibKiidAvailabilityIcon");

        addEditWidget(s, "/instrument2", "Instrument");
        addEditWidget(s, "/transaction", "Transaktion");
        addEditWidgetWithObject(s, "/instrument2", "/transaction",
                "vwd document manager (visible on buy/subscribe)", "pibKiidAvailability");
        addEditWidgetWithObject(s, "/instrument2", "/transaction",
                "vwd document manager (visible on buy/subscribe)", "pibKiidAvailabilityIcon");

    }

    @Override
    public void preProcess(DataContainerNode dcn, boolean decl) {
        final DataContainerCompositeNode parent = (DataContainerCompositeNode) dcn;
        parent.getChildren().clear();

        final ParsedTypeInfo instrumentsPti = pti(TiType.TI_SHELL_MM, "Instrument", false, false, "", "", "", "", 0, "");
        instrumentsPti.getFolderTypes().addAll(ShellMMTypeUtil.getSecurityTypes());

        addDecl(decl, parent, "instrument", instrumentsPti);
        addDecl(decl, parent, "instrument2", instrumentsPti);

        /**
         * MMTalk name values of
         *  sSLTAnlageplanungsTransaktionsTypNA = 'n/a';
         *  sSLTAnlageplanungsTransaktionsTypAnkauf = 'Kaufen';
         *  sSLTAnlageplanungsTransaktionsTypZeichnung = 'Zeichnen';
         *  sSLTAnlageplanungsTransaktionsTypVerkauf = 'Verkaufen';
         *  sSLTAnlageplanungsTransaktionsTypHalten = 'Halten';
         *  sSLTAnlageplanungsTransaktionsTypNichtKaufen = 'Nicht kaufen';
         */
        addEnum(decl, parent, "transaction", e("n/a", "n/a"), e("Buy", "Kaufen"), e("Subscribe", "Zeichnen"),
                e("Sell", "Verkaufen"), e("Hold", "Halten"), e("Don't buy", "Nicht kaufen"));
    }
}
