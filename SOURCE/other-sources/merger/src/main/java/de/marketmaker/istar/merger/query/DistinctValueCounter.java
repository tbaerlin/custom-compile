package de.marketmaker.istar.merger.query;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * @param <T> the type of the property to analyze
 *
 * @author mwohlf
 */
public interface DistinctValueCounter<T> {

    public interface PropertyValueKey<T> {
        // for counting simple properties value and name should contain the same data,
        // however when counting underlying instruments, name is the instrument's name and value is the iid

        public T getValue();

        public String getName();

    }

    public String getPropertyName();

    public TreeMap<PropertyValueKey<T>, Integer> getValues();

}
