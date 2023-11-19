package de.marketmaker.istar.merger.provider.chain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.xml.AbstractSaxReader;

/**
 *
 */
class ChainsReader extends AbstractSaxReader {
    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final Map<String, ChainData> result = new HashMap<>();

    // chain metadata
    private String chainInstrument;

    private String chainName;

    private String chainFlag;

    private DateTime chainChangeDate;

    // content
    private List<ChainData.Element> chainElements = new LinkedList<>();

    // single element data
    private long qid = 0;

    private String vwdSymbol;

    private String sortField = "0";

    private String elementName;

    private String wpNameKurz;

    private String wpNameLang;

    private String longName;

    private String wpNameZusatz;

    private String wmTicker;

    private String ticker;

    private String eurexTicker;

    private String isin;

    private String wkn;

    private String vwdCode;

    private String valor;

    private String lei;


    @Override
    public void startElement(String uri, String localName, String tagName,
            Attributes attributes) throws SAXException {
        resetCurrentString();
    }

    @Override
    public void endElement(String uri, String localName, String tagName) throws SAXException {
        try {

            // -- chain header data

            if (tagName.equalsIgnoreCase("CHAIN")) {
                // finished a new chain
                result.put(
                        this.chainInstrument,
                        new ChainData(
                                this.chainInstrument,
                                this.chainName,
                                this.chainFlag,
                                this.chainChangeDate,
                                this.chainElements.toArray(new ChainData.Element[chainElements.size()])
                        ));
                this.chainInstrument = null;
                this.chainName = null;
                this.chainFlag = null;
                this.chainChangeDate = null;
                this.chainElements = new LinkedList<>();
            }
            else if (tagName.equalsIgnoreCase("CHAINNAME")) {  // might be chain or element name
                this.chainName = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("CHAININSTRUMENT")) {
                this.chainInstrument = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("CHAINFLAG")) {
                this.chainFlag = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("CHAINCHANGEDATE")) {
                this.chainChangeDate = getCurrentDateTime(DTF);
            }

            // -- inside element tag

            else if (tagName.equalsIgnoreCase("ELEMENT")) {
                if (this.qid == 0 && this.vwdSymbol == null) {
                    this.logger.error("<endElement> invalid element no qid or vwdSymbol "
                            + chainInstrument + "/" + chainName + "/" + qid + "/" + vwdSymbol);
                }
                else {
                    chainElements.add(new ChainData.Element(
                            this.qid,
                            this.vwdSymbol,
                            this.sortField,
                            this.elementName,
                            this.wpNameKurz,
                            this.wpNameLang,
                            this.longName,
                            this.wpNameZusatz,
                            this.wmTicker,
                            this.ticker,
                            this.eurexTicker,
                            this.isin,
                            this.wkn,
                            this.vwdCode,
                            this.valor,
                            this.lei));
                    this.qid = 0;
                    this.vwdSymbol = null;
                    this.sortField = "0";
                    this.elementName = null;
                    this.wpNameKurz = null;
                    this.wpNameLang = null;
                    this.longName = null;
                    this.wpNameZusatz = null;
                    this.wmTicker = null;
                    this.ticker = null;
                    this.eurexTicker = null;
                    this.isin = null;
                    this.wkn = null;
                    this.vwdCode = null;
                    this.valor = null;
                    this.lei = null;
                }
            }
            else if (tagName.equalsIgnoreCase("qid")) {
                this.qid = getCurrentLong();
            }
            else if (tagName.equalsIgnoreCase("vwdcode")) {
                this.vwdCode = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("vwdsymbol")) {
                this.vwdSymbol = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("sortField")) {
                this.sortField = getCurrentString();
            }

            else if (tagName.equalsIgnoreCase("name")) {
                this.elementName = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("longname")) {
                this.wpNameLang = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("wp_name_kurz")) {
                this.wpNameKurz = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("wp_name_lang")) {
                this.wpNameLang = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("wp_name_zusatz")) {
                this.wpNameZusatz = getCurrentString();
            }

            else if (tagName.equalsIgnoreCase("wm_ticker")) {
                this.wmTicker = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("ticker")) {
                this.ticker = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("eurexticker")) {
                this.eurexTicker = getCurrentString();
            }
            else if (tagName.equalsIgnoreCase("valor")) {
                this.valor = getCurrentString();
            }

            else if (tagName.equalsIgnoreCase("isin")) {
                this.isin = getCurrentString();
            }

            else if (tagName.equalsIgnoreCase("wkn")) {
                this.wkn = getCurrentString();
            }

            else if (tagName.equalsIgnoreCase("lei")) {
                this.lei = getCurrentString();
            }

            else if (tagName.equalsIgnoreCase("CHAINS")) {
                // root element, ignored
            }
            else {
                notParsed(tagName);
            }
        } catch (Exception ex) {
            this.logger.error("<endElement> error in " + tagName, ex);
            this.errorOccured = true;
        }
    }


    @Override
    protected void reset() {
        // not used
    }

    public Map<String, ChainData> getResult() {
        return result;
    }
}
