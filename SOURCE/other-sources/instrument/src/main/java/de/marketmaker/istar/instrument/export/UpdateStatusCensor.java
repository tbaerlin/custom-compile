/*
 * InstrumentCensorLucene.java
 *
 * Created on 03.02.2011 10:39:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.util.IoUtils;

/**
 * @author zzhao
 */
class UpdateStatusCensor {

    static final String FIELD_IID = "iid";

    static final String FIELD_HASH = "md5";

    static final FieldSelector HASH_SELECTOR = fieldName -> FIELD_HASH.equals(fieldName) ?
            FieldSelectorResult.LOAD : FieldSelectorResult.NO_LOAD;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MessageDigest hasher;

    private boolean censorMode;

    private IndexWriter hashWriter;

    private IndexSearcher hashSearcher;

    private File hashDir;

    private boolean optimize = true;

    private boolean useCompoundFile = true;

    private int rAMBufferSizeMB = 15;

    private int mergeFactor = 20;

    private long unchangedNum = 0;

    static MessageDigest createHasher() {
        // commons codec DigestUtil does not reuse MessageDigest instance
        final String alg = "MD5";
        try {
            return MessageDigest.getInstance(alg);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("cannot retrieve digest instance: " + alg);
        }
    }

    UpdateStatusCensor(File hashDir) {
        Assert.isTrue(hashDir != null && hashDir.isDirectory(), "not a dir: " + hashDir);
        this.logger.info("<init> instrument censor hash dir: " + hashDir.getAbsolutePath());
        this.hashDir = hashDir;
        this.hasher = createHasher();
    }
    
    void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    void setUseCompoundFile(boolean useCompoundFile) {
        this.useCompoundFile = useCompoundFile;
    }

    void setrAMBufferSizeMB(int rAMBufferSizeMB) {
        this.rAMBufferSizeMB = rAMBufferSizeMB;
    }

    void setMergeFactor(int mergeFactor) {
        this.mergeFactor = mergeFactor;
    }

    void open(boolean censorMode) throws IOException {
        this.censorMode = censorMode;
        this.logger.info("<open> " + (censorMode ? "in censor mode" : "in create mode"));
        if (censorMode) {
            createHashSearcher();
        }
        createHashWriter();
    }

    private void createHashWriter() throws IOException {
        this.hashWriter = new IndexWriter(FSDirectory.open(this.hashDir),
                new StandardAnalyzer(Version.LUCENE_30), !this.censorMode,
                IndexWriter.MaxFieldLength.LIMITED);
        this.hashWriter.setMergeFactor(this.mergeFactor);
        this.hashWriter.setRAMBufferSizeMB(this.rAMBufferSizeMB);
        this.hashWriter.setMaxBufferedDocs((int) (5.5d * this.rAMBufferSizeMB * 10000));
        this.hashWriter.setUseCompoundFile(this.useCompoundFile);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<open> hash writer created");
        }
    }

    private void createHashSearcher() throws IOException {
        this.hashSearcher = new IndexSearcher(FSDirectory.open(this.hashDir), true);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<open> hash searcher created");
        }
    }

    /**
     * Censors the instrument identified by the given instrument id with its content encoded in the
     * given byte buffer.
     *
     * <p>
     * The content byte buffer is restored to its status as this method were not called.
     *
     * @param iid an instrument id
     * @param content the encoded instrument content
     * @return true if the given instrument's censor status is positive, false if negative
     * @throws IOException
     */
    boolean censor(long iid, ByteBuffer content) throws IOException {
        final String hash = calcHash(content);
        if (this.censorMode) {
            final TermQuery tq = new TermQuery(new Term(FIELD_IID, Long.toString(iid)));
            final TopDocs docs = this.hashSearcher.search(tq, 1);
            if (docs.totalHits < 1) {
                // new instrument
                addHash(iid, hash);
            }
            else if (docs.totalHits == 1) {
                // existing instrument
                final String md5 = this.hashSearcher.getIndexReader().document(docs.scoreDocs[0].doc,
                        HASH_SELECTOR).get(FIELD_HASH);
                if (hash.equals(md5)) {
                    ++this.unchangedNum;
                    return false;
                }
                else {
                    this.hashWriter.deleteDocuments(tq);
                    addHash(iid, hash);
                }
            }
        }
        else {
            // index instrument hash
            addHash(iid, hash);
        }

        return true;
    }

    private String calcHash(ByteBuffer content) {
        // not thread safe
        hasher.reset();
        content.mark();
        hasher.update(content);
        content.reset();

        return Hex.encodeHexString(hasher.digest());
    }

    private void addHash(long iid, String hash) throws IOException {
        final Document doc = new Document();
        doc.add(IndexerUtil.noNorms(FIELD_IID, Long.toString(iid)));
        doc.add(IndexerUtil.unindexed(FIELD_HASH, hash));
        this.hashWriter.addDocument(doc);
    }

    void close() throws IOException {
        if (this.censorMode) {
            this.logger.info("<close> #" + this.unchangedNum + "# instruments w/o changes censored");
            IoUtils.close(this.hashSearcher);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<close> closed hash searcher");
            }
        }
        this.logger.info("<close> number instrument hashes: " + this.hashWriter.numDocs());
        IndexerUtil.close(this.hashWriter, this.optimize);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<close> optimized and closed hash writer");
        }
    }
}
