/*
 * StaticDataSnippetView.java
 *
 * Created on ${DATE}
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.FlowPanel;
import de.marketmaker.iview.dmxml.BNDDetailedStaticData;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.CERStaticData;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.dmxml.EDGRatingData;
import de.marketmaker.iview.dmxml.FNDStaticData;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCStaticData;
import de.marketmaker.iview.dmxml.OPTStaticData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.STKStaticData;
import de.marketmaker.iview.dmxml.WNTStaticData;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.Settings;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitFNDController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions.StaticDataTableExtension;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.EdgUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer.PRICE;

/**
 * @author Stefan Willenbrock
 */
public abstract class StaticDataSnippetView<T extends BlockType> extends SnippetView<StaticDataSnippet<T>>  {

    public static class CurrencyView extends StaticDataSnippetView<STKStaticData> {
        public CurrencyView(StaticDataSnippet snippet) {
            super(snippet, false);
        }

        @Override
        public void update(STKStaticData curData) {
            final List<RowData> list = new ArrayList<>();

            super.addDefaultData(list, curData.getInstrumentdata(), null, curData.getTypename(), curData.getQuotedata());
            final IdentifierData curBenchmark = curData.getBenchmark();
            if (curBenchmark != null) {
                super.add(list, I18n.I.benchmark(), curBenchmark.getInstrumentdata().getName());
            }
            super.addStaticResult(list);
        }
    }

    public static class IndexView extends StaticDataSnippetView<MSCStaticData> {
        public IndexView(StaticDataSnippet snippet) {
            super(snippet, false);
        }

        @Override
        public void update(MSCStaticData indData) {
            final List<RowData> list = new ArrayList<>();

            super.addDefaultData(list, indData.getInstrumentdata(), null, indData.getTypename(), indData.getQuotedata());
            list.add(new RowData(I18n.I.category(), indData.getSector()));
            final IdentifierData indBenchmark = indData.getBenchmark();
            if (indBenchmark != null) {
                super.add(list, I18n.I.benchmark(), indBenchmark.getInstrumentdata().getName());
            }
            super.addStaticResult(list);
        }
    }

    public static class FutureView extends StaticDataSnippetView<STKStaticData> {
        public FutureView(StaticDataSnippet snippet) {
            super(snippet, false);
        }

        @Override
        public void update(STKStaticData data) {
            final List<RowData> list = new ArrayList<>();
            super.addDefaultData(list, data.getInstrumentdata(), data.getTickersymbol(), data.getTypename(), data.getQuotedata());
            super.addStaticResult(list);
        }
    }

    public static class OptionView extends StaticDataSnippetView<OPTStaticData> {
        public OptionView(StaticDataSnippet snippet) {
            super(snippet, false);
        }

        @Override
        public void update(OPTStaticData data) {
            final List<RowData> list = new ArrayList<>();
            super.addDefaultData(list, data.getInstrumentdata(), null, data.getTypename(), data.getQuotedata());
            list.add(new RowData(I18n.I.type(), data.getWarranttype()));
            list.add(new RowData(I18n.I.strike(), Renderer.PRICE.render(data.getStrike())));
            list.add(new RowData(I18n.I.maturity2(), Formatter.LF.formatDate(data.getMaturity())));
            list.add(new RowData(I18n.I.contractSize(), data.getContractSize()));
            super.addStaticResult(list);
        }
    }

    public static class StockView extends StaticDataSnippetView<STKStaticData> {
        public StockView(StaticDataSnippet snippet) {
            super(snippet, false);
        }

        @Override
        public void update(STKStaticData data) {
            final List<RowData> list = new ArrayList<>();
            super.addDefaultData(list, data.getInstrumentdata(), null, data.getTypename(), data.getQuotedata());
            list.add(new RowData(I18n.I.category(), data.getSector()));
            super.addStaticResult(list);
        }
    }

    public static class MscView extends StaticDataSnippetView<MSCStaticData> {
        public MscView(StaticDataSnippet snippet) {
            super(snippet, false);
        }

