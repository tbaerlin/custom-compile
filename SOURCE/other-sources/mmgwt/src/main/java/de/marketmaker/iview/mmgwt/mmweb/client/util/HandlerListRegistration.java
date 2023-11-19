package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.event.shared.HandlerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: umaurer
 * Created: 09.06.15
 */
public class HandlerListRegistration implements HandlerRegistration {
    private final List<HandlerRegistration> list = new ArrayList<>();

    public void add(HandlerRegistration hr) {
        this.list.add(hr);
    }

    @Override
    public void removeHandler() {
        for (HandlerRegistration hr : this.list) {
            hr.removeHandler();
        }
        this.list.clear();
    }
}
