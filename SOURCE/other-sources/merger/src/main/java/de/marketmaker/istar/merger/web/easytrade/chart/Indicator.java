/*
 * Indicator.java
 *
 * Created on 17.08.2009 17:59:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Indicator {
    static class Element {
        private final String key;
        private final String name;
        private final String formula;

        private Element(String key, String name, String formula) {
            this.key = key;
            this.name = name;
            this.formula = formula;
        }

        public String getFormula() {
            return formula;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }
    }

    private static final Map<String, Indicator> INSTANCES
            = Collections.synchronizedMap(new HashMap<String, Indicator>());
    
    static {
        INSTANCES.put("bb", new Indicator("Bollinger Bands", new Element[] {
            new Element("bbu", "Bollinger", ".BollingerBands[].LineGet[\"Oben\"]"),
            new Element("bbl", "Bollinger", ".BollingerBands[].LineGet[\"Unten\"]")
        }));
        INSTANCES.put("fs", new Indicator("Fast Stochastik", new Element[] {
            new Element("fsk", "FSK", ".Stochastik[false].LineGet[\"%K\"]"),
            new Element("fsd", "FSD", ".Stochastik[false].LineGet[\"%D\"]")
        }));
        INSTANCES.put("ss", new Indicator("Slow Stochastik", new Element[] {
            new Element("ssk", "SSK", ".Stochastik[true].LineGet[\"%K\"]"),
            new Element("ssd", "SSD", ".Stochastik[true].LineGet[\"%D\"]")
        }));
        INSTANCES.put("macd", new Indicator("MACD", new Element[] {
            new Element("macd", "MACD", ".MACD[].LineGet[\"Macd\"]"),
            new Element("macdt", "MACDT", ".MACD[].LineGet[\"Trigger\"]")
        }));
        INSTANCES.put("momentum", new Indicator("Momentum", new Element[] {
            new Element("momentum", "Momentum", ".Mom[20;0;\"linear\";0;0]")
        }));
        INSTANCES.put("obos", new Indicator("OBOS", new Element[] {
            new Element("obos", "OBOS", ".OBOS[20]")
        }));
        INSTANCES.put("roc", new Indicator("Rate-of-Change", new Element[] {
            new Element("roc", "ROC", ".ROC[14;1;\"linear\";0;0]")
        }));
        INSTANCES.put("rsi", new Indicator("RSI", new Element[] {
            new Element("rsi", "RSI", ".RSI[14]")
        }));
        INSTANCES.put("vma", new Indicator("Variabler MA", new Element[] {
            new Element("vma", "VMA", ".MA[20;\"exponentiell\"]")
        }));
        INSTANCES.put("vola", new Indicator("Vola", new Element[] {
            new Element("vola", "Vola", ".Vola[20;_]")
        }));        
        INSTANCES.put("", null
        );
    }

    static Indicator getInstance(String key) {
        return INSTANCES.get(key);
    }
    
    private String name;
    
    private Element[] elements;

    private Indicator(String name, Element[] elements) {
        this.name = name;
        this.elements = elements;
    }

    public String getName() {
        return this.name;
    }

    public Element[] getElements() {
        return this.elements;
    }

    public static void main(String[] args) {
        System.out.println(Indicator.getInstance("1"));
    }

}
