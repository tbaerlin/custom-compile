package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 28.09.2010 10:38:39
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class CerCompareChartData {
    private final String[] bmColors = new String[]{"bm2", "bm3", "bm4", "bm5"}; // $NON-NLS$
    private final DmxmlContext.Block<IMGResult> block;
    private final List<String> symbols = new ArrayList<String>();

    public CerCompareChartData(DmxmlContext context) {
        this.block = context.addBlock("IMG_Chartcenter"); // $NON-NLS$
        initBlock();
    }

    private void initBlock() {
        block.setParameter("width", "600"); // $NON-NLS$
        block.setParameter("height", "260"); // $NON-NLS$
        block.setParameter("period", "P1Y"); // $NON-NLS$
        block.setParameter("chartlayout", "basic"); // $NON-NLS$
        block.setParameter("legend", "true"); // $NON-NLS$
        block.setParameter("blendCorporateActions", "true"); // $NON-NLS$
        block.setParameter("blendDividends", "false"); // $NON-NLS$
    }

    public void add(String symbol) {
        this.symbols.add(symbol);
        prepareBlock(this.symbols);
    }

    private void addBenchmarkParam(String symbol, int index) {
        final String bench = this.block.getParameter("benchmark"); // $NON-NLS$
        final String benchColor = this.block.getParameter("benchmarkColor"); // $NON-NLS$

        final String newBench;
        if (bench != null) {
            newBench = bench + ";" + symbol; // $NON-NLS$
        }
        else {
            newBench = symbol;
        }
        this.block.setParameter("benchmark", newBench); // $NON-NLS$

        final String newBenchColor;
        if (bench != null) {
            newBenchColor = benchColor + ";" + bmColors[index]; // $NON-NLS$
        }
        else {
            newBenchColor = bmColors[index];
        }
        this.block.setParameter("benchmarkColor", newBenchColor); // $NON-NLS$
    }

    private void resetBlock() {
        this.block.removeParameter("benchmark"); // $NON-NLS$
        this.block.removeParameter("benchmarkColor"); // $NON-NLS$
        this.block.removeParameter("symbol"); // $NON-NLS$
    }


    private void prepareBlock(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return;
        }
        resetBlock();

        this.block.setParameter("symbol", symbols.get(0)); // $NON-NLS$
        if (symbols.size() > 1) {
            for (int i = 1; i < symbols.size(); i++) {
                final String symbol = symbols.get(i);
                addBenchmarkParam(symbol, i - 1);
            }
        }
    }

    public String getChartUrl() {
        return ChartUrlFactory.getUrl(this.block.getResult().getRequest());
    }

    public void removeSymbol(int index, final AsyncCallback<ResponseType> asyncCallback) {
        this.symbols.remove(index);
        prepareBlock(this.symbols);
        this.block.issueRequest(asyncCallback);
    }
}