package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentryactivity;

public interface SetPasswordDisplay {

    /**
     * This method is called by the presenter with its reference.
     */
    void setPresenter(Presenter presenter);

    String getPassword();

    void setError(String message);

    void clearError();

    void showSuccessMessage(String message);

    void reset();

    void setSubmitButtonEnabled(boolean enabled);

    interface Presenter {

        void onOk();

        void validate();
    }
}
