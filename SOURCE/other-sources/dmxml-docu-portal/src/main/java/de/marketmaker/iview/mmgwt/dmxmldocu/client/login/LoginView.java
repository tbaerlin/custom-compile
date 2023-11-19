package de.marketmaker.iview.mmgwt.dmxmldocu.client.login;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.TextConstants;

/**
 * @author umaurer
 */
public class LoginView implements LoginController.Display {

    private static final boolean SHOW_USERNAME = false;
    private static final boolean SHOW_STORE_SESSION = false;
    private final Label labelMessage;
    private final CheckBox cbStoreSession;
    private final PasswordTextBox tbPassword;
    private final TextBox tbUsername;
    private FlowPanel mainPanel;
    private final Button submitButton;

    public LoginView() {
        final FlexTable flexTable = new FlexTable();
        flexTable.addStyleName("mm-flex-table");
        tbUsername = new TextBox();
        tbPassword = new PasswordTextBox();
        cbStoreSession = new CheckBox("Nach Browserneustart angemeldet bleiben"); //$NON-NLS$

        this.labelMessage = new Label();
        submitButton = new Button("Login"); //$NON-NLS$

        final FlexTable.ColumnFormatter columnFormatter = flexTable.getColumnFormatter();
        columnFormatter.addStyleName(0, "mm-login-label-col"); //$NON-NLS$

        final FlexTable.FlexCellFormatter formatter = flexTable.getFlexCellFormatter();

        int row = 0;
        if (SHOW_USERNAME) {

            flexTable.setText(row, 0, "Username"); //$NON-NLS$
            formatter.setStyleName(row, 0, "mm-login-label-td"); //$NON-NLS$
            flexTable.setWidget(row, 1, this.tbUsername);
            formatter.setStyleName(row, 1, "mm-login-field-td"); //$NON-NLS$
            row++;
        }
        flexTable.setText(row, 0, "Password"); //$NON-NLS$
        formatter.setStyleName(row, 0, "mm-login-label-td"); //$NON-NLS$

        flexTable.setWidget(row, 1, this.tbPassword);
        formatter.setStyleName(row, 1, "mm-login-field-td"); //$NON-NLS$

        if (SHOW_STORE_SESSION) {
            row++;
            formatter.setColSpan(row, 0, 2);
            flexTable.setWidget(row, 0, this.cbStoreSession);
        }
        row++;
        flexTable.setWidget(row, 0, this.labelMessage);
        formatter.setColSpan(row, 0, 2);
        row++;
        flexTable.setWidget(row, 1, submitButton);
        FlowPanel contactPanel = new FlowPanel();
        contactPanel.addStyleName("mm-loginView-contact"); //$NON-NLS$
        contactPanel.add(new InlineLabel("Contact: " + TextConstants.I.contactPerson() + " ")); //$NON-NLS$
        contactPanel.add(new Anchor(TextConstants.I.contactEmailLabel(), TextConstants.I.contactEmail()));
        FlowPanel copyrightPanel = new FlowPanel();
        InlineLabel copyrightText = new InlineLabel(TextConstants.I.copyrightText());
        copyrightPanel.add(copyrightText);
        copyrightPanel.addStyleName("mm-loginView-copyright"); //$NON-NLS$
        FlowPanel loginAndContactPanel = new FlowPanel();
        loginAndContactPanel.add(contactPanel);
        loginAndContactPanel.add(copyrightPanel);
        loginAndContactPanel.addStyleName("mm-loginView-text");
        mainPanel = new FlowPanel();
        final HorizontalPanel topPanel = new HorizontalPanel();

        final Image logo = new Image(TextConstants.I.companyLogo());
        logo.addStyleName("mm-company-logo");
        final InlineLabel companyName = new InlineLabel(TextConstants.I.companyLabel());
        companyName.addStyleName("mm-company-name");
        topPanel.add(logo);
        topPanel.add(companyName);
        topPanel.addStyleName("mm-top-panel");
        mainPanel.add(topPanel);
        mainPanel.setStyleName("mm-loginView"); //$NON-NLS$
        mainPanel.add(flexTable);
        mainPanel.add(loginAndContactPanel);
    }

    public Widget asWidget() {
        return this.mainPanel;
    }

    public HasClickHandlers getSubmitButton() {
        return this.submitButton;
    }

    public HasText getUsernameText() {
        return this.tbUsername;
    }

    public HasText getPasswordText() {
        return this.tbPassword;
    }

    public HasText getMessageText() {
        return this.labelMessage;
    }

    public HasValue<Boolean> isStoreSession() {
        return this.cbStoreSession;
    }

    public Focusable getUsernameFocus() {
        return this.tbUsername;
    }

    public Focusable getPasswordFocus() {
        return this.tbPassword;
    }

    public HasKeyDownHandlers[] getHasKeyDownHandlers() {
        return new HasKeyDownHandlers[]{this.tbUsername, this.tbPassword, this.cbStoreSession};
    }
}