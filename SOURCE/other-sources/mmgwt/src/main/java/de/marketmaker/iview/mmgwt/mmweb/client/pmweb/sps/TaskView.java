package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.pmxml.SubmitAction;

/**
 * Author: umaurer
 * Created: 04.02.14
 */
public class TaskView implements TaskDisplay {
    protected final TaskViewPanel tvp = new TaskViewPanel();
    private Presenter presenter;

    private final Widget buttonPrevious;
    private final Widget buttonRefresh;
    private final SimplePanel panelSubmit = new SimplePanel();
    private final Widget buttonSubmit;
    private final Widget buttonCommit;

    public TaskView() {
        final TaskToolbar taskToolbar = new TaskToolbar(this.tvp);
        this.buttonPrevious = Button.html("&nbsp;&nbsp;&nbsp;&nbsp;") // $NON-NLS$
                .icon("sps-task-back") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        presenter.onPrevious();
                    }
                }).build();
        Tooltip.addQtip(this.buttonPrevious, I18n.I.back());
        //Hint: add tool tip only if button has no label!
        this.buttonSubmit = Button.text(I18n.I.proceed())
                .icon("sps-task-proceed", Button.IconPosition.RIGHT) // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        presenter.onSubmit();
                    }
                }).build();
        this.buttonRefresh = Button.text(I18n.I.doUpdate())
                .icon("sps-task-proceed", Button.IconPosition.RIGHT) // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        presenter.onRefresh();
                    }
                }).build();
        this.buttonCommit = Button.text(I18n.I.spsCommit())
                .icon("sps-task-commit", Button.IconPosition.RIGHT) // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        presenter.onCommit();
                    }
                }).build();

        taskToolbar.addSpace("16px"); // $NON-NLS$
        taskToolbar.addWidget(this.buttonPrevious, "84px"); // $NON-NLS$
        taskToolbar.addWidget(this.panelSubmit, "120px", HasHorizontalAlignment.ALIGN_RIGHT); // $NON-NLS$

        if (SessionData.INSTANCE.isUserPropertyTrue("developer")) { // $NON-NLS$
            taskToolbar.addSpace("240px"); // $NON-NLS$
            taskToolbar.addWidget(Button.text("log properties") // $NON-NLS$
                    .clickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            presenter.onLogProperties();
                        }
                    })
                    .build(), "100px"); // $NON-NLS$
        }
        this.tvp.setContentStyleName(SPS_TASK_VIEW_STYLE);
        this.tvp.setSouthWidget(taskToolbar);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clear() {
        this.tvp.setNorthWidget(null);
        this.tvp.clearContent();
    }

    public void setError(String message) {
        this.tvp.setContentWidget(new Label(message));
    }

    public void setMessage(String message) {
        this.tvp.setContentWidget(new Label(message));
    }

    public void setNorthWidget(Widget widget) {
        this.tvp.setNorthWidget(widget);
    }

    @Override
    public void layoutNorthWidget() {
        this.tvp.layout();
    }

    public void setWidgets(Widget[] widgets) {
        if (widgets.length == 1) {
            this.tvp.setContentWidget(widgets[0]);
            return;
        }

        final FlowPanel panel = new FlowPanel();
        for (Widget widget : widgets) {
            panel.add(widget);
        }
        final Label labelEnd = new Label();
        labelEnd.setStyleName("sps-taskViewEnd");
        panel.add(labelEnd);

        this.tvp.setContentWidget(panel);
    }

    @Override
    public void setActionPreviousVisible(boolean visible) {
        this.buttonPrevious.setVisible(visible);
    }

    @Override
    public void setSubmitAction(SubmitAction action) {
        if (action == null) {
            this.panelSubmit.setWidget(null);
        }
        else if (action == SubmitAction.SA_SAVE) {
            this.panelSubmit.setWidget(this.buttonSubmit);
        }
        else if (action == SubmitAction.SA_COMMIT) {
            this.panelSubmit.setWidget(this.buttonCommit);
        }
        else if (action == SubmitAction.SA_REFRESH) {
            this.panelSubmit.setWidget(this.buttonRefresh);
        }
        else {
            throw new IllegalArgumentException("TaskViewImpl.setSubmitAction(" + action + ") error: submit action not handled"); // $NON-NLS$
        }
    }

    public TaskDisplay withPresenter(Presenter presenter) {
        this.presenter = presenter;
        return this;
    }

    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void updatePinnedMode() {
        this.tvp.updateSouthWidgetPinned();
    }

    /**
     * Ensures that the position of a SPS widget is visible in the TaskView.
     * <p>
     * FF ESR 31 and FF ESR 38 do not automatically scroll the widget that gains the focus via JavaScript
     * calls into view, as IE 9, 10, 11, and Chrome do. Moreover it preserves any scroll positions from
     * the previous page views. Hence, it is necessary to scroll the widget that should gain the focus into
     * view by ourselves. See AS-1263 for further details.
     */
    @Override
    public void ensureVisible(SpsWidget spsWidget) {
        if (spsWidget == null) {
            return;
        }
        this.tvp.ensureVisible(spsWidget.getWidget().getElement());
    }

    @Override
    public Widget asWidget() {
        return this.tvp;
    }
}
