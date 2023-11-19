package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentryactivity;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PasswordTextBox;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SimpleStandaloneEngine;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsColumnSection;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsPasswordEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsSection;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskViewPanel;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ErrorSeverity;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.TiType;

public class SetPasswordView implements SetPasswordDisplay, IsWidget {

    protected final SpsColumnSection formSection;
    private final TaskViewPanel tvp;
    private final Button submitButton;
    private final SpsLeafProperty passwordProperty;
    private Presenter presenter;

    public SetPasswordView() {
        final SpsPasswordEdit password = this.createPasswordField(I18n.I.orderEntryBrokerLoginPassword());
        this.passwordProperty = password.getBindFeature().getSpsProperty();
        this.passwordProperty.addChangeHandler(event -> this.presenter.validate());

        this.formSection = SimpleStandaloneEngine.createColumnSection(0, null);
        this.formSection.withErrorPanelPosition(SpsSection.ErrorPanelPosition.BOTTOM);
        this.formSection.getChildrenFeature().addChild(password);
        SimpleStandaloneEngine.configureSpsWidget(this.formSection);

        final SpsColumnSection container = SimpleStandaloneEngine.createColumnSection(0, null);
        container.setStyle("sps-form-1");  // $NON-NLS$
        container.getChildrenFeature().addChild(this.formSection);
        SimpleStandaloneEngine.configureSpsWidget(container);

        final FlowPanel widget = new FlowPanel();
        widget.add(container.getWidget());

        this.submitButton = SimpleStandaloneEngine.createTaskViewPanelSubmitButtonFactory(
            I18n.I.orderEntrySpecifyPassword(), event -> getPresenter().onOk()).build();
        this.submitButton.setEnabled(true);

        this.tvp = SimpleStandaloneEngine.createTaskViewPanel(widget, this.submitButton);
    }

    @Override
    public void showSuccessMessage(String message) {
        final ErrorMM diError = SimpleStandaloneEngine.toErrorMM(message);
        diError.setErrorSeverity(ErrorSeverity.ESV_HINT);
        this.formSection.visualizeError(diError, true);
    }

    @Override
    public TaskViewPanel asWidget() {
        return this.tvp;
    }

    protected Presenter getPresenter() {
        return this.presenter;
    }

    private SpsPasswordEdit createPasswordField(String label) {
        final ParsedTypeInfo pti = new ParsedTypeInfo();
        pti.setTypeId(TiType.TI_STRING);
        pti.setDemanded(true);

        final SpsPasswordEdit spsPassword = new SpsPasswordEdit().withCaption(label).withPropertyUpdateOnKeyUp();
        spsPassword.setBaseStyle("sps-edit");  // $NON-NLS$

        final SpsBoundWidget<PasswordTextBox, SpsLeafProperty> spsWidget = SimpleStandaloneEngine.configureSpsWidget(spsPassword, pti);
        spsWidget.getBindFeature().getSpsProperty().setNullValue();

        return spsPassword;
    }

    @Override
    public String getPassword() {
        return this.passwordProperty.getStringValue();
    }

    @Override
    public void reset() {
        this.passwordProperty.setNullValue();
        Scheduler.get().scheduleDeferred(this.formSection::focusFirst);
    }

    @Override
    public void setSubmitButtonEnabled(boolean enabled) {
        this.submitButton.setEnabled(enabled);
    }

    @Override
    public void setError(String message) {
        this.formSection.visualizeError(SimpleStandaloneEngine.toErrorMM(message), true);
    }

    @Override
    public void clearError() {
        this.formSection.visualizeError(null, true);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
