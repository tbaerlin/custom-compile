package de.marketmaker.istar.merger.query;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;

/**
 * collection of comparators for supported bean properties,
 * also handles annotations on the getterMethod to use a user defined comparator
 *
 * @author mwohlf
 */
public enum ComparatorRegistry {
    INSTANCE;

    public static final DateTimeFormatter DEFAULT_DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    // we have two different usecases for the comparators:
    //
    // - sorting:
    //   means that the comparator must fully comply with the requirements of the
    //   Java API Comparator interface: reflexive, transitive, anticommutative...
    //   one additional requirement we also need here is the comparator must be able
    //   to handle null values comping from a bean property and string values coming
    //   from a query term, the comparator itself is responsible fo translating the
    //   string into a comparable object value
    //
    // - filtering/matching:
    //   means we only need to detect if we have a match with some kind of string based
    //   filter criterion, typically we use the 0 return value of the comparator for this,
    //   but this kind of comparator might not comply with the requirements for sorting
    //
    public interface SortCompare<T> extends Comparator<T> {
        public int compare(T value1, String value2);
    }

    public interface MatchCompare<T> {
        public boolean isMatch(T value1, String value2);
    }


    private final OrSetStringMatcher orSetStringMatcher = new OrSetStringMatcher();
    private final IgnoreCaseStringComparator stringComparator = new IgnoreCaseStringComparator();
    private final BooleanComparator booleanComparator = new BooleanComparator();
    private final DateTimeComparator dateTimeComparator = new DateTimeComparator();
    private final LocalDateComparator localDateComparator = new LocalDateComparator();
    private final LongComparator longComparator = new LongComparator();
    private final IntegerComparator integerComparator = new IntegerComparator();
    private final FloatComparator floatComparator = new FloatComparator();
    private final BigDecimalComparator bigDecimalComparator = new BigDecimalComparator();

    /*
     * used for sorting and for "less than", "more than" criterion
     */
    SortCompare getSortCompare(Method getterMethod) {
        if (!getterMethod.isAnnotationPresent(UseComparator.class)) {
            return sorterMap.get(getterMethod.getReturnType().getName());
        } else {
            Class<?> sorterClass = getterMethod.getAnnotation(UseComparator.class).sort();
            if (sorterClass.equals(Void.class)) {
                return sorterMap.get(getterMethod.getReturnType().getName());
            } else {
                return (SortCompare) getEnhancedComparator(sorterClass, getterMethod, SortCompare.class);
            }
        }
    }

    /*
     * used for "in" or "equals" criterion
     */
    MatchCompare getMatchCompare(Method getterMethod) {
        if (!getterMethod.isAnnotationPresent(UseComparator.class)) {
            return matcherMap.get(getterMethod.getReturnType().getName());
        } else {
            Class<?> filterClass = getterMethod.getAnnotation(UseComparator.class).filter();
            if (filterClass.equals(Void.class)) {
                return matcherMap.get(getterMethod.getReturnType().getName());
            } else {
                return (MatchCompare) getEnhancedComparator(filterClass, getterMethod, MatchCompare.class);
            }
        }
    }

    // creates an instance of a comparator and checks if this is the class we need
    // also print some nice error messages in case something goes wrong
    private Object getEnhancedComparator(Class<?> clazz, Method getterMethod, Class<?> neededClass) {
        try {
            Object result = clazz.getConstructor().newInstance();
            if (!neededClass.isAssignableFrom(result.getClass())) {
                throw new BeanCriterionException(""
                        + "problem while creating comparator for getterMethod: '" + getterMethod
                        + "' the comparator class was configured by annotation, the class is: '"
                        + clazz + "', the class needs to implement '" + neededClass + "'"
                        + " and apparently doesn't, please check the implementation and try again");
            }
            return result;
        } catch (NoSuchMethodException ex) {
            throw new BeanCriterionException(""
                    + "exception while creating comparator for getterMethod: '" + getterMethod
                    + "' comparator class was configured by annotation, the class should have been: '"
                    + clazz + "', check if this class has a default public visible constructor without parameters"
                    + ", expect more errors", ex);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new BeanCriterionException(""
                    + "exception while creating comparator for getterMethod '" + getterMethod
                    + "', expect more errors", ex);
        }
    }

    public String toString() {
        return "ComparatorRegistry; contains the following maps:"
                + "sorterMap: [" + sorterMap.keySet().toString() + "]"
                + "matcherMap: [" + matcherMap.keySet().toString() + "]";
    }

    private final HashMap<String, SortCompare<?>> sorterMap = new HashMap<String, SortCompare<?>>() {{
        put(String.class.getName(), stringComparator);
        put(DateTime.class.getName(), dateTimeComparator);
        put(LocalDate.class.getName(), localDateComparator);
        put(Boolean.class.getName(), booleanComparator);
        put("boolean", booleanComparator);
        put(Long.class.getName(), longComparator);
        put("long", longComparator);
        put(Integer.class.getName(), integerComparator);
        put("int", integerComparator);
        put(Float.class.getName(), floatComparator);
        put("float", floatComparator);
        put(BigDecimal.class.getName(), bigDecimalComparator);
    }};

    private final HashMap<String, MatchCompare<?>> matcherMap = new HashMap<String, MatchCompare<?>>() {{
        put(String.class.getName(), orSetStringMatcher);
        put(DateTime.class.getName(), dateTimeComparator);
        put(LocalDate.class.getName(), localDateComparator);
        put(Boolean.class.getName(), booleanComparator);
        put("boolean", booleanComparator);
        put(Long.class.getName(), longComparator);
        put("long", longComparator);
        put(Integer.class.getName(), integerComparator);
        put("int", integerComparator);
        put(Float.class.getName(), floatComparator);
        put("float", floatComparator);
        put(BigDecimal.class.getName(), bigDecimalComparator);
    }};


