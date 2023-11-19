/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.domainimpl.data.MasterDataCertificateImpl;
import de.marketmaker.istar.merger.provider.LocalDateCache;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CertificateMasterDataVwdReader extends AbstractSaxReader {

    private final static DateTimeFormatter DTF_DATETIME = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateCache localDateCache = new LocalDateCache();

    private long instrumentid = -1;

    private String typeKeyVwd;

    private LocalizedString.Builder typeVwd = new LocalizedString.Builder();

    private String subtypeVwd;

    private String subtypeKeyVwd;

    private String typeKeyDZ;

    private String typeDZ;

    private String subtypeDZ;

    private String typeKeyWGZ;

    private String typeWGZ;

    private String subtypeWGZ;

    private BigDecimal cap;

    private BigDecimal knockin;

    private BigDecimal bonuslevel;

    private BigDecimal barrier;

    private BigDecimal startvalue;

    private BigDecimal stopvalue;

    private BigDecimal charge;

    private BigDecimal coupon;

    private String type;

    private String typeKey;

    private LocalDate firsttradingday;

    private LocalDate lasttradingday;

    private LocalDate lasttradingdayEuwax;

    private LocalDate deadlineDate1;

    private LocalDate deadlineDate2;

    private LocalDate deadlineDate3;

    private LocalDate deadlineDate4;

    private BigDecimal guaranteelevel;

    private String exercisetypeName;

    private String interestPaymentInterval;

    private LocalDate issuedate;

    private LocalDate paymentDate;

    private String issuername;

    private String issuerCountryCode;

    private LocalDate knockindate;

    private Boolean knockout;

    private DateTime knockoutdate;

    private DateTime upperbarrierdate;

    private DateTime lowerbarrierdate;

    private BigDecimal nominal;

    private BigDecimal protectlevel;

    private BigDecimal quantity;

    private Boolean quanto;

    private String range;

    private String rollover;

    private String guaranteetype;

    private String leveragetype;

    private BigDecimal stoploss;

    private BigDecimal strikeprice;

    private BigDecimal issueprice;

    private BigDecimal variabletradingamount;

    private String pricetype;

    private String annotation;

    private String productNameIssuer;

    private String category;

    private LocalizedString.Builder localizedCategory = new LocalizedString.Builder();

    private BigDecimal maxSpreadEuwax;

    private BigDecimal refundMaximum;

    private BigDecimal refund;

    private BigDecimal floor;

    private BigDecimal participationFactor;

    private BigDecimal participationLevel;

    private BigDecimal subscriptionRatio;

    private String multiassetName;

    private String currencyStrike;

    private List<Long> basketIids;

    private LocalDate settlementday;


    private InstrumentBasedUpdatable<MasterDataCertificate> provider;

    public CertificateMasterDataVwdReader(
            InstrumentBasedUpdatable<MasterDataCertificate> provider) {
        this.provider = provider;
    }

    public void endElement(String uri, String localName, String tagName) throws SAXException {
        try {
            if (tagName.equals("ROW")) {
                // i have finished a new row => process
                storeFields();
            }
            else if (tagName.equals("IID")) {
                this.instrumentid = getCurrentLong();
            }
            else if (tagName.equals("TYPEKEYVWD")) {
                this.typeKeyVwd = getCurrentString();
            }
            else if (tagName.equals("TYPEVWD__DE")) {
                this.typeVwd.add(getCurrentString(), Language.de);
            }
            else if (tagName.equals("TYPEVWD__EN")) {
                this.typeVwd.add(getCurrentString(), Language.en);
            }
            else if (tagName.equals("SUBTYPEVWD__DE")) {
                this.subtypeVwd = getCurrentString();
            }
            else if (tagName.equals("SUBTYPEKEYVWD")) {
                this.subtypeKeyVwd = getCurrentString();
            }
            else if (tagName.equals("TYPEKEYDZ")) {
                this.typeKeyDZ = getCurrentString();
            }
            else if (tagName.equals("TYPEDZ")) {
                this.typeDZ = getCurrentString();
            }
            else if (tagName.equals("SUBTYPEDZ")) {
                this.subtypeDZ = getCurrentString();
            }
            else if (tagName.equals("TYPEKEYWGZ")) {
                this.typeKeyWGZ = getCurrentString();
            }
            else if (tagName.equals("TYPEWGZ")) {
                this.typeWGZ = getCurrentString();
            }
            else if (tagName.equals("SUBTYPEWGZ")) {
                this.subtypeWGZ = getCurrentString();
            }
            else if (tagName.equals("CAP")) {
                this.cap = getCurrentBigDecimal();
            }
            else if (tagName.equals("KNOCKIN")) {
                this.knockin = getCurrentBigDecimal();
            }
            else if (tagName.equals("BONUSLEVEL")) {
                this.bonuslevel = getCurrentBigDecimal();
            }
            else if (tagName.equals("BARRIER")) {
                this.barrier = getCurrentBigDecimal();
            }
            else if (tagName.equals("STARTVALUE")) {
                this.startvalue = getCurrentBigDecimal();
            }
            else if (tagName.equals("STOPVALUE")) {
                this.stopvalue = getCurrentBigDecimal();
            }
            else if (tagName.equals("CHARGE")) {
                this.charge = getCurrentBigDecimal();
            }
            else if (tagName.equals("COUPON")) {
                this.coupon = getCurrentBigDecimal();
            }
            else if (tagName.equals("TYPE")) {
                this.type = getCurrentString();
            }
            else if (tagName.equals("TYPEKEY")) {
                this.typeKey = getCurrentString();
            }
            else if (tagName.equals("FIRSTTRADINGDAY")) {
                this.firsttradingday = getDate();
            }
            else if (tagName.equals("LASTTRADINGDAY")) {
                this.lasttradingday = getDate();
            }
            else if (tagName.equals("LASTTRADINGDAYEUWAX")) {
                this.lasttradingdayEuwax = getDate();
            }
            else if (tagName.equals("GUARANTEELEVEL")) {
                this.guaranteelevel = getCurrentBigDecimal();
            }
            else if (tagName.equals("EXERCISETYPENAME")) {
                this.exercisetypeName = getCurrentString();
                if (this.exercisetypeName.startsWith("europ")) {
                    this.exercisetypeName = "europäisch";
                }
            }
            else if (tagName.equals("INTERESTPAYMENTINTERVAL")) {
                this.interestPaymentInterval = getCurrentString();
            }
            else if (tagName.equals("ISSUEDATE")) {
                this.issuedate = getDate();
            }
            else if (tagName.equals("ISSUERNAME")) {
                this.issuername = getCurrentString();
            }
            else if (tagName.equals("KNOCKINDATE")) {
                this.knockindate = getDate();
            }
            else if (tagName.equals("KNOCKOUT")) {
                this.knockout = "ja".equals(getCurrentString());
            }
            else if (tagName.equals("KNOCKOUTDATE")) {
                this.knockoutdate = DTF_DATETIME.parseDateTime(getCurrentString(false));
            }
            else if (tagName.equals("UPPERBARRIERDATE")) {
                this.upperbarrierdate = DTF_DATETIME.parseDateTime(getCurrentString(false));
            }
            else if (tagName.equals("LOWERBARRIERDATE")) {
                this.lowerbarrierdate = DTF_DATETIME.parseDateTime(getCurrentString(false));
            }
            else if (tagName.equals("NOMINAL")) {
                this.nominal = getCurrentBigDecimal();
            }
            else if (tagName.equals("PROTECTLEVEL")) {
                this.protectlevel = getCurrentBigDecimal();
            }
            else if (tagName.equals("QUANTITY")) {
                this.quantity = getCurrentBigDecimal();
            }
            else if (tagName.equals("QUANTO")) {
                this.quanto = "ja".equals(getCurrentString());
            }
            else if (tagName.equals("RANGE")) {
                this.range = getCurrentString();
            }
            else if (tagName.equals("ROLLOVER")) {
                this.rollover = getCurrentString();
            }
            else if (tagName.equals("GUARANTEETYPE")) {
                this.guaranteetype = getCurrentString();
            }
            else if (tagName.equals("LEVERAGETYPE")) {
                this.leveragetype = getCurrentString();
            }
            else if (tagName.equals("STOPLOSS")) {
                this.stoploss = getCurrentBigDecimal();
            }
            else if (tagName.equals("STRIKEPRICE")) {
                this.strikeprice = getCurrentBigDecimal();
            }
            else if (tagName.equals("VARIABLETRADINGAMOUNT")) {
                this.variabletradingamount = getCurrentBigDecimal();
            }
            else if (tagName.equals("ISSUEPRICE")) {
                this.issueprice = getCurrentBigDecimal();
            }
            else if (tagName.equals("ANNOTATION")) {
                this.annotation = getCurrentString();
            }
            else if (tagName.equals("PRODUCTNAMEISSUER")) {
                this.productNameIssuer = getCurrentString();
            }
            else if (tagName.equals("CATEGORY")) {
                this.category = getCurrentString();
                this.localizedCategory.add(this.category, Language.de);
            }
            else if (tagName.equals("CATEGORY_EN")) {
                this.localizedCategory.add(this.category, Language.en);
            }
            else if (tagName.equals("INTERESTPAYMENTDATE")) {
                this.paymentDate = getDate();
            }
            else if (tagName.equals("MAXSPREADEUWAX")) {
                this.maxSpreadEuwax = getCurrentBigDecimal();
            }
            else if (tagName.equals("REFUNDMAXIMUM")) {
                this.refundMaximum = getCurrentBigDecimal();
            }
            else if (tagName.equals("REFUND")) {
                this.refund = getCurrentBigDecimal();
            }
            else if (tagName.equals("DEADLINEDATE1")) {
                this.deadlineDate1 = getDate();
            }
            else if (tagName.equals("DEADLINEDATE2")) {
                this.deadlineDate2 = getDate();
            }
            else if (tagName.equals("DEADLINEDATE3")) {
                this.deadlineDate3 = getDate();
            }
            else if (tagName.equals("DEADLINEDATE4")) {
                this.deadlineDate4 = getDate();
            }
            else if (tagName.equals("FLOOR")) {
                this.floor = getCurrentBigDecimal();
            }
            else if (tagName.equals("PARTICIPATIONFACTOR")) {
                this.participationFactor = getCurrentBigDecimal();
            }
            else if (tagName.equals("PARTICIPATIONLEVEL")) {
                this.participationLevel = getCurrentBigDecimal();
            }
            else if (tagName.equals("SUBSCRIPTIONRATIO")) {
                this.subscriptionRatio = getCurrentBigDecimal();
            }
            else if (tagName.equals("MULTIASSETNAME")) {
                this.multiassetName = getCurrentString();
            }
            else if (tagName.equals("CURRENCYSTRIKE")) {
                this.currencyStrike = getCurrentString();
            }
            else if (tagName.equals("BASKETIIDS")) {
                final String str = getCurrentString(false);
                final String[] tokens = str.split("\\|");
                this.basketIids = new ArrayList<>(tokens.length);
                for (final String token : tokens) {
                    this.basketIids.add(Long.parseLong(token));
                }
            }
            else if (tagName.equals("SETTLEMENTDAY")) {
                this.settlementday = getDate();
            }
            else {
                notParsed(tagName);
            }
        } catch (Exception e) {
            this.logger.error("<endElement> error in " + tagName, e);
            this.errorOccured = true;
        }
    }

    protected BigDecimal getCurrentBigDecimal() {
        return getCurrentBigDecimal(true);
    }

    private LocalDate getDate() {
        final String s = getCurrentString(false);
        return this.localDateCache.getDate(s);
    }

    private void storeFields() {
        this.limiter.ackAction();

        if (this.errorOccured) {
            reset();
            return;
        }

        if (this.instrumentid < 0) {
            reset();
            return;
        }

        final MasterDataCertificate data = new MasterDataCertificateImpl(this.instrumentid,
                this.typeKeyVwd, this.typeVwd.build(), this.subtypeKeyVwd, this.subtypeVwd,
                this.typeKeyDZ, this.typeDZ,
                // todo: null as quick hack to compile - class not used anymore !?
                null,
                this.subtypeDZ,
                this.typeKeyWGZ, this.typeWGZ, this.subtypeWGZ,
                cap, knockin, bonuslevel, barrier, startvalue, stopvalue, charge, coupon, type, typeKey, firsttradingday, lasttradingday, lasttradingdayEuwax,
                guaranteelevel, exercisetypeName, interestPaymentInterval, issuedate, issuername, issuerCountryCode, knockindate,
                knockout, knockoutdate, lowerbarrierdate, upperbarrierdate, nominal, protectlevel, quantity, quanto, range, rollover, guaranteetype,
                leveragetype, stoploss, strikeprice, issueprice, variabletradingamount, pricetype, annotation,
                productNameIssuer, category, this.localizedCategory.build(), paymentDate, maxSpreadEuwax, refundMaximum, refund,
                deadlineDate1, deadlineDate2, deadlineDate3, deadlineDate4, floor, participationFactor,
                participationLevel, subscriptionRatio, multiassetName, currencyStrike, basketIids, settlementday);

        this.provider.addOrReplace(this.instrumentid, data);

        reset();
    }

    protected void reset() {
        this.instrumentid = -1;
        this.typeKeyVwd = null;
        this.typeVwd.reset();
        this.subtypeKeyVwd = null;
        this.subtypeVwd = null;
        this.typeKeyDZ = null;
        this.typeDZ = null;
        this.subtypeDZ = null;
        this.typeKeyWGZ = null;
        this.typeWGZ = null;
        this.subtypeWGZ = null;
        this.type = null;
        this.typeKey = null;
        this.issueprice = null;
        this.issuername = null;
        this.issuerCountryCode = null;
        this.strikeprice = null;
        this.pricetype = null;
        this.coupon = null;
        this.cap = null;
        this.knockin = null;
        this.bonuslevel = null;
        this.barrier = null;
        this.guaranteetype = null;
        this.leveragetype = null;
        this.charge = null;
        this.firsttradingday = null;
        this.lasttradingday = null;
        this.lasttradingdayEuwax = null;
        this.guaranteelevel = null;
        this.exercisetypeName = null;
        this.interestPaymentInterval = null;
        this.issuedate = null;
        this.knockindate = null;
        this.knockout = null;
        this.knockoutdate = null;
        this.upperbarrierdate = null;
        this.lowerbarrierdate = null;
        this.nominal = null;
        this.protectlevel = null;
        this.quantity = null;
        this.quanto = null;
        this.range = null;
        this.rollover = null;
        this.stoploss = null;
        this.strikeprice = null;
        this.variabletradingamount = null;
        this.annotation = null;
        this.productNameIssuer = null;
        this.category = null;
        this.localizedCategory.reset();
        this.paymentDate = null;
        this.maxSpreadEuwax = null;
        this.refundMaximum = null;
        this.refund = null;
        this.startvalue = null;
        this.stopvalue = null;
        this.deadlineDate1 = null;
        this.deadlineDate2 = null;
        this.deadlineDate3 = null;
        this.deadlineDate4 = null;
        this.floor = null;
        this.participationFactor = null;
        this.participationLevel = null;
        this.subscriptionRatio = null;
        this.multiassetName = null;
        this.currencyStrike = null;
        this.basketIids = null;
        this.settlementday = null;
        this.errorOccured = false;
    }

}
