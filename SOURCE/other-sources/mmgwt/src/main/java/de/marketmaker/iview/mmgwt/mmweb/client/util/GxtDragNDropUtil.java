package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.widget.Component;

/**
 * Created on 20.05.2010 11:29:10
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class GxtDragNDropUtil<T> {

    private final GxtDragNDrop<T> dNd;
    private final String dNdGroupId;
    private final DropTarget target;

    public GxtDragNDropUtil(Component view, String dNdGroupId, GxtDragNDrop<T> dNd) {
        this.dNd = dNd;
        this.dNdGroupId = dNdGroupId;
        this.target = new DropTarget(view);
        initDd();
    }

    protected void initDd() {
        this.target.addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                //noinspection unchecked
                dNd.onDrop((T) e.getData());
            }

            @Override
            public void dragEnter(DNDEvent e) {
                //noinspection unchecked
                dNd.onDragEnter((T) e.getData(), e);            
            }
        });
        this.target.setGroup(this.dNdGroupId);
        this.target.setOverStyle("drag-ok"); // $NON-NLS-0$
    }

    public void disable() {
        this.target.disable();
    }

    public void enable() {
        this.target.enable();
    }
}
