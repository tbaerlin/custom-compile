package de.marketmaker.istar.merger.query;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * regex match a String
 *
 * @author mwohlf
 */
public class MatchCriterion extends BeanCriterion {

    private final String propertyName;
    private final String patternString;

    MatchCriterion(String propertyName, String patternString) {
        this.propertyName = propertyName;
        this.patternString = patternString;
    }

    @Override
    public boolean evaluate(Object bean) {
        try {
            return doEvaluate(getReadMethod(bean, propertyName), bean);
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException ex) {
            throw new BeanCriterionException("can't evaluate match condition for property named '"
                    + propertyName + "' " + " in a Bean '" + bean + "' using pattern '" + patternString + "'", ex);
        }
    }

    protected boolean doEvaluate(Method readMethod, Object bean)
            throws InvocationTargetException, IllegalAccessException {
        final Object propertyValue = readMethod.invoke(bean);
        final Class type = readMethod.getReturnType();

        if (type.equals(String.class)) {
            return isMatch((String)propertyValue, patternString);
        }
        throw new BeanCriterionException("can't evaluate match criterion for pattern '" + patternString
                + "' on property named '" + propertyName + "',  returning false");
    }

    protected boolean isMatch(String value, String patternString) {
        if (value == null) {
            return false;
        }

        final Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(value).find();
    }

}
