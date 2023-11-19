package de.marketmaker.istar.merger.query;

import org.springframework.beans.BeanUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * base class for bean comparing and filtering,
 * single criteria can be combined into larger expressions to perform filtering based on bean property values
 *
 * @author mwohlf
 */
public abstract class BeanCriterion {

    public static final BeanCriterion True = new BeanCriterion() {
        @Override
        public boolean evaluate(Object bean) {
            return true;
        }
    };

    public static final BeanCriterion False = new BeanCriterion() {
        @Override
        public boolean evaluate(Object bean) {
            return false;
        }
    };

    static class Not extends BeanCriterion {

        private final BeanCriterion beanCriterion;

        public Not(BeanCriterion beanCriterion) {
            this.beanCriterion = beanCriterion;
        }

        @Override
        public boolean evaluate(Object bean) {
            return !beanCriterion.evaluate(bean);
        }
    }

    protected Method getReadMethod(Object bean, String propertyName) throws IntrospectionException {
        final PropertyDescriptor result = BeanUtils.getPropertyDescriptor(bean.getClass(), propertyName);
        if (result == null || result.getReadMethod() == null) {
            throw new BeanCriterionException("can't find read method for property named '" + propertyName + "' "
                    + " in a Bean of class '" + bean.getClass() + "'");
        }
        return result.getReadMethod();
    }

    public abstract boolean evaluate(Object bean);

}
