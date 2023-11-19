package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * Author: umaurer
 * Created: 27.02.15
 */
public class Finalizer<T> {
    private T t;

    public Finalizer() {
    }

    public Finalizer(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }

    public void set(T t) {
        this.t = t;
    }
}
