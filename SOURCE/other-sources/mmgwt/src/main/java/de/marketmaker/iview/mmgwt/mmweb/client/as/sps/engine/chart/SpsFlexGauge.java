package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.chart;

import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.itools.gwtutil.client.widgets.chart.Gauge;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsListBindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ListUtil;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Author: umaurer
 * Created: 14.08.15
 */
public class SpsFlexGauge extends SpsBoundWidget<Gauge, SpsLeafProperty> implements NoValidationPopup {
    private final SpsListBindFeature spsListBindFeature;

    public SpsFlexGauge(Context context, BindToken parentToken, BindToken itemsBindToken) {
        this.spsListBindFeature = new SpsListBindFeature(context, parentToken, itemsBindToken) {
            @Override
            public void onChange() {
                onItemsChange();
            }
        };
    }

    private void onItemsChange() {
        getWidget().clearTicks();
        getWidget().clearRanges();

        String minLabel = null;
        String maxLabel = null;
        List<Gauge.Range> ranges = null;
        List<Gauge.Tick> thresholdTicks = null;
        String minOverflowStyle = null;
        String maxOverflowStyle = null;

        final SpsListProperty itemListProperty = this.spsListBindFeature.getSpsProperty();
        final List<SpsProperty> children = itemListProperty.getChildren();
        for (final SpsProperty spsProperty : children) {
            if (!(spsProperty instanceof SpsGroupProperty)) {
                throw new IllegalStateException("not group property: " + spsProperty.getBindToken()); // $NON-NLS$
            }
            final SpsGroupProperty gp = (SpsGroupProperty) spsProperty;
            final SpsProperty spsPropKey = gp.get("key"); // $NON-NLS$
            if (spsPropKey == null) {
                throw new RuntimeException("SpsFlexGauge - field \"key\" not specified - " + gp.getBindToken()); // $NON-NLS$
            }
            final String key = ((SpsLeafProperty) spsPropKey).getStringValue();
            if ("Range".equals(key)) { // $NON-NLS$
                ranges = ListUtil.add(ranges, createRange(gp));
            }
            else if ("Threshold".equals(key)) { // $NON-NLS$
                thresholdTicks = ListUtil.add(thresholdTicks, createThresholdTick(gp));
            }
            else if ("OverflowMin".equals(key)) { // $NON-NLS$
                minOverflowStyle = getString(gp, "style", true); // $NON-NLS$
            }
            else if ("OverflowMax".equals(key)) { // $NON-NLS$
                maxOverflowStyle = getString(gp, "style", true); // $NON-NLS$
            }
            else if ("MinLabel".equals(key)) { // $NON-NLS$
                minLabel = getString(gp, "caption", true); // $NON-NLS$
            }
            else if ("MaxLabel".equals(key)) { // $NON-NLS$
                maxLabel = getString(gp, "caption", true); // $NON-NLS$
            }
        }

        getWidget().set(minLabel, maxLabel, ranges, thresholdTicks, minOverflowStyle, maxOverflowStyle);
    }

    private Gauge.Tick createThresholdTick(SpsGroupProperty gp) {
        final BigDecimal value = getBigDecimal(gp, "value", true); // $NON-NLS$
        final String caption = getString(gp, "caption", false); // $NON-NLS$
        final String description = getString(gp, "description", false); // $NON-NLS$
        final String style = getString(gp, "style", false); // $NON-NLS$
        return new Gauge.Tick(value, caption)
                .withTooltip(TextUtil.toSafeHtmlOrNull(description))
                .withStyle(style == null ? "mm-threshold" : (style + " mm-threshold")); // $NON-NLS$
    }

    private Gauge.Range createRange(SpsGroupProperty gp) {
        final BigDecimal from = getBigDecimal(gp, "from", true); // $NON-NLS$
        final BigDecimal to = getBigDecimal(gp, "to", true); // $NON-NLS$
        final String style = getString(gp, "style", true); // $NON-NLS$
        final String description = getString(gp, "description", false); // $NON-NLS$
        return new Gauge.Range(from, to)
                .withTooltip(TextUtil.toSafeHtmlOrNull(description))
                .withStyle(style);
    }

    private SpsLeafProperty getLeaf(SpsGroupProperty gp, String key, boolean force) {
        final SpsLeafProperty leaf = (SpsLeafProperty) gp.get(key);
        if (force && leaf == null) {
            throw new IllegalArgumentException(getClass().getSimpleName() + ": child property '" + key + "' not found in " + gp.getBindToken()); // $NON-NLS$
        }
        return leaf;
    }

    private BigDecimal getBigDecimal(SpsGroupProperty gp, String key, boolean force) {
        final SpsLeafProperty leaf = getLeaf(gp, key, force);
        if (leaf == null) {
            return null;
        }
        if (force && leaf.getDataItem() == null) {
            throw new IllegalArgumentException(getClass().getSimpleName() + ": child property '" + key + "' is null in " + gp.getBindToken()); // $NON-NLS$
        }
        return MmTalkHelper.asBigDecimal(leaf.getDataItem());
    }

    private String getString(SpsGroupProperty gp, String key, boolean force) {
        final SpsLeafProperty leaf = getLeaf(gp, key, force);
        if (leaf == null) {
            return null;
        }
        final String value = leaf.getStringValue();
        if (force && value == null) {
            throw new IllegalArgumentException(getClass().getSimpleName() + ": child property '" + key + "' is null in " + gp.getBindToken()); // $NON-NLS$
        }
        return value;
    }

    @Override
    public void onPropertyChange() {
        final BigDecimal value = new BigDecimal(MmTalkHelper.asMMNumber(getBindFeature().getSpsProperty().getDataItem()).getValue());
        getWidget().setValue(value);
    }

    @Override
    protected Gauge createWidget() {
        final ParsedTypeInfo pti = getBindFeature().getSpsProperty().getParsedTypeInfo();
        final DataItemFormatter dataItemFormatter = getBindFeature().getDataItemFormatter(null);
        final Gauge gauge = new Gauge(new BigDecimal(pti.getMin()), new BigDecimal(pti.getMax()), dataItemFormatter.getNumberFormatter());
        final String description = pti.getDisplayName();
        if (StringUtility.hasText(description)) {
            gauge.setCaption(TextUtil.toSafeHtml(description));
        }
        return gauge;
    }
}
