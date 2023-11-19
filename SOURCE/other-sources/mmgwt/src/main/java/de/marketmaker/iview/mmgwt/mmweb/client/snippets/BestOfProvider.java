/*
 * BestOfProvider.java
 *
 * Created on 17.11.11 11:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.BestOfCell;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tkiesgen
 */
public class BestOfProvider {
    public static final BestOfProvider INSTANCE = new BestOfProvider();

    private final Map<String, BestOfConfiguration> mapConfigurations = new HashMap<String, BestOfConfiguration>();
    private final int maxCellCount;

    private BestOfProvider() {
        final JSONWrapper jsonBestOf = SessionData.INSTANCE.getGuiDef("cer_bestof"); // $NON-NLS$
        int maxCellCount = 0;
        for (int i = 0, size = jsonBestOf.size(); i < size; i++) {
            final JSONWrapper jsonConfig = jsonBestOf.get(i);
            final BestOfConfiguration configuration = getConfiguration(jsonConfig);
            this.mapConfigurations.put(jsonConfig.get("id").stringValue(), configuration); // $NON-NLS$
            final int cellCount = configuration.getCells().size();
            if (cellCount > maxCellCount) {
                maxCellCount = cellCount;
            }
        }

        this.maxCellCount = maxCellCount;
/*
        this.type2configuration.put(InstrumentTypeEnum.STK, Arrays.asList(this.bonus, this.discount, this.reverseConvertible, this.turbo));
        this.type2configuration.put(InstrumentTypeEnum.IND, Arrays.asList(this.discount, this.deepDiscount, this.bonus, this.reverseConvertible, this.turbo));
        this.type2configuration.put(InstrumentTypeEnum.CUR, Arrays.asList(this.reverseConvertible, this.miniFutures, this.turbo));
        this.type2configuration.put(InstrumentTypeEnum.MER, Arrays.asList(this.bonus, this.discount, this.index, this.turbo));
*/
    }

/*
    public List<BestOfConfiguration> getOverviewConfiguration() {
        return Arrays.asList(this.bonus, this.discount, this.deepDiscount, this.index, this.reverseConvertible, this.turbo, this.miniFutures);
    }
*/

    public int getMaxCellCount() {
        return this.maxCellCount;
    }

    public BestOfConfiguration getConfiguration(String id) {
        return this.mapConfigurations.get(id);
    }

    private static BestOfConfiguration getConfiguration(JSONWrapper jsonConfig) {
        return new BestOfConfiguration(jsonConfig);
    }

    public static class BestOfConfiguration {
        protected final Map<String, String> parameters = new HashMap<String, String>();
        protected final Map<String, String> finderParameters = new HashMap<String, String>();

        protected final List<BestOfCell> cells = new ArrayList<BestOfCell>();

        private final String name;
        private final String title;
        private final String explanation;
        private final String sortFieldTitle;
        private final String renderer;

        private BestOfConfiguration(JSONWrapper jsonConfig) {
            this.name = jsonConfig.get("name").stringValue(); // $NON-NLS$
            this.title = jsonConfig.get("title").stringValue(); // $NON-NLS$

            this.explanation = jsonConfig.get("explanation").stringValue(); // $NON-NLS$
            this.renderer = jsonConfig.get("renderer").stringValue(); // $NON-NLS$
            this.sortFieldTitle = jsonConfig.get("sortFieldTitle").stringValue(); // $NON-NLS$

            final JSONWrapper jsonCells = jsonConfig.get("cells"); // $NON-NLS$
            for (String cellKey : jsonCells.keySet()) {
                final String cellValue = jsonCells.get(cellKey).stringValue();
                this.cells.add(new BestOfCell(cellKey, cellValue));
            }

            final JSONWrapper jsonParameters = jsonConfig.get("parameters"); // $NON-NLS$
            for (String pKey : jsonParameters.keySet()) {
                this.parameters.put(pKey, jsonParameters.get(pKey).stringValue());
            }

            final JSONWrapper jsonFinderParameters = jsonConfig.get("finderParameters"); // $NON-NLS$
            for (String pKey : jsonFinderParameters.keySet()) {
                final String pValue = jsonFinderParameters.get(pKey).stringValue();
                this.finderParameters.put(pKey, pValue);
            }
        }

        public String getName() {
            return this.name;
        }

        public String getLongTitle() {
            return StringUtil.hasText(this.sortFieldTitle)
                    ? this.title + " " + I18n.I.sortBy() + " " + this.sortFieldTitle
                    : this.title;
        }

        public String getTitle() {
            return this.title;
        }

        public String getExplanation() {
            return this.explanation;
        }

        public String getSortFieldTitle() {
            return this.sortFieldTitle;
        }

        public Map<String, String> getParameters() {
            return this.parameters;
        }

        public Map<String, String> getFinderParameters() {
            return this.finderParameters;
        }

        public List<BestOfCell> getCells() {
            return cells;
        }

        public String getRenderer() {
            return renderer;
        }
    }
}
