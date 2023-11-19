/*
 * AlertProvider.java
 *
 * Created on 16.12.2008 09:53:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.merger.alert")
public interface AlertProvider {

    AckAlertNotificationResponse ackAlerts(AckAlertNotificationRequest r);

    DeleteAlertResponse deleteAlert(DeleteAlertRequest r);

    InsertAlertUserResponse insertAlertUser(InsertAlertUserRequest r);

    RetrieveAlertUserResponse retrieveAlertUser(RetrieveAlertUserRequest r);

    RetrieveAlertsResponse retrieveAlerts(RetrieveAlertsRequest r);

    UpdateAlertResponse updateAlert(UpdateAlertRequest r);

    UpdateAlertUserResponse updateAlertUser(UpdateAlertUserRequest r);

    DeleteAlertUserResponse deleteAlertUser(DeleteAlertUserRequest r);

}
