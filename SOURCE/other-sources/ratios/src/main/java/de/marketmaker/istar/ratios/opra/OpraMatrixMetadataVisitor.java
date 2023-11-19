package de.marketmaker.istar.ratios.opra;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.GenericSearchEngineVisitor;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse.GroupValueKey;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse.Node;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgVisitor;
import de.marketmaker.istar.ratios.frontend.SearchParameterParser;
import de.marketmaker.istar.ratios.frontend.Selectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import static de.marketmaker.istar.ratios.frontend.MatrixMetadataVisitor.COUNT;
import static de.marketmaker.istar.ratios.frontend.MatrixMetadataVisitor.KEY_COUNT_DISTINCT;
import static de.marketmaker.istar.ratios.frontend.MatrixMetadataVisitor.KEY_GROUP_BY;

public class OpraMatrixMetadataVisitor implements GenericSearchEngineVisitor<OpraItem, MatrixMetadataRatioSearchResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpraMatrixMetadataVisitor.class);

    private final MatrixMetadataRatioSearchResponse response = new MatrixMetadataRatioSearchResponse();

    private RatioFieldDescription.Field[] groupByFields;

    private RatioFieldDescription.Field[] countFields;

    private GroupValueKey[] countKeys;

    @Override
    public void init(SearchParameterParser spp) {
        this.groupByFields = getFields(spp, KEY_GROUP_BY);
        this.countFields = getFields(spp, KEY_COUNT_DISTINCT);
        this.countKeys = new GroupValueKey[this.countFields.length];
        for (int i = 0; i < this.countFields.length; i++) {
            countKeys[i] = new GroupValueKey(this.countFields[i].name(), COUNT);
        }
    }

    private RatioFieldDescription.Field[] getFields(SearchParameterParser parameterParser, String key) {
        final String[] groupBys = StringUtils.trimArrayElements(
                StringUtils.commaDelimitedListToStringArray(parameterParser.getParameterValue(key)));
        RatioFieldDescription.Field[] result = new RatioFieldDescription.Field[groupBys.length];
        for (int i = 0; i < groupBys.length; i++) {
            RatioFieldDescription.Field field = RatioFieldDescription.getFieldByName(groupBys[i]);
            if (field == null) {
                throw new IllegalArgumentException("field not found for : '" + groupBys[i] + "'");
            }
            result[i] = field;
        }
        return result;
    }

    @Override
    public void visit(OpraItem item) {
        if (hasCountValues(this.countFields, item)) {
            MatrixMetadataRatioSearchResponse.Node currentNode = initNodeChilds(item);
            increaseCount(this.countFields, currentNode, item);
        }
    }

    /**
     * Initializes node childs if they are not.
     */
    private MatrixMetadataRatioSearchResponse.Node initNodeChilds(OpraItem item) {
        MatrixMetadataRatioSearchResponse.Node node = this.response.getResult();
        for (RatioFieldDescription.Field field : this.groupByFields) {
            final String value = getFieldValueAsString(item, field);
            node = node.getChild(new GroupValueKey(field.name(), value));
        }
        return node;
    }

    /**
     * Opra data is not localized.
     * @return the non-localized field value
     */
    private String getFieldValueAsString(Selectable item, RatioFieldDescription.Field field) {
        return MinMaxAvgVisitor.getString(item, field, 0);
    }

    @Override
    public MatrixMetadataRatioSearchResponse getResponse() {
        return this.response;
    }

    private boolean hasCountValues(RatioFieldDescription.Field[] countFields, Selectable selectable) {
        for (RatioFieldDescription.Field field : countFields) {
            final String countValue = getFieldValueAsString(selectable, field);
            if (countValue != null) {
                return true;
            }
        }
        return false;
    }

    private void increaseCount(RatioFieldDescription.Field[] countFields,
                               Node currentNode, Selectable selectable) {
        for (RatioFieldDescription.Field countField : countFields) {
            final String countValue = getFieldValueAsString(selectable, countField);
            if (countValue != null) {
                Node countNode = currentNode.getChild(new GroupValueKey(countField.name(), COUNT));
                countNode.addCount(countValue);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<increaseCount> field: {}, count: {}", countField.toString(), countNode.getCount(countValue));
                }
            }
        }
    }

    public OpraMatrixMetadataVisitor merge(OpraMatrixMetadataVisitor v) {
        this.response.merge(v.response);
        return this;
    }
}
