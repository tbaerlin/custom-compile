/*
 * ScreenerProviderImpl.java
 *
 * Created on 03.04.2007 19:15:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.screener;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.data.ScreenerUpDownData;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.instrument.data.screener.ScreenerData;
import de.marketmaker.istar.instrument.data.screener.ScreenerDataRepository;
import de.marketmaker.istar.instrument.data.screener.ScreenerFieldDescription;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ScreenerProviderImpl implements ScreenerProvider {
    private final static DecimalFormat RENDERER_PRICE
            = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    static {
        RENDERER_PRICE.applyLocalizedPattern("0.#");
    }

    private static final String RESOURCE_FILE =
            "/de/marketmaker/istar/merger/provider/screener/screener-names.properties";

    private static final Map<Integer, String> HEADLINES_DE = new HashMap<>();
    private static final Map<Integer, String> HEADLINES_EN = new HashMap<>();
    private static final Map<Integer, String> NAMES = new HashMap<>();

    private final static int[] FIELDIDS_BASE = new int[]{
            ScreenerFieldDescription.MMF_NAME,
            ScreenerFieldDescription.MMF_TICKER,
            ScreenerFieldDescription.MMF_ISIN,
            ScreenerFieldDescription.MMF_CCY,
            ScreenerFieldDescription.MMF_COUNTRY,
            ScreenerFieldDescription.MMF_GROUP,
            ScreenerFieldDescription.MMF_SECTOR,
            ScreenerFieldDescription.MMF_IDX,
            ScreenerFieldDescription.MMF_PRICE
    };
    private final static int[] FIELDIDS_ANALYSIS = new int[]{
            ScreenerFieldDescription.MMF_ANALYSISDATE,
            ScreenerFieldDescription.MMF_FIRSTDATE,
            ScreenerFieldDescription.MMF_IRST,
            ScreenerFieldDescription.MMF_MC,
            ScreenerFieldDescription.MMF_ERT,
            ScreenerFieldDescription.MMF_VR,
            ScreenerFieldDescription.MMF_GPE,
            ScreenerFieldDescription.MMF_LTPE,
            ScreenerFieldDescription.MMF_LTG,
            ScreenerFieldDescription.MMF_NBRANL,
            ScreenerFieldDescription.MMF_DIVIDEND,
            ScreenerFieldDescription.MMF_TT,
            ScreenerFieldDescription.MMF_RP,
            ScreenerFieldDescription.MMF_GLEVAL
    };
    private final static int[] FIELDIDS_RISK = new int[]{
            ScreenerFieldDescription.MMF_RISKZONE,
            ScreenerFieldDescription.MMF_BMF,
            ScreenerFieldDescription.MMF_BMFRISK,
            ScreenerFieldDescription.MMF_BNF,
            ScreenerFieldDescription.MMF_BNFRISK,
            ScreenerFieldDescription.MMF_BETA,
            ScreenerFieldDescription.MMF_CORR,
            ScreenerFieldDescription.MMF_VARVAL
    };

    static {
        try {
            final Properties properties =
                    PropertiesLoader.load(ScreenerProviderImpl.class.getResourceAsStream(RESOURCE_FILE));

            for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
                final String[] strings = entry.getKey().toString().split("\\.");
                final int fieldid = ScreenerFieldDescription.getFieldByName(strings[0]);

                if ("headline_de".equals(strings[1])) {
                    HEADLINES_DE.put(fieldid, entry.getValue().toString());
                } else if ("headline_en".equals(strings[1])) {
                    HEADLINES_EN.put(fieldid, entry.getValue().toString());
                } else if ("fieldname".equals(strings[1])) {
                    NAMES.put(fieldid, entry.getValue().toString());
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ScreenerDataRepository repository;
    private static final BigDecimal BIG_DECIMAL_PERCENT = new BigDecimal("0.01");

    public void setRepository(ScreenerDataRepository repository) {
        this.repository = repository;
    }

    public ScreenerResult getScreenerResult(long instrumentid, String language) {
        final ScreenerData screenerData = this.repository.getScreenerData(instrumentid, language);

        final Map<Integer, String> headlines = (language != null && language.startsWith("de")) ? HEADLINES_DE : HEADLINES_EN;
        final ScreenerResult result = new ScreenerResult(instrumentid, language);
        if (screenerData != null) {
            result.setBaseFields(getFields(screenerData, FIELDIDS_BASE, headlines));
            result.setAnalysisFields(getFields(screenerData, FIELDIDS_ANALYSIS, headlines));
            result.setRiskFields(getFields(screenerData, FIELDIDS_RISK, headlines));
            result.setGroupAlternatives(screenerData.getAltGroup());
            result.setCountryAlternatives(screenerData.getAltCountry());
        }

        return result;
    }

    private List<ScreenerField> getFields(ScreenerData screenerData, int[] fieldids, Map<Integer, String> headlines) {
        final List<ScreenerField> result = new ArrayList<>();

        for (final int fieldid : fieldids) {
            final SnapField field = screenerData.getField(fieldid);

            if (!field.isDefined()) {
                continue;
            }

            final ScreenerFieldImpl resultField;
            switch (ScreenerFieldDescription.TYPES[fieldid]) {
                case ScreenerFieldDescription.TYPE_UCHAR:
                    resultField = new ScreenerFieldImpl(fieldid, field.getValue());
                    break;
                case ScreenerFieldDescription.TYPE_PRICE:
                    resultField = new ScreenerFieldImpl(fieldid, field.getPrice().multiply(getFactor(fieldid)));
                    break;
                case ScreenerFieldDescription.TYPE_DATE:
                    final Number n = (Number) field.getValue();
                    resultField = new ScreenerFieldImpl(fieldid, DateUtil.yyyymmddToDateTime(n.intValue()));
                    break;
                case ScreenerFieldDescription.TYPE_UNUM4:
                    final Number l = (Number) field.getValue();
                    resultField = new ScreenerFieldImpl(fieldid, l.longValue());
                    break;
                default:
                    continue;
            }

            resultField.setName(NAMES.get(fieldid));
            resultField.setHeadline(headlines.get(fieldid));


            final ScreenerData.EvaluatedRule rule = screenerData.getEvaluatedRule(fieldid);
            if (rule != null) {
                resultField.setShortText(rule.getShortText());
                resultField.setLongText(rule.getLongText());
            }

            addImageName(resultField);
            addStarImageName(resultField);
            result.add(resultField);
        }

        addStarImageName(result);

        return result;
    }

    private BigDecimal getFactor(int fieldid) {
        switch (fieldid) {
            case ScreenerFieldDescription.MMF_DIVIDEND:
            case ScreenerFieldDescription.MMF_RP:
            case ScreenerFieldDescription.MMF_CORR:
                return BIG_DECIMAL_PERCENT;
        }

        return BigDecimal.ONE;
    }

    private void addStarImageName(List<ScreenerField> result) {
        int count = 0;

        int irst = 0;
        ScreenerFieldImpl rp = null;
        for (final ScreenerField field : result) {
            switch (field.getId()) {
                case ScreenerFieldDescription.MMF_ERT:
                case ScreenerFieldDescription.MMF_VR:
                case ScreenerFieldDescription.MMF_TT:
                    if (field.isStar() != null && field.isStar()) {
                        count++;
                    }
                    break;
                case ScreenerFieldDescription.MMF_RP:
                    rp = (ScreenerFieldImpl) field;
                    break;
                case ScreenerFieldDescription.MMF_IRST:
                    irst = ((Long) field.getValue()).intValue();
                    break;
            }
        }

        if (rp != null && irst != count) {
            rp.setStar(true);
        }
    }

    private void addStarImageName(ScreenerFieldImpl field) {
        switch (field.getId()) {
            case ScreenerFieldDescription.MMF_ERT:
                final BigDecimal ert = (BigDecimal) field.getValue();
                field.setStar(ert.compareTo(BigDecimal.ZERO) > 0);
                break;
            case ScreenerFieldDescription.MMF_VR:
                final Long vr = (Long) field.getValue();
                field.setStar(vr >= 0);
                break;
            case ScreenerFieldDescription.MMF_TT:
                final BigDecimal tt = (BigDecimal) field.getValue();
                field.setStar(tt.compareTo(BigDecimal.ZERO) > 0);
                break;
        }
    }

    private void addImageName(ScreenerFieldImpl field) {
        switch (field.getId()) {
            case ScreenerFieldDescription.MMF_ERT:
            case ScreenerFieldDescription.MMF_TT:
                field.setImageName("arrow" + format(field.getValue()) + ".gif");
                break;
            case ScreenerFieldDescription.MMF_VR:
                field.setImageName("arrow" + field.getValue() + ".gif");
                break;
            case ScreenerFieldDescription.MMF_IRST:
                field.setImageName("irst" + field.getValue() + ".gif");
                break;
            case ScreenerFieldDescription.MMF_GLEVAL:
                field.setImageName("gleval" + field.getValue() + ".gif");
                break;
            case ScreenerFieldDescription.MMF_RISKZONE:
            case ScreenerFieldDescription.MMF_BMFRISK:
            case ScreenerFieldDescription.MMF_BNFRISK:
                field.setImageName("risk" + field.getValue() + ".gif");
                break;
        }
    }

    private String format(final Object value) {
        synchronized (RENDERER_PRICE) {
            return RENDERER_PRICE.format(value);
        }
    }

    public ScreenerUpDownData getUpDownData(String region) {
        return this.repository.getUpDownData(region);
    }
}
