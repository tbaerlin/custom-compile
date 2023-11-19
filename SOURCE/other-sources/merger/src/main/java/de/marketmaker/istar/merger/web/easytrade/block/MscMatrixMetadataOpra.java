package de.marketmaker.istar.merger.web.easytrade.block;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataVisitor;
import de.marketmaker.istar.ratios.opra.OpraMatrixMetadataVisitor;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.web.QueryCommand;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;

import static de.marketmaker.istar.ratios.opra.OpraSymbolProviderImpl.FIELDS;

/**
 * <p>
 * Query, group and count distinct values of OpraData.<br/>
 * <br/>
 * Provided with a set of field names for grouping and a set of field names for distinct value counting,
 * this block returns a tree structure that contains the count of occurrences for each distinct value in each
 * specified group.<br/>
 * <br/>
 * Possible field names are the fields of OPT_FinderOpra.
 * </p>
 *
 * @author Michael Wohlfart
 */
public class MscMatrixMetadataOpra extends OpraFinder {

    // used in the keys of the leaf nodes that contain the counts
    static final String COUNT = "count";

    public static class Command  extends DefaultSymbolCommand implements QueryCommand {

        private String query;

        private String groupBy = "";  // must not be null

        private String countDistinct;

        /**
         * <p>
         * The names of the fields who's values will be counted in the defined groups.
         * Null values in the fields will be ignored, all other values will be counted.<br/>
         * For each field a unsorted set of distinct values with count of occurrence will be provided in the result.<br/>
         * Multiple comma separated fieldnames are allowed.
         * </p>
         *
         * @sample strike,expirationDate,isFlex,contractSize,optionCategory
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
         * Defines the fieldnames for the grouping that will be performed on the query's result set.
         * For each occurring value in a specified field a separate group will be created,
         * null field values will be grouped like any other valid value.<br/>
         * </p>
         *
         * @sample market,optionType,optionCategory,exerciseType,contractSize
         */
        public String getGroupBy() {
            return groupBy;
        }

        public void setGroupBy(String groupBy) {
            this.groupBy = groupBy;
        }

        /**
         * <p>
         * The query to be performed on the specified instrument type.
         * Valid fields to be used in the query are:<br/>
         * underlyingSymbol, market, symbol, vwdCode, optionType, version, strike, expirationDate
         * </p>
         * @sample expirationDate &gt; 2014-05-01
         */
        @Override
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

    }

    public MscMatrixMetadataOpra() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) {
        final Command cmd = (Command) o;

        checkPermission(Selector.OPRA);

        final String underlyingSymbol = getUnderlyingSymbol(cmd);
        if (underlyingSymbol == null) {
            return getModelAndViewForItems(null, null);
        }

        MatrixMetadataRatioSearchResponse searchResponse = createResponse(cmd.getQuery(), cmd.getGroupBy(), cmd.getCountDistinct(), underlyingSymbol);
        MatrixMetadataRatioSearchResponse.Node result = searchResponse.getResult();
        MscMatrixMetadata.addFieldTranslations(FIELDS, result);

        if (!searchResponse.isValid()) {
            errors.reject("opra.searchfailed", "invalid search response");
            return null;
        }

        return getModelAndViewForItems(result, underlyingSymbol);
    }

    private ModelAndView getModelAndViewForItems(MatrixMetadataRatioSearchResponse.Node result, String underlyingQuote) {
        final Map<String, Object> model = new HashMap<>();
        model.put("result", result);
        model.put("underlyingQuote", underlyingQuote);
        return new ModelAndView("mscmatrixmetadataopra", model);
    }

    protected MatrixMetadataRatioSearchResponse createResponse(String query, String groupfieldNames, String countFieldNames, String underlyingSymbol) {
        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.OPT, (String[]) null, null);
        final Map<String, String> parameters = parseQuery(query, FIELDS);

        replaceUnderlyingVwdcode(parameters);
        rsr.addParameters(parameters);

        rsr.addParameter("underlyingWkn", "'" + underlyingSymbol + "'");
        rsr.addParameter("n", Integer.toString(25000));  // default is 100 somewhere in the backend

        rsr.addParameter(MatrixMetadataVisitor.KEY_GROUP_BY, MscMatrixMetadata.getFieldnames(FIELDS, groupfieldNames));
        rsr.addParameter(MatrixMetadataVisitor.KEY_COUNT_DISTINCT, MscMatrixMetadata.getFieldnames(FIELDS, countFieldNames));
        rsr.setVisitorClass(OpraMatrixMetadataVisitor.class);

        return ratiosProvider.getOpraMatrix(rsr);
    }
}
