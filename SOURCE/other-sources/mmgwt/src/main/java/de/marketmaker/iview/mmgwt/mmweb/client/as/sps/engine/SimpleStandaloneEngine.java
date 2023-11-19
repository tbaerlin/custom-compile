/*
 * SimpleStandaloneEngine.java
 *
 * Created on 07.11.2014 11:45
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory.SectionFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskViewPanel;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ErrorSeverity;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public final class SimpleStandaloneEngine {
    public static <S extends SpsWidget<W>, W extends Widget> S configureSpsWidget(
            S configuredSpsWidget) {
        return configureSpsWidget(configuredSpsWidget, false);
    }

    public static <S extends SpsWidget<W>, W extends Widget> S configureSpsWidget(
            S configuredSpsWidget, boolean parentIsForm) {
        if (parentIsForm) {
            configuredSpsWidget.forceCaptionWidget();
        }
        configuredSpsWidget.onWidgetConfigured();
        configuredSpsWidget.asWidgets();
        return configuredSpsWidget;
    }

    @SuppressWarnings(value = "unchecked")
    public static <S extends SpsBoundWidget<W, P>, W extends Widget, P extends SpsLeafProperty> S configureSpsWidget(
            S configuredSpsWidget, ParsedTypeInfo pti) {
        final P property = (P) new SpsLeafProperty(null, null, pti);
        return createSpsWidget(configuredSpsWidget, pti.isDemanded(), property);
    }

    private static <S extends SpsBoundWidget<W, P>, W extends Widget, P extends SpsLeafProperty> S createSpsWidget(
            S configuredSpsWidget, boolean mandatory, P spsProperty) {
        configuredSpsWidget.setMandatory(mandatory);
        configuredSpsWidget.getBindFeature().setProperty(spsProperty);
        return configureSpsWidget(configuredSpsWidget);
    }

    public static TaskViewPanel createTaskViewPanel(Widget content, Widget submitButton) {
        return createTaskViewPanel(content, null, submitButton);
    }

    public static TaskViewPanel createTaskViewPanel(Widget content, Widget cancelButton,
            Widget submitButton) {
        final TaskViewPanel panel = createTaskViewPanel();
        panel.setContentWidget(content);
        createAndSetTaskToolbar(panel, cancelButton, submitButton);
        return panel;
    }

    public static TaskViewPanel createTaskViewPanel() {
        final TaskViewPanel panel = new TaskViewPanel();
        panel.setContentStyleName(TaskDisplay.SPS_TASK_VIEW_STYLE);
        return panel;
    }

    public static TaskToolbar createAndSetTaskToolbar(TaskViewPanel panel, Widget cancelButton,
            Widget submitButton) {
        final TaskToolbar taskToolbar = new TaskToolbar(panel);
        taskToolbar.addSpace("16px");
        if (cancelButton != null) {
            taskToolbar.addWidget(cancelButton, "84px");
        }
        else {
            taskToolbar.addSpace("84px");
        }
        addSubmitButton(taskToolbar, submitButton);
        panel.setSouthWidget(taskToolbar);
        return taskToolbar;
    }

    public static void addSubmitButton(TaskToolbar taskToolbar, Widget submitButton) {
        taskToolbar.addWidget(submitButton, "120px", HasHorizontalAlignment.ALIGN_RIGHT);
    }

    public static SpsColumnSection createColumnSection(int level, String caption) {
        final SpsColumnSection section = new SpsColumnSection(SectionFactory.FORM_COLUMN_STYLES);
        section.withFormContainer(true);
        section.setBaseStyle("sps-section");
        section.setStyle("sps-form");
        section.setLevel(level);
        section.withCaption(caption);
        return section;
    }

    public static SpsLeafProperty createLeafProperty(String bindKey, SpsGroupProperty parent,
            ParsedTypeInfo pti, ChangeHandler changeHandler) {
        final SpsLeafProperty leaf = new SpsLeafProperty(bindKey, parent, pti);
        if (changeHandler != null) {
            leaf.addChangeHandler(changeHandler);
        }
        parent.put(bindKey, leaf, false);
        return leaf;
    }

    public static Button.Factory createTaskViewPanelSubmitButtonFactory(String save,
            ClickHandler clickHandler) {
        return Button.text(save).icon("sps-task-commit", Button.IconPosition.RIGHT)
                .clickHandler(clickHandler);
    }

    public static Button.Factory createTaskViewPanelButtonFactory(String label,
            ClickHandler clickHandler) {
        return Button.text(label)
                .clickHandler(clickHandler);
    }

    public static SpsSection createTrailingSection() {
        final SpsSection trailingSection = new SpsSection();
        trailingSection.setBaseStyle("sps-section");
        trailingSection.setStyle("trailing");
        return trailingSection;
    }

    public static ErrorMM toErrorMM(String errorText) {
        final ErrorMM diError = new ErrorMM();
        diError.setErrorSeverity(ErrorSeverity.ESV_ERROR);
        diError.setErrorString(errorText);
        return diError;
    }
}
