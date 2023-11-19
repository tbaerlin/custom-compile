/*
 * MerDataProviderImpl.java
 *
 * Created on 26.09.2008 07:36:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.quotemetadata;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.data.QuoteMetadata;
import de.marketmaker.istar.domainimpl.data.NullQuoteMetadata;
import de.marketmaker.istar.domainimpl.data.QuoteMetadataImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class QuoteMetadataProviderImpl implements InitializingBean, QuoteMetadataProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private File file;

    private final AtomicReference<Repository> repository = new AtomicReference<>();

    private boolean limitedRead = true;

    public void setLimitedRead(boolean limitedRead) {
        this.limitedRead = limitedRead;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception {
        final FileResource resource = new FileResource(this.file);
        resource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readItems();
            }
        });
        this.activeMonitor.addResource(resource);

        readItems();
    }

    private void readItems() {
        try {
            this.logger.info("<readItems> start reading " + this.file);
            final TimeTaker tt = new TimeTaker();

            final ItemsReader ur = new ItemsReader(this.limitedRead);
            ur.read(this.file);

            this.repository.set(ur.getRepository());
            this.logger.info("<readItems> read repository, #" + this.repository.get().size() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readItems> failed", e);
        }
    }

    public QuoteMetadata getQuoteMetadata(long qid) {
        return this.repository.get().getQuoteMetadata(qid);
    }

    private static class ItemsReader extends AbstractSaxReader {
        private final Repository repository = new Repository();

        private long qid = -1;

        private boolean estimatesReuters;

        private boolean convensys;

        private boolean screener;

        private boolean edg;

        private boolean gisFndReport;

        private boolean gisCerReport;

        private boolean stockselectionFndReport;

        private boolean stockselectionCerReport;

        private boolean ssatFndReport;

        private boolean factset;

        private boolean vwdbenlFundamentalData;

        private boolean funddataMorningstar;

        private boolean funddataVwdBeNl;

        private boolean cer_underlying;

        private boolean wnt_underlying;

        private boolean cer_underlying_dzbank;

        private boolean cer_underlying_wgzbank;

        private boolean wnt_underlying_dzbank;

        private boolean cer_dzbank;

        private boolean cer_wgzbank;

        private boolean wnt_dzbank;

        private boolean fut_underlying;

        private boolean opt_underlying;

        private boolean indexConstituent;

        private final boolean limitedRead;

        public ItemsReader(boolean limitedRead) {
            this.limitedRead = limitedRead;
        }

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new row => process
                    storeFields();
                }
                else if (tagName.equals("QID")) {
                    this.qid = getCurrentLong();
                }
                else if (tagName.equals("SCREENER")) {
                    this.screener = getCurrentBoolean();
                }
                else if (tagName.equals("ESTIMATES_REUTERS")) {
                    this.estimatesReuters = getCurrentBoolean();
                }
                else if (tagName.equals("CONVENSYS")) {
                    this.convensys = getCurrentBoolean();
                }
                else if (tagName.equals("EDG_RATING")) {
                    this.edg = getCurrentBoolean();
                }
                else if (tagName.equals("GISFNDREPORT")) {
                    this.gisFndReport = getCurrentBoolean();
                }
                else if (tagName.equals("GISCERREPORT")) {
                    this.gisCerReport = getCurrentBoolean();
                }
                else if (tagName.equals("STOCKSELECTIONFNDREPORT")) {
                    this.stockselectionFndReport = getCurrentBoolean();
                }
                else if (tagName.equals("STOCKSELECTIONCERREPORT")) {
                    this.stockselectionCerReport = getCurrentBoolean();
                }
                else if (tagName.equals("SSATFNDREPORT")) {
                    this.ssatFndReport = getCurrentBoolean();
                }
                else if (tagName.equals("FACTSET")) {
                    this.factset = getCurrentBoolean();
                }
                else if (tagName.equals("VWDBENLFUNDAMENTALDATA")) {
                    this.vwdbenlFundamentalData = getCurrentBoolean();
                }
                else if (tagName.equals("FUNDDATAMORNINGSTAR")) {
                    this.funddataMorningstar = getCurrentBoolean();
                }
                else if (tagName.equals("FUNDDATAVWDBENL")) {
                    this.funddataVwdBeNl = getCurrentBoolean();
                }
                else if (tagName.equals("CER_UNDERLYING")) {
                    this.cer_underlying = getCurrentBoolean();
                }
                else if (tagName.equals("WNT_UNDERLYING")) {
                    this.wnt_underlying = getCurrentBoolean();
                }
                else if (tagName.equals("CER_UNDERLYING_DZBANK")) {
                    this.cer_underlying_dzbank = getCurrentBoolean();
                }
                else if (tagName.equals("CER_UNDERLYING_WGZBANK")) {
                    this.cer_underlying_wgzbank = getCurrentBoolean();
                }
                else if (tagName.equals("WNT_UNDERLYING_DZBANK")) {
                    this.wnt_underlying_dzbank = getCurrentBoolean();
                }
                else if (tagName.equals("CER_DZBANK")) {
                    this.cer_dzbank = getCurrentBoolean();
                }
                else if (tagName.equals("CER_WGZBANK")) {
                    this.cer_wgzbank = getCurrentBoolean();
                }
                else if (tagName.equals("WNT_DZBANK")) {
                    this.wnt_dzbank = getCurrentBoolean();
                }
                else if (tagName.equals("OPT_UNDERLYING")) {
                    this.opt_underlying = getCurrentBoolean();
                }
                else if (tagName.equals("FUT_UNDERLYING")) {
                    this.fut_underlying = getCurrentBoolean();
                }
                else if (tagName.equals("INDEXCONSTITUENT")) {
                    this.indexConstituent = getCurrentBoolean();
                }
                else {
                    notParsed(tagName);
                }
            } catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private void storeFields() {
            if (this.limitedRead) {
                this.limiter.ackAction();
            }

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.qid < 0 || !(this.estimatesReuters || this.convensys || this.screener || this.edg
                    || this.gisFndReport || this.gisCerReport || this.stockselectionFndReport
                    || this.stockselectionCerReport || this.ssatFndReport || this.factset
                    || this.vwdbenlFundamentalData || this.funddataMorningstar || this.funddataVwdBeNl
                    || this.cer_underlying || this.wnt_underlying
                    || this.cer_underlying_dzbank || this.cer_underlying_wgzbank || this.wnt_underlying_dzbank
                    || this.cer_dzbank || this.cer_wgzbank || this.wnt_dzbank
                    || this.opt_underlying || this.fut_underlying || this.indexConstituent)) {
                reset();
                return;
            }

            this.repository.add(this.qid, this.screener, this.convensys, this.estimatesReuters,
                    this.edg, this.gisFndReport, this.gisCerReport, this.stockselectionFndReport,
                    this.stockselectionCerReport, this.ssatFndReport, this.factset,
                    this.vwdbenlFundamentalData, this.funddataMorningstar, this.funddataVwdBeNl,
                    this.cer_underlying, this.wnt_underlying, this.cer_underlying_dzbank, this.cer_underlying_wgzbank,
                    this.wnt_underlying_dzbank, this.cer_dzbank, this.cer_wgzbank, this.wnt_dzbank,
                    this.opt_underlying, this.fut_underlying, this.indexConstituent);

            reset();
        }

        protected void reset() {
            this.qid = -1;
            this.estimatesReuters = false;
            this.convensys = false;
            this.screener = false;
            this.edg = false;
            this.gisFndReport = false;
            this.gisCerReport = false;
            this.stockselectionFndReport = false;
            this.stockselectionCerReport = false;
            this.ssatFndReport = false;
            this.factset = false;
            this.vwdbenlFundamentalData = false;
            this.funddataMorningstar = false;
            this.funddataVwdBeNl = false;
            this.cer_underlying = false;
            this.wnt_underlying = false;
            this.cer_underlying_dzbank = false;
            this.cer_underlying_wgzbank = false;
            this.wnt_underlying_dzbank = false;
            this.cer_dzbank = false;
            this.cer_wgzbank = false;
            this.wnt_dzbank = false;
            this.opt_underlying = false;
            this.fut_underlying = false;
            this.indexConstituent = false;
            this.errorOccured = false;
        }

        public Repository getRepository() {
            return this.repository;
        }
    }

    private final static class Repository {
        private final List<Long> qids = new ArrayList<>();

        private final BitSet screener = new BitSet();

        private final BitSet convensys = new BitSet();

        private final BitSet estimatesReuters = new BitSet();

        private final BitSet edg = new BitSet();

        private final BitSet gisFndReport = new BitSet();

        private final BitSet gisCerReport = new BitSet();

        private final BitSet stockselectionsFndReport = new BitSet();

        private final BitSet stockselectionsCerReport = new BitSet();

        private final BitSet ssatFndReport = new BitSet();

        private final BitSet factset = new BitSet();

        private final BitSet vwdbenlFundamentalData = new BitSet();

        private final BitSet funddataMorningstar = new BitSet();

        private final BitSet funddataVwdBeNl = new BitSet();

        private final BitSet cer_underlying = new BitSet();

        private final BitSet wnt_underlying = new BitSet();

        private final BitSet cer_underlying_dzbank = new BitSet();

        private final BitSet cer_underlying_wgzbank = new BitSet();

        private final BitSet wnt_underlying_dzbank = new BitSet();

        private final BitSet cer_dzbank = new BitSet();

        private final BitSet cer_wgzbank = new BitSet();

        private final BitSet wnt_dzbank = new BitSet();

        private final BitSet opt_underlying = new BitSet();

        private final BitSet fut_underlying = new BitSet();

        private final BitSet indexConstituent = new BitSet();

        public void add(long qid, boolean screener, boolean convensys, boolean estimatesReuters,
                boolean edg, boolean gisFndReport, boolean gisCerReport,
                boolean stockselectionFndReport, boolean stockselectionCerReport,
                boolean ssatFndReport, boolean factset, boolean vwdbenlFundamentalData,
                boolean funddataMorningstar, boolean funddataVwdBeNl, boolean cer_underlying,
                boolean wnt_underlying, boolean cer_underlying_dzbank,
                boolean cer_underlying_wgzbank,
                boolean wnt_underlying_dzbank, boolean cer_dzbank, boolean cer_wgzbank,
                boolean wnt_dzbank,
                boolean opt_underlying, boolean fut_underlying, boolean indexConstituent) {
            final int index = this.qids.size();
            this.qids.add(qid);
            this.screener.set(index, screener);
            this.convensys.set(index, convensys);
            this.estimatesReuters.set(index, estimatesReuters);
            this.edg.set(index, edg);
            this.gisFndReport.set(index, gisFndReport);
            this.gisCerReport.set(index, gisCerReport);
            this.stockselectionsFndReport.set(index, stockselectionFndReport);
            this.stockselectionsCerReport.set(index, stockselectionCerReport);
            this.ssatFndReport.set(index, ssatFndReport);
            this.factset.set(index, factset);
            this.vwdbenlFundamentalData.set(index, vwdbenlFundamentalData);
            this.funddataMorningstar.set(index, funddataMorningstar);
            this.funddataVwdBeNl.set(index, funddataVwdBeNl);
            this.cer_underlying.set(index, cer_underlying);
            this.wnt_underlying.set(index, wnt_underlying);
            this.cer_underlying_dzbank.set(index, cer_underlying_dzbank);
            this.cer_underlying_wgzbank.set(index, cer_underlying_wgzbank);
            this.wnt_underlying_dzbank.set(index, wnt_underlying_dzbank);
            this.cer_dzbank.set(index, cer_dzbank);
            this.cer_wgzbank.set(index, cer_wgzbank);
            this.wnt_dzbank.set(index, wnt_dzbank);
            this.opt_underlying.set(index, opt_underlying);
            this.fut_underlying.set(index, fut_underlying);
            this.indexConstituent.set(index, indexConstituent);
        }

        public QuoteMetadata getQuoteMetadata(long qid) {
            final int index = Collections.binarySearch(this.qids, qid);
            if (index < 0) {
                return NullQuoteMetadata.INSTANCE;
            }

            return new QuoteMetadataImpl(qid,
                    this.screener.get(index),
                    this.convensys.get(index),
                    this.estimatesReuters.get(index),
                    this.edg.get(index),
                    this.gisFndReport.get(index),
                    this.gisCerReport.get(index),
                    this.stockselectionsFndReport.get(index),
                    this.stockselectionsCerReport.get(index),
                    this.ssatFndReport.get(index),
                    this.factset.get(index),
                    this.vwdbenlFundamentalData.get(index),
                    this.funddataMorningstar.get(index),
                    this.funddataVwdBeNl.get(index),
                    this.cer_underlying.get(index),
                    this.wnt_underlying.get(index),
                    this.cer_underlying_dzbank.get(index),
                    this.cer_underlying_wgzbank.get(index),
                    this.wnt_underlying_dzbank.get(index),
                    this.cer_dzbank.get(index),
                    this.cer_wgzbank.get(index),
                    this.wnt_dzbank.get(index),
                    this.opt_underlying.get(index),
                    this.fut_underlying.get(index),
                    this.indexConstituent.get(index)
            );
        }

        public int size() {
            return this.qids.size();
        }
    }

    public static void main(String[] args) throws Exception {
        final QuoteMetadataProviderImpl p = new QuoteMetadataProviderImpl();
        p.setActiveMonitor(new ActiveMonitor());
        p.setFile(new File("d:/produktion/var/data/provider/istar-quote-metadata.xml.gz"));
        p.afterPropertiesSet();

        System.out.println("Daimler@FFM: " + p.getQuoteMetadata(25548));
        System.out.println("Daimler@ETR: " + p.getQuoteMetadata(312864));
    }
}
