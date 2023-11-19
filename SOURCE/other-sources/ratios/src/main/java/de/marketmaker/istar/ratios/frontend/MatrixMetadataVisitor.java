package de.marketmaker.istar.ratios.frontend;


import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse.GroupValueKey;

public class MatrixMetadataVisitor implements MergeableSearchEngineVisitor<MatrixMetadataVisitor> {

    private static class AttributedField {
        private final RatioFieldDescription.Field field;
        private final boolean instrumentField;
        private final int localeIndex;
        private final String name;

        private AttributedField(RatioFieldDescription.Field field, List<Locale> locales) {
            this.field = field;
            this.instrumentField = field.isInstrumentField();
            this.localeIndex = RatioFieldDescription.getLocaleIndex(field, locales);
            this.name = field.name();
        }
    }

    public static final String COUNT = "count";

    // comma separated list of field names that will be grouped together
    public static final String KEY_GROUP_BY = "mmd:groupBy";
    // comma separated list of field names to count in the leaf nodes
    public static final String KEY_COUNT_DISTINCT = "mmd:count";

    private final MatrixMetadataRatioSearchResponse response = new MatrixMetadataRatioSearchResponse();

    private AttributedField[] groupByFields;
    private AttributedField[] countFields;
    private GroupValueKey[] countKeys;

    @Override
    public void init(SearchParameterParser parameterParser) {
        groupByFields = getFields(parameterParser, KEY_GROUP_BY);
        countFields = getFields(parameterParser, KEY_COUNT_DISTINCT);
        countKeys = new GroupValueKey[countFields.length];
        for (int i = 0; i < countFields.length; i++) {
            countKeys[i] = new GroupValueKey(countFields[i].name, COUNT);
        }
    }

    private AttributedField[] getFields(SearchParameterParser parameterParser, String key) {
        final List<Locale> locales = parameterParser.getLocales();

        final String[] groupBys = StringUtils.trimArrayElements(
                StringUtils.commaDelimitedListToStringArray(parameterParser.getParameterValue(key)));
        AttributedField[] result = new AttributedField[groupBys.length];
        for (int i = 0; i < groupBys.length; i++) {
            RatioFieldDescription.Field field = RatioFieldDescription.getFieldByName(groupBys[i]);
            if (field == null) {
                throw new IllegalArgumentException("field not found for : '" + groupBys[i] + "'");
            }
            result[i] = new AttributedField(field, locales);
        }
        return result;
    }

    @Override
    public void visit(RatioData data) {
        final QuoteRatios qr = data.getSearchResult();

        if (hasOnlyNullCountValues(qr)) {
            return;
        }

        MatrixMetadataRatioSearchResponse.Node node = initNodeChilds(qr);

        for (int i = 0; i < this.countFields.length; i++) {
            final String value = getFieldValueAsString(qr, countFields[i]);
            if (value != null) {
                final MatrixMetadataRatioSearchResponse.Node countNode = node.getChild(countKeys[i]);
                countNode.addCount(value);
            }
        }
    }

    private MatrixMetadataRatioSearchResponse.Node initNodeChilds(QuoteRatios qr) {
        MatrixMetadataRatioSearchResponse.Node node = this.response.getResult();
        for (AttributedField field : this.groupByFields) {
            final String value = getFieldValueAsString(qr, field);
            node = node.getChild(new GroupValueKey(field.name, value));
        }
        return node;
    }

    private boolean hasOnlyNullCountValues(QuoteRatios qr) {
        return Stream.of(this.countFields)
                .map(countField -> getFieldValueAsString(qr, countField))
                .allMatch(Objects::isNull);
    }

    private String getFieldValueAsString(QuoteRatios qr, AttributedField af) {
        final Selectable selectable = af.instrumentField ? qr.getInstrumentRatios() : qr;
        return MinMaxAvgVisitor.getString(selectable, af.field, af.localeIndex);
    }

    @Override
    public RatioSearchResponse getResponse() {
        return response;
    }

    @Override
    public MatrixMetadataVisitor merge(MatrixMetadataVisitor that) {
        this.response.merge(that.response);
        return this;
    }

    public static void main(String[] args) throws Exception {
        // InstrumentTypeEnum.WNT, InstrumentTypeEnum.WNT
        InstrumentTypeEnum type = InstrumentTypeEnum.OPT;

        final FileRatioDataStore store = new FileRatioDataStore();
        store.setBaseDir(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/ratios"));
        TypeData data = new TypeData(type);
        store.restore(type, data);

        final TimeTaker tt = new TimeTaker();
        final RatioSearchRequest ratioRequest = new RatioSearchRequest(ProfileFactory.valueOf(true), null);
        ratioRequest.setType(type);
        ratioRequest.setDataRecordStrategyClass(DefaultDataRecordStrategy.class);
        ratioRequest.setVisitorClass(MatrixMetadataVisitor.class);
        final Map<String, String> mmaParameters = new HashMap<>();

        mmaParameters.put(MatrixMetadataVisitor.KEY_GROUP_BY,
             //   "exerciseType, vwdmarket, optionCategory, strikePrice, expires");
             //   "exerciseType, vwdmarket, optionCategory");
                  "vwdmarket, optionCategory, exerciseType");
        mmaParameters.put(MatrixMetadataVisitor.KEY_COUNT_DISTINCT,
                  "expires, strikePrice");
             //  expires, currency, referenceprice, issuercategory, marketmanagername
        ratioRequest.setParameters(mmaParameters);

        final SearchParameterParser spp = new SearchParameterParser(ratioRequest, null);
        final ForkJoinPool pool = new ForkJoinPool(10);
        final RatioSearchResponse response = data.searchAndVisit(spp, pool);

        System.out.println("response: " + response);
        System.out.println("time: " + tt.getElapsedMs());
    }

}
