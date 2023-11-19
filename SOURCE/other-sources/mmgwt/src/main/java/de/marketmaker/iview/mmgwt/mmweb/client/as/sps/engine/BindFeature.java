package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.ValidationResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.Validator;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: umaurer
 * Created: 27.01.14
 */
@SuppressWarnings({"Convert2Lambda", "Convert2streamapi"})
// due to source compatibility with 5_30/as 1.30 do not introduce diamonds, lambdas, or streams here. TODO: remove annotation after cherry pick into 1.30
public class BindFeature<P extends SpsProperty> {
    private final HasBindFeature hasBindFeature;
    private P spsProperty;
    private HandlerRegistration handlerRegistration;
    private List<HandlerRegistration> varPropHandlerRegistrations;
    private List<HandlerRegistration> valSourceHandlerRegistrations;
    private DataItemFormatter dataItemFormatter = null; // lazy initialization

    public BindFeature(final HasBindFeature hasBindFeature) {
        this.hasBindFeature = hasBindFeature;
    }

    boolean released = false;

    public void release() {
        assert !this.released : "bind feature already released!";
        this.released = true;

        if (this.handlerRegistration != null) {
            this.handlerRegistration.removeHandler();
        }
        if (this.varPropHandlerRegistrations != null) {
            for (HandlerRegistration varPropHandlerRegistration : varPropHandlerRegistrations) {
                varPropHandlerRegistration.removeHandler();
            }
        }
        if (this.valSourceHandlerRegistrations != null) {
            for (HandlerRegistration valSourceHandlerRegistration : valSourceHandlerRegistrations) {
                valSourceHandlerRegistration.removeHandler();
            }
        }
        this.spsProperty = null;
    }

    public void setProperty(P aSpsProperty) {
        if (this.handlerRegistration != null) {
            this.handlerRegistration.removeHandler();
        }
        this.spsProperty = aSpsProperty;
        this.handlerRegistration = this.spsProperty.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                try {
                    onPropertyChange();
                }
                catch (Exception e) {
                    final String where = spsProperty != null ? " (" + spsProperty.getBindToken().toString() + ") ": " ";  // $NON-NLS$
                    ExceptionUtil.logErrorOrPropertyIsNull("Calling onPropertyChange after property change in BindFeature" + where + "threw exception", e);  // $NON-NLS$
                    if (!ExceptionUtil.hasOnlyPropertyIsNullExceptions(e)) {
                        DebugUtil.logToServer("Calling onPropertyChange after property change in BindFeature" + where + "threw exception", e);  // $NON-NLS$
                        DebugUtil.showDeveloperNotification("Calling onPropertyChange after property change in BindFeature" + where + "threw exception", e); // $NON-NLS$
                    }
                }
            }
        });
    }

    /**
     * spsProperty-reference may change during runtime. be aware when storing spsproperty in variable!
     */
    public P getSpsProperty() {
        assert !this.released : "getSpsProperty but already released!";
        return this.spsProperty;
    }

    public void setContextAndTokens(final Context context, final BindToken parentToken, final BindToken bindToken) {
        if (!bindToken.containsVariable()) {
            resolveProperty(bindToken, context, false);
            return;
        }

        BindKey bindKey = bindToken.getHead();
        do {
            if (!(bindKey instanceof BindKeyVariable)) {
                bindKey = bindKey.getNext();
                continue;
            }
            final BindToken token = BindToken.create(parentToken, ((BindKeyVariable) bindKey).getBindKeys());
            final SpsProperty varProp = context.getRootProp().get(token.getHead());
            if (!(varProp instanceof SpsLeafProperty)) {
                throw new IllegalStateException("variable property reference must be instanceof SpsLeafProperty!"); // $NON-NLS$
            }
            if (this.varPropHandlerRegistrations == null) {
                this.varPropHandlerRegistrations = new ArrayList<>();
            }
            this.varPropHandlerRegistrations.add(varProp.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    resolveProperty(BindToken.resolveVariables(parentToken, bindToken, context), context, true);
                }
            }));
            bindKey = bindKey.getNext();
        } while (bindKey != null);
        resolveProperty(BindToken.resolveVariables(parentToken, bindToken, context), context, true);
    }

    private void resolveProperty(BindToken bindToken, Context context, boolean firePropertyChange) {
        final SpsProperty newProp = context.getRootProp().get(bindToken.getHead());
        //TODO: Check that the declared data types of the former and current property matches and throw an exception if not.
        if (this.spsProperty != null && this.spsProperty.getClass() != newProp.getClass()) {
            throw new IllegalStateException("unequal property types: " + // $NON-NLS$
                    this.spsProperty.getBindToken() + "(" + this.spsProperty.getClass().getSimpleName() + ") != " +
                    newProp.getBindToken() + "(" + newProp.getClass().getSimpleName() + ")");
        }
        @SuppressWarnings("unchecked") final P castProp = (P) newProp;
        setProperty(castProp);
        registerValidationSourceEventHandler(bindToken, context);
        if (firePropertyChange) {
            onPropertyChange();
        }
    }

    private void onPropertyChange() {
        this.hasBindFeature.onPropertyChange();
    }

    private void registerValidationSourceEventHandler(final BindToken targetToken, final Context context) {
        List<Validator> validators = context.getValidators(targetToken);
        if (validators == null) {
            return;
        }
        for (final Validator validator : validators) {
            if (this.valSourceHandlerRegistrations == null) {
                this.valSourceHandlerRegistrations = new ArrayList<>();
            }
            this.valSourceHandlerRegistrations.add(validator.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    fireValidationEvent(context.getTaskId(), targetToken, validator);
                }
            }));
            fireValidationEvent(context.getTaskId(), targetToken, validator);
        }
    }

    private void fireValidationEvent(String taskId, BindToken targetToken, Validator validator) {
        ValidationEvent.fire(taskId, Collections.singletonList(new ValidationResponse(targetToken, validator.validate())));
    }

    public DataItemFormatter getDataItemFormatter(String style) {
        if (this.dataItemFormatter == null) {
            this.dataItemFormatter = new DataItemFormatter(((SpsLeafProperty) getSpsProperty()).getParsedTypeInfo(), style);
        }
        return this.dataItemFormatter;
    }
}