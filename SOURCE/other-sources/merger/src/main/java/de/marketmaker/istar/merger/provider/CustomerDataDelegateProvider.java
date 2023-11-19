/*
 * CustomerDataDelegateProvider.java
 *
 * Created on 07.10.2008 13:44:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.customerData")
public interface CustomerDataDelegateProvider extends
        DerivativeIpoProviderDzbank,
        WGZCertificateProvider,
        DzBankRecordProvider {
}
