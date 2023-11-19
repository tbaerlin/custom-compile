package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RadioButton;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author umaurer
 */
public class WorkspaceConstituentsConfig extends Composite {
    private final RadioButton[][] radioButtons;

    public WorkspaceConstituentsConfig(final ValueChangeListener valueChangeListener) {
        final List<AbstractWorkspaceItem> allWorkspaceItems = WorkspaceLists.INSTANCE.getAllWorkspaceItems();
        final FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(4);
        table.setWidth("100%"); // $NON-NLS$
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        formatter.setWidth(0, 0, "200px"); // $NON-NLS$
        table.setText(0, 1, I18n.I.orientationLeft());
        table.setText(0, 2, I18n.I.orientationRight());
        table.setText(0, 3, I18n.I.orientationHidden());
        formatter.setStyleName(0, 1, "mm-center"); // $NON-NLS$
        formatter.setStyleName(0, 2, "mm-center"); // $NON-NLS$
        formatter.setStyleName(0, 3, "mm-center"); // $NON-NLS$
        this.radioButtons = new RadioButton[allWorkspaceItems.size()][3];
        final HTML lblError = new HTML("&nbsp;"); // $NON-NLS$
        lblError.setStyleName("mm-valueError");
        final ValueChangeHandler<Boolean> rbChangeHandler = new ValueChangeHandler<Boolean>() {
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                final boolean oneWorkspaceVisible = isOneWorkspaceVisible(radioButtons);
                lblError.setHTML(oneWorkspaceVisible ? "&nbsp;" : I18n.I.oneWorkspaceVisible()); // $NON-NLS$
                if (valueChangeListener != null) {
                    valueChangeListener.onOneWorkspaceVisible(oneWorkspaceVisible);
                }
            }
        };
        for (int i = 0, allPanelsSize = allWorkspaceItems.size(); i < allPanelsSize; i++) {
            final AbstractWorkspaceItem panel = allWorkspaceItems.get(i);
            final int row = i + 1;
            this.radioButtons[i] = createRadioButtons(panel.getStateKey(), rbChangeHandler);
            table.setText(row, 0, panel.getHeading());
            table.setWidget(row, 1, this.radioButtons[i][0]);
            table.setWidget(row, 2, this.radioButtons[i][1]);
            table.setWidget(row, 3, this.radioButtons[i][2]);
            formatter.setStyleName(row, 1, "mm-center"); // $NON-NLS$
            formatter.setStyleName(row, 2, "mm-center"); // $NON-NLS$
            formatter.setStyleName(row, 3, "mm-center"); // $NON-NLS$
        }
        final int row = table.getRowCount();
        table.setWidget(row, 0, lblError);
        formatter.setColSpan(row, 0, 4);
        initWidget(table);
    }

    public static interface ValueChangeListener {
        void onOneWorkspaceVisible(boolean oneWorkspaceVisible);
    }

    private boolean isOneWorkspaceVisible(RadioButton[][] radioButtons) {
        for (RadioButton[] rbs : radioButtons) {
            if (rbs[0].getValue() || rbs[1].getValue()) {
                return true;
            }
        }
        return false;
    }

    private RadioButton[] createRadioButtons(String stateKey, ValueChangeHandler<Boolean> rbChangeHandler) {
        final RadioButton[] radioButtons = new RadioButton[3];
        radioButtons[0] = new RadioButton("workspaceConfig" + stateKey); // $NON-NLS$
        radioButtons[0].addValueChangeHandler(rbChangeHandler);
        radioButtons[1] = new RadioButton("workspaceConfig" + stateKey); // $NON-NLS$
        radioButtons[1].addValueChangeHandler(rbChangeHandler);
        radioButtons[2] = new RadioButton("workspaceConfig" + stateKey); // $NON-NLS$
        radioButtons[2].addValueChangeHandler(rbChangeHandler);
        if (WorkspaceLists.INSTANCE.isEast(stateKey)) {
            radioButtons[1].setValue(Boolean.TRUE);
        }
        else if (WorkspaceLists.INSTANCE.isHidden(stateKey)) {
            radioButtons[2].setValue(Boolean.TRUE);
        }
        else {
            radioButtons[0].setValue(Boolean.TRUE);
        }
        return radioButtons;
    }

    public void saveSettings() {
        saveSettings(this.radioButtons);
        AbstractMainController.INSTANCE.getView().resetWestAndEastPanel();
    }

    private void saveSettings(RadioButton[][] radioButtons) {
        final StringBuilder[] sb = new StringBuilder[3];
        for (RadioButton[] rbs : radioButtons) {
            saveSettings(rbs, sb);
        }
        WorkspaceLists.INSTANCE.saveConstituentKeys(
                sb[1] == null ? null : sb[1].toString(),
                sb[2] == null ? null : sb[2].toString()
        );
    }

    private void saveSettings(RadioButton[] rbs, StringBuilder[] sb) {
        for (int i = 0; i < rbs.length; i++) {
            if (rbs[i].getValue()) {
                final String stateKey = rbs[i].getName().substring("workspaceConfig".length()); // $NON-NLS$
                if (sb[i] == null) {
                    sb[i] = new StringBuilder();
                }
                else {
                    sb[i].append("-"); // $NON-NLS$
                }
                sb[i].append(stateKey);
            }
        }
    }
}
