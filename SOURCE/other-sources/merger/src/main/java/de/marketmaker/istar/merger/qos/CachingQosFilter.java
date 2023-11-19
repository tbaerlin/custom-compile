package de.marketmaker.istar.merger.qos;

import java.io.Serializable;

import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public abstract class CachingQosFilter<T, V extends Serializable> extends QosFilter<T> {

    private QosFilterCache cache;

    private String keyPrefix;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (this.keyPrefix == null) {
            setKeyPrefix(this.beanName);
        }
    }

    public void setCache(QosFilterCache cache) {
        this.cache = cache;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
        this.logger.info("<setKeyPrefix> " + this.keyPrefix);
    }

    private String getKey(String key) {
        return this.keyPrefix + ":" + key;
    }

    protected void store(final String key, final V value) {
        if (this.cache != null) {
            this.cache.store(getKey(key), value);
        }
    }

    protected V retrieve(String key) {
        if (this.cache != null) {
            return (V) this.cache.retrieve(getKey(key));
        }
        return null;
    }
}
