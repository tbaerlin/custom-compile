package de.marketmaker.istar.merger.provider.pages;

/**
 * User: swild
 * Created on 13.07.2010 14:39:04
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.io.output.NullWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.feed.pages.PageDao;
import de.marketmaker.istar.feed.pages.PageData;

/**
 * This class provides method {@link }
 * @author Sebastian Wild
 */
public class PageIndexer implements InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The data access object used to retrieve the page-data to index.
     */
    private PageDao dao;

    /**
     * The index directory to store the index into
     */
    private Directory indexDirectory;

    /**
     * If not-null, a call to {@link #index()} will create a zip archive of name {@code zipFile}
     * containing the newly created index.
     */
    private File zipFile;

    /**
     * maximal read speed for zipping of index files, in bytes per second. <br/>
     * -1 means unlimited.
     */
    private long maxBps = -1;

    private PrintWriter invalidRefsWriter = new PrintWriter(new NullWriter());

    private BitSet ids = new BitSet();

    private final BitSet existingPages = new BitSet();

    private final BitSet referencedPages = new BitSet();

    private IndexWriter indexWriter;

    private final PdlPageFactory pageFactory = new PdlPageFactory();

    private int dynamicPages;
    private int staticPages;
    private int dualLanguage;


    public void setDao(PageDao dao) {
        this.dao = dao;
    }

    public void setIndexDirectory(Directory indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public void setMaxBps(long maxBps) {
        this.maxBps = maxBps;
    }

    public void setInvalidRefsCsvFile(File invalidRefsCsvFile) {
        try {
            this.invalidRefsWriter = new PrintWriter(invalidRefsCsvFile);
            this.invalidRefsWriter.println("source page number;invalid target page number");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an index of all pages accessible via the
     * {@link de.marketmaker.istar.feed.pages.PageDbDao} object stored internally.
     * <b>You have to inject this object into <tt>this</tt> before calling this method!</b>
     * <br />
     * The index is saved in the given {@code indexDirectory}.
     * @throws IOException if some error occurs while writing to {@code indexDirectory}
     */
    public void index() throws IOException {
        this.logger.info("<index> Starting index creation into directory " + indexDirectory + ".");
        for (Integer id : dao.getPagenumbers()) {
            this.ids.set(id);
        }
        this.logger.info("<index> Found " + ids.cardinality() + " page ids");

        staticPages = 0;
        dynamicPages = 0;
        dualLanguage = 0;
        this.dao.getAllPages(this::index, null);
        this.logger.info("<index> staticPages " + staticPages + " (ignored)");
        this.logger.info("<index> dynamicPages " + dynamicPages);
        this.logger.info("<index> dual language pages " + dualLanguage);
    }


    private void index(PageData page) {
        if (!page.isDynamic()) {
            staticPages ++;
            return;
        } else {
            dynamicPages ++;
        }
        this.existingPages.set(page.getId());
        if (dynamicPages % 10_000 == 0) {
            this.logger.info("<index> " + dynamicPages + " dynamicPages processed");
        }
        if (StringUtils.isEmpty(page.getText()) && StringUtils.isEmpty(page.getTextg())) {
            this.logger.warn("<index> Page " + page.getId() + " does not contain any text");
            return;
        }
        final List<Boolean> languages = new ArrayList<>(2);
        if (!StringUtils.isEmpty(page.getText())) {
            languages.add(false);
        }
        if (!StringUtils.isEmpty(page.getTextg())) {
            languages.add(true);
        }
        if (!StringUtils.isEmpty(page.getTextg()) && !StringUtils.isEmpty(page.getTextg())) {
            dualLanguage++;
        }

        for (boolean useTextG : languages) {
            final String content = useTextG ? page.getTextg() : page.getText();
            final PdlPage parsedPage = pageFactory.createPage(String.valueOf(page.getId()), content);

            Document doc = DocumentFactory.createDocument(page, useTextG, parsedPage);
            try {
                this.indexWriter.addDocument(doc);
            } catch (IOException e) {
                this.logger.error("<index> failed for " + page, e);
                continue;
            }

            handleReferences(page.getId(), parsedPage);
        }
    }

    private void handleReferences(int id, PdlPage page) {
        for (PdlObject object : page.getObjects()) {
            if (object.getType() == PdlObject.TYPE_PAGEPOINTER) {
                String value = object.getContent().trim();
                try {
                    final Integer reference = Integer.parseInt(value);
                    if (this.ids.get(reference)) {
                        // value ref, add to set
                        this.referencedPages.set(reference);
                    }
                    else {
                        this.invalidRefsWriter.println(id + ";" + reference);
                    }
                } catch (NumberFormatException e) {
                    this.invalidRefsWriter.println(id + ";" + value);
                }
            }
        }
    }

    /**
     * This method is used in method {@link #index()}, iff
     * {@code this.zipFile != null}. It stores all files in the index-directory
     * into a zip archive of name {@link #zipFile}. <br/>
     * Note that not all implementations of {@link org.apache.lucene.store.Directory} allow to list
     * files, therefore this method <em>only</em> works, if {@link #indexDirectory} is an
     * instance of {@link org.apache.lucene.store.FSDirectory}.
     */
    private void storeIndexInZip() {
        if (this.indexDirectory instanceof FSDirectory) {
            final FSDirectory directory = (FSDirectory) this.indexDirectory;
            final File indexDir = directory.getFile();
            // treat all files not ending with .zip as index files.
            File[] files = indexDir.listFiles((dir, name) -> !name.endsWith(".zip"));
            try {
                FileUtil.zipFiles(Arrays.asList(files), this.zipFile, indexDir, this.maxBps);
            } catch (IOException e) {
                this.logger.error("<storeIndexInZip> Could not create ZIP archive of index!", e);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.dao == null) {
            throw new IllegalStateException("First inject a PageDbDao object into this via setDao!");
        }

        final Analyzer analyzer = new DefaultAnalyzer();
        this.indexWriter = new IndexWriter(this.indexDirectory,
                analyzer, true /* force re-creation from scratch */,
                IndexWriter.MaxFieldLength.UNLIMITED);
        index();
    }

    @Override
    public void destroy() throws Exception {
        this.invalidRefsWriter.close();

        this.logger.info("<destroy> optimize...");
        this.indexWriter.optimize();
        this.logger.info("<destroy> optimized index with " + this.indexWriter.maxDoc() + " docs");

        this.indexWriter.close();

        if (this.zipFile != null) {
            storeIndexInZip();
        }
    }
}
