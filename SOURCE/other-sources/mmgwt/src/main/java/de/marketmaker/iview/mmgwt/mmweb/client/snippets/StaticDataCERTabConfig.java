package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DecimalCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.QwiCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.StringCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndDataConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.EdgUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

/**
 * StaticDataCERTabConfig.java
 * Created on Nov 6, 2008 3:40:53 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author mloesch
 */
public class StaticDataCERTabConfig implements TableColumnAndDataConfig<StaticDataCER> {

    private abstract class Cert implements TableColumnAndData<StaticDataCER> {
        private final TableColumnModel tabModel;

        protected RowData isin;

        protected RowData wkn;

        protected RowData productname;

        protected RowData category;

        protected RowData subtype;

        protected RowData issuername;

        protected RowData currency;

        protected RowData issueprice;

        protected RowData paymentdate;

        protected RowData maturity;

        protected RowData maxspread;

        protected RowData charge;

        protected RowData coupon;

        protected RowData issuedate;

        protected RowData edg;

        protected RowData unit;

        protected CellData isinCell;

        protected CellData wknCell;

        protected CellData productnameCell;

        protected CellData categoryCell;

        protected CellData subtypeCell;

        protected CellData issuernameCell;

        protected CellData currencyCell;

        protected CellData issuepriceCell;

        protected CellData paymentdateCell;

        protected CellData maturityCell;

        protected CellData maxspreadCell;

        protected CellData chargeCell;

        protected CellData couponCell;

        protected CellData issuedateCell;

        protected CellData edgCell;

        protected CellData unitCell;

        public Cert() {
            this.tabModel = new DefaultTableColumnModel(new TableColumn[]{
                    new TableColumn(I18n.I.type(), 0.3f, TableCellRenderers.DEFAULT_LABEL),
                    new TableColumn(I18n.I.value(), 0.7f, TableCellRenderers.DEFAULT_RIGHT)
            }, false);
        }

        public TableColumnModel getTableColumnModel() {
            return this.tabModel;
        }

        public List<RowData> getRowData(StaticDataCER sdc) {
            final List<RowData> list = getCompleteList(sdc.getData(), sdc.getEdg());
            onCompleteList(list);
            return list;
        }

        protected void onCompleteList(List<RowData> list) {
            // empty
        }

        protected List<RowData> getCompleteList(CERDetailedStaticData data, EDGData edg) {
            setData(data, edg);

            List<RowData> list = new ArrayList<RowData>();
            if (SessionData.INSTANCE.isShowIsin()) {
                list.add(this.isin);
            }
            if (SessionData.INSTANCE.isShowWkn()) {
                list.add(this.wkn);
            }
            list.add(this.category);
            list.add(this.subtype);
            list.add(this.productname);
            list.add(this.issuername);
            list.add(this.currency);
            list.add(this.issuedate);
            list.add(this.issueprice);
            list.add(this.maturity);
            list.add(this.paymentdate);
            list.add(this.maxspread);
            list.add(this.charge);
            list.add(this.coupon);
            if (edgAllowedAndAvailable(edg)) {
                this.edg = new RowData(EdgUtil.getEdgTopClassLink(edg), EdgUtil.getEdgTopClassRating(edg));
                list.add(this.edg);
            }
            if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
                list.add(this.unit);
            }
            return list;
        }

        private void setData(CERDetailedStaticData data, EDGData edg) {
            setCellData(data, edg);

            this.isin = new RowData("ISIN", this.isinCell); // $NON-NLS-0$
            this.wkn = new RowData("WKN", this.wknCell); // $NON-NLS-0$
            this.category = new RowData(I18n.I.category(), this.categoryCell);
            this.subtype = new RowData(I18n.I.certificateTypeWithSpace(), this.subtypeCell);
            this.productname = new RowData(I18n.I.productName(), this.productnameCell);
            this.issuername = new RowData(I18n.I.issuer(), this.issuernameCell);
            this.currency = new RowData(I18n.I.currency(), this.currencyCell);
            this.issuedate = new RowData(I18n.I.issueDate4(), this.issuedateCell);
            this.issueprice = new RowData(I18n.I.issuePrice3(), this.issuepriceCell);
            this.maturity = new RowData(I18n.I.maturity(), this.maturityCell);
            this.paymentdate = new RowData(I18n.I.paymentDate(), this.paymentdateCell);
            this.maxspread = new RowData(I18n.I.maxSpreadAbbr(), this.maxspreadCell);
            this.charge = new RowData(I18n.I.certificateCharge(), this.chargeCell);
            this.coupon = new RowData(I18n.I.coupon(), this.couponCell);
            this.unit = new RowData(I18n.I.quotationLabel(), this.unitCell);
            if (edgAllowedAndAvailable(edg)) {
                this.edg = new RowData(EdgUtil.getEdgTopClassLink(edg), this.edgCell);
            }

        }