        @Override
        public void update(MSCStaticData data) {
            final List<RowData> list = new ArrayList<>();
            super.addDefaultData(list, data.getInstrumentdata(), null, data.getTypename(), data.getQuotedata());
            list.add(new RowData(I18n.I.category(), data.getSector()));
            super.addStaticResult(list);
        }
    }

    public static class WarrantView extends StaticDataSnippetView<WNTStaticData> {
        public WarrantView(StaticDataSnippet snippet) {
            super(snippet, false);
        }

        @Override
        public void update(WNTStaticData data) {
            final List<RowData> list = new ArrayList<>();
            super.addDefaultData(list, data.getInstrumentdata(), data.getTickersymbol(), data.getTypename(), data.getQuotedata());
            list.add(new RowData(I18n.I.issuer(), data.getIssuer()));
            list.add(new RowData(I18n.I.strike(), PRICE.render(data.getStrike())));
            list.add(new RowData(I18n.I.issueDate2(), Formatter.LF.formatDate(data.getIssuedate())));
            list.add(new RowData(I18n.I.firstTradingDay(), Formatter.LF.formatDate(data.getFirstTradingDate())));
            list.add(new RowData(I18n.I.lastTradingDay(), Formatter.LF.formatDate(data.getLastTradingDate())));
            list.add(new RowData(I18n.I.maturity2(), Formatter.LF.formatDate(data.getMaturity())));
            list.add(new RowData(I18n.I.warrantTypePutCall(), data.getWarranttype()));
            list.add(new RowData(I18n.I.exerciseTypeUsEu(), data.getExercisetype()));
            list.add(new RowData(I18n.I.subscriptionRatio(), data.getSubscriptionratioText()));
            super.addEdg(list);
            super.addStaticResult(list);
        }
    }

    public static class BondView extends StaticDataSnippetView<BNDDetailedStaticData> {
        public BondView(StaticDataSnippet snippet) {
            super(snippet, hasRankings());
        }

        private static boolean hasRankings() {
            return Selector.RATING_FITCH.isAllowed() || Selector.RATING_MOODYS.isAllowed() || Selector.RATING_SuP.isAllowed();
        }

        @Override
        public void update(BNDDetailedStaticData data) {
            final List<RowData> list = new ArrayList<>();

            final InstrumentData instrumentdata = data.getInstrumentdata();
            super.addDefaultData(list, instrumentdata, null, null, data.getQuotedata());
            if (this.snippet.isWithQuoteLink()) {
                list.add(new RowData(I18n.I.name(), new QuoteWithInstrument(instrumentdata, data.getQuotedata())));
            }
            list.add(new RowData(I18n.I.wmName(), data.getWmName()));
            list.add(new RowData(I18n.I.currency(), data.getQuotedata().getCurrencyIso()));
            list.add(new RowData(I18n.I.country(), data.getCountry()));
            list.add(new RowData(I18n.I.issuer(), data.getIssuername()));
            list.add(new RowData(I18n.I.bondType(), data.getBondType()));
            list.add(new RowData(I18n.I.interestType(), data.getInterestTypeName()));
            list.add(new RowData(I18n.I.coupon(), Renderer.PERCENT23.render(data.getInterest())));
            list.add(new RowData(I18n.I.redemptionPrice1(), Renderer.PRICE.render(data.getRedemptionPrice())));
            list.add(new RowData(I18n.I.firstCouponDate(), Formatter.LF.formatDate(data.getFirstCouponDate())));
            list.add(new RowData(I18n.I.lastCouponDate(), Formatter.LF.formatDate(data.getLastCouponDate())));
            final String expirationDate = data.getExpirationDate();
            list.add(new RowData(I18n.I.maturity2(), Formatter.LF.formatDate(expirationDate)));
            list.add(new RowData(I18n.I.yearsToExpiration(), getYearsToExpiration(expirationDate)));
            list.add(new RowData(I18n.I.couponFrequency(), getCouponFrequency(data.getCouponFrequency())));
            list.add(new RowData(I18n.I.issuePrice1(), Renderer.PRICE.render(data.getIssuePrice())));
            list.add(new RowData(I18n.I.issueDate(), Formatter.LF.formatDate(data.getIssueDate())));
            list.add(new RowData(I18n.I.issueVolume(), Renderer.LARGE_NUMBER.render(data.getIssueVolume())));
            list.add(new RowData(I18n.I.benchmark(), data.getBenchmark().getInstrumentdata().getName()));
            list.add(new RowData(I18n.I.smallestTransferableUnit(), Renderer.PRICE.render(data.getSmallestTransferableUnit())));
            list.add(new RowData(I18n.I.minAmountOfTransferableUnit(), Renderer.PRICE.render(data.getMinAmountOfTransferableUnit())));
            QuotationUnit quotationUnit = QuotationUnit.fromString(data.getQuotedata().getQuotedPer());
            if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
                list.add(new RowData(I18n.I.quotationLabel(), quotationUnit.getLabel()));
            }
            super.addStaticResult(list);

