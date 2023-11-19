package de.marketmaker.istar.merger.web.easytrade.block;


import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DataRecordStrategy;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataVisitor;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Query, group and count distinct values of RatioData.<br/>
 * <br/>
 * Provided with a set of field names for grouping and a set of field names for distinct value counting,
 * this block returns a tree structure that contains the count of occurrences for each distinct value in each
 * specified group.<br/>
 * <br/>
 * </p>
 * @author Michael Wohlfart
 */
public class MscMatrixMetadata extends AbstractFindersuchergebnis {

    public static class Command extends DefaultSymbolCommand {
        private String providerPreference;

        private InstrumentTypeEnum instrumentType;

        private String groupBy;

        private String countDistinct;

        private String query;

        private boolean withSiblingsForUnderlying;

        /**
         * Specifies data from which provider is preferred.
         */
        @RestrictedSet("VWD,SMF")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }

        /**
         * <p>
         * Specifies the instrument type to be queried (e.g. OPT).
         * </p>
         * @sample OPT
         */
        @NotNull
        public InstrumentTypeEnum getInstrumentType() {
            return instrumentType;
        }

        public void setInstrumentType(InstrumentTypeEnum instrumentType) {
            this.instrumentType = instrumentType;
        }

        /**
         * <p>
         * The names of the fields who's values will be counted in the defined groups.
         * Null values in the fields are ignored, all other values are counted.<br/>
         * For each field a unsorted set of distinct values with count of occurrence is provided in the result.<br/>
         * Multiple comma separated fieldnames are allowed.
         * </p>
         * @sample strike, expirationDate
         */
        @NotNull
        public String getCountDistinct() {
            return countDistinct;
        }

        public void setCountDistinct(String countDistinct) {
            this.countDistinct = countDistinct;
        }

        /**
         * <p>
         * Defines the fieldnames for the grouping that is performed on the query's result set.
         * For each occurring value in a specified field a separate group will be created,
         * null field values will be grouped like any other valid value.<br/>
         * </p>
         * @sample market, optionCategory, exerciseType
         */
        @NotNull
        public String getGroupBy() {
            return groupBy;
        }

        public void setGroupBy(String groupBy) {
            this.groupBy = groupBy;
        }

        /**
         * The query to be performed on the specified instrument type.
         */
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * @return if set to true, instruments having the same underlying will also be returned.
         */
        public boolean isWithSiblingsForUnderlying() {
            return withSiblingsForUnderlying;
        }

        public void setWithSiblingsForUnderlying(boolean withSiblingsForUnderlying) {
            this.withSiblingsForUnderlying = withSiblingsForUnderlying;
        }
    }

    public MscMatrixMetadata() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Map<String, Object> model = new HashMap<>();
        final Command cmd = (Command) o;
        final Quote underlyingQuote = this.instrumentProvider.getQuote(cmd);
        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields = getFields(cmd.getInstrumentType(), cmd.getProviderPreference());

        RatioSearchResponse searchResult = search(cmd, underlyingQuote, fields);
        if (!searchResult.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final MatrixMetadataRatioSearchResponse searchResponse = (MatrixMetadataRatioSearchResponse) searchResult;
        final MatrixMetadataRatioSearchResponse.Node result = searchResponse.getResult();

        addFieldTranslations(fields, result);

        model.put("result", result);
        model.put("underlyingQuote", underlyingQuote);
        return new ModelAndView("mscmatrixmetadata", model);
    }

    public static void addFieldTranslations(
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields,
            MatrixMetadataRatioSearchResponse.Node result) {
        if (result.getChildren() == null) {
            return;
        }

        for (final Map.Entry<MatrixMetadataRatioSearchResponse.GroupValueKey, MatrixMetadataRatioSearchResponse.Node> entry : result.getChildren().entrySet()) {
            final String group = entry.getKey().getGroup();
            entry.getKey().withTranslation(translate(fields, group));

            if (!result.getChildren().isEmpty()) {
                addFieldTranslations(fields, entry.getValue());
            }
        }
    }

    private static String translate(Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields,
            String group) {
        for (final Map.Entry<RatioDataRecord.Field, RatioFieldDescription.Field> entry : fields.entrySet()) {
            if (entry.getValue() != null && entry.getValue().name().equals(group)) {
                return entry.getKey().name();
            }
        }
        slogger.warn("<translate> unknown field " + group);
        return group;
    }

    private RatioSearchResponse search(Command cmd, Quote underlyingQuote,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        final RatioSearchRequest searchRequest = createRequest(cmd.getInstrumentType(), null, DataRecordStrategy.Type.DEFAULT);
        final Map<String, String> parameters = parseQuery(cmd.getQuery(), fields);
        parameters.put("underlyingiid", Long.toString(underlyingQuote.getInstrument().getId()));
        replaceUnderlyingSymbol(parameters, cmd.isWithSiblingsForUnderlying());
        searchRequest.addParameters(parameters);
        searchRequest.addParameter(MatrixMetadataVisitor.KEY_GROUP_BY, getFieldnames(fields, cmd.getGroupBy()));
        searchRequest.addParameter(MatrixMetadataVisitor.KEY_COUNT_DISTINCT, getFieldnames(fields, cmd.getCountDistinct()));
        searchRequest.setVisitorClass(MatrixMetadataVisitor.class);
        return this.ratiosProvider.search(searchRequest);
    }

    public static String getFieldnames(Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields,
            final String names) {
        final String[] tokens = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(names));
        final StringBuilder sb = new StringBuilder();
        for (final String token : tokens) {
            sb.append(",");
            RatioFieldDescription.Field field = getField(fields, token);
            if (field == null) {
                throw new BadRequestException("unknown field: " + token);
            }
            sb.append(field.name());
        }
        return sb.substring(1);
    }
}
