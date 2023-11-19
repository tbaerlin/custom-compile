/*
 * InstrumentWidget.java
 *
 * Created on 07.06.13 12:38
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmPlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.Instrument;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.Market;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.UserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history.PmItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringWithSupplementRenderer;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.Collections;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractUserObjectView.addField;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractUserObjectView.addMultilineField;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractUserObjectView.addSection;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractUserObjectView.addSubHeading;

/**
 * @author Markus Dick
 */
public class InstrumentWidget extends Composite implements TakesValue<Instrument> {
    private static final StringWithSupplementRenderer NAME_TOKEN =
            new StringWithSupplementRenderer(PmRenderers.NO_NA, PmRenderers.NO_NA);

    private final Panel layout;
    private Instrument security;

    public InstrumentWidget() {
        this.security = null;
        this.layout = new FlowPanel();
        this.layout.setStyleName(AbstractUserObjectView.VIEW_STYLE);
        initWidget(this.layout);
    }

    private void update() {
        final Instrument s = this.security;
        this.layout.clear();

        if (s == null) {
            this.layout.add(new HTML("<center>" + I18n.I.noDataAvailable() + "</center>")); // $NON-NLS$
            return;
        }

        addStaticData(s);
        addTypeSpecificFields(s);
        addWMData(s);
        addUserDefinedFields(s);
    }

    private void addStaticData(Instrument s) {
        final Panel p = addSection(this.layout, I18n.I.staticData());

        addField(p, I18n.I.name(), s.getOriginalName());
        addField(p, I18n.I.wmName(), s.getWmName());
        addField(p, I18n.I.displayName(), s.getName());
        addField(p, I18n.I.ticker(), s.getTickerSymbol());
        addField(p, I18n.I.type(), PmRenderers.SHELL_MM_TYPE.render(s.getType()));
        addField(p, I18n.I.securityId(), s.getSecurityId());
        addField(p, "ISIN", s.getIsin()); // $NON-NLS$
        addField(p, "DEWKN", s.getDewkn()); // $NON-NLS$
        addField(p, I18n.I.valorNumber(), s.getValorNumber());
        addField(p, "OEWKN", s.getOewkn()); // $NON-NLS$
        addField(p, I18n.I.country(), joinNoNA(s.getCountryToken(), s.getCountryName()));
        addField(p, I18n.I.sector(), joinNoNA(s.getSectorToken(), s.getSectorName()));
        addField(p, I18n.I.issueDate(), PmRenderers.DATE_STRING.render(s.getIssuingDate()));
        addField(p, I18n.I.smallestTransferableUnit(), s.getTradingUnit());
        addField(p, I18n.I.firstTradingDay(), PmRenderers.DATE_STRING.render(s.getFirstTradingDay()));
        addField(p, I18n.I.lastTradingDay(), PmRenderers.DATE_STRING.render(s.getLastTradingDay()));

        addField(p, I18n.I.benchmark(), newPortraitLinkWidget(s.getBenchmarkInstrumentName(),
                s.getBenchmarkInstrumentSecurityId(), s.getBenchmarkInstrumentType()));
        addMultilineField(p, I18n.I.comment(), s.getComment());
    }

