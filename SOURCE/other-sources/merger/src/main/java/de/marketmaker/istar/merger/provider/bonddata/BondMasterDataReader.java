package de.marketmaker.istar.merger.provider.bonddata;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataBond;
import de.marketmaker.istar.domainimpl.data.MasterDataBondImpl;

class BondMasterDataReader extends AbstractSaxReader {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private static final Map<String, Integer> COUPON_COUNT_PA = new HashMap<>();

    static {
        COUPON_COUNT_PA.put("Keine Zinstermine", 0);
        COUPON_COUNT_PA.put("Ganzjährig", 1);
        COUPON_COUNT_PA.put("Halbjährlich", 2);
        COUPON_COUNT_PA.put("Viermonatlich", 3);
        COUPON_COUNT_PA.put("Vierteljährlich", 4);
        COUPON_COUNT_PA.put("Zweimonatlich", 6);
        COUPON_COUNT_PA.put("Monatlich", 12);
        COUPON_COUNT_PA.put("Neunmonatlich", null);
        COUPON_COUNT_PA.put("Elfmonatlich", null);
        COUPON_COUNT_PA.put("Zweijährlich", null);
        COUPON_COUNT_PA.put("Fünfjährlich", null);
        COUPON_COUNT_PA.put("Zinszhlg. am Ende", null);
        COUPON_COUNT_PA.put("Unregelmäßig", null);
        COUPON_COUNT_PA.put("Siebenmonatlich", null);
        COUPON_COUNT_PA.put("Achtmonatlich", null);
    }

    final Map<Long, MasterDataBond> values = new HashMap<>();

    private final Set<String> unknownPeriods = new HashSet<>();

    private long instrumentid = -1;

    private LocalizedString.Builder bondtype = new LocalizedString.Builder();

    private LocalizedString.Builder coupontype = new LocalizedString.Builder();

    private DateTime issuedate;

    private BigDecimal nominalInterest;

    private BigDecimal facevalue;

    private BigDecimal redemptionPrice;

    private BigDecimal issuevolume;

    private Integer numberOfCoupons;

    private String issuername;

    private String countryOfIssuer;

    private String countryOfIssuerCode;

    private LocalizedString.Builder interestperiod = new LocalizedString.Builder();

    private String couponDateDayNumber;

    private String couponDateMonthNumber;

    private BigDecimal issuePrice;

    private String issueCurrency;

    private String interestType;

    private String interestRunDeviation;

    private String interestCalculationMethod;

    private DateTime expirationDate;

    private String couponFrequency;

    private Integer couponDateDays;

    private DateTime firstCouponDate;

    private DateTime lastCouponDate;

    private DateTime couponDate;

    private BigDecimal smallestTransferableUnit;

    private BigDecimal minAmountOfTransferableUnit;

    private BigDecimal conversionFactor;

