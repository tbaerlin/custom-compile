/*
 * WGZCertificateProvider.java
 *
 * Created on 20.01.2009 09:36:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.domain.data.WGZCertificateData;

import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface WGZCertificateProvider {
    List<WGZCertificateData> getWGZCertificateData();
}
