package de.marketmaker.istar.merger.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a comparator for beans, can be configured with a sort expression in the constructor
 *
 * @param <T> the bean class we want to sort
 * @author mwohlf
 *
 */
public class BeanSorter<T> implements Comparator<T> {
    static final Logger LOGGER = LoggerFactory.getLogger(BeanSorter.class);

    public static final String ASC = "asc";
    public static final String DESC = "desc";

    private static final String SORT_EXPR = "(\\w+)( +(" + ASC + "|" + DESC + "))?";
    private static final Pattern SORT_PATTERN = Pattern.compile(SORT_EXPR, Pattern.CASE_INSENSITIVE);
    private final HashMap<String, Method> sortFields = new HashMap<>();
    private final List<SortElement> currentSnorts = new ArrayList<>();


    public static class SortElement {
        private final String name;
        private final boolean ascending;

        public SortElement(String name, boolean ascending) {
            this.name = name;
            this.ascending = ascending;
        }

        public String getName() {
            return this.name;
        }

        public boolean isAscending() {
            return this.ascending;
        }
    }

    public static <U> Set<String> getSortableFields(Class<U> clazz) {
       return new BeanSorter(clazz) .getSortableFields();
    }

    private BeanSorter(Class<T> clazz) {
        try {
            initFields(clazz);
        } catch (IntrospectionException ex) {
            throw new IllegalArgumentException("can't initialize bean sorter for '" + clazz + "'", ex);
        }
    }

    public BeanSorter(Class<T> clazz, String sortString) {
        try {
          initFields(clazz);
          initSorts(sortString);
        } catch (IntrospectionException ex) {
            throw new IllegalArgumentException("can't initialize bean sorter for '"
                    + clazz + "' using sort string '" + sortString + "'", ex);
        }
    }

    public Set<String> getSortableFields() {
        return new HashSet<>(sortFields.keySet());
    }

    private void  initFields(Class<T> clazz) throws IntrospectionException {
        final PropertyDescriptor[] propDescriptors = BeanUtils.getPropertyDescriptors(clazz);
        for (PropertyDescriptor desc : propDescriptors) {
            sortFields.put(desc.getName(), desc.getReadMethod());
        }
    }

    private void initSorts(String multiSortingString) {
        currentSnorts.clear();
        final String[] subsorts = multiSortingString.split(",");
        for (String string : subsorts) {
            currentSnorts.add(createSingleSort(string.trim()));
        }
    }

    private SortElement createSingleSort(String singleSortString) {
        final Matcher matcher = SORT_PATTERN.matcher(singleSortString);
        if (matcher.matches()) {
            final String field = matcher.group(1);
            final String sortString = matcher.group(3);
            final boolean asc = (sortString == null) || sortString.equalsIgnoreCase(ASC);
            return new SortElement(field, asc);
        }
        throw new IllegalArgumentException("can't create sort for string '" + singleSortString
                + "', the regular expression for the match is '" + SORT_PATTERN.pattern() + "'");
    }

    @Override
    public int compare(T left, T right) {
        try {
            for (SortElement sort : currentSnorts) {
                final String propertyName = sort.getName();
                final Method method = sortFields.get(propertyName);
                if (method == null) {
                    throw new BeanCriterionException("problem while sorting beans, "
                            + "there is no getter for sort property '" + propertyName+ "'");
                }
                int result = compareProperty(method, left, right);
                if (result == 0) {
                    // try the next sort level
                    continue;
                }
                return sort.isAscending()?result:-result;
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new BeanCriterionException("problem while sorting beans, "
                    + "left bean was: " + left
                    + "right bean was: " + right
                    , ex);
        }
        return 0; // equal according to the sort fields
    }

    private int compareProperty(Method readMethod, T left, T right) throws InvocationTargetException, IllegalAccessException {
        final Object leftValue = readMethod.invoke(left);
        final Object rightValue = readMethod.invoke(right);

        final ComparatorRegistry.SortCompare comparator =  ComparatorRegistry.INSTANCE.getSortCompare(readMethod);
        if (comparator == null) {
            LOGGER.warn("no comparator found for sorting a bean according to its property, "
            + "getter for property is '" + readMethod + "', the type for failed comparator lookup is '" + readMethod.getReturnType() + "' "
            + "the registry is: " + ComparatorRegistry.INSTANCE);
            return 0;
        }
        return comparator.compare(leftValue, rightValue);
    }

}