    private String bondRank;


    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        if (!this.unknownPeriods.isEmpty()) {
            this.logger.warn("<endDocument> unknown interest periods = " + this.unknownPeriods);
        }
    }

    public void endElement(String uri, String localName, String tagName) throws SAXException {
        try {
            if (tagName.equals("ROW")) {
                // i have finished a new row => create
                storeFields();
            }
            else if (tagName.equals("IID")) {
                this.instrumentid = getCurrentLong();
            }
            else if (tagName.equals("BONDTYPE")) {
                this.bondtype.add(getCurrentString(), Language.de);
            }
            else if (tagName.equals("BONDTYPE_EN")) {
                this.bondtype.add(getCurrentString(), Language.en);
            }
            else if (tagName.equals("COUPONTYPE")) {
                this.coupontype.add(getCurrentString(), Language.de);
            }
            else if (tagName.equals("COUPONTYPE_EN")) {
                this.coupontype.add(getCurrentString(), Language.en);
            }
            else if (tagName.equals("ISSUEDATE")) {
                this.issuedate = getDate();
            }
            else if (tagName.equals("NOMINALINTEREST")) {
                this.nominalInterest = getCurrentBigDecimal();
            }
            else if (tagName.equals("FACEVALUE")) {
                this.facevalue = getCurrentBigDecimal();
            }
            else if (tagName.equals("REDEMPTIONPRICE")) {
                this.redemptionPrice = getCurrentBigDecimal();
            }
            else if (tagName.equals("ISSUEVOLUME")) {
                this.issuevolume = getCurrentBigDecimal();
            }
            else if (tagName.equals("INTERESTPERIOD")) {
                this.interestperiod.add(getCurrentString(), Language.de);
                this.numberOfCoupons = getCount(getCurrentString());
            }
            else if (tagName.equals("INTERESTPERIOD_EN")) {
                this.interestperiod.add(getCurrentString(), Language.en);
            }
            else if (tagName.equals("ISSUERNAME")) {
                this.issuername = getCurrentString();
            }
            else if (tagName.equals("COUNTRYOFISSUER")) {
                this.countryOfIssuer = getCurrentString();
            }
            else if (tagName.equals("COUNTRYOFISSUERCODE")) {
                this.countryOfIssuerCode = getCurrentString();
            }
            else if (tagName.equals("COUPONDATEDAYNUMBER")) {
                this.couponDateDayNumber = getCurrentString();
            }
            else if (tagName.equals("COUPONDATEMONTHNUMBER")) {
                this.couponDateMonthNumber = getCurrentString();
            }
            else if (tagName.equals("ISSUEPRICE")) {
                this.issuePrice = getCurrentBigDecimal();
            }
            else if (tagName.equals("ISSUECURRENCY")) {
                this.issueCurrency = getCurrentString();
            }
            else if (tagName.equals("INTERESTTYPE")) {
                this.interestType = getCurrentString();
            }
            else if (tagName.equals("INTERESTRUNDEVIATION")) {
                this.interestRunDeviation = getCurrentString();
            }
            else if (tagName.equals("INTERESTCALCULATIONMETHOD")) {
                this.interestCalculationMethod = getCurrentString();
            }
            else if (tagName.equals("EXPIRATIONDATE")) {
                this.expirationDate = getDate();
            }
            else if (tagName.equals("COUPONFREQUENCY")) {
                this.couponFrequency = getCurrentString();
            }
            else if (tagName.equals("COUPONDATEDAYS")) {
                this.couponDateDays = getCurrentInt();
            }
            else if (tagName.equals("FIRSTCOUPONDATE")) {
                this.firstCouponDate = getDate();
            }
            else if (tagName.equals("LASTCOUPONDATE")) {
                this.lastCouponDate = getDate();
            }
            else if (tagName.equals("COUPONDATE")) {
                this.couponDate = getDate();
            }
            else if (tagName.equals("SMALLESTTRANSFERABLEUNIT")) {
                this.smallestTransferableUnit = getCurrentBigDecimal();
            }
            else if (tagName.equals("MINAMOUNTOFTRANSFERABLEUNIT")) {
                this.minAmountOfTransferableUnit = getCurrentBigDecimal();
            }
            else if (tagName.equals("CONVERSIONFACTOR")) {
                this.conversionFactor = getCurrentBigDecimal();
            }
            else if (tagName.equals("WMBONDRANK")) {
                this.bondRank = getCurrentString();
            }
            else if (tagName.equals("ROWS")) {
                //ignored
            }
            else {
                notParsed(tagName);
            }
        } catch (Exception e) {
            this.logger.error("<endElement> error in " + tagName, e);
            this.errorOccured = true;
        }
    }

    private DateTime getDate() {
        return getCurrentDateTime(DTF, true);
    }

    private Integer getCount(String interestperiod) {
        if (COUPON_COUNT_PA.containsKey(interestperiod)) {
            return COUPON_COUNT_PA.get(interestperiod);
        }

        this.unknownPeriods.add(interestperiod);
        return null;
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

        // TODO: implement
//            YearMonthDay interestdate;
//            try {
//                interestdate = resolveFirstCouponDate(this.gd812, this.gd813, this.issuedate, this.numberOfCoupons);
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//                System.out.println("problem for " + instrumentid);
//                interestdate=new YearMonthDay();
//            }

        final MasterDataBond data = new MasterDataBondImpl(this.instrumentid, bondtype.build(), coupontype.build(),
                issuedate, nominalInterest, facevalue, redemptionPrice, issuevolume,
                numberOfCoupons, this.interestperiod.build(),
                null,
//                    interestdate == null ? null : interestdate.toDateTimeAtMidnight(),
// TODO: use again when above implementation is made
                issuername, countryOfIssuer, countryOfIssuerCode, this.couponDateDayNumber,
                this.couponDateMonthNumber,
                this.issuePrice,
                this.issueCurrency,
                this.interestType,
                this.interestRunDeviation,
                this.interestCalculationMethod,
                this.expirationDate,
                this.couponFrequency,
                this.couponDateDays,
                this.firstCouponDate,
                this.lastCouponDate,
                this.couponDate,
                this.smallestTransferableUnit,
                this.minAmountOfTransferableUnit,
                this.conversionFactor,
                this.bondRank);
        this.values.put(this.instrumentid, data);

        reset();
    }

    protected void reset() {
        this.instrumentid = -1;
        this.bondtype.reset();
        this.coupontype.reset();
        this.issuedate = null;
        this.nominalInterest = null;
        this.facevalue = null;
        this.redemptionPrice = null;
        this.issuevolume = null;
        this.interestperiod.reset();
        this.numberOfCoupons = null;
        this.issuername = null;
        this.countryOfIssuer = null;
        this.couponDateDayNumber = null;
        this.couponDateMonthNumber = null;
        this.issuePrice = null;
        this.issueCurrency = null;
        this.interestType = null;
        this.interestRunDeviation = null;
        this.interestCalculationMethod = null;
        this.expirationDate = null;
        this.couponFrequency = null;
        this.couponDateDays = null;
        this.firstCouponDate = null;
        this.lastCouponDate = null;
        this.couponDate = null;
        this.smallestTransferableUnit = null;
        this.minAmountOfTransferableUnit = null;
        this.conversionFactor = null;
        this.errorOccured = false;
        this.bondRank = null;
    }

    public Map<Long, MasterDataBond> getValues() {
        return values;
    }

    protected BigDecimal getCurrentBigDecimal() {
        return getCurrentBigDecimal(true);
    }
}
