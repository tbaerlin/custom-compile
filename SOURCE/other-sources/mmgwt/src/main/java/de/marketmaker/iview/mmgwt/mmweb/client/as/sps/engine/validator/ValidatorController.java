package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasBindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasChildrenFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 06.06.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class ValidatorController implements ValidationHandler {

    private final TaskController taskController;
    private boolean active = false;

    public ValidatorController(TaskController taskController) {
        this.taskController = taskController;
        EventBusRegistry.get().addHandler(ValidationEvent.getType(), this);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean checkForShowStopper(boolean createValidationEvent) {
        final List<ValidationResponse> responses = new ArrayList<>();
        final SpsWidget rootWidget = this.taskController.getRootWidget();
        final Context spsContext = this.taskController.getSpsContext();
        if (rootWidget == null || spsContext == null) {
            return false;
        }
        collectAndValidateShowStoppers(spsContext, responses, rootWidget);

        if (!responses.isEmpty()) {
            if (createValidationEvent) {
                ValidationEvent.fire(this.taskController.getTaskId(), I18n.I.activityShowStopperMsg(), true, responses);
            }
            return true;
        }
        return false;
    }

    private void collectAndValidateShowStoppers(Context spsContext, List<ValidationResponse> responses, SpsWidget widget) {
        if (widget instanceof HasChildrenFeature) {
            final List<SpsWidget> children = ((HasChildrenFeature) widget).getChildrenFeature().getChildren();
            for (SpsWidget child : children) {
                collectAndValidateShowStoppers(spsContext, responses, child);
            }
        }
        if (!(widget instanceof HasBindFeature)) {
            return;
        }
        final BindFeature bindFeature = ((HasBindFeature) widget).getBindFeature();
        final SpsProperty spsProperty = bindFeature.getSpsProperty();
        if (spsProperty == null) {
            return;
        }
        final BindToken bindToken = spsProperty.getBindToken();
        final List<Validator> validators = spsContext.getValidators(bindToken);
        if (validators != null) {
            for (Validator validator : validators) {
                if (validator.isShowStopper()) {
                    final WidgetAction action = validator.validate();
                    if (action instanceof NothingToDo) {
                        continue;
                    }
                    responses.add(new ValidationResponse(bindToken, action, validator.getDescription()));
                }
            }
        }
    }

    private List<SpsWidget> findWidgets(BindToken bindToken) {
        final ArrayList<SpsWidget> spsWidgets = new ArrayList<>();
        final SpsWidget rootWidget = this.taskController.getRootWidget();
        if (rootWidget == null) {
            return spsWidgets;
        }
        rootWidget.findWidgets(bindToken, spsWidgets);

        logFoundWidgets(bindToken, spsWidgets);

        return spsWidgets;
    }

    private void logFoundWidgets(BindToken bindToken, ArrayList<SpsWidget> spsWidgets) {
        final StringBuilder sb = new StringBuilder();
        if(spsWidgets != null) {
            for (SpsWidget spsWidget : spsWidgets) {
                if (spsWidget != null) {
                    sb.append("- ").append(spsWidget.getClass().getSimpleName()).append(" ").append(spsWidget.getDescId()).append("\n");  // $NON-NLS$
                }
            }
        }
        Firebug.debug("<ValidatorController.findWidgets> bindToken=\"" + bindToken.toString() + "\" found " + (spsWidgets != null ? spsWidgets.size() : 0) + " widgets.\n" + sb.toString());
    }

    class FirstWidget {
        SpsWidget widget;
        public void maybeSetWidget(SpsWidget widget) {
            if (this.widget == null) {
                this.widget = widget;
            }
        }
        public void focusFirst() {
            if (this.widget != null) {
                /* FF ESR 31 and FF ESR 38 do not automatically scroll the widget that gains the focus via JavaScript
                 * calls into view, as IE 9, 10, 11, and Chrome do. Moreover it preserves any scroll positions from
                 * the previous page views. Hence, it is necessary to scroll the widget that should gain the focus into
                 * view by ourselves. See AS-1263 for further details.
                 */
                taskController.ensureVisible(this.widget);
                widget.focusFirst();
            }
        }
    }

    public void onValidation(ValidationEvent validationEvent) {
        if (!isResponsible(validationEvent) || !isActive()) {
            return;
        }
        final List<ValidationResponse> responses = validationEvent.getResponses();
        final FirstWidget firstWidget = new FirstWidget();
        for (ValidationResponse response : responses) {
            executeActions(response, firstWidget);
        }
        firstWidget.focusFirst();
    }

    private boolean isResponsible(ValidationEvent validationEvent) {
        final String id = validationEvent.getId();
        return id.equals(this.taskController.getTaskId());
    }

    private void executeActions(ValidationResponse response, FirstWidget firstWidget) {
        final List<SpsWidget> widgets = findWidgets(response.getTargetBind());
        for (SpsWidget widget : widgets) {
            response.getAction().doIt(widget, response);
            firstWidget.maybeSetWidget(widget);
        }
    }
}
