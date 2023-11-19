/*
 * IssuerRatingReader.java
 *
 * Created on 07.05.12 12:12
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * @author zzhao
 */
public class IssuerRatingReader {

    private static final XMLInputFactory xif = XMLInputFactory.newInstance();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<IssuerRatingMetaDataKey, Set<Object>> metaData;

    private final List<IssuerRatingImpl> issuerRatings;

    private final StringBuilder sb;

    public IssuerRatingReader() {
        this.metaData = new HashMap<>(10);
        this.issuerRatings = new ArrayList<>(1024 * 32);
        this.sb = new StringBuilder(128);
    }

    public void parse(InputStream is) throws Exception {
        XMLStreamReader reader = null;
        try {
            reader = xif.createXMLStreamReader(is);
            parseIntern(reader);
        } finally {
            if (null != reader) {
                reader.close();
            }
        }
    }

    private void parseIntern(XMLStreamReader reader) throws XMLStreamException {
        String ln = null;
        int rowNr = 0;
        IssuerRatingImpl issuerRating = null;
        int event = -1;
        do {
            event = reader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    ln = reader.getLocalName();
                    if ("ROW".equals(ln)) {
                        issuerRating = new IssuerRatingImpl();
                        rowNr = Integer.parseInt(reader.getAttributeValue(0));
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.CDATA:
                    this.sb.append(reader.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    ln = reader.getLocalName();
                    if ("ROW".equals(ln)) {
                        if (isValidIssuerRating(issuerRating)) {
                            addIssuerRating(issuerRating);
                            if (this.issuerRatings.size() % 10000 == 0) {
                                this.logger.info("<parseIntern> parsed " + this.issuerRatings.size()
                                        + " issuer ratings");
                            }
                        }
                        else {
                            this.logger.warn("<parseIntern> invalid issuer rating at row: " + rowNr);
                        }
                    }
                    else if (!"ROWS".equals(ln)) {
                        try {
                            final IssuerRatingDescriptor desc = IssuerRatingDescriptor.valueOf(ln);
                            switch (desc) {
                                case SOURCE:
                                    issuerRating.setSource(RatingSource.fromFullName(getCurrentString()));
                                    break;
                                case VWDSYMBOL:
                                    /* remove feed type */
                                    final VendorkeyVwd v = VendorkeyVwd.getInstance(getCurrentString());
                                    if (VendorkeyVwd.ERROR != v) {
                                        issuerRating.setProperty(desc, v.toString());
                                    }
                                    break;
                                case ISSUERNAME:
                                case LEI:
                                case COUNTRYISO:
                                case CURRENCYISO:
                                    issuerRating.setProperty(desc, getCurrentString());
                                    break;
                                case RATING_FITCH_ISSUER_LT:
                                case RATING_FITCH_ISSUER_ST:
                                case RATING_FITCH_ISSUER_IFS:
                                case RATING_MDYS_ISSR_LT:
                                case RATING_MDYS_ISSR_ST:
                                case RATING_MDYS_ISSR_LT_B:
                                case RATING_MDYS_ISSR_ST_B:
                                case RATING_MDYS_ISSR_LT_SU:
                                case RATING_MDYS_ISSR_ST_SU:
                                case RATING_MDYS_ISSR_LT_SU_B:
                                case RATING_MDYS_ISSR_ST_SU_B:
                                case RATING_MDYS_ISSR_LT_BDR:
                                case RATING_MDYS_ISSR_ST_BDR:
                                case RATING_MDYS_ISSR_LT_BDR_B:
                                case RATING_MDYS_ISSR_ST_BDR_B:
                                case RATING_MDYS_ISSR_LT_IFSR:
                                case RATING_MDYS_ISSR_ST_IFSR:
                                case RATING_MDYS_ISSR_LT_IFSR_B:
                                case RATING_MDYS_ISSR_ST_IFSR_B:
                                case RATING_SNP_ISSUER_LT:
                                case RATING_SNP_ISSUER_ST:
                                case RATING_SNP_ISSUER_LT_FSR:
                                case RATING_SNP_ISSUER_ST_FSR:
                                case RATING_SNP_ISSUER_LT_FER:
                                case RATING_SNP_ISSUER_ST_FER:
                                    final String symbol = getCurrentString();
                                    if (StringUtils.isNotBlank(symbol)) {
                                        issuerRating.setProperty(desc, symbol.trim());
                                    }
                                    break;
                                case RATING_FITCH_ISSUER_LT_ACTION:
                                case RATING_FITCH_ISSUER_ST_ACTION:
                                case RATING_FITCH_ISSUER_IFS_ACTION:
                                case RATING_MDYS_ISSR_LT_A:
                                case RATING_MDYS_ISSR_ST_A:
                                case RATING_MDYS_ISSR_LT_A_B:
                                case RATING_MDYS_ISSR_ST_A_B:
                                case RATING_MDYS_ISSR_LT_A_SU:
                                case RATING_MDYS_ISSR_ST_A_SU:
                                case RATING_MDYS_ISSR_LT_A_SU_B:
                                case RATING_MDYS_ISSR_ST_A_SU_B:
                                case RATING_MDYS_ISSR_LT_A_BDR:
                                case RATING_MDYS_ISSR_ST_A_BDR:
                                case RATING_MDYS_ISSR_LT_A_BDR_B:
                                case RATING_MDYS_ISSR_ST_A_BDR_B:
                                case RATING_MDYS_ISSR_LT_A_IFSR:
                                case RATING_MDYS_ISSR_ST_A_IFSR:
                                case RATING_MDYS_ISSR_LT_A_IFSR_B:
                                case RATING_MDYS_ISSR_ST_A_IFSR_B:
                                case RATING_SNP_ISSUER_LT_ACTION:
                                case RATING_SNP_ISSUER_ST_ACTION:
                                case RATING_SNP_ISSUER_LT_FSR_ACTN:
                                case RATING_SNP_ISSUER_ST_FSR_ACTN:
                                case RATING_SNP_ISSUER_LT_FER_ACTN:
                                case RATING_SNP_ISSUER_ST_FER_ACTN:
                                    issuerRating.setProperty(desc, Entry.getAction(getCurrentString()));
                                    break;
                                case RATING_FITCH_ISSUER_LT_DATE:
                                case RATING_FITCH_ISSUER_ST_DATE:
                                case RATING_FITCH_ISSUER_IFS_DATE:
                                case RATING_MDYS_ISSR_LT_D:
                                case RATING_MDYS_ISSR_ST_D:
                                case RATING_MDYS_ISSR_LT_D_B:
                                case RATING_MDYS_ISSR_ST_D_B:
                                case RATING_MDYS_ISSR_LT_D_SU:
                                case RATING_MDYS_ISSR_ST_D_SU:
                                case RATING_MDYS_ISSR_LT_D_SU_B:
                                case RATING_MDYS_ISSR_ST_D_SU_B:
                                case RATING_MDYS_ISSR_LT_D_BDR:
                                case RATING_MDYS_ISSR_ST_D_BDR:
                                case RATING_MDYS_ISSR_LT_D_BDR_B:
                                case RATING_MDYS_ISSR_ST_D_BDR_B:
                                case RATING_MDYS_ISSR_LT_D_IFSR:
                                case RATING_MDYS_ISSR_ST_D_IFSR:
                                case RATING_MDYS_ISSR_LT_D_IFSR_B:
                                case RATING_MDYS_ISSR_ST_D_IFSR_B:
                                case RATING_SNP_ISSUER_LT_DATE:
                                case RATING_SNP_ISSUER_ST_DATE:
                                case RATING_SNP_ISSUER_LT_FSR_DATE:
                                case RATING_SNP_ISSUER_ST_FSR_DATE:
                                case RATING_SNP_ISSUER_LT_FER_DATE:
                                case RATING_SNP_ISSUER_ST_FER_DATE:
                                    issuerRating.setProperty(desc,
                                            Entry.getDate(getCurrentString()));
                                    break;
                                case RATING_SNP_ISSUER_LT_RID:
                                    issuerRating.setProperty(desc, Entry.getRegulatoryId(getCurrentString()));
                                    break;
                                default:
                                    /* do nothing */
                            }
                        } catch (IllegalArgumentException noEnumConstantException) {
                            /* do nothing - enables us to change the file layout in the future
                             * without being forced to immediately deploy a new reader.
                             */
                        }
                    }
                    this.sb.setLength(0);
                    break;
            }
        } while (XMLStreamConstants.END_DOCUMENT != event);
        this.logger.info("<parseIntern> parsed " + this.issuerRatings.size()
                + " issuer ratings totally");
    }

    private void addIssuerRating(IssuerRatingImpl issuerRating) {
        this.issuerRatings.add(issuerRating);
        for (IssuerRatingMetaDataKey mdk : IssuerRatingMetaDataKey.ALL_KEYS) {
            final Object prop = issuerRating.getProperty(mdk.getDesc());
            if (null != prop) {
                if (!this.metaData.containsKey(mdk)) {
                    this.metaData.put(mdk, new HashSet<>());
                }
                this.metaData.get(mdk).add(prop);
            }
        }
    }

    private boolean isValidIssuerRating(IssuerRatingImpl issuerRating) {
        return StringUtils.isNotBlank(issuerRating.getIssuerName())
                && null != issuerRating.getSource();
    }

    private String getCurrentString() {
        return StringUtils.normalizeSpace(this.sb.toString());
    }

    public List<IssuerRatingImpl> getIssuerRatings() {
        return this.issuerRatings;
    }

    public Map<IssuerRatingMetaDataKey, List<Object>> getMetaData() {
        final HashMap<IssuerRatingMetaDataKey, List<Object>> ret = new HashMap<>(10);
        for (Map.Entry<IssuerRatingMetaDataKey, Set<Object>> entry : this.metaData.entrySet()) {
            ret.put(entry.getKey(), getSortedList(entry.getValue()));
        }

        return ret;
    }

    private ArrayList<Object> getSortedList(Set<Object> set) {
        final TreeSet<Object> treeSet = new TreeSet<>();
        treeSet.addAll(set);
        ArrayList<Object> list = new ArrayList<>(set.size());
        for (Object o : treeSet) {
            list.add(o);
        }
        return list;
    }
}
