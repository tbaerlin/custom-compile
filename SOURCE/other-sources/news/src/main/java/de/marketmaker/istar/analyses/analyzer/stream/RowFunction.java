package de.marketmaker.istar.analyses.analyzer.stream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * turning an Entry<?,?> element into a list of String values according to a set of fieldnames
 * the list is exactly one row in the final result this is used to get a generic and
 * dynamic view with selectable columns
 * the inpput/entry element consists of a key value pair
 *
 *  @param <K> key class of the input value
 *  @param <V> value class of the input value
 */
public class RowFunction<K, V> implements Function<Entry<K, V>, List<String>> {

    // use the key for a column
    private static Function<Entry<?,?>, String> fromKey(Function<Object, String> inner) {
        return entry -> inner.apply(entry.getKey());
    }

    // use the value for a column
    private static Function<Entry<?,?>, String> fromValue(Function<Object, String> inner) {
        return entry -> inner.apply(entry.getValue());
    }

    // for each column we keep a function that picks the right property from the entry's key/value
    private final Map<String, Function<Entry<?,?>, String>> pickFunctions = new LinkedHashMap<>();

    /**
     * configure a row in the result
     * @param columns selected columns for the result set
     * @param keyClass key class in the entry object
     * @param valueClass value class in the entry object
     */
    public RowFunction(Iterable<String> columns, Class<K> keyClass, Class<V> valueClass) {
        columns.forEach(field -> {
            Function<Object, String> function;
            // the field might be picked either from value...
            function = Columns.FIELDS.get(valueClass, field);
            if (function != null) {
                pickFunctions.put(field, fromValue(function));
                return;
            }
            // ... or from key
            function = Columns.FIELDS.get(keyClass, field);
            if (function != null) {
                pickFunctions.put(field, fromKey(function));
                return;
            }
            // nothing found
            pickFunctions.put(field, Columns.UNKNOWN_FIELD_FUNCTION);
        });
    }

    @Override
    public List<String> apply(Entry<K, V> entry) {
        final List<String> result = new ArrayList<>(pickFunctions.size());
        pickFunctions.values().forEach(function -> result.add(function.apply(entry)));
        return result;
    }

}
