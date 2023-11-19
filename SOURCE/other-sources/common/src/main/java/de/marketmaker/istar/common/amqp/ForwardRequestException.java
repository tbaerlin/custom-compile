/*
 * ServiceProviderSelection.java
 *
 * Created on 19.12.11 15:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.amqp;

/**
 * Used to tell the rabbitmq server to forward the current request to some other queue as we
 * don't have the data to answer it.
 * @author oflege
 */
public class ForwardRequestException extends RuntimeException {
    private final String queueName;

    public ForwardRequestException(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }
}
