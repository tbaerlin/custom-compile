package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentryactivity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.OrderEntryActivityServiceAsync;
import de.marketmaker.iview.mmgwt.mmweb.client.OrderEntryServiceException;
import de.marketmaker.iview.pmxml.CheckAndSetBackendCredentialsRequest;
import de.marketmaker.iview.pmxml.CheckAndSetBackendCredentialsResponse;
import de.marketmaker.iview.pmxml.OrderResultCode;

public class OrderEntryActivityMethods {

    public final static OrderEntryActivityMethods INSTANCE = GWT.create(OrderEntryActivityMethods.class);

    public void login(final String password, final AsyncCallback<Void> callback) {
        final CheckAndSetBackendCredentialsRequest request = new CheckAndSetBackendCredentialsRequest();
        request.setPassword(password);

        OrderEntryActivityServiceAsync.App.getInstance().login(request, new AsyncCallback<CheckAndSetBackendCredentialsResponse>() {
            @Override
            public void onSuccess(CheckAndSetBackendCredentialsResponse result) {
                final OrderResultCode code = result.getCode();
                if (code == OrderResultCode.ORC_OK) {
                    callback.onSuccess(null);
                } else {
                    //not necessary to handle RPC timeouts here, because in case of a timeout,
                    //the server side throws an OrderEntryServiceException, which is handled
                    //via onFailure.
                    callback.onFailure(new OrderEntryActivityException(code));
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    public String getMessageForThrowable(Throwable throwable) {
        if (throwable instanceof OrderEntryServiceException) {
            final OrderEntryServiceException ex = (OrderEntryServiceException) throwable;
            final String code = ex.getCode();
            if (code == null) {
                return I18n.I.internalError();
            }
            switch (code) {
                case "remote_timeout": // $NON-NLS$
                    return I18n.I.orderEntryOrcTimeout();
                case "password_empty": // $NON-NLS$
                    return I18n.I.changePasswordResponsePasswordIsTooShortPm();
                case "encrypt_password_failed": // $NON-NLS$
                case "unspecified.error": // $NON-NLS$
                default:
                    return I18n.I.orderEntryOrcAnyError(code);
            }
        } else if (throwable instanceof OrderEntryActivityException) {
            final OrderEntryActivityException ex = (OrderEntryActivityException) throwable;
            final OrderResultCode orc = ex.getOrderResultCode();
            if (orc == null) {
                return I18n.I.internalError();
            }
            switch (orc) {
                case ORC_BACKEND_TIME_OUT:
                    return I18n.I.orderEntryOrcTimeout();
                case ORC_BROKER_LOGIN_FAILED:
                    return I18n.I.orderEntryOrcBrokerLoginFailed();
                default:
                    return I18n.I.orderEntryOrcAnyError(orc.value());
            }
        } else {
            return I18n.I.orderEntryOrcAnyError(I18n.I.internalError());
        }
    }

    @SuppressWarnings("GwtInconsistentSerializableClass")
    public static class OrderEntryActivityException extends RuntimeException {

        private final OrderResultCode orderResultCode;

        public OrderEntryActivityException(OrderResultCode orderResultCode) {
            super(orderResultCode.toString());
            this.orderResultCode = orderResultCode;
        }

        public OrderResultCode getOrderResultCode() {
            return this.orderResultCode;
        }
    }
}
