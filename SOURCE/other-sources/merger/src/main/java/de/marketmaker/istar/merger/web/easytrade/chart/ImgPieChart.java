/*
 * ImgPieChart.java
 *
 * Created on 14.03.2011 20:00:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;

import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.data.PieData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgPieChart extends AbstractImgChart {
    public static class ImgPieChartCommand extends BaseImgCommand {
        public enum LabelType {
            FULL, NUMBER
        }

        private String defaultLabel;

        private Integer numItems;

        private Double minimumValue;

        private boolean byValue = true;

        private LabelType labelType = LabelType.FULL;

        private String[] label;

        private BigDecimal[] value;

        @Override
        public StringBuilder appendParameters(StringBuilder sb) {
            super.appendParameters(sb);
            sb.append("&labelType=").append(this.labelType.name())
                    .append("&byValue=").append(this.byValue);
            if (this.defaultLabel != null) {
                sb.append("&defaultLabel=").append(this.defaultLabel);
            }
            if (this.numItems != null) {
                sb.append("&numItems=").append(this.numItems);
            }
            if (this.minimumValue != null) {
                sb.append("&minimumValue=").append(this.minimumValue);
            }
            for (int i = 0; i < this.label.length; i++) {
                sb.append("&label=").append(this.label[i]);
                sb.append("&value=").append(this.value[i]);
            }
            return sb;
        }

        @Range(min = 10, max = 1000)
        public int getHeight() {
            return super.getHeight();
        }

        @Range(min = 10, max = 1000)
        public int getWidth() {
            return super.getWidth();
        }

        public boolean isByValue() {
            return byValue;
        }

        public void setByValue(boolean byValue) {
            this.byValue = byValue;
        }

        public LabelType getLabelType() {
            return labelType;
        }

        public void setLabelType(LabelType labelType) {
            this.labelType = labelType;
        }

        public Integer getNumItems() {
            return numItems;
        }

        public void setNumItems(Integer numItems) {
            this.numItems = numItems;
        }

        public Double getMinimumValue() {
            return minimumValue;
        }

        public void setMinimumValue(Double minimumValue) {
            this.minimumValue = minimumValue;
        }

        public String getDefaultLabel() {
            return this.defaultLabel == null ? "Unbekannt" : this.defaultLabel;
        }

        public void setDefaultLabel(String defaultLabel) {
            this.defaultLabel = defaultLabel;
        }

        public String[] getLabel() {
            return label;
        }

        public void setLabel(String[] label) {
            this.label = label;
        }

        public BigDecimal[] getValue() {
            return value;
        }

        public void setValue(BigDecimal[] value) {
            this.value = value;
        }
    }

    public ImgPieChart() {
        super(ImgPieChartCommand.class);
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);
        binder.registerCustomEditor(ImgPieChartCommand.LabelType.class,
                new EnumEditor<>(ImgPieChartCommand.LabelType.class));
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException bindException) throws Exception {

        final ImgPieChartCommand cmd = (ImgPieChartCommand) object;

        final ImgPieChart.LabelFactory lf = cmd.getLabelType() == ImgPieChartCommand.LabelType.NUMBER
                ? new ImgPieChart.NumberLabelFactory() : new ImgPieChart.FullLabelFactory();

        final PieData pd = createPieData(cmd, lf);

        final ChartModelAndView result = new ChartModelAndView(cmd.getLayout(), cmd.getStyle(),
                cmd.getWidth(), cmd.getHeight(), getEncodingConfig());
        result.addObject("pie", pd);
        return result;
    }

    private PieData createPieData(ImgPieChartCommand cmd, ImgPieChart.LabelFactory lf) {
        PieData pd = null;
        if (!cmd.isByValue()) {
            try {
                pd = getByPercent(cmd.getLabel(), cmd.getValue(), cmd.getNumItems(), cmd.getMinimumValue(), cmd.getDefaultLabel(), lf);
            } catch (IllegalArgumentException e) {
                // allocations add up to more than 100%, ignore and use getByValue instead
            }
        }
        if (pd == null) {
            pd = getByValue(cmd.getLabel(), cmd.getValue(), cmd.getNumItems(), cmd.getMinimumValue(), lf);
        }
        pd.finish();
        return pd;
    }

    private PieData getByValue(String[] label, BigDecimal[] value, Integer numItems,
            Double minimumValue, ImgPieChart.LabelFactory lf) {
        final PieData.ByValue pdByValue;
        if (numItems != null) {
            pdByValue = new PieData.ByValue(numItems);
        }
        else if (minimumValue != null) {
            pdByValue = new PieData.ByValue(minimumValue);
        }
        else {
            pdByValue = new PieData.ByValue();
        }

        if (label == null || label.length == 0) {
            pdByValue.add("100% Unbekannt", BigDecimal.ONE);
        }
        else {
            for (int i = 0; i < label.length; i++) {
                pdByValue.add(lf.getLabel(value[i], label[i]), value[i]);
            }
        }
        return pdByValue;
    }

    private PieData getByPercent(String[] label, BigDecimal[] value, Integer numItems,
            Double minimumValue, String defaultLabel, ImgPieChart.LabelFactory lf) {
        final PieData.ByPercent pdByPercent;
        if (numItems != null) {
            pdByPercent = new PieData.ByPercent(numItems);
        }
        else if (minimumValue != null) {
            pdByPercent = new PieData.ByPercent(minimumValue);
        }
        else {
            pdByPercent = new PieData.ByPercent();
        }

        pdByPercent.setDefaultLabel(defaultLabel);
        if (label != null) {
            for (int i = 0; i < label.length; i++) {
                pdByPercent.add(lf.getLabel(value[i], label[i]), value[i].doubleValue() * 100);
            }
        }
        return pdByPercent;
    }

    static interface LabelFactory {
        String getLabel(BigDecimal share, String label);
    }

    static class FullLabelFactory implements LabelFactory {
        private static final DecimalFormat FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        static {
            FORMAT.applyLocalizedPattern("0.00");
        }

        synchronized public String getLabel(BigDecimal share, String label) {
            return FORMAT.format(share.doubleValue() * 100) + "% " + label;
        }
    }

    static class NumberLabelFactory implements LabelFactory {
        int i = 0;

        public String getLabel(BigDecimal share, String label) {
            return Integer.toString(++this.i);
        }
    }
}
