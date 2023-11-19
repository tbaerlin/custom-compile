package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data;

import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDataExtendedElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DecimalCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RendererContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.StringCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

/**
 * Created on 08.09.2010 14:19:44
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class CerPriceData implements CerData {
    private final DmxmlContext.Block<MSCPriceDataExtended> blockPrice;
    private static final String[] NAMES = new String[]{
            I18n.I.currentPrice(),
            I18n.I.dateTime(),
            I18n.I.changeNetAbbr(),
            I18n.I.changePercentAbbr(),
            I18n.I.bid(),
            I18n.I.ask(),
            I18n.I.openAbbr(),
            I18n.I.high(),
            I18n.I.low(),
            I18n.I.high52WeeksAbbr(),
            I18n.I.low52WeeksAbbr(),
    };

    public CerPriceData(DmxmlContext.Block<MSCPriceDataExtended> blockPrice) {
        this.blockPrice = blockPrice;
    }

    public CellData[] getValues() {
        if (this.blockPrice.isResponseOk() && this.blockPrice.getResult().getElement().size() > 0) {
            final MSCPriceDataExtendedElement e = this.blockPrice.getResult().getElement().get(0);
            final de.marketmaker.iview.dmxml.PriceData data = e.getPricedata();
            return new CellData[]{
                createField(data.getPrice()),
                createDateField(data.getDate()),
                createChangeNetField(data.getChangeNet()),
                createChangePercentField(data.getChangePercent()),
                createField(data.getBid()),
                createField(data.getAsk()),
                createField(data.getOpen()),
                createField(data.getHighDay()),
                createField(data.getLowDay()),
                createField(data.getHighYear()),
                createField(data.getLowYear())
            };
        }
        else {
            return new CellData[NAMES.length];
        }
    }

    public String[] getNames() {
        return NAMES;
    }

    private DecimalCellData createField(String text) {
        return new DecimalCellData(Renderer.PRICE, text, CellData.Sorting.ASC);
    }

    private DecimalCellData createChangeNetField(String changeNet) {
        return new DecimalCellData(Renderer.CHANGE_PRICE, changeNet, CellData.Sorting.NONE, true);
    }

    private DecimalCellData createChangePercentField(String change) {
        return new DecimalCellData(Renderer.CHANGE_PERCENT, change, CellData.Sorting.NONE, true);
    }


    private StringCellData createDateField(String date) {
        final StringBuffer sb = new StringBuffer();
        TableCellRenderers.DATE_AND_TIME.render(date, sb, new RendererContext(null, null));
        if (sb.toString().endsWith("00:00:00")) { // $NON-NLS$
            sb.setLength(0);
            TableCellRenderers.DATE.render(date, sb, new RendererContext(null, null));
        }
        return new StringCellData(sb.toString(), true);
    }


}
