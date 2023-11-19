package de.marketmaker.istar.merger.web.easytrade.block;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DataRecordStrategy;
import de.marketmaker.istar.ratios.frontend.MetaDataMapKey;
import de.marketmaker.istar.ratios.frontend.RatioDataRecordImpl;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

import static de.marketmaker.istar.merger.web.easytrade.block.CdsFinder.Command.DEFAULT_SORT_BY;
import static de.marketmaker.istar.ratios.frontend.SearchParameterParser.PREFIX_SORT;
import static de.marketmaker.istar.ratios.frontend.SearchParameterParser.SUFFIX_DESC;

/**
 * Queries credit default swaps that match given criteria.
 * <p>
 * Criteria are specified by the <code>query</code> parameter, which is composed of field predicates.
 * Note that some fields have only limited values. Those values can be queried using
 * {@see CDS_FinderMetadata}.
 * </p>
 * <p>
 * Allowed search fields can be found in the sort field lists in the response.
 * </p>
 *
 */
public class CdsFinder extends AbstractFindersuchergebnis {


    public static class Command extends ListCommand {

        public static final String DEFAULT_SORT_BY = "issuername";

        public static final boolean DEFAULT_ASCENDING = true;

        private String query;

        public Command() {
            setSortBy(DEFAULT_SORT_BY);
            setAscending(DEFAULT_ASCENDING);
        }

        /**
         * search term.
         * @sample currency='JPY'
         */
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            if (StringUtils.hasText(query)) {
                this.query = query;
            }
        }

    }

    public CdsFinder() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields = getFields(InstrumentTypeEnum.ZNS);
        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.ZNS, null,
                DataRecordStrategy.Type.DEFAULT, fields, true, null);

        final Map<String, String> parameters = parseQuery(cmd.getQuery(), fields);
        rsr.addParameters(parameters);
        rsr.addParameter("znsCategory", "CDS");

        final List<String> sortfields = asSortfields(fields);

        final ListResult listResult = ListResult.create(cmd, sortfields, DEFAULT_SORT_BY, 0);
        addSortFields(fields, rsr, listResult);

        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.getAnzahl()));

        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final Map<String, Object> model = createResultModel(sr, listResult, fields, true, false);   // with prices, without all quotes

        final List<String> translateMe = Arrays.asList(RatioDataRecord.Field.restructuringRule.name(), RatioDataRecord.Field.debtRanking.name());
        replaceElementsWithTranslation((Map<MetaDataMapKey, Object>) model.get("metadata"), translateMe);

        ArrayList<RatioDataRecordImpl> elements = (ArrayList<RatioDataRecordImpl>) model.get("records");
        model.put("records", wrapForTranslation(elements));

        return new ModelAndView("cdsfinder", model);
    }

    private static void addSortFields(
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields,
            RatioSearchRequest rsr, ListResult listResult) {
        final List<ListResult.Sort> sorts = listResult.getSorts();
        // TODO: HACK, without frontend multiple sort support, add maturity sort implicitly
        boolean sortOnMaturity = false;
        for (int i = 0; i < sorts.size(); i++) {
            final int sortSer = i + 1;
            final RatioFieldDescription.Field ratioField = fields.get(
                    RatioDataRecord.Field.valueOf(sorts.get(i).getName()));
            if (RatioFieldDescription.maturity == ratioField) {
                sortOnMaturity = true;
            }
            rsr.addParameter(PREFIX_SORT + sortSer, ratioField.name());
            rsr.addParameter(PREFIX_SORT + sortSer + SUFFIX_DESC, Boolean.toString(!sorts.get(i).isAscending()));
        }

        if (!sortOnMaturity) {
            final int sortSer = sorts.size() + 1;
            rsr.addParameter(PREFIX_SORT + sortSer, RatioFieldDescription.maturity.name());
            rsr.addParameter(PREFIX_SORT + sortSer + SUFFIX_DESC, Boolean.toString(false));
        }
    }

    private void replaceElementsWithTranslation(Map<MetaDataMapKey, Object> metadata,
            List<String> translateMe) {
        for (Map.Entry<MetaDataMapKey, Object> entry : metadata.entrySet()) {
            MetaDataMapKey key = entry.getKey();
            if (translateMe.contains(key.getName())) {
                metadata.put(key, CdsFinderMetadata.localizeItemList((List<FinderMetaItem>) (metadata.get(key)), key.getName()));
            }
        }
    }

    private ArrayList<RatioDataRecord> wrapForTranslation(ArrayList<RatioDataRecordImpl> elements) {
        ClassLoader loader = this.getClass().getClassLoader();
        ArrayList<RatioDataRecord> result = new ArrayList<>();
        for (RatioDataRecordImpl record : elements) {
            Class[] interfaces = new Class[]{RatioDataRecord.class};
            LocalizationWrapper localizer = new LocalizationWrapper(record);
            RatioDataRecord proxy = (RatioDataRecord) Proxy.newProxyInstance(loader, interfaces, localizer);
            result.add(proxy);
        }
        return result;
    }

    private static class LocalizationWrapper implements InvocationHandler {
        private Object object;

        public LocalizationWrapper(Object object) {
            this.object = object;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(this.object, args);
            String name = method.getName();
            if (result != null) {
                String key = result.toString();
                if ("getRestructuringRule".equals(name)) {
                    result = CdsFinderMetadata.localize("restructuringRule." + key, key);
                }
                else if ("getDebtRanking".equals(name)) {
                    result = CdsFinderMetadata.localize("debtRanking." + key, key);
                }
            }
            return result;
        }
    }

}
