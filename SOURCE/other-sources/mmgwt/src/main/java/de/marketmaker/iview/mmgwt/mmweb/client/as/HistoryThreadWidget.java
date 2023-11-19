/*
 * HistoryThreadWidget.java
 *
 * Created on 30.11.12
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.HistoryThreadEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.HistoryThreadHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadManager;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;

import java.util.Collections;
import java.util.List;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * @author Ulrich Maurer
 */
public class HistoryThreadWidget extends Composite implements HistoryThreadHandler {
    private static HistoryThreadWidget INSTANCE = null;
    private final HistoryThreadManager manager;
    private int currentThreadId = -1;
    private final IconImageIcon iconMenu;
    private final IconImageIcon iconNew;
    private final IconImageIcon iconDel;

    public static HistoryThreadWidget getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HistoryThreadWidget();
        }
        return INSTANCE;
    }

    public HistoryThreadWidget() {
        this.manager = AbstractMainController.INSTANCE.getHistoryThreadManager();
        this.currentThreadId = this.manager.getActiveThreadId();

        this.iconNew = IconImage.getIcon("as-plus-24"); // $NON-NLS$
        this.iconNew.setStyleName("thread");
        this.iconNew.withClickHandler(event -> changeThread(-1));
        Tooltip.addQtip(this.iconNew, I18n.I.threadNew());

        this.iconDel = IconImage.getIcon("as-minus-24"); // $NON-NLS$
        this.iconDel.setStyleName("thread");
        this.iconDel.withClickHandler(event -> manager.delThread(currentThreadId));
        Tooltip.addQtip(this.iconDel, I18n.I.threadDelete());

        this.iconMenu = IconImage.getIcon("thread-menu"); // $NON-NLS$
        this.iconMenu.setStyleName("thread");
        this.iconMenu.withClickHandler(event -> showThreadPopup(iconMenu));
        Tooltip.addQtip(this.iconMenu, I18n.I.threadMenu());

        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("as-historyControl");
        panel.add(this.iconNew);
        panel.add(this.iconDel);
        panel.add(this.iconMenu);

        initWidget(panel);
        EventBusRegistry.get().addHandler(HistoryThreadEvent.getType(), this);
        updateButtonState();
    }

    private void updateButtonState() {
        final List<Integer> threadIds = this.manager.getAllHistoryThreadIds();
        final boolean allowAdd = threadIds.size() < HistoryThreadManager.MAX_THREAD_NUM;
        final boolean allowDelete = threadIds.size() > 1;
        this.iconDel.setEnabled(allowDelete);
        this.iconNew.setEnabled(allowAdd);
    }

    private void changeThread(int newThreadId) {
        if (newThreadId == -1) {
            // Disabling the icon avoids race conditions created by schedule deferred executions.
            // The icon is enabled again if any thread event has been received, see onHistoryThreadChange.
            this.iconNew.setEnabled(false);
            this.manager.newThread(HistoryToken.Builder.fromToken(AbstractMainController.INSTANCE.getStartPage()).build());
        }
        else {
            this.manager.switchToThread(newThreadId);
        }
    }

    @Override
    public void onHistoryThreadChange(final HistoryThreadEvent event) {
        switch (event.getReason()) {
            case NEW:
                this.currentThreadId = event.getThreadId();
                break;
            case SWITCHED_TO:
                this.currentThreadId = event.getThreadId();
                break;
            case REPLACED_ALL:
                this.currentThreadId = event.getThreadId();
                break;
        }
        updateButtonState();
    }

    private void showThreadPopup(Widget triggerWidget) {
        final PopupPanel popupPanel = new PopupPanel(true, true);
        popupPanel.addAttachHandler(event -> PopupPanelFix.addFrameDummy(popupPanel));
        popupPanel.setStyleName("as-popup-menu as-historyPopup");
        final FlowPanel panel = new FlowPanel();
        final List<Integer> threadIds = this.manager.getAllHistoryThreadIds();
        Collections.reverse(threadIds);
        for (final Integer threadId : threadIds) {
            final SafeHtml threadTitle = this.manager.getThreadTitle(threadId);
            if (this.currentThreadId == threadId) {
                final SafeHtml iconCurrentHtml = IconImage.get("thread-current-dot").getSafeHtml(); // $NON-NLS$
                final SafeHtmlBuilder sb = new SafeHtmlBuilder().append(iconCurrentHtml);
                if (threadTitle != null) {
                    sb.append(threadTitle);
                }
                final HTML threadLabel = new HTML(sb.toSafeHtml());
                threadLabel.setStyleName("historyEntry");
                Tooltip.addCompletion(threadLabel, threadTitle);
                panel.add(threadLabel);
            }
            else {
                final HTML threadLabel = new HTML(threadTitle != null ? threadTitle : SafeHtmlUtils.EMPTY_SAFE_HTML);
                threadLabel.setStyleName("historyEntry as-popup-link");
                threadLabel.getElement().getStyle().setPaddingLeft(22, PX); // padding 4px + image size of thread-current-dot (16px) + image padding (2px)
                threadLabel.addClickHandler(event -> {
                    popupPanel.hide();
                    manager.switchToThread(threadId);
                });
                Tooltip.addCompletion(threadLabel, threadTitle);
                panel.add(threadLabel);
            }
        }
        popupPanel.setWidget(panel);
        popupPanel.showRelativeTo(triggerWidget);
    }
}