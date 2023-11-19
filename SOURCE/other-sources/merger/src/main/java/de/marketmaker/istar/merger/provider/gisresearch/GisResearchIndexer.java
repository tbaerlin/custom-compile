/*
 * GisResearchIndexer.java
 *
 * Created on 07.04.14 12:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import de.marketmaker.istar.common.http.RestTemplateFactory;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.fusion.dmxml.DmxmlFacade;
import de.marketmaker.istar.fusion.dmxml.DmxmlRequest;
import de.marketmaker.istar.fusion.dmxml.HttpDmxmlFacade;
import de.marketmaker.istar.fusion.dmxml.InstrumentData;
import de.marketmaker.istar.fusion.dmxml.MSCStaticData;
import de.marketmaker.istar.fusion.dmxml.MSCStaticDataList;
import de.marketmaker.istar.fusion.dmxml.QuoteData;
import de.marketmaker.istar.merger.provider.ContentFlagsWriter;

import static de.marketmaker.istar.domain.instrument.ContentFlags.Flag.*;

/**
 * @author oflege
 */
public class GisResearchIndexer implements InitializingBean {

    static final String GIS_RESEARCH_DOCS = "gis-research-docs.obj";

    static class InstrumentInfo {

        private final String iid;

        private final String qid;

        String name;

        String sector;

        public InstrumentInfo(String iid, String qid) {
            this.iid = iid;
            this.qid = qid;
        }

        @Override
        public String toString() {
            return "InstrumentInfo{" + this.iid + ", " + this.qid +
                    ", name='" + name + '\'' +
                    ", sector='" + sector + '\'' +
                    '}';
        }
    }

    private static final FileFilter CONTROL_FILE_FILTER
            = f -> f.isFile() && f.getName().endsWith(".xml");

    private static final List<String> ENTITLEMENTS
            = Arrays.asList("HM1", "HM2", "HM3", "FP4");

    private static final ContentFlags.Flag[] FLAGS = new ContentFlags.Flag[]{
            ResearchDzHM1, ResearchDzHM2, ResearchDzHM3, ResearchDzFP4
    };

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private DmxmlFacade dmxmlFacade;

    private final Map<String, Set<ContentFlags.Flag>> contentFlags = new TreeMap<>();

    private final Map<String, InstrumentInfo> isinToInstrument = new HashMap<>();

    private File baseDir = new File(System.getProperty("user.home"),
            "produktion/var/data/gisresearch");

    private File current;

    private File archive;

    private File index;

    private File out;

    private String authentication = "mm-xml";

    private String authenticationType = "resource";

    /**
     * next time after now() at which a new research document has to be added or
     * an existing document has to be removed from the index
     */
    private DateTime nextEvent;

