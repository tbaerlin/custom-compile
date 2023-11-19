package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

/**
 * Author: umaurer
 * Created: 27.03.14
 */
public abstract class BindKey {
    private BindKey prev;
    private BindKey next;

    public BindKey getPrev() {
        return prev;
    }

    public void setPrev(BindKey prev) {
        this.prev = prev;
    }

    public BindKey withParent(BindKey parent) {
        setPrev(parent);
        return this;
    }

    public BindKey getNext() {
        return next;
    }

    public void setNext(BindKey next) {
        this.next = next;
    }

    public BindKey withNext(BindKey child) {
        setNext(child);
        return this;
    }
}