    private void addWMData(Instrument s) {
        final Panel p = addSection(this.layout, I18n.I.wmData());

        addField(p, I18n.I.wmSecurityFullName(1), s.getWmLongName1());
        addField(p, I18n.I.wmSecurityFullName(2), s.getWmLongName2());
        addField(p, I18n.I.settlementCurrency(), s.getWmSettlementCurrencyIso());
        Market hx = s.getWmHomeExchange();
        addField(p, I18n.I.domesticStockExchange(), (hx != null ? joinNoNA(hx.getMic(), hx.getName()) : ""));
        addField(p, I18n.I.wmSector(), joinNoNA(s.getWmSectorToken(), s.getWmSectorName()));
        addField(p, I18n.I.issuerCategory(),
                joinNoNA(s.getWmIssuerCategoryToken(), s.getWmIssuerCategoryName()));
        addField(p, I18n.I.wmProductTypeSegmentation(),
                joinNoNA(s.getWmProductTypeSectorToken(), s.getWmProductTypeSectorName()));
        addField(p, I18n.I.wmProductGroupSegmentation(),
                joinNoNA(s.getWmProductGroupSectorToken(), s.getWmProductGroupSectorName()));
        addField(p, I18n.I.wmInstrumentType(),
                joinNoNA(s.getWmInstrumentenTypeToken(), s.getWmInstrumentenTypeName()));
        addField(p, I18n.I.wmInstrumentTypeAddition(1),
                joinNoNA(s.getWmInstrumentenTypeAnnex1Token(), s.getWmInstrumentenTypeAnnex1Name()));
        addField(p, I18n.I.wmInstrumentTypeAddition(2),
                joinNoNA(s.getWmInstrumentenTypeAnnex2Token(), s.getWmInstrumentenTypeAnnex2Name()));
        addField(p, I18n.I.wmInstrumentTypeAddition(3),
                joinNoNA(s.getWmInstrumentenTypeAnnex3Token() , s.getWmInstrumentenTypeAnnex3Name()));
        addField(p, I18n.I.wmInstrumentTypeAddition(4),
                joinNoNA(s.getWmInstrumentenTypeAnnex4Token(), s.getWmInstrumentenTypeAnnex4Name()));
    }

    private void addUserDefinedFields(Instrument s) {
        final Panel p = addSection(this.layout, I18n.I.userDefinedFields());
        for (UserDefinedFields field : s.getUserDefinedFields()) {
            AbstractUserObjectView.addUserDefinedFields(p, field);
        }
    }

    private void addTypeSpecificFields(Instrument s) {
        final Panel p = addSection(this.layout, I18n.I.extended());
        final String expiration = PmRenderers.DATE_TIME_STRING.render(s.getExpiration());

        switch (s.getType()) {
            case ST_AKTIE:
                addStockSpecificFields(s, p);
                break;
            case ST_ANLEIHE:
                addBondSpecific(s, p, expiration);
                break;
            case ST_OPTION:
            case ST_OS:
                addOptionAndWarrantSpecific(s, p, expiration);
                break;
            case ST_CERTIFICATE:
                addCertificateSpecific(s, p, expiration);
                break;
            case ST_FUTURE:
                addFutureSpecific(p, expiration);
                break;
            case ST_GENUSS:
                addParticipatoryCertificateSpecific(p, expiration);
                break;
            default:
                this.layout.remove(p);
        }
    }

    private void addStockSpecificFields(Instrument s, Panel p) {
        final String currencyOfEstimations = s.getEstimationCurrency();
        final Renderer<String> pr = new PriceWithCurrencyRenderer(currencyOfEstimations);

        final int firstYear;
        if(StringUtil.hasText(s.getEstimatesForYearOne())) {
            firstYear = Integer.parseInt(s.getEstimatesForYearOne());
        }
        else {
            firstYear = 0;
        }

        addField(p, I18n.I.percentQuotation(), s.isPercentQuotation());
        addField(p, I18n.I.perValue(), s.getNominalValue());
        addField(p, I18n.I.shareholdersMeetingDate(), PmRenderers.DATE_STRING.render(s.getShareholdersMeetingDate()));

        addField(p, I18n.I.estimatesForYearOne(), (firstYear > 0 ? Integer.toString(firstYear) : ""));
        addField(p, I18n.I.estimationCurrency(), PmRenderers.NO_NA.render(s.getEstimationCurrency()));

        if(firstYear > 0) {
            addSubHeading(p, I18n.I.estimatesEarnings());
            int year = firstYear;
            addField(p, Integer.toString(year), pr.render(s.getEarningsEstimates1()));
            addField(p, Integer.toString(++year), pr.render(s.getEarningsEstimates2()));
            addField(p, Integer.toString(++year), pr.render(s.getEarningsEstimates3()));
            addField(p, Integer.toString(++year), pr.render(s.getEarningsEstimates4()));

            addSubHeading(p, I18n.I.estimatesDividends());
            year = firstYear;
            addField(p, Integer.toString(year), pr.render(s.getDividendEstimates1()));
            addField(p, Integer.toString(++year), pr.render(s.getDividendEstimates2()));
            addField(p, Integer.toString(++year), pr.render(s.getDividendEstimates3()));
            addField(p, Integer.toString(++year), pr.render(s.getDividendEstimates4()));

            addSubHeading(p, I18n.I.estimatesCashFlow());
            addField(p, Integer.toString(firstYear), pr.render(s.getCashFlowEstimates1()));
        }
    }