    private ContentFlagsWriter contentFlagsWriter = ContentFlagsWriter.NULL;

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public void setContentFlagsWriter(ContentFlagsWriter contentFlagsWriter) {
        this.contentFlagsWriter = contentFlagsWriter;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setDmxmlFacade(DmxmlFacade dmxmlFacade) {
        this.dmxmlFacade = dmxmlFacade;
    }

    // this is intended to run by a cronjob so updating the content flags on afterPropertiesSet is enough
    @Override
    public void afterPropertiesSet() throws Exception {
        this.current = new File(this.baseDir, "current");
        this.archive = new File(this.baseDir, "archive");
        this.out = new File(this.baseDir, "out");
        this.index = new File(this.out, "index");

        final Map<String, ControlFile> map = getCurrentFiles();
        this.logger.info("<afterPropertiesSet> #currentFiles = " + map.size());

        List<ControlFile> validFiles = removeInvalidFiles(map.values());
        this.logger.info("<afterPropertiesSet> #validFiles = " + validFiles.size());

        initInstrumentInfos(validFiles);

        createIndex(validFiles);

        writeDocuments(validFiles);
        writeReportForDocsWithUnknownType(validFiles);

        updateFlags(validFiles);
    }

    private void initInstrumentInfos(List<ControlFile> files) {
        List<String> isinList = new ArrayList<>(getUniqueIsins(files));

        this.logger.info("<initInstrumentInfos> for " + isinList.size() + " isins...");

        for (int i = 0; i < isinList.size(); i += 50) {
            doInitInstrumentInfos(isinList.subList(i, Math.min(isinList.size(), i + 50)));
        }

        if (this.logger.isDebugEnabled()) {
            for (String isin : new TreeSet<>(this.isinToInstrument.keySet())) {
                this.logger.debug(isin + " => " + this.isinToInstrument.get(isin));
            }
        }

        this.logger.info("<initInstrumentInfos> done.");
    }

    private DmxmlRequest.Builder getBuilder() {
        return DmxmlRequest.createBuilder().withAuth(authentication, authenticationType);
    }

    private void doInitInstrumentInfos(List<String> isins) {
        String[] symbols = isins.toArray(new String[isins.size()]);

        DmxmlRequest.Builder builder = getBuilder();

        DmxmlRequest.Builder.Block<MSCStaticDataList> staticData
                = builder.addBlock("MSC_StaticData_List", MSCStaticDataList.class)
                .with("symbol", symbols);

        DmxmlRequest request = builder.build();

        if (!this.dmxmlFacade.evaluate(request)) {
            throw new IllegalStateException();
        }
        if (staticData.getError() != null) {
            throw new IllegalStateException("got block error");
        }

        for (MSCStaticData e : staticData.getResult().getElement()) {
            InstrumentInfo info = (e != null) ? getInfo(e.getInstrumentdata(), e.getQuotedata()) : null;
            if (info != null) {
                info.name = e.getInstrumentdata().getName();
                info.sector = e.getSector();
            }
        }
    }

    private InstrumentInfo getInfo(InstrumentData id, QuoteData qd) {
        if (id == null || id.getIsin() == null) {
            return null;
        }
        return this.isinToInstrument.computeIfAbsent(id.getIsin(), key -> new InstrumentInfo(id.getIid(), qd.getQid()));
    }

    private List<String> getUniqueIsins(List<ControlFile> files) {
        return files.stream()
                .flatMap(cf -> cf.isins.stream())
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ArrayList::new));
    }

    private List<ControlFile> removeInvalidFiles(Collection<ControlFile> files) {
        DateTime now = DateTime.now();
        List<ControlFile> result = new ArrayList<>(files.size());
        for (ControlFile cf : files) {
            if (!isFileReadable(cf.getPdfFile())) {
                this.logger.error("<removeInvalidFiles> no .pdf file for: " + cf);
                moveToArchive(cf.researchId);
            }
            else if (!isFileReadable(cf.getTxtFile())) {
                this.logger.error("<removeInvalidFiles> no .txt file for: " + cf);
                moveToArchive(cf.researchId);
            }
            else if (!cf.getPdfFile().getName().equals(cf.name)) {
                this.logger.error("<removeInvalidFiles> references unknown pdf: " + cf);
                moveToArchive(cf.researchId);
            }
            else if (cf.end.isBefore(now)) {
                this.logger.info("<removeInvalidFiles> no longer valid: " + cf);
                moveToArchive(cf.researchId);
            }
            else if (cf.start.isAfterNow()) {
                this.logger.info("<removeInvalidFiles> not yet valid: " + cf);
                updateNextEvent(cf.start);
            }
            else {
                result.add(cf);
            }
        }
        return result;
    }

    private void createIndex(List<ControlFile> files) throws IOException {
        this.logger.info("<createIndex> add documents...");

        IndexWriter iw = new IndexWriter(FSDirectory.open(this.index), Research2Document.ANALYZER,
                true, IndexWriter.MaxFieldLength.UNLIMITED);

        final Research2Document r2d = new Research2Document(this.isinToInstrument);

        for (ControlFile cf : files) {
            String text = PlainTextSupport.getText(cf.getTxtFile());
            iw.addDocument(r2d.toDocument(cf, text));
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<createIndex> added " + cf);
            }
        }

        this.logger.info("<createIndex> optimize...");
        iw.optimize();

        iw.close();

        this.logger.info("<createIndex> done.");
    }

    private void writeReportForDocsWithUnknownType(
            List<ControlFile> validFiles) throws IOException {
        final File f = new File(this.out, "unknown_resourceIds.txt");
        backupLastReport(f);

        final List<ControlFile> others = getOtherFiles(validFiles);
        if (others.isEmpty()) {
            this.logger.info("<writeReportForDocsWithUnknownType> found none");
        }

        // sort so that file contents are identical for the same input set.
        others.sort(null);

        try (PrintWriter pw = new PrintWriter(f)) {
            for (ControlFile cf : others) {
                pw.append(cf.resourceId).append(" - ").println(cf.file.getName());
            }
        }
        this.logger.info("<writeReportForDocsWithUnknownType> wrote " + others.size());
    }

    private void backupLastReport(File f) {
        if (f.exists()) {
            FileUtil.backupFile(f, ".last");
        }
        else {
            final File last = new File(f.getParentFile(), f.getName() + ".last");
            if (last.exists() && !last.delete()) {
                this.logger.warn("<backupLastReport> failed to delete " + last.getAbsolutePath());
            }
        }
    }

    private List<ControlFile> getOtherFiles(List<ControlFile> validFiles) {
        return validFiles.stream()
                .filter(cf -> cf.documentType == DocumentType.OTHER)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void writeDocuments(List<ControlFile> validFiles) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(this.out, GIS_RESEARCH_DOCS)))) {
            oos.writeInt(validFiles.size());
            for (ControlFile cf : validFiles) {
                GisResearchDoc doc = new GisResearchDoc(cf, getSector(cf));
                oos.writeObject(doc);
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<writeDocuments> wrote " + doc);
                }
            }
        }
        this.logger.info("<writeDocuments> wrote " + validFiles.size() + " docs");
    }

    private InstrumentInfo getPrimaryInfo(ControlFile cf) {
        if (cf == null || cf.isins.isEmpty()) {
            return null;
        }
        return this.isinToInstrument.get(cf.isins.get(0));
    }

    private String getSector(ControlFile cf) {
        final InstrumentInfo info = getPrimaryInfo(cf);
        return (info != null) ? info.sector : null;
    }

    private void updateFlags(List<ControlFile> validFiles) throws IOException {
        initFlags(validFiles);
        this.contentFlagsWriter.writeContentFlags(this.contentFlags);
    }

    private void initFlags(List<ControlFile> files) {
        this.logger.info("<initFlags> ...");
        for (ControlFile cf : files) {
            updateAvailability(toEnumSet(cf.productEntitlements), cf.issuerNumbers());
        }
        this.logger.info("<initFlags> done.");
    }

    private boolean isFileReadable(File f) {
        if (!f.canRead()) {
            this.logger.warn("<isFileReadable> cannot read " + f.getAbsolutePath());
            return false;
        }
        return true;
    }

    private EnumSet<ContentFlags.Flag> toEnumSet(List<String> entitlements) {
        return entitlements.stream()
                .map(ENTITLEMENTS::indexOf)
                .filter(i -> i > 0)
                .map(i -> FLAGS[i])
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ContentFlags.Flag.class)));
    }

    private void updateAvailability(EnumSet<ContentFlags.Flag> permissions, List<String> issuerNumbers) {
        for (String issuerNumber : issuerNumbers) {
            if (!issuerNumber.matches("[0-9]+")) {
                continue;
            }
            this.contentFlags.computeIfAbsent(issuerNumber, k -> EnumSet.noneOf(ContentFlags.Flag.class))
                    .addAll(permissions);
        }
    }

    private void updateNextEvent(DateTime dt) {
        if (this.nextEvent == null || dt.isBefore(this.nextEvent)) {
            this.nextEvent = dt;
        }
    }

    private Map<String, ControlFile> getCurrentFiles() {
        Map<String, ControlFile> map = new HashMap<>();
        List<ControlFile> deletes = new ArrayList<>();

        File[] files = current.listFiles(CONTROL_FILE_FILTER);
        for (File f : files) {
            try {
                ControlFile cf = ControlFile.create(f);
                if (Boolean.TRUE == cf.statusTypeDeleted) {
                    deletes.add(cf);
                }
                else {
                    map.put(cf.researchId, cf);
                }
            } catch (Exception e) {
                this.logger.error("<getCurrentFiles> failed for " + f.getName(), e);
                throw new Error("getCurrentFiles failed", e);
            }
        }

        for (ControlFile delete : deletes) {
            if (delete.start.isAfterNow()) {
                this.logger.info("<getCurrentFiles> delete not yet valid: " + delete);
                updateNextEvent(delete.start);
                continue;
            }
            final ControlFile cf = map.remove(delete.researchId);
            if (cf == null) {
                this.logger.warn("<getCurrentFiles> delete for unknown document " + delete.researchId);
            }
            moveToArchive(delete.researchId);
        }
        return map;
    }

    private void moveToArchive(final String version) {
        final File[] files = this.current.listFiles(f -> f.isFile() && f.getName().contains(version));
        for (File src : files) {
            moveToArchive(src);
        }
    }

    private void moveToArchive(File src) {
        if (src.renameTo(new File(this.archive, src.getName()))) {
            this.logger.info("<moveToArchive> " + src.getName());
        }
        else {
            this.logger.error("<moveToArchive> failed for " + src.getName());
        }
    }

    public static void main(String[] args) throws Exception {
        new AnnotationConfigApplicationContext(AppConfig.class).close();
    }

    @Configuration
    static class AppConfig {
        @Autowired
        Environment env;

        @Bean
        public static PropertySourcesPlaceholderConfigurer ppc() {
            PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
            ppc.setLocation(new ClassPathResource("GisResearchIndexer.properties"));
            ppc.setIgnoreResourceNotFound(true);
            return ppc;
        }

        @Bean
        RestTemplateFactory restTemplate() {
            return new RestTemplateFactory();
        }

        @Bean
        HttpDmxmlFacade dmxmlFacade() {
            HttpDmxmlFacade df = new HttpDmxmlFacade();
            String host = env.getProperty("dmxmlHost", "gis-test.vwd.com");
            String path = env.getProperty("dmxmlUrl", "/dmxml-1/iview/retrieve.xml");
            df.setRetrieveUrl("http://" + host + path);
            df.setRestTemplate(restTemplate().getObject());
            return df;
        }

        @Bean
        GisResearchIndexer indexer() {
            GisResearchIndexer gri = new GisResearchIndexer();
            gri.setDmxmlFacade(dmxmlFacade());
            return gri;
        }
    }
}


