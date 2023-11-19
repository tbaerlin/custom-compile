/*
 * AmqpRpcAddressImplEditor.java
 *
 * Created on 09.03.2011 11:24:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.connections;

import java.beans.PropertyEditorSupport;

/**
 * PropertyEditor for {@link AmqpRpcAddress} properties. Because
 * this class resides in the same package and bears the name <tt><i>PropertyType</i><b>Editor</b></tt> it is
 * <b>automagically</b> used by Spring.
 * <p/>
 * So, if you are configuring a bean with a property {@code address} of type {@link AmqpRpcAddress},
 * thanks to this class you can write
<pre>{@code
<property name="address"
          value="exchange/queue?requestQueueMessageTTL=5000&amp;queueAutoDelete=true"/>
}</pre>
 * instead of
<pre>{@code
<property name="address">
    <bean class="de.marketmaker.itools.amqprpc.connections.AmqpRpcAddressImpl">
        <property name="pseudoUrl"
                  value="exchange/queue?requestQueueMessageTTL=5000&amp;queueAutoDelete=true"/>
    </bean>
</property>
}</pre>
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AmqpRpcAddressEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        AmqpRpcAddress value = (AmqpRpcAddress) getValue();
        return (value != null ? value.getPseudoUrl() : "");
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null) {
            setValue(null);
        } else {
            AmqpRpcAddressImpl address = new AmqpRpcAddressImpl();
            address.setPseudoUrl(text);
            setValue(address);
        }
    }
}
