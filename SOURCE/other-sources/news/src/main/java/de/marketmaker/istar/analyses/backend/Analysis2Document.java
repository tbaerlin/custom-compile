package de.marketmaker.istar.analyses.backend;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;

import de.marketmaker.istar.analyses.frontend.AnalysesIndexConstants;

import static de.marketmaker.istar.news.frontend.NewsIndexConstants.FIELD_ID;
import static de.marketmaker.istar.news.frontend.NewsIndexConstants.FIELD_SHORTID;
import static org.joda.time.DateTimeConstants.MILLIS_PER_SECOND;

/**
 * generate the lucene search document from the protobuf analysis data,
 * the search fields might be remapped in RscFinderTermVisitor
 *
 * @author oflege
 */
public class Analysis2Document {

    static Document toDocument(Protos.Analysis analysis) {
        final Document document = new Document();
        // used as primary key in the DB backend to retrieve the protobuf blob
        stored(document, FIELD_ID, AnalysesProvider.encodeId(analysis.getId()));

        noNorms(document, FIELD_SHORTID, AnalysesProvider.encodeShortId(analysis.getId()));

        document.add(new NumericField(AnalysesIndexConstants.FIELD_DATE)
                .setIntValue(encodeTimestamp(analysis.getAgencyDate())));

        noNorms(document, AnalysesIndexConstants.FIELD_SOURCE, analysis.getSource());

        noNorms(document, AnalysesIndexConstants.FIELD_RECOMMENDATION, analysis.getRating().name());

        if (analysis.hasAgencyId()) {
            noNorms(document, AnalysesIndexConstants.FIELD_AGENCY_ID, analysis.getAgencyId());
        }

        for (int i = 0; i < analysis.getIidCount(); i++) {
            noNorms(document, AnalysesIndexConstants.FIELD_IID, Long.toString(analysis.getIid(i)));
        }
        for (int i = 0; i < analysis.getSymbolCount(); i++) {
            noNorms(document, AnalysesIndexConstants.FIELD_SYMBOL, analysis.getSymbol(i));
            tokenized(document, AnalysesIndexConstants.FIELD_TEXT, analysis.getSymbol(i));
        }
        if (analysis.getIidCount() == 0 && analysis.getSymbolCount() == 0) {
            // make sure analyses w/o instrument reference can be searched
            noNorms(document, AnalysesIndexConstants.FIELD_IID, "0");
        }
        for (int i = 0; i < analysis.getAnalystNameCount(); i++) {
            tokenized(document, AnalysesIndexConstants.FIELD_ANALYST, analysis.getAnalystName(i));
        }
        for (int i = 0; i < analysis.getBranchCount(); i++) {
            noNorms(document, AnalysesIndexConstants.FIELD_BRANCH, analysis.getBranch(i));
        }
        for (int i = 0; i < analysis.getSubcategoryCount(); i++) {
            noNorms(document, AnalysesIndexConstants.FIELD_SUBCATEGORY, analysis.getSubcategory(i));
        }
        for (int i = 0; i < analysis.getCategoryCount(); i++) {
            noNorms(document, AnalysesIndexConstants.FIELD_CATEGORY, analysis.getCategory(i));
        }
        for (int i = 0; i < analysis.getCountryCount(); i++) {
            noNorms(document, AnalysesIndexConstants.FIELD_COUNTRY, analysis.getCountry(i));
        }

        // additional search fields for provider WEBSIM
        if (analysis.getProvider() == Protos.Analysis.Provider.WEBSIM) {
            if (analysis.hasWebSimRaccfond()) {
                noNorms(document, AnalysesIndexConstants.FIELD_RACCFOND, analysis.getWebSimRaccfond());
            }
            if (analysis.hasWebSimRacctecn()) {
                noNorms(document, AnalysesIndexConstants.FIELD_RACCTECN, analysis.getWebSimRacctecn());
            }

        // additional search fields for provider DPAAFX, AWP
        } else if (analysis.getProvider() == Protos.Analysis.Provider.DPAAFX
                || analysis.getProvider() == Protos.Analysis.Provider.AWP) {
            // target price
            if (analysis.hasTarget()) {
                try {
                    noNorms(document, AnalysesIndexConstants.FIELD_TARGET, new Double(analysis.getTarget()));
                } catch (NumberFormatException ex) {
                    // ignored
                }
            }
            if (analysis.hasTimeframe()) {
                noNorms(document, AnalysesIndexConstants.FIELD_TIMEFRAME,analysis.getTimeframe());
            }
        }

        if (analysis.hasHeadline()) {
            tokenized(document, AnalysesIndexConstants.FIELD_HEADLINE, analysis.getHeadline());
            tokenized(document, AnalysesIndexConstants.FIELD_TEXT, analysis.getHeadline());
        }
        for (int i = 0; i < analysis.getTextCount(); i++) {
            tokenized(document, AnalysesIndexConstants.FIELD_TEXT, analysis.getText(i));
        }
        return document;
    }

    private static void noNorms(Document document, String name, Double value) {
        document.add(new NumericField(name).setDoubleValue(value));
    }

    public static int encodeTimestamp(long timestamp) {
        return (int) (timestamp / MILLIS_PER_SECOND);
    }

    private static void stored(Document document, String name, String value) {
        document.add(field(name, value, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    }

    private static void noNorms(Document document, String name, String value) {
        document.add(field(name, value, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
    }

    private static Field field(String name, String value, Field.Store store, Field.Index index) {
        final Field result = new Field(name, value, store, index);
        result.setOmitTermFreqAndPositions(true);
        return result;
    }

    private static void tokenized(Document document, String field, String value) {
        document.add(new Field(field, value, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.NO));
    }

}
