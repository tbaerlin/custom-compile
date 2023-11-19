package de.marketmaker.istar.merger.qos;


import de.marketmaker.istar.merger.provider.pages.GisPageProvider;

public class GisPageproviderQosFilter extends CachingQosFilter<GisPageProvider, String> 
        implements GisPageProvider {

    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    protected boolean tryService() throws Exception {
        return this.delegate.getPage(this.key) != null;
    }

    public String getPage(String key) throws Exception {
        if (isEnabled()) {
            final String page = this.delegate.getPage(key);
            store(key, page);
            return page;
        }
        return retrieve(key);
    }
}