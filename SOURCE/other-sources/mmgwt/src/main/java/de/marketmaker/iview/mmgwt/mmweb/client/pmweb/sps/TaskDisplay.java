package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.user.client.ui.IsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.pmxml.SubmitAction;

/**
 * Author: umaurer
 * Created: 07.02.14
 */
public interface TaskDisplay extends SpsBaseDisplay, IsWidget {
    String SPS_TASK_VIEW_STYLE = "sps-taskView"; // $NON-NLS$

    void setPresenter(Presenter presenter);
    void clear();
    void setError(String message);
    void setMessage(String message);
    void setActionPreviousVisible(boolean visible);
    void setSubmitAction(SubmitAction action);
    void updatePinnedMode();

    void ensureVisible(SpsWidget spsWidget);

    interface Presenter {
        void onCancel();
        void onPrevious();
        void onRefresh();
        void onSubmit();
        void onCommit();

        void onLogProperties();
    }
}
