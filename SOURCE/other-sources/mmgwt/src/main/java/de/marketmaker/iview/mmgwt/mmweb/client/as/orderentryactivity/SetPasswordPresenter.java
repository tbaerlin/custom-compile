package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentryactivity;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PendingRequestsEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

public class SetPasswordPresenter implements SetPasswordDisplay.Presenter {

    private final SetPasswordDisplay display;

    public SetPasswordPresenter(SetPasswordDisplay display) {
        this.display = display;
        this.display.setPresenter(this);
    }

    public void reset() {
        this.display.clearError();
        this.display.reset();
        validate();
    }

    @Override
    public void onOk() {
        try {
            setViewLoadingState(true);

            OrderEntryActivityMethods.INSTANCE.login(this.display.getPassword(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable throwable) {
                    setViewLoadingState(false);
                    Firebug.error("<onOk> broking login for activities failed", throwable);

                    final String message = OrderEntryActivityMethods.INSTANCE.getMessageForThrowable(throwable);
                    display.setError(message);
                }

                @Override
                public void onSuccess(Void aVoid) {
                    setViewLoadingState(false);
                    Firebug.info("<onOk> broking login successful");

                    display.showSuccessMessage(I18n.I.orderEntrySpecifyPasswordSuccessful());
                    display.reset();

                }
            });
        } catch (Exception e) {
            Firebug.error("<onOk> error", e);
            setViewLoadingState(false);
        }
    }

    private void setViewLoadingState(boolean loading) {
        if (loading) {
            EventBusRegistry.get().fireEvent(new PendingRequestsEvent(1, 1));
        } else {
            EventBusRegistry.get().fireEvent(new PendingRequestsEvent(0, 0));
        }
        this.display.setSubmitButtonEnabled(!loading);
    }

    @Override
    public void validate() {
        final boolean enabled = StringUtil.hasText(this.display.getPassword());
        this.display.setSubmitButtonEnabled(enabled);
    }
}
