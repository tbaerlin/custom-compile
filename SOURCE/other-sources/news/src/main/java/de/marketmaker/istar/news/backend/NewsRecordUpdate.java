/* NewsRecordUpdate.java
 *
 * Created on28.09.2009 09:34:25
 *
 * Copyright(c)MARKET MAKER Software AG.All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;

import static de.marketmaker.istar.news.frontend.NewsIndexConstants.FIELD_SYMBOL;
import static org.springframework.util.StringUtils.collectionToDelimitedString;

/**
 * An update to be performed on a NewsRecord. Limited to fields that contain enumerated values.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class NewsRecordUpdate {

    private static final String DELIMITER = ",";

    private final VwdFieldDescription.Field field;

    private final NewsAttributeEnum attributeEnum;

    private final Map<String, String> mappings;

    NewsRecordUpdate(VwdFieldDescription.Field field, String oldValue, String newValue) {
        this(field, Collections.singletonMap(oldValue, newValue));
    }

    NewsRecordUpdate(VwdFieldDescription.Field field, Map<String, String> mappings) {
        this.field = field;
        this.attributeEnum = NewsAttributeEnum.byField(field);
        this.mappings = mappings;
    }

    @Override
    public String toString() {
        return "NewsRecordUpdate{" + this.field + ": " + this.mappings + '}';
    }

    Query toQuery() {
        final String termField = getTermField();
        final boolean symbolField = isSymbolField();

        final BooleanQuery result = new BooleanQuery(true);
        for (String key : mappings.keySet()) {
            final String value = key.toLowerCase();
            result.add(termQuery(termField, value), BooleanClause.Occur.SHOULD);
            if (symbolField) {
                result.add(termQuery(FIELD_SYMBOL, value), BooleanClause.Occur.SHOULD);
            }
        }
        return result;
    }

    private TermQuery termQuery(String termField, String value) {
        return new TermQuery(new Term(termField, value));
    }

    private boolean isSymbolField() {
        return this.field == VwdFieldDescription.NDB_ISINList
                || this.field == VwdFieldDescription.NDB_Wpknlist;
    }

    private String getTermField() {
        return NewsIndexConstants.ATTRIBUTE_2_FIELDNAME.get(this.attributeEnum);
    }

    void apply(NewsRecordEditor editor) {
        String field = editor.getField(this.field);
        if (!StringUtils.hasText(field)) {
            return;
        }
        Set<String> unique = new LinkedHashSet<>();
        boolean mappedAny = false;
        for (String value : field.split(DELIMITER)) {
            String updatedValue = this.mappings.get(value);
            if (updatedValue != null) {
                unique.add(updatedValue);
                mappedAny = true;
            }
            else {
                unique.add(value);
            }
        }
        if (mappedAny) {
            editor.replaceField(this.field, collectionToDelimitedString(unique, DELIMITER));
        }
    }
}
