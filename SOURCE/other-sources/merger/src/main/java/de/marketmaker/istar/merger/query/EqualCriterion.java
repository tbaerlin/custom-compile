package de.marketmaker.istar.merger.query;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EqualCriterion extends BeanCriterion {

    private final String propertyName;
    private final String compareValue;

    EqualCriterion(String propertyName, String compareValue) {
        this.propertyName = propertyName;
        this.compareValue = compareValue;
    }

    @Override
    public boolean evaluate(Object bean) {
        try {
            return doEvaluate(getReadMethod(bean, propertyName), bean);
       } catch (IntrospectionException | InvocationTargetException | IllegalAccessException ex) {
            throw new BeanCriterionException("can't evaluate condition for '" + propertyName + "' "
                    + " in a Bean '" + bean + "' for compareValue '" + compareValue + "'", ex);
        }
    }

    protected boolean doEvaluate(Method readMethod, Object bean)
            throws InvocationTargetException, IllegalAccessException {
        final Object propertyValue = readMethod.invoke(bean);

        final ComparatorRegistry.MatchCompare comparator =  ComparatorRegistry.INSTANCE.getMatchCompare(readMethod);
        if (comparator != null) {
            return comparator.isMatch(propertyValue, compareValue);
        } else {
            throw new BeanCriterionException("can't evaluate equal criterion for value '" + compareValue
                    + "' on property named '" + propertyName + "',"
                    + " no filter for return type '" + readMethod.getReturnType().getName() + "' found"
                    + " current registry is: " + ComparatorRegistry.INSTANCE);
        }
    }

}
