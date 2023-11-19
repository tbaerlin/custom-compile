package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

/**
 * TODO: use this class in TerminalPagesView
 */
public class SearchToolbar extends FloatingToolbar {

    interface SearchToolbarPresenter {

        void prevPage();

        void nextPage();

        void bookmarkCurrentPage();

        void doSearch(String text);
    }

    protected final SearchToolbarPresenter presenter;

    protected Button bookmarkButton;

    protected Button prevButton;

    protected Button nextButton;

    protected TextBox searchBox;


    SearchToolbar(SearchToolbarPresenter presenter) {
        super(ToolbarHeight.FOR_ICON_SIZE_S);
        this.presenter = presenter;
    }

    public void setQueryText(String queryText) {
        this.searchBox.setText(queryText);
    }

    public SearchToolbar addSearchLabel(String labelText) {
        final Label label = new Label(labelText);
        label.setStyleName("mm-toolbar-text"); // $NON-NLS$
        add(label);
        addEmpty("5"); // $NON-NLS$
        return this;
    }

    public SearchToolbar addSearchBox() {
        searchBox = new TextBox();
        searchBox.setMaxLength(40);
        searchBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
                    presenter.doSearch(searchBox.getText().trim());
                }
            }
        });
        add(this.searchBox);
        return this;
    }

    public SearchToolbar addSpacer() {
        addSeparator();
        return this;
    }

    public SearchToolbar addPagingButtons() {
        add(this.prevButton = Button.icon("x-tbar-page-prev") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        presenter.prevPage();
                    }
                })
                .build());
        this.prevButton.setEnabled(false);
        add(this.nextButton = Button.icon("x-tbar-page-next") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        presenter.nextPage();
                    }
                })
                .build());
        this.nextButton.setEnabled(false);
        return this;
    }

    public SearchToolbar withBookmarker() {
        if (!SessionData.INSTANCE.isAnonymous()) {
            addSeparator();
            add(this.bookmarkButton = Button.icon("mm-bookmark") // $NON-NLS$
                    .tooltip(I18n.I.addBookmarks())
                    .clickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            presenter.bookmarkCurrentPage();
                        }
                    })
                    .build());
            this.bookmarkButton.setEnabled(false);
        }
        return this;
    }

}