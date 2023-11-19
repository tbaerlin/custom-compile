/*
 * ModulePanel.java
 *
 * Created on 22.04.13 16:51
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author umaurer
 */
public class ModulePanel extends Composite {
    private final FlowPanel panel = new FlowPanel();
    private final Map<String, Icon> mapIcons = new HashMap<>();
    private Icon selectedIcon = null;

    public enum SelectionState {
        SELECTED, WAS_SELECTED, UNSELECTED
    }

    class Icon {
        private final HTML html;
        private NavigationWidget navigationWidget;
        private SelectionState state;

        Icon(String iconClass, String title, Command onClickCommand) {
            this(iconClass, title);
            this.html.addClickHandler(event -> {
                Firebug.debug("<ModulePanel.Icon.onClick> onClickCommand");
                onClickCommand.execute();
            });
        }

        Icon(String iconClass, String title, final NavigationWidget nw) {
            this(iconClass, title);
            this.html.addClickHandler(event -> {
                Firebug.debug("<ModulePanel.Icon.onClick> onNavWidgetSelected");
                select(Icon.this, SelectionState.SELECTED).onNavWidgetSelected();
            });
            this.navigationWidget = nw;
        }

        Icon(String iconClass, String title) {
            final SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.append(IconImage.get(iconClass).getSafeHtml());
            sb.appendHtmlConstant("<br/>"); // $NON-NLS$
            sb.appendEscaped(title);
            this.html = new HTML(sb.toSafeHtml());
            setSelectionState(SelectionState.UNSELECTED);
            ModulePanel.this.panel.add(this.html);
        }

        public void setSelectionState(SelectionState state) {
            this.state = state;
            html.setStyleName("as-icon");
            switch (state) {
                case SELECTED:
                    html.addStyleName("selected");
                    break;
                case WAS_SELECTED:
                    html.addStyleName("was-selected");
                    break;
            }
        }

        public SelectionState getState() {
            return this.state;
        }
    }

    public ModulePanel() {
        this.panel.setStyleName("as-iconPanel");
        initWidget(this.panel);
    }

    public void addIcon(String id, String iconClass, String title, NavigationWidget nw) {
        if (this.mapIcons.put(id, new Icon(iconClass, title, nw)) != null) {
            throw new IllegalArgumentException("ModulePanel.addIcon(" + id + ") --> duplicate id"); // $NON-NLS$
        }
    }

    public void addIcon(String id, String iconClass, String title, Command doOnClickCommand) {
        if (this.mapIcons.put(id, new Icon(iconClass, title, doOnClickCommand)) != null) {
            throw new IllegalArgumentException("ModulePanel.addIcon(" + id + ") --> duplicate id"); // $NON-NLS$
        }
    }

    private NavigationWidget select(Icon icon, SelectionState selectionState) {
        if (this.selectedIcon != null) {
            this.selectedIcon.setSelectionState(SelectionState.UNSELECTED);
        }
        this.selectedIcon = icon;
        this.selectedIcon.setSelectionState(selectionState);
        return this.selectedIcon.navigationWidget;
    }

    public NavigationWidget changeSelection(String[] ids) {
        final Icon icon = this.mapIcons.get(ids[0]);
        if (icon == null) {
            if (this.selectedIcon != null) {
                this.selectedIcon.setSelectionState(SelectionState.WAS_SELECTED);
            }
            return null;
        }
        return select(icon, icon.navigationWidget.isResponsibleFor(ids)
                ? SelectionState.SELECTED
                : SelectionState.WAS_SELECTED);
    }

    public void clear() {
        this.panel.clear();
        this.mapIcons.clear();
        this.selectedIcon = null;
    }
}