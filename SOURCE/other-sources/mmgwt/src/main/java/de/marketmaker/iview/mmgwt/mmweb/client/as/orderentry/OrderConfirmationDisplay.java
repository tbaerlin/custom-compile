/*
 * OrderConfirmationDisplay.java
 *
 * Created on 05.02.13 15:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Dick
 */
public interface OrderConfirmationDisplay<P extends OrderConfirmationDisplay.Presenter> {
    void setPresenter(P presenter);

    void setTitle(String title);
    void show();
    void hide();

    void setExecuteButtonText(String text);
    void setExecuteButtonVisible(boolean visible);
    void setCancelButtonText(String text);
    void setCancelButtonVisible(boolean visible);
    void setBackButtonVisible(boolean visible);
    void setBackButtonText(String text);

    void setColumns(int count);
    void setSections(List<Section> sections);
    void setPrintDate(MmJsDate date);
    void setPrintDateVisible(boolean visible);
    String getPrintHtml();

    boolean isShowing();

    public interface Presenter {
        void onExecuteClicked();
        void onCancelClicked();
        void onBackClicked();
        void onPrintClicked();
    }

    static class Section {
        private final String headline;
        private ArrayList<SimpleEntry> entries;
        private final int colspan;

        public Section(String headline) {
            this(headline, 1);
        }

        public Section(String headline, int colspan) {
            this.headline = headline;
            this.colspan = colspan;
        }

        public String getHeadline() {
            return this.headline;
        }

        public ArrayList<SimpleEntry> getEntries() {
            if(this.entries == null) {
                this.entries = new ArrayList<SimpleEntry>();
            }
            return this.entries;
        }

        public int getColumnSpan() {
            return this.colspan;
        }
    }

    static class WidgetEntry extends SimpleEntry {
        private final Widget widget;

        public WidgetEntry(Widget widget) {
            super(null);
            this.widget = widget;
        }

        public Widget getWidget() {
            return this.widget;
        }
    }

    static class Entry extends SimpleEntry {
        private String label;

        public Entry(String label, String value) {
            super(value);
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    static class SimpleEntry {
        private String value;

        public SimpleEntry(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
}
