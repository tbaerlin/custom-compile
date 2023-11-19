package de.marketmaker.istar.merger.web.easytrade.block;

import static de.marketmaker.istar.ratios.frontend.SearchParameterParser.SUFFIX_LOWER;
import static de.marketmaker.istar.ratios.frontend.SearchParameterParser.SUFFIX_UPPER;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.RatioFieldDescription.Field;
import de.marketmaker.istar.ratios.frontend.BestToolRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.BestToolVisitor;
import de.marketmaker.istar.ratios.frontend.DataRecordStrategy;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;
import java.util.Map;
import org.springframework.util.StringUtils;


public class BestToolRatioSearchMethod {
    private final BestToolCommand cmd;
    private final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields;
    private final InstrumentTypeEnum instrumentType;
    private final InstrumentTypeEnum[] additionalTypes;
    private final RatiosProvider ratiosProvider;

    public static BestToolRatioSearchMethod create(BestToolCommand cmd, RatiosProvider ratiosProvider) {
        return new BestToolRatioSearchMethod(cmd, ratiosProvider);
    }

    private BestToolRatioSearchMethod(BestToolCommand cmd, RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
        this.cmd = cmd;
        final PermissionType pt;
        this.instrumentType = cmd.getType();
        this.additionalTypes = cmd.getAdditionalType();
        if (instrumentType == InstrumentTypeEnum.FND) {
            pt = PermissionType.FUNDDATA;
        }
        else if (instrumentType == InstrumentTypeEnum.CER) {
            pt = null;
        }
        else {
            pt = null;
        }
        this.fields = AbstractFindersuchergebnis.getFields(instrumentType, pt);
    }


    protected BestToolRatioSearchResponse ratioSearch() {
        final RatioSearchRequest ratioSearchRequest = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        ratioSearchRequest.setType(this.instrumentType);
        ratioSearchRequest.setAdditionalTypes(this.additionalTypes);
        ratioSearchRequest.setVisitorClass(BestToolVisitor.class);

        final DataRecordStrategy.Type strategy = cmd.getDataRecordStrategy();
        if (strategy != null) {
            ratioSearchRequest.setDataRecordStrategyClass(strategy.getClazz());
        }

        final Map<String, String> parameters = AbstractFindersuchergebnis.parseQuery(this.cmd.getQuery(), this.fields);

        ratioSearchRequest.setParameters(parameters);
        parameters.put("n", Integer.toString(cmd.getNumResults()));

        final RatioFieldDescription.Field sortfield = getField(cmd.getSortField(), true);
        final RatioFieldDescription.Field primaryfield = getField(cmd.getPrimaryField(), true);
        final RatioFieldDescription.Field secondaryfield = getField(cmd.getSecondaryField(), false);

        if (StringUtils.hasText(this.cmd.getMaxSortFieldValue())) {
            parameters.put(sortfield.name() + (cmd.isAscending() ? SUFFIX_LOWER : SUFFIX_UPPER), this.cmd.getMaxSortFieldValue());
        }
        parameters.put(BestToolVisitor.KEY_GROUP_BY, primaryfield.name() +
                (secondaryfield == null ? "" : "," + secondaryfield.name()));
        parameters.put(BestToolVisitor.KEY_OPERATOR, (StringUtils.hasText(this.cmd.getPrimaryFieldOperator()) ? this.cmd.getPrimaryFieldOperator() : "")
                + (secondaryfield == null ? "" : ("," + (StringUtils.hasText(this.cmd.getSecondaryFieldOperator()) ? this.cmd.getSecondaryFieldOperator() : ""))));
        parameters.put(BestToolVisitor.KEY_SOURCE, sortfield.name());

        return getResponse(ratioSearchRequest);
    }

    public RatioFieldDescription.Field getField(String fieldName, boolean required) {
        final Field field =
            AbstractFindersuchergebnis.getField(this.fields, fieldName);
        if (required && field == null) {
            // AbstractFindersuchergebnis.getField will throw BadRequestException if field name
            // is invalid. If valid and null, only reason must be permission
            throw new PermissionDeniedException("not allowed field: " + fieldName);
        }
        return field;
    }

    public RatioFieldDescription.Field getPrimaryField() {
        return getField(this.cmd.getPrimaryField(), true);
    }

    private BestToolRatioSearchResponse getResponse(RatioSearchRequest request) {
        final RatioSearchResponse result = this.ratiosProvider.search(request);
        if (!result.isValid()) {
            throw new NoDataException("<getResponse> got invalid response from " + result.getServerInfo() + "! search failed");
        }
        return (BestToolRatioSearchResponse) result;
    }
}