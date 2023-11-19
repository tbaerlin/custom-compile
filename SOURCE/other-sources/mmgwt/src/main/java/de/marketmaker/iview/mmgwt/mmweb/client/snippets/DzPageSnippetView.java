package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.DzPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.TerminalPagesView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasIsPrintable;


public class DzPageSnippetView extends SnippetView<DzPageSnippet> implements HasIsPrintable {

    private final TextField<String> pageInput;

    private final SimplePanel panel;

    private final Frame frame;

    // we need to store the content for the print view
    private String currentHtmlContent;

    private final EventListener localEventListener = new EventListener() {
        public void onBrowserEvent(Event event) {
            if (DOM.eventGetType(event) != Event.ONCLICK) {
                return;
            }

            final Element target = DOM.eventGetTarget(event);
            final AnchorElement anchor = AnchorElement.as(target);
            if (!"".equals(anchor.getTarget())) { // $NON-NLS-0$
                return;
            }

            final int pos = anchor.getHref().indexOf(DzPageController.LINK_TEXT);
            if (pos == -1) {
                return;
            }

            final String text = anchor.getHref().substring(pos + DzPageController.LINK_TEXT.length());
            pageInput.setValue(text);
            loadPage(text);
            event.preventDefault();
        }
    };

    protected DzPageSnippetView(final DzPageSnippet snippet, SnippetConfiguration config) {
        super(snippet);
        pageInput = new TextField<>();
        pageInput.setWidth("250px");  // $NON-NLS$
        setTitle(config.getString("title", I18n.I.dzBankPages())); // $NON-NLS-0$
        panel = new SimplePanel();
        panel.setHeight("100%"); // $NON-NLS$
        panel.setWidth("100%"); // $NON-NLS$
        // we have scrollbars in the iframe, no need to have 2 sets of scrollbars
        panel.getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
        frame = new Frame();
        frame.setStyleName("mm-terminalPageFrame"); // $NON-NLS-0$
        panel.setWidget(frame);
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.panel);
        this.container.setTopWidget(createToolbar());
    }

    private ToolBar createToolbar() {
        pageInput.setValue(getConfiguration().getString("pagenumber","")); // $NON-NLS$
        pageInput.addKeyListener(new KeyListener() {
            @Override
            public void componentKeyUp(ComponentEvent componentEvent) {
                if (KeyCodes.KEY_ENTER == componentEvent.getKeyCode()) {
                    final String value = pageInput.getValue();
                    if (StringUtil.hasText(value)) {
                        loadPage(value.trim());
                    }
                }
            }
        });

        final ToolBar toolbar = new ToolBar();
        toolbar.add(new LabelToolItem(I18n.I.page() + " ")); // $NON-NLS$
        toolbar.add(pageInput);
        return toolbar;
    }

    private void loadPage(String key) {
        this.snippet.loadPage(key);
    }

    public void showError() {
        this.panel.setWidget(new HTML(I18n.I.pageCannotBeDisplayed()));
    }

    public void showEmpty() {
        this.panel.setWidget(new HTML(I18n.I.pageWithoutContent()));
    }

    public void showContent(String content) {
        this.currentHtmlContent = content;
        // needed to replace the iframe with a html div in the print view, we need the pagenumber for this
        this.panel.getElement().setAttribute("pagenumber", getConfiguration().getString("pagenumber"));  // $NON-NLS$
        displayContent();
    }

    private void displayContent() {
        // the frame might be replaced by a widget in showError or showEmpty
        this.panel.setWidget(frame);

        if (!DOMUtil.hasContentWindow(frame)) {
            // calling fill frame would cause an exception
            Firebug.warn("can't display iframe content for dzpage"); // $NON-NLS$
            return;
        }

        DOMUtil.fillFrame(frame, currentHtmlContent);
        final String additionalStylesheet = getAdditionalStylesheet();
        if (additionalStylesheet != null) {
            DOMUtil.loadStylesheet(frame, additionalStylesheet, true);
        }

        final EventListener listener;
        if (this.snippet.useLocalLinks()) {
            listener = this.localEventListener;
        } else {
            listener = TerminalPagesView.PAGE_LINK_LISTENER;
        }

        Element docElem = DOMUtil.getDocument(frame).getDocumentElement();
        DOM.sinkEvents(docElem, Event.ONCLICK | DOM.getEventsSunk(docElem));
        DOM.setEventListener(docElem, listener);
    }

    public String getAdditionalStylesheet() {
        return "./style/dzorgstyles.css"; // $NON-NLS$
    }

    @Override
    public boolean isPrintable() {
        return true;
    }

    @Override
    public String getPrintHtml() {
        return currentHtmlContent;
    }

}
