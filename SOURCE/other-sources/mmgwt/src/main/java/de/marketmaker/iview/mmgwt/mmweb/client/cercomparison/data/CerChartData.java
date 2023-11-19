package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data;

import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.StringCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author umaurer
 */
public class CerChartData implements CerData {
    private static final String[] NAMES = new String[]{""};

    private DmxmlContext.Block<IMGResult> blockImg;

    
    public CerChartData(DmxmlContext.Block<IMGResult> blockImg) {
        this.blockImg = blockImg;
    }

    public CellData[] getValues() {
        return new CellData[]{
                new StringCellData("<img src=\"" + ChartUrlFactory.getUrl(this.blockImg.getResult().getRequest()) + "\"/>") { // $NON-NLS$
                    @Override
                    public boolean isHtml() {
                        return true;
                    }
                }
        };
    }

    public String[] getNames() {
        return NAMES;
    }

}
