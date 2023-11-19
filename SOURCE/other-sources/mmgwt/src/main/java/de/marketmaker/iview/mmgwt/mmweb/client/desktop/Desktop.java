/*
 * Desktop.java
 *
 * Created on 14.08.2008 13:30:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.desktop;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ulrich Maurer
 */
public class Desktop<D> extends Composite {
    public enum Mode {
        FLOW,
        TABLE
    }

    private FlexTable tab;
    final Panel panel;
    private int rowCount = 0;
    private int colCount = 0;
    private Mode mode;

    public Desktop(Mode mode) {
        this.mode = mode;
        if (this.mode == Mode.TABLE) {
            this.tab = new FlexTable();
            this.panel = new SimplePanel();
            this.panel.add(this.tab);
        }
        else {
            this.panel = new FlowPanel();
        }
        this.panel.setStyleName("mm-desktop"); // $NON-NLS-0$
        initWidget(this.panel);
    }

    public void add(final DesktopIcon<D> desktopIcon) {
        final String[] subText = desktopIcon.getSubText();
        final Panel panelIcon;
        if (desktopIcon.getHref() == null) {
            panelIcon = new FlowPanel() {
                public void onBrowserEvent(Event event) {
                    if (event.getTypeInt() == Event.ONCLICK) {
                        final LinkContext<D> linkContext = new LinkContext<D>(desktopIcon.getListener(), desktopIcon.getData());
                        desktopIcon.getListener().onClick(linkContext, getElement());
                    }
                }
            };
            panelIcon.sinkEvents(Event.ONCLICK);
        }
        else {
            panelIcon = new LinkPanel(desktopIcon.getHref(), "_blank"); // $NON-NLS-0$
        }
        panelIcon.setStyleName("mm-desktopIcon"); // $NON-NLS-0$

        if (desktopIcon.getIconStyle() != null) {
            final Label labelIcon = new Label();
            labelIcon.setStyleName(desktopIcon.getIconStyle());
            panelIcon.add(labelIcon);
        }

        if (desktopIcon.getImageUrl() != null) {
            final Image image = new Image();
            image.setStyleName("mm-desktopChartIcon"); // $NON-NLS-0$
            image.setUrl(desktopIcon.getImageUrl());
            panelIcon.add(image);
        }

        for (String text : subText) {
            panelIcon.add(new Label(text));
        }
        if(subText.length > 0) {
            final List<String> token = Arrays.asList(subText);
            panelIcon.setTitle(StringUtil.join(", ", removeHasNoTextItems(token)));
        }

        if (this.mode == Mode.TABLE) {
            if (this.colCount > 2) {
                this.rowCount++;
                this.colCount = 0;
            }
            this.tab.setWidget(this.rowCount, this.colCount, panelIcon);
            this.colCount++;
        }
        else if (mode == Mode.FLOW) {
            this.panel.add(panelIcon);
        }
    }

    private List<String> removeHasNoTextItems(List<String> token) {
        final List<String> result = new ArrayList<String>();
        for (String s : token) {
            if (StringUtil.hasText(s)) {
                result.add(s);
            }
        }
        return result;
    }

    public void clear() {
        if (this.mode == Mode.TABLE) {
            this.tab.clear();
        } else {
            this.panel.clear();
        }
    }
}
