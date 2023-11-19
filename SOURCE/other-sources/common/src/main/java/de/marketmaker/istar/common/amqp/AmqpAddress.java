/*
 * QueueName.java
 *
 * Created on 05.03.15 08:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.amqp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for interfaces that are supposed to be end points of amqp remote procedure calls.
 * This annotation is processed by instances of
 * <tt>de.marketmaker.itools.amqprpc.impl.AmqpPostProcessor</tt> (see amqp-rpc git repository
 * and that class's javadoc for a detailed description or/and usages of that class in
 * application contexts in the <tt>config</tt> submodule for examples).
 * <p>
 * The <tt>AmqpPostProcessor</tt> has to be configured with the names of or references to
 * beans that implement services that should be made available to other programs via amqp and
 * have been annotated with <tt>@AmpqAddress</tt>. The post processor will also look for
 * <tt>ref="amqp..."</tt> properties on all beans in its application context. If the context
 * does not contain a bean with that name and the bean property refers to a service annotated
 * with this annotation, the post processor will add a corresponding proxy bean that
 * performs the remote procedure calls.
 * <p>
 * The <tt>AmqpPostProcessor</tt> also takes care of selecting the appropriate rabbitmq host
 * and adds all supporting amqp beans.
 * </p>
 *
 * @author oflege
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AmqpAddress {
    /**
     * name of the queue over which the annotated service is provided. If the service is
     * exported on a machine with DOMAINID=="DEV" (i.e., a developer machine),
     * <tt>.${MACHINEID}</tt> will be appended to the queue name and the lifetime of the amqp
     * queue will be limited to the lifetime of the service that created it.
     * <p>
     * If a service provided on a local machine (as described above, e.g., <tt>foo.bar.machineX</tt>)
     * should be used from another process, that process should be started with
     * <tt>-Damqp.local.queues=foo.bar(,another.queue)*</tt>
     * , which causes the post processor to append the
     * machine name to those queue names as well.
     */
    String queue();

    String exchange() default "istar.rpc";

    /**
     * after how long (ms) a broker may discard a message if it has not been able to forward
     * the message to a consumer.
     */
    int ttl() default 10000;

    /**
     * how long (ms) clients are expected to wait for results for requests issued on this queue
     */
    int rpcTimeout() default 10000;

    /**
     * @return iff true, each consumer will create an additional queue named
     * <tt>{@link de.marketmaker.istar.common.Constants#MACHINE_NAME} + "." + queue() </tt>, which
     * can be used to send requests explicitly to this consumer.
     */
    boolean dedicatedConsumer() default false;
}
