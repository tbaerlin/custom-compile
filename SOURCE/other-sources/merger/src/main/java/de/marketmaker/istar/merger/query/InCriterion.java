package de.marketmaker.istar.merger.query;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author mwohlf
 */
public class InCriterion extends BeanCriterion {

    private final Collection<String> values;
    private final String propertyName;

    InCriterion(String propertyName, Collection<String> values) {
        this.propertyName = propertyName;
        this.values = values;
    }

    @Override
    public boolean evaluate(Object bean) {
        try {
            return doEvaluate(getReadMethod(bean, propertyName), bean);
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException ex) {
            throw new BeanCriterionException("can't evaluate in criterion for '" + propertyName + "' "
                    + " in a Bean '" + bean + "' for values '" + values + "'", ex);
        }
    }

    protected boolean doEvaluate(Method readMethod, Object bean)
            throws InvocationTargetException, IllegalAccessException {
        final Object propertyValue = readMethod.invoke(bean);

        final ComparatorRegistry.MatchCompare comparator =  ComparatorRegistry.INSTANCE.getMatchCompare(readMethod);
        if (comparator != null) {
            for (String object : values) {
                if (comparator.isMatch(propertyValue, object)) {
                    return true;
                }
            }
            return false;
        } else {
            throw new BeanCriterionException("can't evaluate in-criterion for values '" + values
                    + "' on property named '" + propertyName + "',"
                    + " no filter for return type '" + readMethod.getReturnType().getName() + "' found"
                    + " current registry is: " + ComparatorRegistry.INSTANCE);
        }
    }

}
