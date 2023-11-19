/*
 * NullFactoryBean.java
 *
 * Created on 11.11.2010 14:13:52
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.spring;

import org.springframework.beans.factory.FactoryBean;

/**
 * A bean that can be used to "create" null objects. Useful if you need a reference set can either
 * be set to a valid bean or that needs to be null. Example:
 * <pre>
 * &lt;property name="foo" ref="${foo}"//>
 * ...
 * &lt;bean id="realFoo" class="Foo"//>
 * &lt;bean id="nullFoo" class="de.marketmaker.istar.common.spring.NullFactoryBean"//>
 * </pre>
 * @author oflege
 */
public class NullFactoryBean implements FactoryBean {
    public Void getObject() throws Exception {
        return null;
    }

    public Class<? extends Void> getObjectType() {
        return null;
    }

    public boolean isSingleton() {
        return true;
    }
}
