package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory;

import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.chart.ChartFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.chart.SpsChart;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.chart.SpsFlexGauge;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsListDataTable;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsListMultiFloat;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsListMultiTable;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsListSingleSelect;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsListSingleTable;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsListSingleTablePicker;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsReadonlyListMultiFloat;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsReadonlyListMultiTable;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list.SpsReadonlyListSingleSelect;
import de.marketmaker.iview.pmxml.ListWidgetDesc;

/**
 * Author: umaurer
 * Created: 03.04.14
 */
public class ListFactory extends Factory<ListWidgetDesc> {
    public ListFactory(String baseClass) {
        super(baseClass);
    }

    @Override
    SpsWidget doCreateSpsWidget(ListWidgetDesc lwd, Context context, BindToken parentToken) {
        final SpsWidget spsWidget = createWidget(lwd, context, parentToken);
        spsWidget.setCaption(lwd.getCaption());
        return spsWidget;
    }

    private SpsWidget createWidget(ListWidgetDesc lwd, Context context, BindToken parentToken) {
        final BindToken bindToken = parentToken.append(lwd.getBind());
        final SpsProperty boundProperty = context.getRootProp().get(bindToken.getHead());
        final BindToken itemsBindToken = parentToken.append(lwd.getItemsBind());
        if (TextUtil.hasStyle(lwd, "table")) { // $NON-NLS$
            return new SpsListDataTable(context, parentToken, itemsBindToken, lwd.getColumns())
                    .withShellMMInfoLink(context.getHistoryContextName())
                    .withMainInput(context.getMainInput());
        }
        else if (TextUtil.hasStyle(lwd, "pie", "chartPie")) { // $NON-NLS$
            if (TextUtil.hasStyle(lwd, "pie")) {  // $NON-NLS$
                Firebug.warn("ListFactory <createWidget> style 'pie' is deprecated, use 'chartPie' instead");
            }
            return new SpsChart(context, parentToken, itemsBindToken, lwd.getColumns(), ChartFactory.PIE);
        }
        else if (TextUtil.hasStyle(lwd, "bar", "chartBar")) { // $NON-NLS$
            if (TextUtil.hasStyle(lwd, "bar")) {  // $NON-NLS$
                Firebug.warn("ListFactory <createWidget> style 'bar' is deprecated, use 'chartBar' instead");
            }
            return new SpsChart(context, parentToken, itemsBindToken, lwd.getColumns(), TextUtil.hasStyle(lwd, "horizontal") ? ChartFactory.HORIZ_BAR : ChartFactory.BAR); // $NON-NLS$
        }
        else if (TextUtil.hasStyle(lwd, "chartDonut")) { // $NON-NLS$
            return new SpsChart(context, parentToken, itemsBindToken, lwd.getColumns(), ChartFactory.DONUT);
        }
        else if (TextUtil.hasStyle(lwd, "chartPoint")) { // $NON-NLS$
            return new SpsChart(context, parentToken, itemsBindToken, lwd.getColumns(), ChartFactory.POINT);
        }
        else if (TextUtil.hasStyle(lwd, "chartLine")) { // $NON-NLS$
            return new SpsChart(context, parentToken, itemsBindToken, lwd.getColumns(), ChartFactory.LINE);
        }
        else if (TextUtil.hasStyle(lwd, "chartArea")) { // $NON-NLS$
            return new SpsChart(context, parentToken, itemsBindToken, lwd.getColumns(), ChartFactory.AREA);
        }
        else if (TextUtil.hasStyle(lwd, "gauge", "chartGauge")) { // $NON-NLS$
            if (TextUtil.hasStyle(lwd, "gauge")) {  // $NON-NLS$
                Firebug.warn("ListFactory <createWidget> style 'gauge' is deprecated, use 'chartGauge' instead");
            }
            return new SpsFlexGauge(context, parentToken, itemsBindToken);
        }
        else if (lwd.isIsReadonly() || context.isForceReadonly()) {
            if (boundProperty instanceof SpsLeafProperty) {
                //SingleSelect
                if(TextUtil.hasStyle(lwd, "tablePicker")) {   // $NON-NLS$
                    final String pmIcon = CssUtil.getStyleWithPrefix(lwd.getStyle(), "PmIcon");  // $NON-NLS$
                    final SpsListSingleTablePicker<SpsProperty> tablePicker = new SpsListSingleTablePicker<>(context, parentToken, itemsBindToken, lwd.getKeyField(), lwd.getColumns(), pmIcon);
                    tablePicker.setReadonly(true);
                    return tablePicker;
                }
                return new SpsReadonlyListSingleSelect(context, parentToken, itemsBindToken, lwd.getKeyField(), lwd.getColumns());
            }
            if (!(boundProperty instanceof SpsListProperty)) {
                throw new IllegalArgumentException("In multi select mode the bound property (" + bindToken + ") must be of list type, but is " + boundProperty.getClass().getSimpleName()); // $NON-NLS$
            }
            if (lwd.getColumns().size() == 1 && TextUtil.hasStyle(lwd, "multiFloat")) { // $NON-NLS$
                //MultiSelect SingleColumn
                return new SpsReadonlyListMultiFloat(context, parentToken, itemsBindToken, lwd.getKeyField(), lwd.getColumns().get(0));
            }
            //MultiSelect MultiColumn
            return new SpsReadonlyListMultiTable(context, parentToken, itemsBindToken, lwd.getKeyField(), lwd.getColumns());

        }
        else {
            if (boundProperty instanceof SpsLeafProperty) {
                //SingleSelect
                if(TextUtil.hasStyle(lwd, "tablePicker")) {   // $NON-NLS$
                    final String pmIcon = CssUtil.getStyleWithPrefix(lwd.getStyle(), "PmIcon");  // $NON-NLS$
                    return new SpsListSingleTablePicker<>(context, parentToken, itemsBindToken, lwd.getKeyField(), lwd.getColumns(), pmIcon);
                }
                if (lwd.getColumns().size() == 1) {
                    return new SpsListSingleSelect(context, parentToken, itemsBindToken, lwd.getKeyField(), lwd.getColumns());
                }
                return new SpsListSingleTable(context, parentToken, itemsBindToken, lwd.getKeyField(), lwd.getColumns());
            }
            if (!(boundProperty instanceof SpsListProperty)) {
                throw new IllegalArgumentException("In multi select mode the bound property (" + bindToken + ") must be of list type, but is " + boundProperty.getClass().getSimpleName()); // $NON-NLS$
            }
            if (lwd.getColumns().size() == 1 && TextUtil.hasStyle(lwd, "multiFloat")) { // $NON-NLS$
                //MultiSelect SingleColumn
                return new SpsListMultiFloat(context, parentToken, itemsBindToken, lwd.getKeyField(), lwd.getColumns().get(0));
            }
            //MultiSelect MultiColumn
            return new SpsListMultiTable(context, parentToken, itemsBindToken, lwd.getKeyField(), lwd.getColumns());
        }
    }
}
