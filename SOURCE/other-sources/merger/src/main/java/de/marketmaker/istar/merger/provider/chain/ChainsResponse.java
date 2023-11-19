package de.marketmaker.istar.merger.provider.chain;

import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 *
 */
public class ChainsResponse extends AbstractIstarResponse {
    protected static final long serialVersionUID = 1L;

    private final List<ChainData> elements;
    private int total;

    public ChainsResponse(List<ChainData> elements, int total) {
        this.elements = elements;
        this.total = total;
    }

    public List<ChainData> getElements() {
        return elements;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
