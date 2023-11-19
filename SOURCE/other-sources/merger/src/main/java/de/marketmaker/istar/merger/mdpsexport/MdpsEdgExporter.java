package de.marketmaker.istar.merger.mdpsexport;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.EdgData;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.provider.certificatedata.EdgDataReader;
import de.marketmaker.istar.merger.provider.certificatedata.InstrumentBasedUpdatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsEdgExporter implements InstrumentBasedUpdatable<EdgData>, InitializingBean {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyyMMdd_HHmmss");
    private final DateTimeFormatter DTF_DATE = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final DecimalFormat FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        FORMAT.applyLocalizedPattern("0.#####");
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final StringBuilder sb = new StringBuilder();
    private int count;
    private File inputFile;
    private File outputDir;
    private Writer writer;


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

        this.count = 0;
        final File file = new File(this.outputDir, "mm-mdps-edg-" + DTF.print(new DateTime()) + ".txt");
        this.writer = new BufferedWriter(new FileWriter(file));

        final EdgDataReader reader = new EdgDataReader(this);
        reader.read(this.inputFile);

        this.logger.info("<process> read data took " + tt);


        this.writer.close();
        this.logger.info("<process> exported " + this.count + " records in " + tt);
    }

    public void addOrReplace(long instrumentid, EdgData data) {
        if (!StringUtils.hasText(data.getIsin())) {
            return;
        }

        this.sb.setLength(0);
        addField(VwdFieldDescription.ADF_EDG_DATE, data.getEdgRatingDate());
        addField(VwdFieldDescription.ADF_EDG_SCORE_RK_1, data.getEdgScore1());
        addField(VwdFieldDescription.ADF_EDG_SCORE_RK_2, data.getEdgScore2());
        addField(VwdFieldDescription.ADF_EDG_SCORE_RK_3, data.getEdgScore3());
        addField(VwdFieldDescription.ADF_EDG_SCORE_RK_4, data.getEdgScore4());
        addField(VwdFieldDescription.ADF_EDG_SCORE_RK_5, data.getEdgScore5());
        addField(VwdFieldDescription.ADF_EDG_TOP_CLASS, data.getEdgTopClass());
        addField(VwdFieldDescription.ADF_DDV_Evaluation_date, data.getDdvDate());
        addField(VwdFieldDescription.ADF_DDV_Var10, data.getDdvVar10d());
        addField(VwdFieldDescription.ADF_DDV_Pricerisk10, data.getDdvPriceRisk10d());
        addField(VwdFieldDescription.ADF_DDV_Interestrisk10, data.getDdvInterestRisk10d());
        addField(VwdFieldDescription.ADF_DDV_Currencyrisk10, data.getDdvCurrencyRisk10d());
        addField(VwdFieldDescription.ADF_DDV_Issuerrisk10, data.getDdvIssuerRisk10d());
        addField(VwdFieldDescription.ADF_DDV_Volatilityrisk10, data.getDdvVolatilityRisk10d());
        addField(VwdFieldDescription.ADF_DDV_Diversification10, data.getDdvDiversificationRisk10d());
        addField(VwdFieldDescription.ADF_DDV_Timevalue10, data.getDdvTimevalue10d());
        addField(VwdFieldDescription.ADF_DDV_Var250, data.getDdvVar250d());
        addField(VwdFieldDescription.ADF_DDV_Pricerisk250, data.getDdvPriceRisk250d());
        addField(VwdFieldDescription.ADF_DDV_Interestrisk250, data.getDdvInterestRisk250d());
        addField(VwdFieldDescription.ADF_DDV_Currencyrisk250, data.getDdvCurrencyRisk250d());
        addField(VwdFieldDescription.ADF_DDV_Issuerrisk250, data.getDdvIssuerRisk250d());
        addField(VwdFieldDescription.ADF_DDV_Volatilityrisk250, data.getDdvVolatilityRisk250d());
        addField(VwdFieldDescription.ADF_DDV_Diversification250, data.getDdvDiversificationRisk250d());
        addField(VwdFieldDescription.ADF_DDV_Timevalue250, data.getDdvTimevalue250d());
        addField(VwdFieldDescription.ADF_DDV_Rk10, data.getDdvRiskclass10d());

        if (this.sb.length() > 0) {
            try {
                this.writer.write(data.getIsin());
                this.writer.write(sb.toString());
                this.writer.write("\n");
            }
            catch (IOException e) {
                this.logger.error("failed", e);
                throw new RuntimeException(e);
            }
        }

        this.count++;
    }

    private void addField(VwdFieldDescription.Field field, BigDecimal value) {
        addField(field, value, false);
    }

    private void addField(VwdFieldDescription.Field field, BigDecimal value, boolean percent) {
        if (value == null) {
            return;
        }
        addValue(field, FORMAT.format(percent ? value.movePointRight(2) : value));
    }

    private void addField(VwdFieldDescription.Field field, Integer value) {
        if (value == null) {
            return;
        }
        addValue(field, Integer.toString(value));
    }

    private void addField(VwdFieldDescription.Field field, LocalDate date) {
        if (date== null) {
            return;
        }
        addValue(field, DTF_DATE.print(date));
    }

    private void addValue(VwdFieldDescription.Field field, String value) {
        this.sb.append(";").append(field.id()).append(":").append(value);
    }

    public static void main(String[] args) throws Exception {
        final MdpsEdgExporter e = new MdpsEdgExporter();
        e.setInputFile(new File(LocalConfigProvider.getProductionBaseDir(),
                "var/data/provider/istar-edg-ratings.xml.gz"));
        e.setOutputDir(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/"));
        e.afterPropertiesSet();
    }
}