            final List<RowData> rankingList = new ArrayList<>();
            if (Selector.RATING_FITCH.isAllowed()) {
                addIfDataPresent(rankingList, I18n.I.fitchRating() + " " + I18n.I.longTerm(), data.getRatingFitchLongTerm());
                addIfDataPresent(rankingList, I18n.I.fitchRating() + " " + I18n.I.longTerm() + " " + I18n.I.date() , data.getRatingFitchLongTermDate());
                addIfDataPresent(rankingList, I18n.I.fitchRating() + " " + I18n.I.shortTerm(), data.getRatingFitchShortTerm());
                addIfDataPresent(rankingList, I18n.I.fitchRating() + " " + I18n.I.shortTerm() + " " + I18n.I.date() , data.getRatingFitchShortTermDate());
            }
            if (Selector.RATING_MOODYS.isAllowed()) {
                addIfDataPresent(rankingList, I18n.I.moodysRating() + " " + I18n.I.longTerm(), data.getRatingMoodys());
                addIfDataPresent(rankingList, I18n.I.moodysRating() + " " + I18n.I.longTerm() + " " + I18n.I.date(), data.getRatingMoodysDate());
                addIfDataPresent(rankingList, I18n.I.moodysRating() + " " + I18n.I.longTerm() + " " + I18n.I.type(), data.getRatingMoodysAction());
                addIfDataPresent(rankingList, I18n.I.moodysRating() + " " + I18n.I.shortTerm(), data.getRatingMoodysShort());
                addIfDataPresent(rankingList, I18n.I.moodysRating() + " " + I18n.I.shortTerm() + " " + I18n.I.date(), data.getRatingMoodysShortDate());
                addIfDataPresent(rankingList, I18n.I.moodysRating() + " " + I18n.I.shortTerm() + " " + I18n.I.type(), data.getRatingMoodysShortAction());
            }
            if (Selector.RATING_SuP.isAllowed()) {
                addIfDataPresent(rankingList, I18n.I.ratingSAndPForeign() + " " + I18n.I.longTerm(), data.getRatingSnPLongTerm());
                addIfDataPresent(rankingList, I18n.I.ratingSAndPForeign() + " " + I18n.I.longTerm(), data.getRatingSnPLongTermDate());
                addIfDataPresent(rankingList, I18n.I.ratingSAndPForeign() + " " + I18n.I.shortTerm(), data.getRatingSnPShortTerm());
                addIfDataPresent(rankingList, I18n.I.ratingSAndPForeign() + " " + I18n.I.shortTerm(), data.getRatingSnPShortTermDate());
                addIfDataPresent(rankingList, I18n.I.ratingSAndPLocal() + " " + I18n.I.longTerm(), data.getRatingSnPLocalLongTerm());
                addIfDataPresent(rankingList, I18n.I.ratingSAndPLocal() + " " + I18n.I.longTerm(), data.getRatingSnPLocalLongTermDate());
                addIfDataPresent(rankingList, I18n.I.ratingSAndPLocal() + " " + I18n.I.shortTerm(), data.getRatingSnPLocalShortTerm());
                addIfDataPresent(rankingList, I18n.I.ratingSAndPLocal() + " " + I18n.I.shortTerm(), data.getRatingSnPLocalShortTermDate());
            }

