package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import de.marketmaker.itools.gwtutil.client.widgets.chart.MiniBar;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.pmxml.MMNumber;

/**
 * Author: umaurer
 * Created: 09.01.15
 */
public class SpsMiniBar extends SpsBoundWidget<MiniBar, SpsLeafProperty> {
    private double minValue;
    private double maxValue;

    private final MiniBar miniBar = new MiniBar();

    public SpsMiniBar(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public void onPropertyChange() {
        final MMNumber number = MmTalkHelper.asMMNumber(getBindFeature().getSpsProperty().getDataItem());
        final double value = number == null ? 0d : Double.parseDouble(number.getValue());
        this.miniBar.setValue(value, this.minValue, this.maxValue);
    }

    @Override
    protected MiniBar createWidget() {
        return this.miniBar;
    }
}
