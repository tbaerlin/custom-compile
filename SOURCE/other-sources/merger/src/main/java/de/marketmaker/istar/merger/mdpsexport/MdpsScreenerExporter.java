package de.marketmaker.istar.merger.mdpsexport;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.data.screener.ScreenerFieldDescription;
import de.marketmaker.istar.instrument.data.screener.ScreenerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsScreenerExporter implements InitializingBean {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyyMMdd_HHmmss");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Map<Integer, Integer> SCREENER_TO_MDPS = new HashMap<>();

    static {
        SCREENER_TO_MDPS.put(ScreenerFieldDescription.MMF_IRST, VwdFieldDescription.ADF_Preisbewertung_TS.id());
    }

    private File inputFile;
    private File outputDir;

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public void afterPropertiesSet() throws Exception {
        process();
    }

    private void process() throws Exception {
        final TimeTaker tt = new TimeTaker();
        final Map<Long, SnapRecord> map = ScreenerRepository.readScreenerSnaps(this.inputFile, null, null);

        this.logger.info("<process> read snaps took " + tt);

        final DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        format.applyLocalizedPattern("0.#####");

        final Writer writer = new BufferedWriter(new FileWriter(new File(this.outputDir, "mm-mdps-screener-" + DTF.print(new DateTime()) + ".txt")));
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<Long, SnapRecord> entry : map.entrySet()) {
            sb.setLength(0);
//            System.out.println(entry.getKey() + ": " + entry.getValue().getField(ScreenerFieldDescription.MMF_IRST));

            final SnapField isinField = entry.getValue().getField(ScreenerFieldDescription.MMF_ISIN);
            if (!isinField.isDefined()) {
                this.logger.warn("<process> no isin for " + entry.getKey() + ".iid");
                continue;
            }
            sb.append(isinField.getValue().toString());

            for (final Map.Entry<Integer, Integer> ids : SCREENER_TO_MDPS.entrySet()) {
                final SnapField field = entry.getValue().getField(ids.getKey());

                final int type = ScreenerFieldDescription.TYPES[ids.getKey()];
                switch (type) {
                    case ScreenerFieldDescription.TYPE_PRICE:
                        if (field.isDefined()) {
                            sb.append(";").append(ids.getValue()).append(":").append(format.format(field.getPrice()));
                        }
                        break;
                    case ScreenerFieldDescription.TYPE_UNUM4:
                        if (field.isDefined()) {
                            sb.append(";").append(ids.getValue()).append(":").append(((Number) field.getValue()).longValue());
                        }
                        break;
                    default:
                        this.logger.warn("<process> unknown type: " + type);
                        break;
                }
            }

            writer.write(sb.toString() + "\n");
        }

        writer.close();
        this.logger.info("<process> exported " + map.size() + " records in " + tt);
    }

    public static void main(String[] args) throws Exception {
        final MdpsScreenerExporter e = new MdpsScreenerExporter();
        e.setInputFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-screener.xml.gz"));
        e.setOutputDir(new File(LocalConfigProvider.getProductionBaseDir(), "prog/istar-ratios-mdpsexport/out/"));
        e.afterPropertiesSet();
    }
}
