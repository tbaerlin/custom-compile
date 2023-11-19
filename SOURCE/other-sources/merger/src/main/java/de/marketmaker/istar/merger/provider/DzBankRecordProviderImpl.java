package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.special.DzBankRecord;
import de.marketmaker.istar.domain.special.DzBankRecordImpl;
import de.marketmaker.istar.instrument.Controller;
import de.marketmaker.istar.instrument.InstrumentRequest;
import de.marketmaker.istar.instrument.InstrumentResponse;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.instrument.InstrumentServerImpl;
import de.marketmaker.istar.merger.query.BeanCriterion;
import de.marketmaker.istar.merger.query.BeanSearchTermVisitor;
import de.marketmaker.istar.merger.query.BeanSorter;
import de.marketmaker.istar.merger.query.DistinctValueCounter;
import de.marketmaker.istar.merger.query.DistinctValueCounterImpl;
import de.marketmaker.istar.merger.query.UnderlyingCounter;
import de.marketmaker.istar.merger.web.easytrade.ListInfo;
import de.marketmaker.istar.merger.web.finder.Term;

import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created on 19.10.12 10:15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 * @author Michael Wohlfart
 */

public class DzBankRecordProviderImpl implements DzBankRecordProvider, InitializingBean {

    private static final int INSTRUMENT_CHUNK_SIZE = 50;

    private static class Reader extends IstarMdpExportReader<List<DzBankRecord>> {
        final List<DzBankRecord> result = new ArrayList<>();

        @Override
        protected void handleRow() {
            final Long iid = getLong("IID");
            final String wkn = get("WKN");
            final Integer sort = getInt("SORT");
            final String pibUrl = get("PIBURL");
            final Integer risikoklasse = getInt("RISIKOKLASSE");
            final Integer bonifikation = getInt("BONIFIKATION");
            final String bonifikationstyp = get("BONIFIKATIONSTYP");
            final BigDecimal bonibrief = getBigDecimal("BONIBRIEF");
            final String hinweise = get("HINWEISE");
            final String topargument = get("TOPARGUMENT");
            final String sonderheit = get("SONDERHEIT");
            final boolean isTopProdukt = getBoolean("ISTOPPRODUKT");
            final boolean isKapitalmarktFavorit = getBoolean("ISKAPITALMARKTFAVORIT");
            final BigDecimal rendite = getBigDecimal("RENDITE");
            final String bezeichnung = get("BEZEICHNUNG");
            final String offertenkategorie = get("OFFERTENKATEGORIE");
            final BigDecimal coupon = getBigDecimal("COUPON");
            final DateTime referencedate = getDateTime("REFERENCEDATE");
            final String issuerName = get("ISSUERNAME");
            final String typeKey = get("TYPEKEY");
            final String type = get("TYPE");

            final DzBankRecordImpl pbr = new DzBankRecordImpl(iid, wkn, sort, pibUrl, risikoklasse,
                    bonifikation, bonibrief, hinweise, topargument, sonderheit,
                    referencedate != null ? referencedate.toLocalDate() : null,
                    bonifikationstyp, isTopProdukt, isKapitalmarktFavorit,
                    rendite, bezeichnung, offertenkategorie, coupon,
                    issuerName, typeKey, type);
            result.add(pbr);
        }

        @Override
        public List<DzBankRecord> getResult() {
            return result;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GuardedBy("dataMutex")
    private List<DzBankRecord> dzdata = new ArrayList<>();

    private final Object dataMutex = new Object();

    private ActiveMonitor activeMonitor;

    private File dzFile;

    private InstrumentServer instrumentServer;

    public void setDzFile(File dzFile) {
        this.dzFile = dzFile;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setInstrumentServer(InstrumentServer instrumentServer) {
        this.instrumentServer = instrumentServer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        doReadDzData();

        if (this.activeMonitor != null) {
            final FileResource resource = new FileResource(this.dzFile);
            resource.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    readDzData();
                }
            });

            this.activeMonitor.addResource(resource);
        }
    }

    public List<DzBankRecord> getDzBankRecords(List<Long> iids) {
        final List<DzBankRecord> result = new ArrayList<>(iids.size());
        synchronized (dataMutex) {
            for (DzBankRecord data : dzdata) {
                if (iids.contains(data.getIid())) {
                    result.add(data);
                }
            }
        }
        return result;
    }