    private void addBondSpecific(Instrument s, Panel p, String maturity) {
        addField(p, I18n.I.bondType(), s.getBondType());
        addField(p, I18n.I.pmSecurityStaticDataBondMaturity(), maturity);
        addField(p, I18n.I.pmSecurityStaticDataCoupon(), Renderer.PERCENT23.render(s.getCoupon()));
        addField(p, I18n.I.pmSecurityStaticDataCouponPaymentDay(), s.getCouponPaymentDate());
        addField(p, I18n.I.pmSecurityStaticDataRedemptionPrice(), s.getRedemptionPrice());
        addField(p, I18n.I.pmSecurityStaticDataCouponFrequency(), s.getCouponFrequency());
        addField(p, I18n.I.pmSecurityStaticDataDayCountConvention(), s.getDayCountConvention());
    }

    private void addOptionAndWarrantSpecific(Instrument s, Panel p, String expiration) {
        addField(p, I18n.I.optionType(), s.getOptionType());
        addField(p, I18n.I.pmSecurityStaticDataMaturity(), expiration);
    }

    private void addCertificateSpecific(Instrument s, Panel p, String maturity) {
        addField(p, I18n.I.certificateType(), s.getCertificateTypeName());
        addField(p, I18n.I.speculationType(), s.getSpeculationTypeName());
        addField(p, I18n.I.productNameIssuer(), s.getProductName());
        addField(p, I18n.I.pmSecurityStaticDataMaturity(), maturity);
    }

    private void addFutureSpecific(Panel p, String expiration) {
        addField(p, I18n.I.pmSecurityStaticDataMaturity(), expiration);
    }

    private void addParticipatoryCertificateSpecific(Panel p, String expiration) {
        addField(p, I18n.I.pmSecurityStaticDataMaturity(), expiration);
    }

    private static Map.Entry<String, String> pair(String s1, String s2) {
        return Collections.singletonMap(s1, s2).entrySet().iterator().next();
    }

    private Widget newPortraitLinkWidget(String name, String securityId, ShellMMType type) {
        if(StringUtil.hasText(name)) {
            if(StringUtil.hasText(securityId) && type != null) {
                final ShellMMInfo shellMMInfo = new ShellMMInfo();
                shellMMInfo.setMMSecurityID(securityId);
                shellMMInfo.setTyp(type);

                final String historyContextName = this.security.getName();

                final Label label = new Label(name);
                label.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        final PmItemListContext.ShellMMInfoItemListContext historyContext =
                                PmItemListContext.createForShellMMInfo(historyContextName,
                                        shellMMInfo, Collections.singletonList(shellMMInfo));

                        PmPlaceUtil.goTo(shellMMInfo, historyContext);
                    }
                });
                label.setStyleName("mm-link");
                return label;
            }
            return new Label(name);
        }
        return new Label();
    }

    @Override
    public void setValue(Instrument value) {
        this.security = value;
        update();
    }

    @Override
    public Instrument getValue() {
        return this.security;
    }

    private static String joinNoNA(String token, String name) {
        return NAME_TOKEN.render(pair(name, token));
    }

    private static final class PriceWithCurrencyRenderer implements Renderer<String> {
        private final String currency;

        private PriceWithCurrencyRenderer(String currency) {
            this.currency = currency;
        }

        @Override
        public String render(String s) {
            if(!StringUtil.hasText(s)) {
                return "";
            }
            return StringUtil.join(' ', Renderer.PRICE23.render(s), currency);
        }
    }
}
