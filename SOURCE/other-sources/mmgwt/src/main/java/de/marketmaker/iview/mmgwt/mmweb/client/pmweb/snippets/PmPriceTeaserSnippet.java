/*
 * PmPriceTeaserSnippet.java
 *
 * Created on 10.06.13 16:07
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.PriceData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ShellMMTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.Instrument;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceTeaserSnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentHeaderProvider;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.Date;

/**
 * @author Markus Dick
 */
public class PmPriceTeaserSnippet
        extends AbstractSnippet<PmPriceTeaserSnippet, PriceTeaserSnippetView<PmPriceTeaserSnippet>>
        implements SecurityIdSnippet, ContentHeaderProvider {

    public static class Class extends SnippetClass {
        public Class() {
            super("PmPriceTeaser"); //$NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PmPriceTeaserSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("colSpan", 3); // $NON-NLS$
        }
    }

    private Instrument.InstrumentBaseDataWithPrice instrumentMmTalker;
    private final DmxmlContext.Block<MMTalkResponse> block;

    public PmPriceTeaserSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        this.block = this.context.addBlock("PM_MMTalk"); // $NON-NLS$
        getConfiguration().put("marketSelection", false); // $NON-NLS$
        setView(new PriceTeaserSnippetView<>(this, false, false, false, false));
    }

    @Override
    public void destroy() {
        destroyBlock(this.block);
    }

    @Override
    public void updateView() {
        if (this.block.isResponseOk()) {
            final Instrument i = this.instrumentMmTalker.createResultObject(this.block.getResult());
            if (i == null) {
                Firebug.warn("<PmPriceTeaserSnippet.updateView> response ok but instrument is null");
                doUpdateViewNoData();
                return;
            }
            doUpdateView(i);
            return;
        }
        Firebug.warn("<PmPriceTeaserSnippet.updateView> response not ok: " + this.block.getError().getCode());
        doUpdateViewNoData();
    }

    private void doUpdateView(Instrument i) {
        if (SessionData.isAsDesign()) {
            final String name = i.getName() + I18n.I.zoneNameSuffix(i.getZone());
            AbstractMainController.INSTANCE.getView().setContentHeader(name);
        }
        getView().update(toInstrumentData(i), toQuoteData(i), null, toPrice(i), null, null, null);
    }

    private void doUpdateViewNoData() {
        if (SessionData.isAsDesign()) {
            AbstractMainController.INSTANCE.getView().setContentHeader(I18n.I.noDataAvailable());
        }
        getView().update(null, null, null, null, null, null, null);
    }

    private Price toPrice(final Instrument i) {
        final PriceData pd = new PriceData();
        pd.setPrice(toDmxmlNumberString(i.getPrice()));
        pd.setHigh1Y(toDmxmlNumberString(i.getHigh52Weeks()));
        pd.setLow1Y(toDmxmlNumberString((i.getLow52Weeks())));
        pd.setDate(toDmxmlDateString(i.getPriceDateTime()));
        pd.setChangePercent(toDmxmlNumberString(i.getChangePercent()));
        pd.setPriceQuality(StringUtil.hasText(i.getPrice()) ? "END_OF_DAY" : null); //$NON-NLS$
        return new Price(pd);
    }

    private String toDmxmlNumberString(String pmxmlNumberString) {
        if(!StringUtil.hasText(pmxmlNumberString)) {
            return null;
        }
        return pmxmlNumberString;
    }

    private String toDmxmlDateString(String pmxmlDateString) {
        if (!StringUtil.hasText(pmxmlDateString)) {
            return null;
        }

        final Date date = Formatter.PM_DATE_TIME_FORMAT_MMTALK.parse(pmxmlDateString);
        return Formatter.DMXML_DATE_TIME_FORMAT.format(date);
    }

    private QuoteData toQuoteData(Instrument i) {
        final QuoteData qd = new QuoteData();
        qd.setCurrencyIso(i.getCurrencyIso());
        qd.setQid(i.getSecurityId());
        qd.setMarketName(i.getHomeExchange().getName());
        return qd;
    }

    private InstrumentData toInstrumentData(Instrument i) {
        final InstrumentData id = new InstrumentData();
        id.setName(i.getName());
        id.setIsin(i.getIsin());
        id.setType(ShellMMTypeUtil.toInstrumentTypeString(i.getType()));
        id.setIid(i.getSecurityId());
        id.setWkn(i.getDewkn());
        id.setExpiration(i.getExpiration());
        return id;
    }

    @Override
    public void setSecurityId(ShellMMType shellMMType, String securityId) {
        this.instrumentMmTalker = new Instrument.InstrumentBaseDataWithPrice();
        this.instrumentMmTalker.setSecurityId(securityId);
        this.block.setParameter(this.instrumentMmTalker.createRequest());
    }
}