    // a special comparator that handles string values with included "@" as
    // set of string which are "or-compared" with the parameter
    // this only works for string and makes no sense for sorting
    private static class OrSetStringMatcher implements MatchCompare<String> {
        @Override
        public boolean isMatch(String value1, String value2) {
            if (value1 == null ^ value2 == null) {
                return false;
            }
            if (value2 == null) {
                return true;
            }
            String[] values = value2.split("@");
            for (String value : values) {
                if (value1.equalsIgnoreCase(value)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class BooleanComparator implements SortCompare<Boolean>, MatchCompare<Boolean> {
        @Override
        public boolean isMatch(Boolean value1, String value2) {
            return compare(value1, value2) == 0;
        }
        @Override
        public int compare(Boolean value1, String value2) {
            return compare(value1, Boolean.parseBoolean(value2));
        }
        @Override
        public int compare(Boolean value1, Boolean value2) {
            if (value1 == null ^ value2 == null) {
                return (value1 == null) ? -1 : 1;
            }
            if (value2 == null) {
                return 0;
            }
            return value1.compareTo(value2);
        }
    }

    // a string comparator that is not case sensitive
    private static class IgnoreCaseStringComparator implements SortCompare<String>, MatchCompare<String> {
        @Override
        public boolean isMatch(String value1, String value2) {
            return compare(value1, value2) == 0;
        }
        @Override
        public int compare(String value1, String value2) {
            if (value1 == null ^ value2 == null) {
                return (value1 == null) ? -1 : 1;
            }
            if (value2 == null) {
                return 0;
            }
            return value1.compareToIgnoreCase(value2);
        }
    }

    private static class DateTimeComparator implements SortCompare<DateTime>, MatchCompare<DateTime> {
        @Override
        public boolean isMatch(DateTime value1, String value2) {
            return compare(value1, value2) == 0;
        }
        @Override
        public int compare(DateTime value1, String value2) {
            return compare(value1, ComparatorRegistry.DEFAULT_DTF.parseDateTime(value2));
        }
        @Override
        public int compare(DateTime value1, DateTime value2) {
            if (value1 == null ^ value2 == null) {
                return (value1 == null) ? 1 : -1;
            }
            if (value2 == null) {
                return 0;
            }
            return value1.compareTo(value2);
        }
    }

    private static class LocalDateComparator implements SortCompare<LocalDate>, MatchCompare<LocalDate> {
        @Override
        public boolean isMatch(LocalDate value1, String value2) {
            return compare(value1, value2) == 0;
        }
        @Override
        public int compare(LocalDate value1, String value2) {
            return compare(value1, ComparatorRegistry.DEFAULT_DTF.parseLocalDate(value2));
        }
        @Override
        public int compare(LocalDate value1, LocalDate value2) {
            if (value1 == null ^ value2 == null) {
                return (value1 == null) ? 1 : -1;
            }
            if (value2 == null) {
                return 0;
            }
            return value1.compareTo(value2);
        }
    }

    private static class LongComparator implements SortCompare<Long>, MatchCompare<Long> {
        @Override
        public boolean isMatch(Long value1, String value2) {
            return compare(value1, value2) == 0;
        }
        @Override
        public int compare(Long value1, String value2) {
            return compare(value1, Long.parseLong(value2));
        }
        @Override
        public int compare(Long value1, Long value2) {
            if (value1 == null ^ value2 == null) {
                return (value1 == null) ? -1 : 1;
            }
            if (value2 == null) {
                return 0;
            }
            return value1.compareTo(value2);
        }
    }

    private static class IntegerComparator implements SortCompare<Integer>, MatchCompare<Integer> {
        @Override
        public boolean isMatch(Integer value1, String value2) {
            return compare(value1, value2) == 0;
        }
        @Override
        public int compare(Integer value1, String value2) {
            return compare(value1, Integer.parseInt(value2));
        }
        @Override
        public int compare(Integer value1, Integer value2) {
            if (value1 == null ^ value2 == null) {
                return (value1 == null) ? -1 : 1;
            }
            if (value2 == null) {
                return 0;
            }
            return value1.compareTo(value2);
        }
    }

    private static class FloatComparator implements SortCompare<Float>, MatchCompare<Float> {
        @Override
        public boolean isMatch(Float value1, String value2) {
            return compare(value1, value2) == 0;
        }
        @Override
        public int compare(Float value1, String value2) {
            return compare(value1, Float.parseFloat(value2));
        }
        @Override
        public int compare(Float value1, Float value2) {
            if (value1 == null ^ value2 == null) {
                return (value1 == null) ? -1 : 1;
            }
            if (value2 == null) {
                return 0;
            }
            return value1.compareTo(value2);
        }
    }

    private static class BigDecimalComparator implements SortCompare<BigDecimal>, MatchCompare<BigDecimal> {
        @Override
        public boolean isMatch(BigDecimal value1, String value2) {
            return compare(value1, value2) == 0;
        }
        @Override
        public int compare(BigDecimal value1, String value2) {
            return compare(value1, new BigDecimal(value2));
        }
        @Override
        public int compare(BigDecimal value1, BigDecimal value2) {
            if (value1 == null ^ value2 == null) {
                return (value1 == null) ? -1 : 1;
            }
            if (value2 == null) {
                return 0;
            }
            return value1.compareTo(value2);
        }
    }

}
