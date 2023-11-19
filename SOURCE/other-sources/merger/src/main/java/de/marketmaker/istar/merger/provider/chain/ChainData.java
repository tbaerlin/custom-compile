package de.marketmaker.istar.merger.provider.chain;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * data container for a single chain
 */
public class ChainData implements Serializable {

    static final long serialVersionUID = 4L;

    private String chainInstrument;
    private String chainName;
    private String chainFlag;
    private DateTime chainChangeDate;

    Element[] elements;

    public ChainData(
            String chainInstrument,
            String chainName,
            String chainFlag,
            DateTime chainChangeDate,
            Element[] elements) {
        this.chainInstrument = chainInstrument;
        this.chainName = chainName;
        this.chainFlag = chainFlag;
        this.chainChangeDate = chainChangeDate;
        this.elements = elements;
    }

    public String getChainInstrument() {
        return chainInstrument;
    }

    public String getChainName() {
        return chainName;
    }

    public String getChainFlag() {
        return chainFlag;
    }

    public DateTime getChainChangeDate() {
        return chainChangeDate;
    }

    public Element[] getElements() {
        return elements;
    }

    // a single chain element
    public static class Element implements Serializable {

        static final long serialVersionUID = 4L;

        final String name;

        final String lei;

        final String valor;

        final String vwdCode;

        final String wpNameKurz;

        final String wpNameLang;

        final String longName;

        final String wpNameZusatz;

        final String wmTicker;

        final String ticker;

        final String eurexTicker;

        final String isin;

        final String wkn;

        final long qid;

        final String vwdSymbol;

        final String sortField;

        public Element(
                long qid,
                String vwdSymbol,
                String sortField,
                String name,
                String wpNameKurz,
                String wpNameLang,
                String longName,
                String wpNameZusatz,
                String wmTicker,
                String ticker,
                String eurexTicker,
                String isin,
                String wkn,
                String vwdCode,
                String valor,
                String lei) {
            this.qid = qid;
            this.vwdSymbol = vwdSymbol;
            this.sortField = sortField;
            this.name = name;
            this.wpNameKurz = wpNameKurz;
            this.wpNameLang = wpNameLang;
            this.longName = longName;
            this.wpNameZusatz = wpNameZusatz;
            this.wmTicker = wmTicker;
            this.ticker = ticker;
            this.eurexTicker = eurexTicker;
            this.isin = isin;
            this.wkn = wkn;
            this.vwdCode = vwdCode;
            this.valor = valor;
            this.lei = lei;
        }

        public long getQid() {
            return qid;
        }

        public String getVwdSymbol() {
            return vwdSymbol;
        }

        public String getSortField() {
            return sortField;
        }

        public String getName() {
            return name;
        }

        public String getLei() {
            return lei;
        }

        public String getValor() {
            return valor;
        }

        public String getVwdCode() {
            return vwdCode;
        }

        public String getWpNameKurz() {
            return wpNameKurz;
        }

        public String getWpNameLang() {
            return wpNameLang;
        }

        public String getLongName() {
            return longName;
        }

        public String getWpNameZusatz() {
            return wpNameZusatz;
        }

        public String getWmTicker() {
            return wmTicker;
        }

        public String getTicker() {
            return ticker;
        }

        public String getEurexTicker() {
            return eurexTicker;
        }

        public String getIsin() {
            return isin;
        }

        public String getWkn() {
            return wkn;
        }
    }

    @Override
    public String toString() {
        return "ChainData {" +
                "chainInstrument='" + chainInstrument + '\'' +
                ", chainName='" + chainName + '\'' +
                ", chainFlag=" + chainFlag +
                ", elements.size='" + elements.length + '\'' +
                '}';
    }

}
