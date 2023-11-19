/*
 * QuoteMetadataProvider.java
 *
 * Created on 05.10.2008 17:25:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.quotemetadata;

import de.marketmaker.istar.domain.data.QuoteMetadata;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface QuoteMetadataProvider {
    QuoteMetadata getQuoteMetadata(long qid);
}
