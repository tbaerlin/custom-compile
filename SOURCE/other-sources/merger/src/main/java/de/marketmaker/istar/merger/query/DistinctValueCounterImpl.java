package de.marketmaker.istar.merger.query;

import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.TreeMap;

/**
 * counting occurrences of property values for providing metatdata in finder
 *
 * @param <B> the bean type
 * @param <P> the property type
 * @author mwohlf
 */
public class DistinctValueCounterImpl<B, P extends Serializable> implements Serializable, DistinctValueCounter<P> {
    protected static final long serialVersionUID = 2L;

    private final String propertyName;
    private final TreeMap<PropertyValueKey<P>, Integer> values = new TreeMap<>();

    public DistinctValueCounterImpl(String propertyName) {
        this.propertyName = propertyName;
    }

    public DistinctValueCounterImpl<B, P> countNonNullValues(List<B> beans)
            throws InvocationTargetException, IllegalAccessException {
        if (beans.size() == 0) {
            return this;
        }
        final Class clazz = beans.get(0).getClass();
        final PropertyDescriptor desc = BeanUtils.getPropertyDescriptor(clazz, propertyName);
        if (desc == null || desc.getReadMethod() == null) {
            throw new IllegalArgumentException("can't find read method in '" + clazz + "' for property string '" + propertyName + "'");
        }
        final Method readMethod = desc.getReadMethod();
        for (B bean : beans) {
            final P value = (P) readMethod.invoke(bean);
            if (value != null) {
                final DistinctValueKeyImpl key = new DistinctValueKeyImpl(value, value == null ? null : value.toString());
                Integer count = values.get(key);
                count = (count == null) ? 1 : count + 1;
                values.put(key, count);
            }
        }
        return this;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public TreeMap<PropertyValueKey<P>, Integer> getValues() {
        return values;
    }

}
