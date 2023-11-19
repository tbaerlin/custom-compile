/*
 * AbstractLabelWithPopup.java
 *
 * Created on 08.04.13 09:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Markus Dick
 */
public abstract class AbstractLabelWithPopup<T>  extends HTML {
    public static final String WITH_ADD_INFO = "withAddInfo"; //$NON-NLS$

    private T popupData;
    private final PopupPanel popupPanel;

    protected abstract void updatePopupPanel();

    private HandlerRegistration clickHandlerRegistration;

    public AbstractLabelWithPopup(T popupData) {
        super();
        setStyleName("mm-LabelWithPopup"); //$NON-NLS$
        this.popupData = popupData;

        this.popupPanel = new PopupPanel(true);
        this.popupPanel.addStyleName("mm-LabelWithPopup"); //$NON-NLS$
        updateWidget();
    }

    public AbstractLabelWithPopup(String label, T popupData) {
        this(popupData);
        setText(label);
        setTitle(label);
    }

    public AbstractLabelWithPopup(SafeHtml safeHtml, T popupData) {
        this(popupData);
        setHTML(safeHtml);
    }

    protected void updateWidget() {
        if(getPopupData() != null) {
            addHandlers();
            addStyleName(WITH_ADD_INFO);
        }
        else {
            removeHandlers();
            removeStyleName(WITH_ADD_INFO);
        }
    }

    private void addHandlers() {
        if(this.clickHandlerRegistration == null) {
            this.clickHandlerRegistration = addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    showPopup();
                }
            });
        }
    }

    private void removeHandlers() {
        if(this.clickHandlerRegistration != null) {
            this.clickHandlerRegistration.removeHandler();
            this.clickHandlerRegistration = null;
        }
    }

    private void showPopup() {
        if(getPopupData() != null) {
            updatePopupPanel();
            getPopupPanel().showRelativeTo(this);
        }
    }

    protected int add(FlexTable t, int row, String value) {
        if(!StringUtil.hasText(value)) {
            return row;
        }

        t.setText(row, 0, value);
        t.getFlexCellFormatter().setColSpan(row, 0, 2);

        return ++row;
    }

    protected int add(FlexTable t, int row, String label, String value) {
        if(!StringUtil.hasText(value)) {
            return row;
        }

        t.setText(row, 0, label);
        t.setText(row, 1, value);

        return ++row;
    }

    protected final PopupPanel getPopupPanel() {
        return this.popupPanel;
    }

    public T getPopupData() {
        return this.popupData;
    }

    public void setPopupData(T popupData) {
        this.popupData = popupData;
        updateWidget();
    }

    protected String htmlEscape(String s) {
        if(s == null) return s;
        return SafeHtmlUtils.htmlEscape(s);
    }
}
