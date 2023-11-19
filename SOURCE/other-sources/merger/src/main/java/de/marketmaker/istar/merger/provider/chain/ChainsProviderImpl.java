package de.marketmaker.istar.merger.provider.chain;


import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.jcip.annotations.GuardedBy;
import org.antlr.runtime.RecognitionException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;

@ManagedResource
public class ChainsProviderImpl implements InitializingBean, ChainsProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @GuardedBy("dataMutex")
    private Map<String, ChainData> currentData = new HashMap<>();

    private final ChainIndex chainIndex = new ChainIndex();

    private final Object dataMutex = new Object();

    private ActiveMonitor activeMonitor;

    private File dataFile = new File(System.getProperty("user.home"),
            "produktion/var/data/provider/istar-chaining.xml.gz");

    public void dumpIndex() throws IOException {
        chainIndex.dumpIndexContent();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        doReadData();

        if (this.activeMonitor != null) {
            final FileResource fileResource = new FileResource(this.dataFile);
            fileResource.addPropertyChangeListener(evt -> readData());
            this.activeMonitor.addResource(fileResource);
        }
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setFile(File file) {
        this.dataFile = file;
    }

    @Override
    public ChainsResponse searchChainElements(ChainsRequest request) {
        try {
            // return new ChainsResponse(getBeanFilteredElements(request.getQuery()));
            Map.Entry<Integer, List<ChainData>> result = getLuceneFilteredElements(
                    request.getQuery(),
                    request.getOffset(),
                    request.getCount());
            return new ChainsResponse(result.getValue(), result.getKey());
        } catch (RecognitionException | IOException ex) {
            logger.warn("<searchChainElements> exception, return empty result", ex);
            return new ChainsResponse(Collections.EMPTY_LIST, 0);
        }
    }

    private Map<String, ChainData> getRecords() {
        return currentData;
    }

    @ManagedOperation
    public void readData() {
        try {
            doReadData();
        } catch (Exception e) {
            this.logger.error("<readData> failed", e);
        }
    }

    private void doReadData() throws Exception {
        final ChainsReader reader = new ChainsReader();
        final TimeTaker tt = new TimeTaker();
        this.logger.info("<doReadData> reading " + this.dataFile + ", length: " + this.dataFile.length() + ", lastModified: " + new DateTime(this.dataFile.lastModified()));
        reader.read(this.dataFile);
        this.logger.info("<doReadData> did read " + this.dataFile + " took " + tt);
        final Map<String, ChainData> newData = reader.getResult();
        this.logger.info("<doReadData> #chains: " + newData.size());

        synchronized (dataMutex) {
            this.chainIndex.reset(newData.values());
            this.currentData = newData;
        }
    }

    // a hack to return 2 values
    private Map.Entry<Integer, List<ChainData>> getLuceneFilteredElements(String queryString,
            long skip, long limit) throws RecognitionException, IOException {
        Query query = getLuceneFilter(Query2Term.toTerm(queryString));
        List<String> ids = chainIndex.find(query);
        List<ChainData> list = ids.stream()
                .skip(skip)
                .limit(limit)
                .map(id -> currentData.get(id))
                .collect(Collectors.toList());
        return new AbstractMap.SimpleEntry<>(ids.size(), list);
    }

    private Query getLuceneFilter(Term term) {
        final ChainTermVisitor visitor = new ChainTermVisitor();
        term.accept(visitor);
        return visitor.getResult();
    }

    public static void main(String[] args) throws Exception {
        final ChainsProviderImpl provider = new ChainsProviderImpl();
        provider.setActiveMonitor(new ActiveMonitor());
        provider.setFile(new File("/home/mwohlf/produktion/var/data/provider/istar-chaining.xml.gz"));
        emitUsedMem();
        provider.afterPropertiesSet();
        emitUsedMem();

        String[] filters = {
                "name =~ 'AUDUSD*'",
                "name = 'AUDUSD+ FORWARD CURVE'",
                "name = 'AUDUSD+?FORWARD?CURVE'",
                "chainInstrument = '#IRSEUR.DZF'",
                "vwdSymbol = '10.IRSEUR.DZF.2Y6M'",
                "qid = '228995665'",
                "vwdSymbol = '10.AUDUSD.DREBA.SPOT'",
        };

        Arrays.asList(filters).stream().forEach(s -> {
            try {
                System.out.println("search: " + s + ": ");
                Map.Entry<Integer, List<ChainData>> result = provider.getLuceneFilteredElements(s, 0, 1_000);
                result.getValue().forEach(e -> System.out.println("   " + e));
                System.out.println("   --");
            } catch (RecognitionException | IOException ex) {
                ex.printStackTrace();
            }
        });

    }

    private static final double MB = 1024d * 1024d;

    private static void emitUsedMem() {
        final Runtime runtime = Runtime.getRuntime();
        final long used = runtime.totalMemory() - runtime.freeMemory();
        final double usedMB = used / MB;
        System.out.println("Used Mem: " + usedMB + " M (" + new Date() + ")" );
    }

}
