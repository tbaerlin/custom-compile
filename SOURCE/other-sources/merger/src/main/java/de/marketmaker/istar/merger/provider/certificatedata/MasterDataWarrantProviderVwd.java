/*
 * WarrantMasterDataProviderVwd.java
 *
 * Created on 6/16/14 12:13 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import java.io.File;
import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.data.MasterDataWarrant;
import de.marketmaker.istar.domain.data.NullMasterDataWarrant;
import de.marketmaker.istar.domainimpl.data.MasterDataWarrantImpl;
import de.marketmaker.istar.merger.provider.MasterDataProvider;

/**
 * @author Stefan Willenbrock
 */
public class MasterDataWarrantProviderVwd extends AbstractUpdatableDataProvider<MasterDataWarrant>
        implements MasterDataProvider<MasterDataWarrant> {

    @Override
    public MasterDataWarrant getMasterData(long instrumentid) {
        final MasterDataWarrant data = getData(instrumentid);
        return data != null ? data : NullMasterDataWarrant.INSTANCE;
    }

    @Override
    protected void read(File file) throws Exception {
        new AbstractSaxReader() {
            private final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

            private long instrumentid = -1;
            private BigDecimal issuePrice;
            private YearMonthDay issueDate;
            private String issuerName;
            private String currencyStrike;
            private Boolean american;
            private LocalDate firstTradingDate;
            private LocalDate lastTradingDate;

            private final InstrumentBasedUpdatable<MasterDataWarrant> provider = MasterDataWarrantProviderVwd.this;

            public void endElement(String uri, String localName, String tagName) throws SAXException {
                try {
                    if (tagName.equals("ROW")) {
                        // i have finished a new row => process
                        storeFields();
                    }
                    else if (tagName.equals("IID")) {
                        this.instrumentid = getCurrentLong();
                    }
                    else if (tagName.equals("ISSUEPRICE")) {
                        this.issuePrice = getCurrentBigDecimal();
                    }
                    else if (tagName.equals("ISSUEDATE")) {
                        this.issueDate = DTF.parseDateTime(getCurrentString(false)).toYearMonthDay();
                    }
                    else if (tagName.equals("ISSUERNAME")) {
                        this.issuerName = getCurrentString();
                    }
                    else if (tagName.equals("ISAMERICAN")) {
                        this.american = "T".equals(getCurrentString());
                    }
                    else if (tagName.equals("CURRENCYSTRIKE")) {
                        this.currencyStrike = getCurrentString();
                    }
                    else if (tagName.equals("FIRSTTRADINGDATE")) {
                        this.firstTradingDate = DTF.parseDateTime(getCurrentString(false)).toLocalDate();
                    }
                    else if (tagName.equals("LASTTRADINGDATE")) {
                        this.lastTradingDate = DTF.parseDateTime(getCurrentString(false)).toLocalDate();
                    }
                    else if (tagName.equals("ROWS")) {
                        // ignored
                    }
                    else {
                        notParsed(tagName);
                    }
                }
                catch (Exception e) {
                    this.logger.error("<endElement> error in " + tagName, e);
                    this.errorOccured = true;
                }
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

                final MasterDataWarrant data = new MasterDataWarrantImpl(this.instrumentid, this.issuePrice, this.issueDate,
                        this.issuerName, this.american, this.currencyStrike, this.firstTradingDate, this.lastTradingDate);
                this.provider.addOrReplace(this.instrumentid, data);

                reset();
            }

            protected void reset() {
                this.instrumentid = -1;
                this.issuePrice = null;
                this.issueDate = null;
                this.issuerName = null;
                this.american = null;
                this.currencyStrike= null;
                this.firstTradingDate = null;
                this.lastTradingDate = null;
                this.errorOccured = false;
            }
        }.read(file);
    }

    public static void main(String[] args) throws Exception {
        final MasterDataWarrantProviderVwd p = new MasterDataWarrantProviderVwd();
        p.setFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-gatrixx-warrant-masterdata.xml.gz"));
        p.afterPropertiesSet();
        final MasterDataWarrant masterData = p.getMasterData(603317L);
        System.out.println(masterData);
    }
}