            super.addRankingResult(rankingList);
        }

        private void addIfDataPresent(List<RowData> data, String key, String value) {
            if (value != null) {
                data.add(new RowData(key, value));
            }
        }

        private String getYearsToExpiration(String expirationDate) {
            if (expirationDate == null) {
                return "--";
            }
            final long maturity = Formatter.parseDay(expirationDate).getTime();
            final long now = new Date().getTime();
            final double diffInYears = (maturity - now) / 86400 / 1000 / 365d;
            return Renderer.PRICE_MAX1.render(Double.toString(diffInYears)) + I18n.I.years();
        }

        private Map<String, String> mapCouponFrequency = null;

        private String getCouponFrequency(String couponFrequency) {
            if (couponFrequency == null) {
                return null;
            }
            if (this.mapCouponFrequency == null) {
                this.mapCouponFrequency = new HashMap<>();
                this.mapCouponFrequency.put("1", I18n.I.annual());  // $NON-NLS-0$
                this.mapCouponFrequency.put("2", I18n.I.semiannual());  // $NON-NLS-0$
                this.mapCouponFrequency.put("4", I18n.I.quarterly());  // $NON-NLS-0$
                this.mapCouponFrequency.put("6", I18n.I.bimonthly());  // $NON-NLS-0$
                this.mapCouponFrequency.put("12", I18n.I.monthly());  // $NON-NLS-0$
            }
            final String result = this.mapCouponFrequency.get(couponFrequency);
            return result == null ? couponFrequency : result;
        }
    }

    public static class CertificateView extends StaticDataSnippetView<CERStaticData> {
        public CertificateView(StaticDataSnippet snippet) {
            super(snippet, false);
        }

        @Override
        public void update(CERStaticData data) {
            final List<RowData> list = new ArrayList<>();

            super.addDefaultData(list, data.getInstrumentdata(), null, data.getTypename(), data.getQuotedata());
            list.add(new RowData(I18n.I.country(), data.getCountry()));
            list.add(new RowData(I18n.I.subscriptionRatio(), data.getSubscriptionratioText()));
            list.add(new RowData(I18n.I.maturity2(), Formatter.LF.formatDate(data.getMaturity())));
            super.addEdg(list);
            super.addStaticResult(list);
        }
    }

    public static class FundView extends StaticDataSnippetView<FNDStaticData> {

        public FundView(StaticDataSnippet snippet) {
            super(snippet, false);
        }

        @Override
        public void update(FNDStaticData data) {
            final List<RowData> list = new ArrayList<>();

            String typeName = data.getTypename();
            String gd873f = null;

            if (this.snippet.getBlockWmStaticData() != null) {
                final Map<String, String> fndModel = PortraitFNDController.buildFndModel(this.snippet.getBlockWmStaticData());
                gd873f = fndModel.get(PortraitFNDController.GD873F);
                typeName += PortraitFNDController.isSingleHedgeFond(fndModel) ? " (Single-Hedgefonds)" : ""; // $NON-NLS$
            }

            super.addDefaultData(list, data.getInstrumentdata(), null, typeName, data.getQuotedata());
            if (!Selector.isFunddataAllowed()) {
                return;
            }
            list.add(new RowData(I18n.I.country(), data.getCountry()));
            list.add(new RowData(I18n.I.marketAdmission(), data.getMarketAdmission()));
            list.add(new RowData(I18n.I.category(), data.getFundtype()));
            if (Selector.DZ_BANK_USER.isAllowed() && FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
                list.add(new RowData(I18n.I.ogawAif(), gd873f));
            }
            if (this.snippet.isAllData()) {
                list.add(new RowData(I18n.I.fundCurrency(), data.getCurrency()));
                final String benchmarkName;
                if (data.getBenchmark() != null) {
                    benchmarkName = data.getBenchmark().getInstrumentdata().getName();
                }
                else {
                    benchmarkName = data.getBenchmarkName();
                }
                list.add(new RowData(I18n.I.vwdBenchmark(), benchmarkName));
                list.add(new RowData(I18n.I.investmentFocus(), data.getInvestmentFocus()));
                list.add(new RowData(I18n.I.issueDate1(), Formatter.LF.formatDate(data.getIssueDate())));
                list.add(new RowData(I18n.I.fundVolume(),
                        Renderer.VOLUME.render(data.getFundVolume()) + (data.getFundVolumeCurrency() != null ? " " + data.getFundVolumeCurrency() : "")));
                String classVol = data.getFundclassVolume();
                if (classVol != null && classVol.trim().length() > 0) {
                    list.add(new RowData(I18n.I.fundClassVolume(),
                            Renderer.VOLUME.render(data.getFundclassVolume()) + " " + data.getFundclassVolumeCurrency()));
                }
                list.add(new RowData(I18n.I.fundManager(), data.getFundManager()));
                list.add(new RowData(
                        (I18n.I.assetManagementCompany()), data.getIssuerName()));
                final String issuerAddress =
                        (data.getIssuerStreet() == null ? "" : data.getIssuerStreet()) // $NON-NLS-0$
                                + "<br/>" // $NON-NLS-0$
                                + (data.getIssuerPostalcode() == null ? "" : data.getIssuerPostalcode()) // $NON-NLS-0$
                                + " " // $NON-NLS-0$
                                + (data.getIssuerCity() == null ? "" : data.getIssuerCity()); // $NON-NLS-0$
                list.add(new RowData(I18n.I.address(), issuerAddress));
                final String issuerEmail = data.getIssuerEmail();
                list.add(new RowData(I18n.I.email(), issuerEmail == null ? "--" : ("<a href=\"mailto:" + issuerEmail + "\">" + issuerEmail + "</a>")));  // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
                final String url = data.getIssuerUrl();
                if (url != null) {
                    if (url.startsWith("http")) { // $NON-NLS-0$
                        list.add(new RowData(I18n.I.url(), new Link(url, "_blank", null, url)));  // $NON-NLS-0$
                    }
                    else {
                        list.add(new RowData(I18n.I.url(), new Link("http://" + url, "_blank", null, url)));  // $NON-NLS-0$ $NON-NLS-1$
                    }
                }
                if (Selector.RATING_FERI.isAllowed()) {
                    final Link link = new Link(Settings.INSTANCE.feriDescriptionUrl(), "_blank", null, I18n.I.ratingFeri());  // $NON-NLS-0$
                    list.add(new RowData(link, data.getFeriRating()));
                }
                if (Selector.RATING_MORNINGSTAR.isAllowed() || Selector.RATING_MORNINGSTAR_UNION_FND.isAllowed()) {
                    final Link link = new Link(Settings.INSTANCE.morningstarDescriptionUrl(), "_blank", null, I18n.I.ratingMorningstar());  // $NON-NLS-0$
                    final String rating = data.getMorningstarRating();
                    final String img = rating == null ? "n/a" : "<img src=\"" + Settings.INSTANCE.morningstarStarUrl().replace("{rating}", rating) + "\"/>"; // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
                    list.add(new RowData(link, img));
                }
            }
            super.addStaticResult(list);
        }
    }

    private static final String FLIP_ID_STATIC = "static"; // $NON-NLS-0$

    private static final String FLIP_ID_RATING = "rating"; // $NON-NLS-0$

    private SnippetTableWidget twStatic;

    private SnippetTableWidget twRating;

    private TableColumnModel columnModelStatic;

    private TableColumnModel columnModelRating;

    private T staticData;

    private EDGRatingData edgRatingData;

    private Map<String, String> fndModel;

    private final boolean withRatingTable;

    protected StaticDataSnippetView(StaticDataSnippet snippet, boolean withRating) {
        super(snippet);
        setTitle(I18n.I.staticData());

        this.withRatingTable = withRating;
        final boolean isValueRight = SessionData.INSTANCE.isAnonymous() && this.snippet.isShowUnderlying();
        this.columnModelStatic = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.type(), 0.3f, TableCellRenderers.DEFAULT_LABEL),
                new TableColumn(I18n.I.value(), 0.7f, isValueRight ? TableCellRenderers.STRING_RIGHT : TableCellRenderers.DEFAULT_RIGHT)
        });
        this.columnModelRating = this.withRatingTable ? new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.type(), 0.6f, TableCellRenderers.DEFAULT_LABEL),
                new TableColumn(I18n.I.value(), 0.4f, isValueRight ? TableCellRenderers.STRING_RIGHT : TableCellRenderers.DEFAULT_RIGHT)
        }) : null;
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();

        final FlowPanel panel = new FlowPanel();

        this.twStatic = SnippetTableWidget.create(this.columnModelStatic);
        panel.add(this.twStatic);
        if (this.withRatingTable) {
            this.twRating = SnippetTableWidget.create(this.columnModelRating);
            panel.add(this.twRating);
        }

        this.container.setContentWidget(panel);
    }

    public abstract void update(T staticData);

    private void addDefaultData(List<RowData> list, InstrumentData instrumentdata,
                                String tickersymbol, String typename, QuoteData quotedata) {
        if (this.snippet.isWithQuoteLink()) {
            list.add(new RowData(I18n.I.name(), (SessionData.INSTANCE.isAnonymous() && this.snippet.isShowUnderlying()) ?
                    instrumentdata.getName() : new QuoteWithInstrument(instrumentdata, quotedata)));
        }
        if (SessionData.INSTANCE.isShowIsin()) {
            add(list, "ISIN", instrumentdata.getIsin()); // $NON-NLS-0$
        }
        if (SessionData.INSTANCE.isShowWkn()) {
            add(list, "WKN", instrumentdata.getWkn()); // $NON-NLS-0$
        }
        add(list, I18n.I.ticker(), tickersymbol);
        add(list, I18n.I.instrumentTypeAbbr(), typename);
    }

    private void add(List<RowData> list, String title, String value) {
        if (value != null) {
            list.add(new RowData(title, value));
        }
    }

    private void addEdg(List<RowData> list) {
        if (this.snippet.isEdgAllowedAndAvailable()) {
            EDGData edgData = this.snippet.getBlockEdg().getResult();
            list.add(new RowData(EdgUtil.getEdgTopClassLink(edgData),
                    EdgUtil.getEdgTopClassRating(edgData)));
        }
    }

    private void addRankingResult(List<RowData> rankingData) {
        if (rankingData == null) {
            this.twRating.updateData(DefaultTableDataModel.NULL);
        }
        else {
            final DefaultTableDataModel dtdm = DefaultTableDataModel.createWithRowData(rankingData);
            dtdm.setMessage(I18n.I.noRankingDataAvailable());
            this.twRating.updateData(dtdm);
        }
    }

    private void addStaticResult(List<RowData> staticData) {

        if (staticData == null) {
            this.twStatic.updateData(DefaultTableDataModel.NULL);
        }
        else {
            final List<StaticDataTableExtension.CellMetaDataEntry> metaDataEntries = new ArrayList<>();

            // add data from extensions
            for (StaticDataTableExtension extension : this.snippet.getExtensions()) {
                extension.addData(staticData, metaDataEntries, 2);
            }

            this.twStatic.updateData(DefaultTableDataModel.createWithRowData(staticData));
        }

    }
}
