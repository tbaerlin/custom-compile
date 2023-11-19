package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.NavigationWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.AbstractWorkspaceItem;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.WorkspaceItem;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.WorkspaceLists;

/**
 * @author Ulrich Maurer
 *         Date: 29.11.12
 */
public abstract class AbstractMainView implements ContentContainer {
    private ContentView lastContentView;
    private Widget content = null;

    public abstract void init();
    public abstract void setDefaultWindowTitle();
    public abstract void setWindowTitlePrefix(String prefix);
    public abstract SafeHtml getContentHeader();
    public abstract void setContentHeader(SafeHtml safeHtml);
    public abstract void changeSelection(final String[] ids, boolean setNavWidget);
    public abstract void showError(SafeHtml safeHtml);
    public abstract void showMessage(SafeHtml safeHtml);
    public abstract void onLogout();
    public abstract void resetWestAndEastPanel();

    // transition to topToolbar
    public abstract TopToolbar getTopToolbar();

    @Override
    public void setContent(ContentView contentView) {
        if (this.lastContentView != null && this.lastContentView != contentView) {
            this.lastContentView.onBeforeHide();
        }
        this.lastContentView = contentView;

        final Widget w = contentView.getWidget();
        final boolean changed = this.content != w;

        this.content = w;

        if (changed) {
            setContentAfterChange(contentView);
            MainController.INSTANCE.getView().getTopToolbar().updatePrintButtonEnabledState();
        }


/*
        TODO: Braucht man das???

        if (this.content instanceof Component) {
            final Component c = (Component) this.content;
            if (!c.isVisible()) {
                c.show();
            }
        }
*/
    }

    @Override
    public boolean isShowing(Widget w) {
        return this.lastContentView != null && this.lastContentView.getWidget() == w;
    }

    public void setContentHeader(String... text) {
        setContentHeader(StringUtil.asHeader(text));
    }

    protected abstract void setContentAfterChange(ContentView contentView);

    @Override
    public Widget getContent() {
        return this.lastContentView == null ? null : this.lastContentView.getWidget();
    }

    public boolean hasNavPanel() {
        return false;
    }

    public void setNavWidget(NavigationWidget nw) {
        throw new UnsupportedOperationException("NavWidget not supported by " + getClass().getName()); // $NON-NLS$
    }

    public void addWorkspaceItem(WorkspaceItem workspaceItem) {
        if (workspaceItem instanceof AbstractWorkspaceItem) {
            WorkspaceLists.INSTANCE.add((AbstractWorkspaceItem) workspaceItem);
        }
    }
}
