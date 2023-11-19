/*
 * Instrument.java
 *
 * Created on 08.05.13 07:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.UserDefinedFields;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.Key;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.QueryStandardSelection;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.*;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class Instrument implements ContextItem {
    public static class Talker extends AbstractMmTalker<QueryStandardSelection, Instrument, Instrument> {

        public Talker(ShellMMType shellMMType) {
            super(Formula.create().withShellMmType(shellMMType));
        }

        @Override
        protected QueryStandardSelection createQuery() {
            final QueryStandardSelection query = new QueryStandardSelection();
            query.setDataItemType(ShellMMType.ST_WP);
            return query;
        }

        @Override
        protected MmTalkWrapper<Instrument> createWrapper(Formula formula) {
            return Instrument.createWrapper(formula);
        }

        @Override
        public Instrument createResultObject(MMTalkResponse response) {
            final List<Instrument> result = this.wrapper.createResultObjectList(response);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        }

        public void setInstrumentId(String instrumentId) {
            getQuery().getKeys().clear();
            final Key key = new Key();
            key.setKey(instrumentId);
            getQuery().getKeys().add(key);
        }
    }

    public static class InstrumentBaseDataWithPrice extends AbstractMmTalker<QueryStandardSelection, Instrument, Instrument> {
        public InstrumentBaseDataWithPrice() {
            super(Formula.create());
        }

        @Override
        protected QueryStandardSelection createQuery() {
            final QueryStandardSelection query = new QueryStandardSelection();
            query.setDataItemType(ShellMMType.ST_WP);
            return query;
        }

        @Override
        protected MmTalkWrapper<Instrument> createWrapper(Formula formula) {
            return Instrument.createBaseDataWithPriceWrapper(formula);
        }

        @Override
        public Instrument createResultObject(MMTalkResponse response) {
            final List<Instrument> result = this.wrapper.createResultObjectList(response);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        }

        public void setSecurityId(String instrumentId) {
            getQuery().getKeys().clear();
            final Key key = new Key();
            key.setKey(instrumentId);
            getQuery().getKeys().add(key);
        }
    }

    public static MmTalkWrapper<Instrument> createBaseDataWithPriceWrapper(Formula formula) {
        final MmTalkWrapper<Instrument> cols = MmTalkWrapper.create(formula, Instrument.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<Instrument>("Id") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.id = asMMNumber(item).getValue();
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Zone") { // $NON-NLS$
            @Override
            public void setValue(Instrument o, MM item) {
                o.zone = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Typ") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.type = asEnum(MMTypRefType.TRT_SHELL_MM_TYP, ShellMMType.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("SecurityId") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.securityId = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.name = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("ISIN") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.isin = asString(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<Instrument, Market>(Market.createWrapper("Platz")) { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MmTalkWrapper<Market> wrapper, MMTable table) {
                i.homeExchange = wrapper.createResultObject(table);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Währung.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.currencyIso = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Letzter_Kurs['Close']") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.price = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Letzter_Kurs['Close'].Datum") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.priceDateTime = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("High.Maximum[Heute.AddWorkingDays[-260];Heute]") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.high52Weeks = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Low.Minimum[Heute.AddWorkingDays[-260];Heute]") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.low52Weeks = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>(
                "$letzter := Letzter_Kurs['Close'];" + // $NON-NLS$
                        "$vorletzter := Kurs[$letzter.Datum.AddWorkingDays[-1];'Close'];" +  // $NON-NLS$
                        "$letzter.DistancePercent[$vorletzter].RoundN[6]") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.changePercent = asString(item);
            }
        });

        return cols;
    }

    public static MmTalkWrapper<Instrument> createWrapper(Formula formula) {
        final List<PmWebSupport.UserFieldCategory> userFieldDecls =
                PmWebSupport.getInstance().getUserFieldDecls(formula.getShellMmType());

        final MmTalkWrapper<Instrument> cols = createBaseDataWithPriceWrapper(formula);
        cols.appendColumnMapper(new MmTalkColumnMapper<Instrument>("OriginalName") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.originalName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("CHValorenNr") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.valorNumber = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("DEWKN") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.dewkn = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("OEWKN") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.oewkn = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("EmissionsDatum") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.issuingDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("FirstTradingDay") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.firstTradingDay = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("LastTradingDay") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.lastTradingDay = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Vergleich.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.benchmarkInstrumentName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Vergleich.SecurityId") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.benchmarkInstrumentSecurityId = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Vergleich.Typ") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.benchmarkInstrumentType = asEnum(MMTypRefType.TRT_SHELL_MM_TYP, ShellMMType.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Land.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.countryName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Land.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.countryToken = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Branche.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.sectorName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Branche.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.sectorToken = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Ticker") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.tickerSymbol = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("TradingUnit") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.tradingUnit = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Bemerkung") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.comment = asString(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<Instrument, UserDefinedFields>(
                UserDefinedFields.createWrapper(Formula.create().withUserFieldCategories(userFieldDecls))) {
            @Override
            public void setValue(Instrument i, MmTalkWrapper<UserDefinedFields> wrapper, MMTable table) {
                i.userDefinedFields = wrapper.createResultObjectList(table);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Prozentnotiz") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.percentQuotation = asBoolean(item);
            }
        });

        appendWMMappers(cols);

        final ShellMMType shellMMType = formula.getShellMmType();
        if (shellMMType == null) return cols;

        switch (shellMMType) {
            case ST_AKTIE:
                appendStockSpecificMappers(cols);
                break;
            case ST_ANLEIHE:
                appendBondSpecificMappers(cols);
                break;
            case ST_OPTION:
            case ST_OS:
                appendOptionAndWarrantSpecificMappers(cols);
                break;
            case ST_CERTIFICATE:
                appendCertificateSpecificMappers(cols);
                break;
            case ST_GENUSS:
            case ST_FUTURE:
                appendExpirationMapper(cols);
                break;
        }

        return cols;
    }

    private static void appendStockSpecificMappers(MmTalkWrapper<Instrument> cols) {
        cols.appendColumnMapper(new MmTalkColumnMapper<Instrument>("SchätzungenErstesJahr") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.estimatesForYearOne = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("SchätzungsWährung.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.estimationCurrency = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Cashflowschätzung1") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.cashFlowEstimates1 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Dividende1") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.dividendEstimates1 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Dividende2") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.dividendEstimates2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Dividende3") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.dividendEstimates3 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Dividende4") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.dividendEstimates4 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Gewinnschätzung1") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.earningsEstimates1 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Gewinnschätzung2") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.earningsEstimates2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Gewinnschätzung3") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.earningsEstimates3 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Gewinnschätzung4") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.earningsEstimates4 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("HVTermin") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.shareholdersMeetingDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Nennwert.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.nominalValue = asString(item);
            }
        });
    }

    private static void appendBondSpecificMappers(MmTalkWrapper<Instrument> cols) {
        appendExpirationMapper(cols);
        cols.appendColumnMapper(new MmTalkColumnMapper<Instrument>("Anleihetyp") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.bondType = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Zins") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.coupon = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Zinstermin") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.couponPaymentDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Rückzahlungskurs") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.redemptionPrice = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Zinsintervall") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.couponFrequency = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("PeriodenFormelTyp") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.dayCountConvention = asString(item);
            }
        });
    }

    private static void appendOptionAndWarrantSpecificMappers(MmTalkWrapper<Instrument> cols) {
        appendExpirationMapper(cols);
        cols.appendColumnMapper(new MmTalkColumnMapper<Instrument>("OptionsTyp") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.optionType = asString(item);
            }
        });
    }

    private static void appendCertificateSpecificMappers(MmTalkWrapper<Instrument> cols) {
        appendExpirationMapper(cols);
        cols.appendColumnMapper(new MmTalkColumnMapper<Instrument>("CertificateType.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.certificateTypeName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("SpeculationType.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.speculationTypeName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("ProductNameIssuer") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.productName = asString(item);
            }
        });
    }

    private static void appendExpirationMapper(MmTalkWrapper<Instrument> cols) {
        cols.appendColumnMapper(new MmTalkColumnMapper<Instrument>("Laufzeit") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.expiration = asString(item);
            }
        });
    }

    private static void appendWMMappers(MmTalkWrapper<Instrument> cols) {
        cols.appendColumnMapper(new MmTalkColumnMapper<Instrument>("NameWM") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMBranche.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmSectorName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMBranche.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmSectorToken = asString(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<Instrument, Market>(Market.createWrapper("WMHeimatboerse")) { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MmTalkWrapper<Market> wrapper, MMTable table) {
                i.wmHomeExchange = wrapper.createResultObject(table);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Emittentenkategorie.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmIssuerCategoryToken = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("Emittentenkategorie.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmIssuerCategoryName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMInstrumentenArt.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmInstrumentenTypeName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMInstrumentenArt.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmInstrumentenTypeToken = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMInstrumentenArtZus1.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmInstrumentenTypeAnnex1Name = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMInstrumentenArtZus1.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmInstrumentenTypeAnnex1Token = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMInstrumentenArtZus2.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmInstrumentenTypeAnnex2Name = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMInstrumentenArtZus2.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmInstrumentenTypeAnnex2Token = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMInstrumentenArtZus3.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmInstrumentenTypeAnnex3Name = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMInstrumentenArtZus3.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmInstrumentenTypeAnnex3Token = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMInstrumentenArtZus4.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmInstrumentenTypeAnnex4Name = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMInstrumentenArtZus4.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmInstrumentenTypeAnnex4Token = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMLName1") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmLongName1 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMLName2") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmLongName2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMProdArtSegment.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmProductTypeSectorName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMProdArtSegment.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmProductTypeSectorToken = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMProdGruppeSegment.Name") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmProductGroupSectorName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMProdGruppeSegment.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmProductGroupSectorToken = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Instrument>("WMABRECHNUNGSWAEHRUNG.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Instrument i, MM item) {
                i.wmSettlementCurrencyIso = asString(item);
            }
        });
    }

    // common
    private String id;
    private String zone;
    private String securityId;
    private String name;

    private String originalName;
    private String tradingUnit;
    private Market homeExchange;
    private String tickerSymbol;

    private String isin;
    private String valorNumber;
    private String dewkn;
    private String oewkn;
    private String countryName;
    private String countryToken;
    private String sectorName;
    private String sectorToken;
    private String issuingDate;
    private String firstTradingDay;
    private String lastTradingDay;

    private String benchmarkInstrumentName;
    private String benchmarkInstrumentSecurityId;
    private ShellMMType benchmarkInstrumentType;

    private String price;
    private String priceDateTime;
    private String changePercent;
    private String high52Weeks;
    private String low52Weeks;
    private String currencyIso;
    private ShellMMType type;
    private String comment;

    private List<UserDefinedFields> userDefinedFields;

    // WM specific
    private String wmName;
    private String wmLongName1;
    private String wmLongName2;
    private String wmSettlementCurrencyIso;
    private Market wmHomeExchange;
    private String wmSectorName;
    private String wmSectorToken;
    private String wmProductTypeSectorName;
    private String wmProductTypeSectorToken;
    private String wmProductGroupSectorName;
    private String wmProductGroupSectorToken;
    private String wmIssuerCategoryToken;
    private String wmIssuerCategoryName;
    private String wmInstrumentenTypeName;
    private String wmInstrumentenTypeToken;
    private String wmInstrumentenTypeAnnex1Name;
    private String wmInstrumentenTypeAnnex1Token;
    private String wmInstrumentenTypeAnnex2Name;
    private String wmInstrumentenTypeAnnex2Token;
    private String wmInstrumentenTypeAnnex3Name;
    private String wmInstrumentenTypeAnnex3Token;
    private String wmInstrumentenTypeAnnex4Name;
    private String wmInstrumentenTypeAnnex4Token;

    // stock
    private String shareholdersMeetingDate;
    private String nominalValue;

    private String cashFlowEstimates1;

    private String estimationCurrency;
    private String estimatesForYearOne;

    private String dividendEstimates1;
    private String dividendEstimates2;
    private String dividendEstimates3;
    private String dividendEstimates4;

    private String earningsEstimates1;
    private String earningsEstimates2;
    private String earningsEstimates3;
    private String earningsEstimates4;

    // shared
    private String expiration;
    private Boolean percentQuotation;

    // bond
    private String couponPaymentDate;
    private String bondType;
    private String redemptionPrice;
    private String coupon;
    private String couponFrequency;
    private String dayCountConvention;

    // option / warrant
    private String optionType;
    // certificate
    private String certificateTypeName;
    private String speculationTypeName;
    private String productName;

    public String getId() {
        return this.id;
    }

    public String getZone() {
        return this.zone;
    }

    public String getIsin() {
        return this.isin;
    }

    public String getDewkn() {
        return this.dewkn;
    }

    public String getCountryName() {
        return this.countryName;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getSecurityId() {
        return this.securityId;
    }

    public List<UserDefinedFields> getUserDefinedFields() {
        return this.userDefinedFields;
    }

    public String getPrice() {
        return this.price;
    }

    public String getCurrencyIso() {
        return this.currencyIso;
    }

    public String getOewkn() {
        return this.oewkn;
    }

    public String getCountryToken() {
        return this.countryToken;
    }

    public String getSectorName() {
        return this.sectorName;
    }

    public String getSectorToken() {
        return this.sectorToken;
    }

    public String getWmSectorName() {
        return this.wmSectorName;
    }

    public String getWmSectorToken() {
        return this.wmSectorToken;
    }

    public ShellMMType getType() {
        return this.type;
    }

    public String getExpiration() {
        return this.expiration;
    }

    public String getCouponPaymentDate() {
        return this.couponPaymentDate;
    }

    public String getBondType() {
        return this.bondType;
    }

    public String getOptionType() {
        return this.optionType;
    }

    public String getCertificateTypeName() {
        return this.certificateTypeName;
    }

    public String getSpeculationTypeName() {
        return this.speculationTypeName;
    }

    public String getProductName() {
        return this.productName;
    }

    public String getCashFlowEstimates1() {
        return cashFlowEstimates1;
    }

    public String getShareholdersMeetingDate() {
        return shareholdersMeetingDate;
    }

    public String getNominalValue() {
        return nominalValue;
    }

    public String getEstimationCurrency() {
        return estimationCurrency;
    }

    public String getEstimatesForYearOne() {
        return estimatesForYearOne;
    }

    public String getDividendEstimates1() {
        return dividendEstimates1;
    }

    public String getDividendEstimates2() {
        return dividendEstimates2;
    }

    public String getDividendEstimates3() {
        return dividendEstimates3;
    }

    public String getDividendEstimates4() {
        return dividendEstimates4;
    }

    public String getEarningsEstimates1() {
        return earningsEstimates1;
    }

    public String getEarningsEstimates2() {
        return earningsEstimates2;
    }

    public String getEarningsEstimates3() {
        return earningsEstimates3;
    }

    public String getEarningsEstimates4() {
        return earningsEstimates4;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getTradingUnit() {
        return tradingUnit;
    }

    public Market getHomeExchange() {
        return homeExchange;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public String getRedemptionPrice() {
        return redemptionPrice;
    }

    public String getCouponFrequency() {
        return couponFrequency;
    }

    public String getDayCountConvention() {
        return dayCountConvention;
    }

    public Market getWmHomeExchange() {
        return wmHomeExchange;
    }

    public String getWmInstrumentenTypeName() {
        return wmInstrumentenTypeName;
    }

    public String getWmInstrumentenTypeAnnex1Name() {
        return wmInstrumentenTypeAnnex1Name;
    }

    public String getWmInstrumentenTypeAnnex2Name() {
        return wmInstrumentenTypeAnnex2Name;
    }

    public String getWmInstrumentenTypeAnnex3Name() {
        return wmInstrumentenTypeAnnex3Name;
    }

    public String getWmInstrumentenTypeAnnex4Name() {
        return wmInstrumentenTypeAnnex4Name;
    }

    public String getWmLongName1() {
        return wmLongName1;
    }

    public String getWmLongName2() {
        return wmLongName2;
    }

    public String getWmProductTypeSectorName() {
        return wmProductTypeSectorName;
    }

    public String getWmProductGroupSectorName() {
        return wmProductGroupSectorName;
    }

    public String getWmProductTypeSectorToken() {
        return wmProductTypeSectorToken;
    }

    public String getWmProductGroupSectorToken() {
        return wmProductGroupSectorToken;
    }

    public String getWmSettlementCurrencyIso() {
        return wmSettlementCurrencyIso;
    }

    public String getWmInstrumentenTypeToken() {
        return wmInstrumentenTypeToken;
    }

    public String getWmInstrumentenTypeAnnex1Token() {
        return wmInstrumentenTypeAnnex1Token;
    }

    public String getWmInstrumentenTypeAnnex2Token() {
        return wmInstrumentenTypeAnnex2Token;
    }

    public String getWmInstrumentenTypeAnnex3Token() {
        return wmInstrumentenTypeAnnex3Token;
    }

    public String getWmInstrumentenTypeAnnex4Token() {
        return wmInstrumentenTypeAnnex4Token;
    }

    public String getWmIssuerCategoryToken() {
        return wmIssuerCategoryToken;
    }

    public String getWmIssuerCategoryName() {
        return wmIssuerCategoryName;
    }

    public String getPriceDateTime() {
        return priceDateTime;
    }

    public String getComment() {
        return comment;
    }

    public String getIssuingDate() {
        return issuingDate;
    }

    public String getFirstTradingDay() {
        return firstTradingDay;
    }

    public String getLastTradingDay() {
        return lastTradingDay;
    }

    public String getWmName() {
        return wmName;
    }

    public String getBenchmarkInstrumentName() {
        return benchmarkInstrumentName;
    }

    public String getBenchmarkInstrumentSecurityId() {
        return benchmarkInstrumentSecurityId;
    }

    public ShellMMType getBenchmarkInstrumentType() {
        return benchmarkInstrumentType;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public String getHigh52Weeks() {
        return high52Weeks;
    }

    public String getLow52Weeks() {
        return low52Weeks;
    }

    public String getValorNumber() {
        return this.valorNumber;
    }

    public Boolean isPercentQuotation() {
        return percentQuotation;
    }

    public String getCoupon() {
        return coupon;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Instrument that = (Instrument) o;

        if (benchmarkInstrumentName != null ? !benchmarkInstrumentName.equals(that.benchmarkInstrumentName) : that.benchmarkInstrumentName != null) {
            return false;
        }
        if (benchmarkInstrumentSecurityId != null ? !benchmarkInstrumentSecurityId.equals(that.benchmarkInstrumentSecurityId) : that.benchmarkInstrumentSecurityId != null) {
            return false;
        }
        if (benchmarkInstrumentType != that.benchmarkInstrumentType) {
            return false;
        }
        if (bondType != null ? !bondType.equals(that.bondType) : that.bondType != null) {
            return false;
        }
        if (cashFlowEstimates1 != null ? !cashFlowEstimates1.equals(that.cashFlowEstimates1) : that.cashFlowEstimates1 != null) {
            return false;
        }
        if (certificateTypeName != null ? !certificateTypeName.equals(that.certificateTypeName) : that.certificateTypeName != null) {
            return false;
        }
        if (changePercent != null ? !changePercent.equals(that.changePercent) : that.changePercent != null) {
            return false;
        }
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) {
            return false;
        }
        if (countryName != null ? !countryName.equals(that.countryName) : that.countryName != null) {
            return false;
        }
        if (countryToken != null ? !countryToken.equals(that.countryToken) : that.countryToken != null) {
            return false;
        }
        if (coupon != null ? !coupon.equals(that.coupon) : that.coupon != null) {
            return false;
        }
        if (couponFrequency != null ? !couponFrequency.equals(that.couponFrequency) : that.couponFrequency != null) {
            return false;
        }
        if (couponPaymentDate != null ? !couponPaymentDate.equals(that.couponPaymentDate) : that.couponPaymentDate != null) {
            return false;
        }
        if (currencyIso != null ? !currencyIso.equals(that.currencyIso) : that.currencyIso != null) {
            return false;
        }
        if (dayCountConvention != null ? !dayCountConvention.equals(that.dayCountConvention) : that.dayCountConvention != null) {
            return false;
        }
        if (dewkn != null ? !dewkn.equals(that.dewkn) : that.dewkn != null) {
            return false;
        }
        if (dividendEstimates1 != null ? !dividendEstimates1.equals(that.dividendEstimates1) : that.dividendEstimates1 != null) {
            return false;
        }
        if (dividendEstimates2 != null ? !dividendEstimates2.equals(that.dividendEstimates2) : that.dividendEstimates2 != null) {
            return false;
        }
        if (dividendEstimates3 != null ? !dividendEstimates3.equals(that.dividendEstimates3) : that.dividendEstimates3 != null) {
            return false;
        }
        if (dividendEstimates4 != null ? !dividendEstimates4.equals(that.dividendEstimates4) : that.dividendEstimates4 != null) {
            return false;
        }
        if (earningsEstimates1 != null ? !earningsEstimates1.equals(that.earningsEstimates1) : that.earningsEstimates1 != null) {
            return false;
        }
        if (earningsEstimates2 != null ? !earningsEstimates2.equals(that.earningsEstimates2) : that.earningsEstimates2 != null) {
            return false;
        }
        if (earningsEstimates3 != null ? !earningsEstimates3.equals(that.earningsEstimates3) : that.earningsEstimates3 != null) {
            return false;
        }
        if (earningsEstimates4 != null ? !earningsEstimates4.equals(that.earningsEstimates4) : that.earningsEstimates4 != null) {
            return false;
        }
        if (estimatesForYearOne != null ? !estimatesForYearOne.equals(that.estimatesForYearOne) : that.estimatesForYearOne != null) {
            return false;
        }
        if (estimationCurrency != null ? !estimationCurrency.equals(that.estimationCurrency) : that.estimationCurrency != null) {
            return false;
        }
        if (expiration != null ? !expiration.equals(that.expiration) : that.expiration != null) {
            return false;
        }
        if (firstTradingDay != null ? !firstTradingDay.equals(that.firstTradingDay) : that.firstTradingDay != null) {
            return false;
        }
        if (high52Weeks != null ? !high52Weeks.equals(that.high52Weeks) : that.high52Weeks != null) {
            return false;
        }
        if (homeExchange != null ? !homeExchange.equals(that.homeExchange) : that.homeExchange != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (isin != null ? !isin.equals(that.isin) : that.isin != null) {
            return false;
        }
        if (issuingDate != null ? !issuingDate.equals(that.issuingDate) : that.issuingDate != null) {
            return false;
        }
        if (lastTradingDay != null ? !lastTradingDay.equals(that.lastTradingDay) : that.lastTradingDay != null) {
            return false;
        }
        if (low52Weeks != null ? !low52Weeks.equals(that.low52Weeks) : that.low52Weeks != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (nominalValue != null ? !nominalValue.equals(that.nominalValue) : that.nominalValue != null) {
            return false;
        }
        if (oewkn != null ? !oewkn.equals(that.oewkn) : that.oewkn != null) {
            return false;
        }
        if (optionType != null ? !optionType.equals(that.optionType) : that.optionType != null) {
            return false;
        }
        if (originalName != null ? !originalName.equals(that.originalName) : that.originalName != null) {
            return false;
        }
        if (percentQuotation != null ? !percentQuotation.equals(that.percentQuotation) : that.percentQuotation != null) {
            return false;
        }
        if (price != null ? !price.equals(that.price) : that.price != null) {
            return false;
        }
        if (priceDateTime != null ? !priceDateTime.equals(that.priceDateTime) : that.priceDateTime != null) {
            return false;
        }
        if (productName != null ? !productName.equals(that.productName) : that.productName != null) {
            return false;
        }
        if (redemptionPrice != null ? !redemptionPrice.equals(that.redemptionPrice) : that.redemptionPrice != null) {
            return false;
        }
        if (sectorName != null ? !sectorName.equals(that.sectorName) : that.sectorName != null) {
            return false;
        }
        if (sectorToken != null ? !sectorToken.equals(that.sectorToken) : that.sectorToken != null) {
            return false;
        }
        if (securityId != null ? !securityId.equals(that.securityId) : that.securityId != null) {
            return false;
        }
        if (shareholdersMeetingDate != null ? !shareholdersMeetingDate.equals(that.shareholdersMeetingDate) : that.shareholdersMeetingDate != null) {
            return false;
        }
        if (speculationTypeName != null ? !speculationTypeName.equals(that.speculationTypeName) : that.speculationTypeName != null) {
            return false;
        }
        if (tickerSymbol != null ? !tickerSymbol.equals(that.tickerSymbol) : that.tickerSymbol != null) {
            return false;
        }
        if (tradingUnit != null ? !tradingUnit.equals(that.tradingUnit) : that.tradingUnit != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (userDefinedFields != null ? !userDefinedFields.equals(that.userDefinedFields) : that.userDefinedFields != null) {
            return false;
        }
        if (valorNumber != null ? !valorNumber.equals(that.valorNumber) : that.valorNumber != null) {
            return false;
        }
        if (wmHomeExchange != null ? !wmHomeExchange.equals(that.wmHomeExchange) : that.wmHomeExchange != null) {
            return false;
        }
        if (wmInstrumentenTypeAnnex1Name != null ? !wmInstrumentenTypeAnnex1Name.equals(that.wmInstrumentenTypeAnnex1Name) : that.wmInstrumentenTypeAnnex1Name != null) {
            return false;
        }
        if (wmInstrumentenTypeAnnex1Token != null ? !wmInstrumentenTypeAnnex1Token.equals(that.wmInstrumentenTypeAnnex1Token) : that.wmInstrumentenTypeAnnex1Token != null) {
            return false;
        }
        if (wmInstrumentenTypeAnnex2Name != null ? !wmInstrumentenTypeAnnex2Name.equals(that.wmInstrumentenTypeAnnex2Name) : that.wmInstrumentenTypeAnnex2Name != null) {
            return false;
        }
        if (wmInstrumentenTypeAnnex2Token != null ? !wmInstrumentenTypeAnnex2Token.equals(that.wmInstrumentenTypeAnnex2Token) : that.wmInstrumentenTypeAnnex2Token != null) {
            return false;
        }
        if (wmInstrumentenTypeAnnex3Name != null ? !wmInstrumentenTypeAnnex3Name.equals(that.wmInstrumentenTypeAnnex3Name) : that.wmInstrumentenTypeAnnex3Name != null) {
            return false;
        }
        if (wmInstrumentenTypeAnnex3Token != null ? !wmInstrumentenTypeAnnex3Token.equals(that.wmInstrumentenTypeAnnex3Token) : that.wmInstrumentenTypeAnnex3Token != null) {
            return false;
        }
        if (wmInstrumentenTypeAnnex4Name != null ? !wmInstrumentenTypeAnnex4Name.equals(that.wmInstrumentenTypeAnnex4Name) : that.wmInstrumentenTypeAnnex4Name != null) {
            return false;
        }
        if (wmInstrumentenTypeAnnex4Token != null ? !wmInstrumentenTypeAnnex4Token.equals(that.wmInstrumentenTypeAnnex4Token) : that.wmInstrumentenTypeAnnex4Token != null) {
            return false;
        }
        if (wmInstrumentenTypeName != null ? !wmInstrumentenTypeName.equals(that.wmInstrumentenTypeName) : that.wmInstrumentenTypeName != null) {
            return false;
        }
        if (wmInstrumentenTypeToken != null ? !wmInstrumentenTypeToken.equals(that.wmInstrumentenTypeToken) : that.wmInstrumentenTypeToken != null) {
            return false;
        }
        if (wmIssuerCategoryName != null ? !wmIssuerCategoryName.equals(that.wmIssuerCategoryName) : that.wmIssuerCategoryName != null) {
            return false;
        }
        if (wmIssuerCategoryToken != null ? !wmIssuerCategoryToken.equals(that.wmIssuerCategoryToken) : that.wmIssuerCategoryToken != null) {
            return false;
        }
        if (wmLongName1 != null ? !wmLongName1.equals(that.wmLongName1) : that.wmLongName1 != null) {
            return false;
        }
        if (wmLongName2 != null ? !wmLongName2.equals(that.wmLongName2) : that.wmLongName2 != null) {
            return false;
        }
        if (wmName != null ? !wmName.equals(that.wmName) : that.wmName != null) {
            return false;
        }
        if (wmProductGroupSectorName != null ? !wmProductGroupSectorName.equals(that.wmProductGroupSectorName) : that.wmProductGroupSectorName != null) {
            return false;
        }
        if (wmProductGroupSectorToken != null ? !wmProductGroupSectorToken.equals(that.wmProductGroupSectorToken) : that.wmProductGroupSectorToken != null) {
            return false;
        }
        if (wmProductTypeSectorName != null ? !wmProductTypeSectorName.equals(that.wmProductTypeSectorName) : that.wmProductTypeSectorName != null) {
            return false;
        }
        if (wmProductTypeSectorToken != null ? !wmProductTypeSectorToken.equals(that.wmProductTypeSectorToken) : that.wmProductTypeSectorToken != null) {
            return false;
        }
        if (wmSectorName != null ? !wmSectorName.equals(that.wmSectorName) : that.wmSectorName != null) {
            return false;
        }
        if (wmSectorToken != null ? !wmSectorToken.equals(that.wmSectorToken) : that.wmSectorToken != null) {
            return false;
        }
        if (wmSettlementCurrencyIso != null ? !wmSettlementCurrencyIso.equals(that.wmSettlementCurrencyIso) : that.wmSettlementCurrencyIso != null) {
            return false;
        }
        if (zone != null ? !zone.equals(that.zone) : that.zone != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        result = 31 * result + (securityId != null ? securityId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (originalName != null ? originalName.hashCode() : 0);
        result = 31 * result + (tradingUnit != null ? tradingUnit.hashCode() : 0);
        result = 31 * result + (homeExchange != null ? homeExchange.hashCode() : 0);
        result = 31 * result + (tickerSymbol != null ? tickerSymbol.hashCode() : 0);
        result = 31 * result + (isin != null ? isin.hashCode() : 0);
        result = 31 * result + (valorNumber != null ? valorNumber.hashCode() : 0);
        result = 31 * result + (dewkn != null ? dewkn.hashCode() : 0);
        result = 31 * result + (oewkn != null ? oewkn.hashCode() : 0);
        result = 31 * result + (countryName != null ? countryName.hashCode() : 0);
        result = 31 * result + (countryToken != null ? countryToken.hashCode() : 0);
        result = 31 * result + (sectorName != null ? sectorName.hashCode() : 0);
        result = 31 * result + (sectorToken != null ? sectorToken.hashCode() : 0);
        result = 31 * result + (issuingDate != null ? issuingDate.hashCode() : 0);
        result = 31 * result + (firstTradingDay != null ? firstTradingDay.hashCode() : 0);
        result = 31 * result + (lastTradingDay != null ? lastTradingDay.hashCode() : 0);
        result = 31 * result + (benchmarkInstrumentName != null ? benchmarkInstrumentName.hashCode() : 0);
        result = 31 * result + (benchmarkInstrumentSecurityId != null ? benchmarkInstrumentSecurityId.hashCode() : 0);
        result = 31 * result + (benchmarkInstrumentType != null ? benchmarkInstrumentType.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (priceDateTime != null ? priceDateTime.hashCode() : 0);
        result = 31 * result + (changePercent != null ? changePercent.hashCode() : 0);
        result = 31 * result + (high52Weeks != null ? high52Weeks.hashCode() : 0);
        result = 31 * result + (low52Weeks != null ? low52Weeks.hashCode() : 0);
        result = 31 * result + (currencyIso != null ? currencyIso.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (userDefinedFields != null ? userDefinedFields.hashCode() : 0);
        result = 31 * result + (wmName != null ? wmName.hashCode() : 0);
        result = 31 * result + (wmLongName1 != null ? wmLongName1.hashCode() : 0);
        result = 31 * result + (wmLongName2 != null ? wmLongName2.hashCode() : 0);
        result = 31 * result + (wmSettlementCurrencyIso != null ? wmSettlementCurrencyIso.hashCode() : 0);
        result = 31 * result + (wmHomeExchange != null ? wmHomeExchange.hashCode() : 0);
        result = 31 * result + (wmSectorName != null ? wmSectorName.hashCode() : 0);
        result = 31 * result + (wmSectorToken != null ? wmSectorToken.hashCode() : 0);
        result = 31 * result + (wmProductTypeSectorName != null ? wmProductTypeSectorName.hashCode() : 0);
        result = 31 * result + (wmProductTypeSectorToken != null ? wmProductTypeSectorToken.hashCode() : 0);
        result = 31 * result + (wmProductGroupSectorName != null ? wmProductGroupSectorName.hashCode() : 0);
        result = 31 * result + (wmProductGroupSectorToken != null ? wmProductGroupSectorToken.hashCode() : 0);
        result = 31 * result + (wmIssuerCategoryToken != null ? wmIssuerCategoryToken.hashCode() : 0);
        result = 31 * result + (wmIssuerCategoryName != null ? wmIssuerCategoryName.hashCode() : 0);
        result = 31 * result + (wmInstrumentenTypeName != null ? wmInstrumentenTypeName.hashCode() : 0);
        result = 31 * result + (wmInstrumentenTypeToken != null ? wmInstrumentenTypeToken.hashCode() : 0);
        result = 31 * result + (wmInstrumentenTypeAnnex1Name != null ? wmInstrumentenTypeAnnex1Name.hashCode() : 0);
        result = 31 * result + (wmInstrumentenTypeAnnex1Token != null ? wmInstrumentenTypeAnnex1Token.hashCode() : 0);
        result = 31 * result + (wmInstrumentenTypeAnnex2Name != null ? wmInstrumentenTypeAnnex2Name.hashCode() : 0);
        result = 31 * result + (wmInstrumentenTypeAnnex2Token != null ? wmInstrumentenTypeAnnex2Token.hashCode() : 0);
        result = 31 * result + (wmInstrumentenTypeAnnex3Name != null ? wmInstrumentenTypeAnnex3Name.hashCode() : 0);
        result = 31 * result + (wmInstrumentenTypeAnnex3Token != null ? wmInstrumentenTypeAnnex3Token.hashCode() : 0);
        result = 31 * result + (wmInstrumentenTypeAnnex4Name != null ? wmInstrumentenTypeAnnex4Name.hashCode() : 0);
        result = 31 * result + (wmInstrumentenTypeAnnex4Token != null ? wmInstrumentenTypeAnnex4Token.hashCode() : 0);
        result = 31 * result + (shareholdersMeetingDate != null ? shareholdersMeetingDate.hashCode() : 0);
        result = 31 * result + (nominalValue != null ? nominalValue.hashCode() : 0);
        result = 31 * result + (cashFlowEstimates1 != null ? cashFlowEstimates1.hashCode() : 0);
        result = 31 * result + (estimationCurrency != null ? estimationCurrency.hashCode() : 0);
        result = 31 * result + (estimatesForYearOne != null ? estimatesForYearOne.hashCode() : 0);
        result = 31 * result + (dividendEstimates1 != null ? dividendEstimates1.hashCode() : 0);
        result = 31 * result + (dividendEstimates2 != null ? dividendEstimates2.hashCode() : 0);
        result = 31 * result + (dividendEstimates3 != null ? dividendEstimates3.hashCode() : 0);
        result = 31 * result + (dividendEstimates4 != null ? dividendEstimates4.hashCode() : 0);
        result = 31 * result + (earningsEstimates1 != null ? earningsEstimates1.hashCode() : 0);
        result = 31 * result + (earningsEstimates2 != null ? earningsEstimates2.hashCode() : 0);
        result = 31 * result + (earningsEstimates3 != null ? earningsEstimates3.hashCode() : 0);
        result = 31 * result + (earningsEstimates4 != null ? earningsEstimates4.hashCode() : 0);
        result = 31 * result + (expiration != null ? expiration.hashCode() : 0);
        result = 31 * result + (percentQuotation != null ? percentQuotation.hashCode() : 0);
        result = 31 * result + (couponPaymentDate != null ? couponPaymentDate.hashCode() : 0);
        result = 31 * result + (bondType != null ? bondType.hashCode() : 0);
        result = 31 * result + (redemptionPrice != null ? redemptionPrice.hashCode() : 0);
        result = 31 * result + (coupon != null ? coupon.hashCode() : 0);
        result = 31 * result + (couponFrequency != null ? couponFrequency.hashCode() : 0);
        result = 31 * result + (dayCountConvention != null ? dayCountConvention.hashCode() : 0);
        result = 31 * result + (optionType != null ? optionType.hashCode() : 0);
        result = 31 * result + (certificateTypeName != null ? certificateTypeName.hashCode() : 0);
        result = 31 * result + (speculationTypeName != null ? speculationTypeName.hashCode() : 0);
        result = 31 * result + (productName != null ? productName.hashCode() : 0);
        return result;
    }
}