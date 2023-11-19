package de.marketmaker.istar.merger.mdpsexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import de.marketmaker.istar.instrument.export.InstrumentSystemUtil;
import de.marketmaker.istar.instrument.export.ScoachInstrumentAdaptor;

/**
 * @author zzhao
 */
public class MdpsQuoteMetadataExporterDp2 implements InitializingBean {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyyMMdd_HHmmss");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File instrumentFile;

    private File instrumentUpdateFile;

    private File outputDir;

    private ScoachInstrumentAdaptor scoachInstrumentAdaptor;

    private int emptyContentFlags = 0;

    public void setInstrumentUpdateFile(File instrumentUpdateFile) {
        this.instrumentUpdateFile = instrumentUpdateFile;
    }

    public void setInstrumentFile(File instrumentFile) {
        this.instrumentFile = instrumentFile;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public void setScoachInstrumentAdaptor(ScoachInstrumentAdaptor scoachInstrumentAdaptor) {
        this.scoachInstrumentAdaptor = scoachInstrumentAdaptor;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.isTrue(null != this.instrumentFile, "instrument file required");
        InstrumentSystemUtil.validateDir(this.instrumentFile.getParentFile());

        Assert.isTrue(null != this.outputDir && this.outputDir.isDirectory(), "output dir required");
        Assert.notNull(this.scoachInstrumentAdaptor, "scoach instrument adaptor required");

        exportQuoteMeta(this.instrumentUpdateFile != null ? this.instrumentUpdateFile.lastModified() : this.instrumentFile.lastModified());
    }

    private void exportQuoteMeta(long curMs) {
        final String dateTimeStr = DTF.print(curMs);
        this.logger.info("<exportQuoteMeta> for: " + dateTimeStr);
        final TimeTaker tt = new TimeTaker();

        this.emptyContentFlags = 0;

        final File outFile = new File(this.outputDir, "mm-mdps-quotemetadata-" + dateTimeStr + ".txt");
        boolean exportFailure = false;
        try (InstrumentDirDao dao = createDao(); Writer writer = new BufferedWriter(new FileWriter(outFile))) {
            for (final Instrument ins : dao) {
                if (InstrumentUtil.isOpraInstrument(ins)) {
                    continue;
                }
                for (Quote quote : ins.getQuotes()) {
                    final String vwdCodeFromQuote = quote.getSymbolVwdcode();
                    if (!StringUtils.hasText(vwdCodeFromQuote)) {
                        continue;
                    }
                    final String b64 = getContentFlagsBase64((QuoteDp2) quote);
                    if (StringUtils.hasText(b64)) {
                        final String vwdCode = getVwdCode(vwdCodeFromQuote);
                        writeFlags(writer, b64, vwdCode);
                        if (vwdCode.endsWith(".FFMST")) {
                            writeFlags(writer, b64, vwdCode.substring(0, vwdCode.length() - 2));
                        }
                    }
                }
            }

        } catch (Exception e) {
            this.logger.error("<exportQuoteMeta> cannot export quote meta data", e);
            e.printStackTrace();
            exportFailure = true;
        }

        if (exportFailure) {
            FileUtils.deleteQuietly(outFile);
        }
        this.logger.info("<exportQuoteMeta> took: " + tt + " for: " + dateTimeStr + ", exported "
                + this.emptyContentFlags + " empty content flags -> "
                + (exportFailure ? "failed" : "succeed"));
    }

    private InstrumentDirDao createDao() throws Exception {
        if (this.instrumentUpdateFile == null) {
            return new InstrumentDirDao(this.instrumentFile.getParentFile());
        }
        else {
            return new InstrumentDirDao(this.instrumentUpdateFile.getParentFile(),
                    new InstrumentDirDao(this.instrumentFile.getParentFile()).getDomainContext());
        }
    }

    private String getContentFlagsBase64(QuoteDp2 quote) {
        final long[] flags = quote.getFlags();
        if (!anyFlagsSet(flags)) {
            this.emptyContentFlags++;
        }
        return InstrumentUtil.toBase64String(flags);
    }

    private boolean anyFlagsSet(long[] flags) {
        if (null == flags || flags.length == 0) {
            return false;
        }
        for (final long flag : flags) {
            if (flag != 0) {
                return true;
            }
        }

        return false;
    }

    private String getVwdCode(String vwdCode) {
        if (!this.scoachInstrumentAdaptor.scoachSymbolExists(vwdCode)) {
            return vwdCode;
        }
        return vwdCode + "ST";
    }

    private static void writeFlags(Writer writer, String b64, String vwdCode) throws IOException {
        writer.write(vwdCode + ";2039:" + b64 + "\n");
    }

    public static void main(String[] args) throws Exception {
        final File pbd = LocalConfigProvider.getProductionBaseDir();
        final ScoachInstrumentAdaptor sia = new ScoachInstrumentAdaptor();
        sia.setSymbolsFile(new File(pbd, "var/data/instrument/ffmst/ffmst.txt"));
        sia.afterPropertiesSet();
        final MdpsQuoteMetadataExporterDp2 e = new MdpsQuoteMetadataExporterDp2();
        e.setScoachInstrumentAdaptor(sia);
        final File insFile = new File(pbd,
                "var/data/istar-instrument-export/full/data/instruments/instruments.dat");
        e.setInstrumentFile(insFile);
        e.setOutputDir(new File(pbd, "var/data/istar-ratios-mdpsexport/out/"));
        e.exportQuoteMeta(insFile.lastModified());
    }
}