    @Override
    public DzBankRecordMetaDataResponse createDzBankRecordsMetadata() {
        final Set<String> fields = BeanSorter.getSortableFields(DzBankRecord.class);
        final List<DzBankRecord> allRecords = getRecords();
        return new DzBankRecordMetaDataResponse(allRecords.size(), fields,
                createMetaData(allRecords));
    }

    @Override
    public DzBankRecordSearchResponse searchDzBankRecords(IstarQueryListRequest req) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<search> " + req);
        }
        final TimeTaker tt = new TimeTaker();
        try {
            return searchIntern(req);
        } finally {
            this.logger.debug("<search> took: " + tt);
        }
    }

    private DzBankRecordSearchResponse searchIntern(IstarQueryListRequest req) {
        final List<DzBankRecord> records = getFilteredRecords(req);

        final BeanSorter<DzBankRecord> sorter = new BeanSorter<>(DzBankRecord.class,
                req.getSortBy() + " " + (req.isAscending() ? BeanSorter.ASC : BeanSorter.DESC));
        records.sort(sorter);

        final int totalCount = records.size();
        final int requestedCount = req.getCount();
        final int offset = req.getOffset();
        final List<DzBankRecord> resultList;
        if (offset < totalCount) {
            resultList = new ArrayList<>(records.subList(offset, Math.min(offset + requestedCount, totalCount)));
        }
        else {
            resultList = Collections.emptyList();
        }

        final ListInfo listInfo = new ListInfo(offset, resultList.size(), totalCount, requestedCount,
                sorter.getSortableFields(), req.isAscending(), req.getSortBy());

        final Set<? extends DistinctValueCounter> metadataSet = createMetaData(records);
        return new DzBankRecordSearchResponse(resultList, listInfo, metadataSet);
    }

    Set<? extends DistinctValueCounter> createMetaData(List<DzBankRecord> records) {
        final HashSet<DistinctValueCounter> counterSet = new HashSet<>();
        try {
            counterSet.add(new DistinctValueCounterImpl<DzBankRecord, Integer>("bonifikation").countNonNullValues(records));
            counterSet.add(new DistinctValueCounterImpl<DzBankRecord, Integer>("risikoklasse").countNonNullValues(records));
            counterSet.add(new DistinctValueCounterImpl<DzBankRecord, Float>("bonibrief").countNonNullValues(records));
            counterSet.add(new DistinctValueCounterImpl<DzBankRecord, String>("bonifikationstyp").countNonNullValues(records));
            counterSet.add(new DistinctValueCounterImpl<DzBankRecord, String>("offertenkategorie").countNonNullValues(records));
            counterSet.add(new DistinctValueCounterImpl<DzBankRecord, String>("type").countNonNullValues(records));
            counterSet.add(new DistinctValueCounterImpl<DzBankRecord, String>("typeKey").countNonNullValues(records));
            counterSet.add(new UnderlyingCounter().countUnderlyings(instrumentServer, records));
        } catch (InvocationTargetException | IllegalAccessException ex) {
            this.logger.error("<createMetaData> exception while calculating metadata, they are very likely incorrect", ex);
        }
        return counterSet;
    }

    private List<DzBankRecord> getFilteredRecords(IstarQueryListRequest req) {
        final List<DzBankRecord> allRecords = getRecords();

        final BeanCriterion filter = getFilter(req.getQueryTerm());

        final List<DzBankRecord> result = new ArrayList<>();
        for (DzBankRecord record : allRecords) {
            if (filter.evaluate(record)) {
                result.add(record);
            }
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getFilteredRecords> all: " + allRecords.size() + ", filtered: " + result.size());
        }
        return result;
    }

    private BeanCriterion getFilter(Term term) {
        if (term == null) {
            return BeanCriterion.True;
        }
        else {
            final BeanSearchTermVisitor visitor = new BeanSearchTermVisitor();
            term.accept(visitor);
            return visitor.getCriterion();
        }
    }

    private void readDzData() {
        try {
            doReadDzData();
        } catch (Exception e) {
            this.logger.error("<readDzData> failed", e);
        }
    }

    void doReadDzData() throws Exception {
        final Reader reader = new Reader();
        reader.read(this.dzFile);
        final List<DzBankRecord> newMap = reader.getResult();
        this.logger.warn("<readDzData> #records: " + newMap.size());
        assignInstruments(newMap);

        newMap.sort(new DzBankRecordSorter());
        for (int i = 0; i < newMap.size(); i++) {
            ((DzBankRecordImpl) newMap.get(i)).setIndexPosition(i);
        }

        synchronized (dataMutex) {
            this.dzdata = newMap;
        }
    }

    private void assignInstruments(List<DzBankRecord> newMap) {
        for (int i = 0; i < newMap.size(); i += INSTRUMENT_CHUNK_SIZE) {
            final List<DzBankRecord> chunk = newMap.subList(i, Math.min(newMap.size(), i + INSTRUMENT_CHUNK_SIZE));
            final InstrumentRequest request = new InstrumentRequest();
            for (DzBankRecord record : chunk) {
                final Long iid = record.getIid();
                if (iid != null) {
                    request.addItem(Long.toString(iid), InstrumentRequest.KeyType.IID);
                }
            }
            final InstrumentResponse response = this.instrumentServer.identify(request);
            final List<Instrument> instruments = response.getInstruments();
            for (int j = 0; j < instruments.size(); j++) {
                final DzBankRecordImpl record = (DzBankRecordImpl) newMap.get(i + j);
                if (record.getIid() != null) {
                    record.setInstrument(instruments.get(j));
                }
            }
        }
    }

    protected List<DzBankRecord> getRecords() {
        synchronized (dataMutex) {
            return new ArrayList<>(dzdata);
        }
    }


    public static class DzBankRecordSorter implements Comparator<DzBankRecord> {
        private static final String AKTIENANLEIHEN = "Aktienanleihen";

        private static final String RENTEN = "Renten";

        private static final String ZERTIFIKATE = "Zertifikate";

        private static final String CERT_BONUS = "CERT_BONUS";

        private static final String CERT_DISCOUNT = "CERT_DISCOUNT";

        // to the right in the array means to the top in the final ordering ---->
        List<String> topProductOrdering = Arrays.asList(RENTEN, ZERTIFIKATE, AKTIENANLEIHEN);

        List<String> bottomProductOrdering = Arrays.asList(ZERTIFIKATE, RENTEN, AKTIENANLEIHEN);

        List<String> certificateOrdering = Arrays.asList(CERT_BONUS, CERT_DISCOUNT);

        @Override
        public int compare(DzBankRecord left, DzBankRecord right) {
            int result;

            // top products first
            result = Boolean.compare(right.getTopProdukt(), left.getTopProdukt());
            if (result != 0) {
                return result;
            }
            // at this point we have the same top product flag in left and right

            // sort by offertenkategorie while depending on the state of topProduct
            if (left.getTopProdukt()) {
                result = Integer.compare(
                        topProductOrdering.indexOf(right.getOffertenkategorie()),
                        topProductOrdering.indexOf(left.getOffertenkategorie()));
            }
            else {
                result = Integer.compare(
                        bottomProductOrdering.indexOf(right.getOffertenkategorie()),
                        bottomProductOrdering.indexOf(left.getOffertenkategorie()));
            }
            if (result != 0) {
                return result;
            }
            // at this point we have the same Offertenkategorie in left and right

            // sorting Renten/Anleihen according to issuer starting with DZ
            if (RENTEN.equalsIgnoreCase(left.getOffertenkategorie())) {
                String issuerLeft = left.getIssuerName();
                issuerLeft = issuerLeft == null ? "" : issuerLeft;
                String issuerRight = right.getIssuerName();
                issuerRight = issuerRight == null ? "" : issuerRight;
                result = Boolean.compare(
                        issuerRight.startsWith("DZ "),
                        issuerLeft.startsWith("DZ "));
            }
            if (result != 0) {
                return result;
            }
            // at this point we have similar IssuerName

            // sorting Zertifikate according to discount, bonus. etc.
            if (ZERTIFIKATE.equalsIgnoreCase(left.getOffertenkategorie())) {
                result = Integer.compare(
                        certificateOrdering.indexOf(right.getTypeKey()),
                        certificateOrdering.indexOf(left.getTypeKey()));
            }
            if (result != 0) {
                return result;
            }

            // include the backend provided sorting
            final Integer sortLeft = left.getSort();
            final Integer sortRight = right.getSort();
            if (sortLeft != null || sortRight != null) {     // at least one != null, never both null
                if (sortLeft == null || sortRight == null) {
                    result = (sortLeft == null) ? 1 : -1;
                }
                else {
                    result = Integer.compare(sortLeft, sortRight);
                }
            }

            if (result != 0) {
                return result;
            }

            // Fallback: sort by Bezeichnung, (at least try to be consistent with equals)
            String val1 = left.getBezeichnung();
            String val2 = right.getBezeichnung();
            val1 = val1 == null ? "" : val1;
            val2 = val2 == null ? "" : val2;
            return val1.compareTo(val2);
        }
    }

    public static void main(String[] args) throws Exception {
        final DzBankRecordProviderImpl dzBankRecordProvider = new DzBankRecordProviderImpl();
        dzBankRecordProvider.setDzFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-dzbank-staticdata.xml.gz"));

        final InstrumentServerImpl impl = new InstrumentServerImpl();

        final Controller controller = new Controller();
        controller.setBaseDir(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/instrument"));
        controller.setInstrumentServer(impl);
        controller.initialize();

        dzBankRecordProvider.setInstrumentServer(impl);
        dzBankRecordProvider.afterPropertiesSet();

        // for testing serialization
        // SerializationUtils.serialize(p.createDzBankRecordsMetadata());

        List<Long> iids = new ArrayList<>();
        iids.add(160394862L);
        iids.add(157848128L);
        iids.add(157472699L);

        final List<DzBankRecord> recs = dzBankRecordProvider.getDzBankRecords(iids);
        for (DzBankRecord rec : recs) {
            System.out.println("-------------------------------------------");
            System.out.println("iid: " + rec.getIid());
            System.out.println("wkn: " + rec.getWkn());
            System.out.println("sort: " + rec.getSort());
            System.out.println("bonibrief: " + rec.getBonibrief());
            System.out.println("bonifikationstyp: " + rec.getBonifikationstyp());
            System.out.println("hinweis: " + rec.getHinweise());
            System.out.println("sonderheit: " + rec.getSonderheit());
            System.out.println("bonifikation: " + rec.getBonifikation());
            System.out.println("risikoklasse: " + rec.getRisikoklasse());
            System.out.println("expiration: " + rec.getExpiration());
            System.out.println("bonifikationstyp: " + rec.getBonifikationstyp());
            System.out.println("topProdukt: " + rec.getTopProdukt());
            System.out.println("tadable: " + rec.getTradable());
            System.out.println("kapitalmarktFavorit: " + rec.getKapitalmarktFavorit());
            System.out.println("rendite: " + rec.getRendite());
            System.out.println("bezeichnung: " + rec.getBezeichnung());
            System.out.println("offertenkategorie: " + rec.getOffertenkategorie());
            System.out.println("coupon: " + rec.getCoupon());
            System.out.println("indexPosition: " + rec.getIndexPosition());
            System.out.println("underlyingIid: " + rec.getUnderlyingIid());
            System.out.println("issuerName: " + rec.getIssuerName());
            System.out.println("typeKey: " + rec.getTypeKey());
            System.out.println("type: " + rec.getType());
            System.out.println("topArgument: " + rec.getTopArgument());
        }

        final DzBankRecordSearchResponse x = dzBankRecordProvider.searchDzBankRecords(
                new IstarQueryListRequest(0, 10, "iid", true, null, null));
        SerializationUtils.serialize(x);

        // check for null values
        List<DzBankRecord> records = dzBankRecordProvider.getRecords();
        int nullCount = 0;
        int noPdfCount = 0;
        for (DzBankRecord record : records) {
            if (record.getIid() == null) {
                nullCount++;
            }
            if (StringUtils.isEmpty(record.getPibUrl())) {
                noPdfCount++;
            }
        }
        System.out.println("nullCount is: " + nullCount);
        System.out.println("noPdfCount is: " + noPdfCount);
    }
}
