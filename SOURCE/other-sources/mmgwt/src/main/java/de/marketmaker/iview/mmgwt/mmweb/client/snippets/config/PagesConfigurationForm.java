/*
 * PagesConfigurationForm.java
 *
 * Created on 05.05.2008 14:15:52
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import java.util.Map;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.itools.gwtutil.client.widgets.input.CheckBox;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Markus Dick
 */
class PagesConfigurationForm extends FlowPanel {
    private final Map<String, String> params;

    protected PagesConfigurationForm(Map<String, String> params) {
        Styles.trySetStyle(this, Styles.get().generalViewStyle());

        final FlexTable form = Styles.tryAddStyles(new FlexTable(),
                Styles.get().generalFormStyle(),
                Styles.get().configurationForm());
        form.setCellPadding(0);
        form.setCellSpacing(0);

        this.params = params;

        final CheckBox checkBox = new CheckBox("true".equals(this.params.get("localLinks")));  // $NON-NLS$
        checkBox.addValueChangeHandler(event -> updateLocalLinks(event.getValue()));

        final Label label = checkBox.createLabel(I18n.I.openLinksInSameWindow());
        Styles.tryAddStyles(label, Styles.get().caption());

        form.setWidget(0, 0, label);
        form.setWidget(0, 1, checkBox);

        add(form);
    }

    private void updateLocalLinks(boolean b) {
        if (b) {
            this.params.put("localLinks", "true"); // $NON-NLS$
        }
        else {
            this.params.remove("localLinks"); // $NON-NLS$
        }
    }
}
