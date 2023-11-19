/*
 * MasterDataFundProfiler.java
 *
 * Created on 20.11.2009 10:24:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import org.springframework.beans.BeanUtils;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.NullMasterDataFund;

/**
 * Applies a profile to a MasterDataFund object, thereby possibly hiding some of the fields
 * in that object. Since MasterDataFundImpl objects are immutable, we create a copy and set only
 * those properties that are allowed. This object does not know anything about the profile itself,
 * it has to be instantiated with the set of properties allowed by the respective profile.
 * @author oflege
 */
@ThreadSafe
public class MasterDataFundProfiler {

    private final Map<Method, Method> propertiesToCopy = new HashMap<>();

    private final Set<String> customers;

    private static final Class<MasterDataFundImpl.Builder> BUILDER_CLASS
            = MasterDataFundImpl.Builder.class;

    private static final Class<MasterDataFund> DATA_CLASS = MasterDataFund.class;

    public final static MasterDataFundProfiler ALLOW_ALL = new MasterDataFundProfiler() {
        @Override
        public MasterDataFund applyTo(MasterDataFund adaptee) {
            return adaptee;
        }
    };

    public MasterDataFundProfiler(String... allowedProperties) {
        this(null, allowedProperties);
    }

    public MasterDataFundProfiler(Collection<String> customers, String... allowedProperties) {
        this(customers, Arrays.asList(allowedProperties));
    }

    /**
     * Creates new instance that will adapt objects by copying only those properties
     * that appear in the allowedProperties
     * @param customers if not null, list of allowed customer names in field customer
     * @param allowedProperties properties that are allowed
     */
    public MasterDataFundProfiler(Collection<String> customers,
            Collection<String> allowedProperties) {
        this.customers = customers == null ? null : new HashSet<>(customers);
        for (String property : allowedProperties) {
            final PropertyDescriptor propToRead = getPropertyToRead(property);
            final PropertyDescriptor propToWrite = getPropertyToWrite(property);
            this.propertiesToCopy.put(propToRead.getReadMethod(), propToWrite.getWriteMethod());
        }
    }

    private static PropertyDescriptor getPropertyToRead(String property) {
        final PropertyDescriptor result = BeanUtils.getPropertyDescriptor(DATA_CLASS, property);
        if (result == null || result.getReadMethod() == null) {
            throw new IllegalArgumentException(property + " not readable in " + DATA_CLASS.getName());
        }
        return result;
    }

    private static PropertyDescriptor getPropertyToWrite(String property) {
        final PropertyDescriptor result = BeanUtils.getPropertyDescriptor(BUILDER_CLASS, property);
        if (result == null || result.getWriteMethod() == null) {
            // if no standard write method exists: try finding a localized version of the property
            final Method lm = tryLocalizedMethod(BUILDER_CLASS.getMethods(), property);
            if (lm != null) {
                try {
                    return new LocalizedStringPropertyDescriptor(property, lm);
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }
            }
            throw new IllegalArgumentException(property + " not writable in " + BUILDER_CLASS.getName());
        }
        return result;
    }

    private static Method tryLocalizedMethod(Method[] methods, String property) {
        for (final Method method : methods) {
            if (method.getName().toLowerCase().endsWith(property.toLowerCase())) {
                if (isLocalizedMethod(method)) {
                    return method;
                }
            }
        }
        return null;
    }

    private static boolean isLocalizedMethod(Method method) {
        final Class<?>[] classes = method.getParameterTypes();
        return classes[0].isAssignableFrom(String.class) && classes[1].isArray() && classes[1].getComponentType().isAssignableFrom(Language.class);
    }

    /**
     * Creates an adapted copy of adaptee that will only have those properties set that are
     * known to be allowed by this instance.
     * @param adaptee to be adapted
     * @return adapted version of adaptee
     */
    public MasterDataFund applyTo(MasterDataFund adaptee) {
        if (adaptee == null || adaptee == NullMasterDataFund.INSTANCE) {
            return adaptee;
        }

        if (adaptee.getCustomer() != null
                && (this.customers == null || !this.customers.contains(adaptee.getCustomer()))) {
            return NullMasterDataFund.INSTANCE;
        }

        final MasterDataFundImpl.Builder builder = new MasterDataFundImpl.Builder();

        builder.setInstrumentid(adaptee.getInstrumentid());

        try {
            for (Map.Entry<Method, Method> e : this.propertiesToCopy.entrySet()) {
                final Method readMethod = e.getKey();
                final Method writeMethod = e.getValue();

                if (readMethod.getReturnType().isAssignableFrom(LocalizedString.class)) {
                    // for LocalizedString properties: copy content of all languages
                    final LocalizedString ls = (LocalizedString) readMethod.invoke(adaptee);
                    for (final Language language : ls.getLanguages()) {
                        final String value = ls.getLocalized(language);
                        writeMethod.invoke(builder, value, new Language[]{language});
                    }
                }
                else {
                    writeMethod.invoke(builder, readMethod.invoke(adaptee));
                }
            }
        }
        catch (Exception ex) {
            // this should never happen
            throw new RuntimeException("invoke failed for " + adaptee, ex);
        }

        return builder.build();
    }

    /**
     * Very simple subclass of PropertyDescriptor to wrap LocalizedString methods for supporting
     * the copy task in #applyTo.
     */
    private static class LocalizedStringPropertyDescriptor extends PropertyDescriptor {
        private Method writeMethod;

        public LocalizedStringPropertyDescriptor(String property,
                Method writeMethod) throws IntrospectionException {
            super(property, null, null);
            this.writeMethod = writeMethod;
        }

        @Override
        public Method getWriteMethod() {
            return this.writeMethod;
        }
    }
}
