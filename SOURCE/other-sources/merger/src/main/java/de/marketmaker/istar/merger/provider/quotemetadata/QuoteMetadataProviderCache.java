/*
 * QuoteMetadataProviderCache.java
 *
 * Created on 14.09.2009 09:38:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.quotemetadata;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import de.marketmaker.istar.domain.data.QuoteMetadata;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class QuoteMetadataProviderCache implements QuoteMetadataProvider {
    private Ehcache cache;

    private QuoteMetadataProvider delegate;

    public void setDelegate(QuoteMetadataProvider delegate) {
        this.delegate = delegate;
    }

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    public QuoteMetadata getQuoteMetadata(long qid) {
        final Element element = this.cache.get(qid);
        if (element != null) {
            return (QuoteMetadata) element.getValue();
        }
        final QuoteMetadata result = this.delegate.getQuoteMetadata(qid);
        this.cache.put(new Element(qid, result));
        return result;
    }
}
