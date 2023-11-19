package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

/**
 * Author: umaurer
 * Created: 15.08.14
 */
public interface InitSequenceProgressBox {
    void update(String message, int step, int count);
    void close();
}