        private void setCellData(CERDetailedStaticData data, EDGData edg) {
            this.isinCell = new QwiCellData(new QuoteWithInstrument(data.getInstrumentdata(), data.getQuotedata(), data.getInstrumentdata().getIsin()));
            this.wknCell = new StringCellData(data.getInstrumentdata().getWkn());
            this.productnameCell = new StringCellData(data.getProductNameIssuer());
            this.categoryCell = new StringCellData(data.getCategory());
            this.subtypeCell = new StringCellData(data.getSubtype());
            this.issuernameCell = new StringCellData(data.getIssuerName());
            this.currencyCell = new StringCellData(data.getQuotedata().getCurrencyIso());
            this.issuepriceCell = new DecimalCellData(Renderer.PRICE, data.getIssuePrice(), data.getQuotedata().getCurrencyIso(), CellData.Sorting.NONE);
            this.paymentdateCell = new StringCellData(Formatter.LF.formatDate(data.getPaymentDate()));
            this.maturityCell = new StringCellData(Formatter.LF.formatDate(data.getMaturity()));
            this.maxspreadCell = new DecimalCellData(Renderer.VOLUME, data.getMaxSpreadEuwax(), CellData.Sorting.NONE);
            this.chargeCell = new StringCellData(data.getCharge(), "--");
            this.couponCell = new StringCellData(data.getCoupon(), "--");
            this.issuedateCell = new StringCellData(Formatter.LF.formatDate(data.getIssueDate()));
            QuotationUnit quotationUnit = QuotationUnit.fromString(data.getQuotedata().getQuotedPer());
            this.unitCell = new StringCellData(quotationUnit.getLabel());
            if (edgAllowedAndAvailable(edg)) {
                this.edgCell = new StringCellData(EdgUtil.getEdgTopClassRating(edg));
            }
        }
    }

    private class CertExpress extends Cert {
        @Override
        protected void onCompleteList(List<RowData> list) {
            list.remove(this.coupon);
        }
    }

    private class CertReverseConvertible extends Cert {
        @Override
        protected void onCompleteList(List<RowData> list) {
            list.remove(this.maxspread);
            list.remove(this.coupon);
        }
    }

    private class CertDefault extends Cert {
        @Override
        protected void onCompleteList(List<RowData> list) {
            list.remove(this.coupon);
        }
    }

    public static final TableColumnAndDataConfig<StaticDataCER> INSTANCE = new StaticDataCERTabConfig();

    private final Map<String, TableColumnAndData<StaticDataCER>> configs;

    public TableColumnAndData<StaticDataCER> getTableColumnAndData(String type) {
        if (this.configs.containsKey(type)) {
            return this.configs.get(type);
        }
        if (type != null) {
            Firebug.log("StaticDataCERTabConfig#getTableColumnAndData: unknown: " + type); // $NON-NLS-0$
        }
        return this.configs.get(CertificateTypeEnum.CERT_OTHER.toString());
    }

    private StaticDataCERTabConfig() {
        this.configs = new HashMap<String, TableColumnAndData<StaticDataCER>>();
        final Cert diverse = new CertDefault();
        this.configs.put(CertificateTypeEnum.CERT_EXPRESS.toString(), new CertExpress());
        this.configs.put(CertificateTypeEnum.CERT_DISCOUNT.toString(), diverse);
        this.configs.put(CertificateTypeEnum.CERT_GUARANTEE.toString(), diverse);
        this.configs.put(CertificateTypeEnum.CERT_BONUS.toString(), diverse);
        this.configs.put(CertificateTypeEnum.CERT_INDEX.toString(), diverse);
        this.configs.put(CertificateTypeEnum.CERT_OUTPERFORMANCE.toString(), diverse);
        this.configs.put(CertificateTypeEnum.CERT_OTHER.toString(), diverse);
        this.configs.put(CertificateTypeEnum.CERT_BASKET.toString(), diverse);
        this.configs.put(CertificateTypeEnum.CERT_SPRINTER.toString(), diverse);
        this.configs.put(CertificateTypeEnum.KNOCK.toString(), diverse);
        final CertReverseConvertible crc = new CertReverseConvertible();
        this.configs.put(CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE.toString(), crc);
        this.configs.put(CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE_COM.toString(), crc);
        this.configs.put(CertificateTypeEnum.CERT_MBI.toString(), diverse);
        this.configs.put(CertificateTypeEnum.CERT_FACTOR.toString(), diverse);
    }

    private boolean edgAllowedAndAvailable(EDGData edg) {
        return (edg != null) && Selector.EDG_RATING.isAllowed() && (edg.getRating().getEdgTopScore() != null);
    }
}
