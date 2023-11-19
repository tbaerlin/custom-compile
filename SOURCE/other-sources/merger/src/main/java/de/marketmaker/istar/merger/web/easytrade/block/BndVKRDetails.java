/*
 * BndVKRDetails.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.MasterDataBond;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.bonddata.BondDataProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BndVKRDetails extends EasytradeCommandController {
    private final static Map<String, String> RISK_MESSAGES = new ConcurrentHashMap<>();

    static {
        RISK_MESSAGES.put("Unternehmensanleihen", "Die Ausstattung von Unternehmensanleihen ist nicht standardisiert. In jedem Fall ist es empfehlenswert die Ausstattung der Anleihe zu analysieren. Unternehmensanleihen unterliegen neben dem allgemeinen Zinsrisiko noch dem Kreditrisiko. Eine Verschlechterung der Bonität (Rating), kann zu erheblichen Kurseinbrüchen führen.");
        RISK_MESSAGES.put("Staatsanleihen", "Staatsanleihen unterliegen den allgemeinen Zinsrisiken. Bei Staaten mit geringer Bonität, wie z.B. von südamerikanischen Staaten, kommen noch Kreditrisiken ähnlich wie bei Unternehmensanleihen hinzu.Ausserdem unterliegen Staatsanleihen gewissen politischen Risiken. politische Veränderungen können die Bonität und somit den Kurs beeinflussen.");
        RISK_MESSAGES.put("Pfandbriefe", "Pfandbriefe unterliegen den allgemeinen Zinsrisken. Bonitätsveränderungen können einen Einfluss auf den Kurs haben. Die Einfüsse auf den Kurs sind jedoch auf Grund der Sicherung nicht so gross.");
        RISK_MESSAGES.put("Postbankanleihen", "Postbank Anleihen wie Schätze und Postbank Pfandbriefe unterliegen den allgemeinen Kredit- und Marktpreisrisiken. Die Postbank verfügt über ein sehr gutes Rating und ist im Einlagensicherungsfonds des Bundesverbandes öffentlicher Banken Deutschlands angeschlossen. Die Preisfindung der Postbankanleihen orientiert sich am Kapitalmarkt und unterliegt somit den allgemeinen Zinsrisiken.");
        RISK_MESSAGES.put("USD-Anleihen", "Die Anleihen unterliegen zusätzlich zum Markt- und Kreditrisiko noch dem Währungsrisiko.");
    }

    private EasytradeInstrumentProvider instrumentProvider;

    private BondDataProvider bondDataProvider;

    private IntradayProvider intradayProvider;

    public BndVKRDetails() {
        super(DefaultSymbolCommand.class);
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setBondDataProvider(BondDataProvider bondDataProvider) {
        this.bondDataProvider = bondDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final MasterDataBond masterData = this.bondDataProvider.getMasterData(quote.getInstrument().getId());
        final PriceRecord priceRecord = this.intradayProvider.getPriceRecords(Collections.singletonList(quote)).get(0);
        final SnapRecord staticSr = this.intradayProvider.getIntradayData(quote, null).getSnap();
        final SnapField field = staticSr.getField(VwdFieldDescription.ADF_WP_Name_kurz.id());
        final String deckung = field.isDefined() ? field.getValue().toString() : null;

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("masterData", masterData);
        model.put("language", LocalizedUtil.getLanguage(masterData));
        model.put("priceRecord", priceRecord);
        model.put("deckung", deckung);
        final SnapField typeField = staticSr.getField(VwdFieldDescription.ADF_WP_Name_lang.id());
        final String type;
        if (typeField.isDefined()) {
            type = (String) typeField.getValue();
        }
        else {
            type = null;
        }
        model.put("riskmessage", type == null ? null : RISK_MESSAGES.get(type));
        return new ModelAndView("bndvkrdetails", model);
    }
}
