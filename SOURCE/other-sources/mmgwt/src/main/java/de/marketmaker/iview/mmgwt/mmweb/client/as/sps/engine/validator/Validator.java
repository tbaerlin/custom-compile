package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Created on 27.05.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 *         <p/>
 *         A Validator executes a {@link de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.WidgetAction}
 *         if a {@link de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.Condition} occures.
 *         If a Condition is a showStopper, a negative result means something like a higher escalation level
 *         (e.g. don't try to save this).
 */
public class Validator {
    private final Condition condition;
    private final WidgetAction action;
    private final String description;
    private final boolean showStopper;


    private Validator(Condition condition, WidgetAction action, boolean showStopper, String description) {
        this.condition = condition;
        this.action = action;
        this.description = description;
        this.showStopper = showStopper;
    }

    public static Validator create(Condition condition, WidgetAction action) {
        return new Validator(condition, action, false, null);
    }

    public static Validator create(Condition condition, String description, boolean showStopper, WidgetAction action) {
        return new Validator(condition, action, showStopper, description);
    }

    public HandlerRegistration addChangeHandler(ChangeHandler changeHandler) {
        return this.condition.getSourceProperty().addChangeHandler(changeHandler);
    }

    public WidgetAction validate() {
        final boolean complied = this.condition.complied();
        return complied ? this.action : NothingToDo.INSTANCE;
    }

    public boolean isShowStopper() {
        return this.showStopper;
    }

    @Override
    public String toString() {
        return "Validator{" + // $NON-NLS$
                "condition=" + this.condition + // $NON-NLS$
                ", action=" + this.action + // $NON-NLS$
                '}';
    }

    public String getDescription() {
        return this.description;
    }
}