/*
 * PagingWidgets.java
 *
 * Created on 31.03.2008 17:12:59
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.paging;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.Separator;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.AbstractFinder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PriceStringRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.DecimalBox;

/**
 * MM's PagingToolbar, not concerned with data loading, rendering etc.
 * Displayed elements can be configured using {@link Config}
 * @author Oliver Flege
 * @author Ulrich Maurer
 */
public class PagingWidgets {

    private static final String PAGE_SIZE_KEY = "PAGE_SIZE";  // $NON-NLS$

    private static final Renderer<Integer> PAGE_RENDERER = new Renderer<Integer>() {
        private PriceStringRenderer renderer = new PriceStringRenderer(StringBasedNumberFormat.ROUND_0, "");

        @Override
        public String render(Integer value) {
            if (value == null) {
                return null;
            }
            return renderer.render(value.toString());
        }
    };

    private PagingEvent.Callback callback;

    public enum Mode {
        FULL, RESTRICTED
    }

    public interface PinCallback {
        void onPin(boolean pinned);
    }

    public interface PdfCallback {
        void onPdf();
    }

    public interface SearchCallback {
        void onSearch();
    }

    public interface ToolbarAddOn {
        void addTo(FloatingToolbar toolbar);
    }

    private final FloatingToolbar toolbar;

    private Label afterText;

    private Label displayText;

    private Label entryCountText = null;

    private Widget displayTextSeparator;

    private Button first;

    private Button last;

    private Button next;

    private DecimalBox currentPageBox;

    private int currentPage = 0;

    private int minPage = 0;

    private int maxPage = 0;

    private Label pageText;

    private Button previous;

    private Label refresh;

    private Button pin;

    private Button search;

    private SelectButton pageSize;

    private boolean pinned = false;

    private boolean allVisible = true;

    private boolean pageChangeEnabled = false;

    final private ArrayList<Widget> addedWidgets = new ArrayList<>();

    public static class Config {
        private PagingWidgets.Mode mode = PagingWidgets.Mode.FULL;

        private FloatingToolbar toolbar;

        private boolean addDisplayText = true;

        private boolean addEntryCount = false;

        private boolean addFill = true;

        private PinCallback pinCallback = null;

        private PdfCallback pdfCallback = null;

        private SearchCallback searchCallback = null;

        private ArrayList<ToolbarAddOn> addOns = new ArrayList<>();

        private boolean addPageSize = false;

        public Config withAddEntryCount(boolean addEntryCount) {
            this.addEntryCount = addEntryCount;
            return this;
        }

        public Config withAddSearchButton(SearchCallback callback) {
            this.searchCallback = callback;
            return this;
        }

        public Config withAddPdfButton(PdfCallback callback) {
            this.pdfCallback = callback;
            return this;
        }

        public Config withAddOn(ToolbarAddOn addOn) {
            this.addOns.add(addOn);
            return this;
        }

        public Config withAddPinButton(PinCallback callback) {
            this.pinCallback = callback;
            return this;
        }

        public Config withMode(PagingWidgets.Mode mode) {
            this.mode = mode;
            return this;
        }

        public Config withToolbar(FloatingToolbar toolbar) {
            this.toolbar = toolbar;
            return this;
        }

        public Config withAddFill(boolean addFill) {
            this.addFill = addFill;
            return this;
        }

        public Config withPageSize(boolean addPageSize) {
            this.addPageSize = addPageSize;
            return this;
        }

    }

    public PagingWidgets(final Config config) {
        this.toolbar = (config.toolbar == null) ? new FloatingToolbar() : config.toolbar;

        if (SessionData.isAsDesign()) {
            addFirstButton();
            addPreviousButton();
            addNextButton();
            addLastButton(config);

            addPageNumber(config);
        }
        else {
            addFirstButton();
            addPreviousButton();

            addSeparator();

            addPageNumber(config);

            addSeparator();

            addNextButton();
            addLastButton(config);

            addSeparator();
        }

        addRefreshIndicator();

        if (config.addEntryCount) {
            this.entryCountText = new Label("");
            this.toolbar.add(this.entryCountText);
        }

        if (config.addPageSize) {
            this.toolbar.addEmpty();
            addSeparator();
            this.toolbar.addEmpty();
            addPageSize();
        }

        if (config.addFill) {
            this.toolbar.addFill();
        }

        boolean addSep = false;

        if (config.addDisplayText) {
            //noinspection ConstantConditions
            addSep = addSeparator(addSep);
            this.displayText = new Label(I18n.I.noData());
            this.displayText.setVisible(false);
            this.toolbar.add(this.displayText);
        }

        for (ToolbarAddOn addOn : config.addOns) {
            addSep = addSeparator(addSep);
            addOn.addTo(this.toolbar);
        }

        if (SessionData.INSTANCE.isAnonymous()) {
            return;
        }

        if (config.pinCallback != null) {
            addSep = addSeparator(addSep);
            this.pin = Button.icon("x-tbar-unpin") // $NON-NLS$
                    .active(!this.pinned)
                    .toggleActive()
                    .tooltip(I18n.I.automaticUpdateActivated())
                    .clickHandler(event -> onPin(config.pinCallback)).build();
            this.toolbar.add(this.pin);
        }

        if (config.pdfCallback != null) {
            //noinspection UnusedAssignment
            addSep = addSeparator(addSep);
            this.toolbar.add(Button.icon("x-tbar-pdf") // $NON-NLS$
                    .tooltip(I18n.I.exportAsPdf())
                    .clickHandler(event -> config.pdfCallback.onPdf())
                    .build());
        }

        if (config.searchCallback != null) {
            //noinspection UnusedAssignment
            addSep = addSeparator(addSep);
            this.search = Button.icon("x-tbar-search") // $NON-NLS$
                    .tooltip(I18n.I.gotoFinder())
                    .clickHandler(event -> config.searchCallback.onSearch())
                    .build();
            this.toolbar.add(this.search);
        }
    }

    private void addPageSize() {
        final Label entriesPerPageLabel = new Label(I18n.I.entriesPerPage());
        this.toolbar.add(entriesPerPageLabel);
        this.addedWidgets.add(entriesPerPageLabel);
        this.toolbar.addEmpty("5px");  // $NON-NLS$

        final Menu menu = new Menu();
        for (Integer pageSize : AbstractFinder.POSSIBLE_PAGE_SIZES) {
            menu.add(new MenuItem(pageSize.toString()).withData(PAGE_SIZE_KEY, pageSize));
        }

        this.pageSize = new SelectButton()
                .withMenu(menu)
                .withSelectionHandler(event -> {
                    Integer pageSize = (Integer) event.getSelectedItem().getData(PAGE_SIZE_KEY);
                    callback.setPageSize(pageSize);
                });

        this.toolbar.add(this.pageSize);
        this.addedWidgets.add(this.pageSize);
    }

    private void addSeparator() {
        final Separator separator = new Separator();
        this.toolbar.add(separator);
        this.addedWidgets.add(separator);
    }

    private void addRefreshIndicator() {
        this.refresh = new Label();
        this.refresh.setStyleName("mm-tbar-done"); // $NON-NLS-0$
        this.toolbar.add(this.refresh);
        this.addedWidgets.add(refresh);
    }

    private void addLastButton(Config config) {
        if (config.mode == Mode.FULL) {
            this.last = Button.icon("x-tbar-page-last") // $NON-NLS$
                    .tooltip(I18n.I.lastPage())
                    .clickHandler(event -> onLast())
                    .build();
            this.toolbar.add(this.last);
            this.addedWidgets.add(this.last);
        }
    }

    private void addNextButton() {
        this.next = Button.icon("x-tbar-page-next") // $NON-NLS$
                .tooltip(I18n.I.nextPage())
                .clickHandler(event -> onNext())
                .build();
        this.toolbar.add(this.next);
        this.addedWidgets.add(this.next);
    }

    private void addPageNumber(Config config) {
        if (!SessionData.isAsDesign()) {
            final Label label = new Label(I18n.I.page());
            this.toolbar.add(label);
            this.addedWidgets.add(label);
        }

        if (config.mode == Mode.FULL) {
            this.currentPageBox = new DecimalBox()
                    .withMandatory()
                    .withMin(1)
                    .withoutSpinOnKeyDownAndMouseWheel()
                    .withAdditionalStyleName("mm-pagingWidgets-currentPageBox") // $NON-NLS$
                    .withValueChangeHandler(this::currentPageBoxValueChangeHandler);
            this.toolbar.add(this.currentPageBox);
            this.addedWidgets.add(this.currentPageBox);
        }
        else {
            this.pageText = new Label("0"); // $NON-NLS-0$
            this.toolbar.add(this.pageText);
            this.addedWidgets.add(this.pageText);
        }

        this.afterText = new Label(" " + I18n.I.fromN(0));
        this.toolbar.add(this.afterText);
        this.addedWidgets.add(this.afterText);
    }

    private void currentPageBoxValueChangeHandler(ValueChangeEvent<BigDecimal> event) {
        if (!this.pageChangeEnabled) {
            return;
        }
        try {
            final BigDecimal value = event.getValue();
            if (value == null) {
                return;
            }

            final int page = value.intValue();
            if (page < this.minPage) {
                this.currentPageBox.setValue(BigDecimal.valueOf(this.minPage));
                return;
            }
            else if (page > this.maxPage) {
                this.currentPageBox.setValue(BigDecimal.valueOf(this.maxPage));
                return;
            }
            else if (page == this.currentPage) {
                this.currentPageBox.setValue(BigDecimal.valueOf(this.currentPage));
                return;
            }

            fireEvent(new PagingEvent(PagingEvent.Action.SPECIFIED, page - 1));
        } catch (Exception e) {
            Firebug.error("<PagingWidgets.currentPageBoxValueChangeHandler> paging failed", e);
        }
    }

    private void addPreviousButton() {
        this.previous = Button.icon("x-tbar-page-prev") // $NON-NLS$
                .tooltip(I18n.I.previousPage())
                .clickHandler(event -> onPrevious())
                .build();
        this.previous.setEnabled(false);
        this.toolbar.add(this.previous);
        this.addedWidgets.add(this.previous);
    }

    private void addFirstButton() {
        this.first = Button.icon("x-tbar-page-first") // $NON-NLS$
                .tooltip(I18n.I.firstPage())
                .clickHandler(event -> onFirst())
                .build();
        this.first.setEnabled(false);
        this.toolbar.add(this.first);
        this.addedWidgets.add(this.first);
    }

    private boolean addSeparator(boolean add) {
        if (add) {
            if (this.displayText != null && this.displayTextSeparator == null) {
                this.displayTextSeparator = new Separator();
                this.displayTextSeparator.setVisible(false);
                this.toolbar.add(this.displayTextSeparator);
            }
            else {
                addSeparator();
            }
        }
        return true;
    }

    private void setDisplayTextVisible(boolean visible) {
        if (this.displayText == null) {
            return;
        }
        this.displayText.setVisible(visible);
        if (this.displayTextSeparator != null) {
            this.displayTextSeparator.setVisible(visible);
        }
    }

    public FloatingToolbar getToolbar() {
        return this.toolbar;
    }

    public void setPagingEventHandler(PagingEvent.Callback callback) {
        this.callback = callback;
    }

    private void fireEvent(PagingEvent event) {
        setButtonState(-1, -1, -1, -1);
        this.refresh.setStyleName("mm-tbar-loading");
        if (this.callback != null) {
            this.callback.handle(event);
        }
    }

    public void handleEvent(PageLoadedEvent event) {
        final int currentPage = event.getCurrentPage();
        final int numPages = event.getNumPages();

        this.pageChangeEnabled = false;
        if (numPages < 1) {
            setButtonState(-1, -1, -1, -1);
            if (this.currentPageBox != null) {
                this.currentPage = 0;
                this.minPage = 0;
                this.maxPage = 0;
                this.currentPageBox.setValue(BigDecimal.ZERO);
                this.currentPageBox.withMin(this.minPage);
                this.currentPageBox.withMax(this.maxPage);
                this.currentPageBox.setEnabled(false);
            }
            else {
                this.pageText.setText("0");  // $NON-NLS$
            }
            this.afterText.setText(" " + I18n.I.fromN(0));
            if (this.entryCountText != null) {
                this.entryCountText.setText("");
            }
            setDisplayTextVisible(this.allVisible);
            if (this.search != null) {
                this.search.setVisible(false);
            }
        }
        else {
            setButtonState(0, currentPage - 1, currentPage + 1, numPages - 1);
            if (this.currentPageBox != null) {
                this.currentPageBox.setEnabled(true);
                this.currentPage = currentPage + 1;
                this.minPage = 1;
                this.maxPage = numPages;
                this.currentPageBox.setValue(BigDecimal.valueOf(this.currentPage));
                this.currentPageBox.withMin(this.minPage);
                this.currentPageBox.withMax(this.maxPage);
            }
            else {
                this.pageText.setText(PAGE_RENDERER.render(currentPage + 1));
            }

            this.afterText.setText(" " + I18n.I.from() + " " + PAGE_RENDERER.render(numPages));
            if (this.entryCountText != null) {
                final String resultFrom = Formatter.FORMAT_NUMBER_GROUPS.format(event.getResultOffset() + 1);
                final String resultTo = Formatter.FORMAT_NUMBER_GROUPS.format(event.getResultOffset() + event.getResultCount());
                final String resultTotal = Formatter.FORMAT_NUMBER_GROUPS.format(event.getResultTotal());
                this.entryCountText.setText(I18n.I.pagingEntryFromToText(resultFrom, resultTo, resultTotal));
            }
            setDisplayTextVisible(false);
            if (this.search != null) {
                this.search.setVisible(this.allVisible);
            }

            if (this.pageSize != null) {
                this.pageSize.setSelectedData(PAGE_SIZE_KEY, event.getPageSize());
            }
        }
        this.pageChangeEnabled = true;
        this.refresh.setStyleName("mm-tbar-done");
    }

    private void onFirst() {
        fireEvent(new PagingEvent(PagingEvent.Action.FIRST));
    }

    private void onLast() {
        fireEvent(new PagingEvent(PagingEvent.Action.LAST));
    }

    private void onNext() {
        fireEvent(new PagingEvent(PagingEvent.Action.NEXT));
    }

    private void onPrevious() {
        fireEvent(new PagingEvent(PagingEvent.Action.PREVIOUS));
    }

    private void setButtonState(int first, int previous, int next, int last) {
        this.first.setEnabled((first == 0 && previous >= 0));
        this.previous.setEnabled((previous >= 0));
        this.next.setEnabled((next >= 0 && next <= last));
        if (this.last != null) {
            this.last.setEnabled((last >= 0 && next <= last));
        }
    }

    private void onPin(PinCallback callback) {
        this.pinned = !this.pinned;
        IconImage.setIconStyle(this.pin, this.pinned ? "x-tbar-pin" : "x-tbar-unpin"); // $NON-NLS$
        Tooltip.addQtip(this.pin, this.pinned ? I18n.I.automaticUpdatesDeactivated() : I18n.I.automaticUpdateActivated());
        callback.onPin(this.pinned);
    }

    public void setVisible(boolean visible) {
        this.allVisible = visible;

        if (!visible) {
            setDisplayTextVisible(false);
        }

        for (Widget w : this.addedWidgets) {
            w.setVisible(visible);
        }
    }

    public void setEnabled(boolean enabled) {
        for (Widget w : this.addedWidgets) {
            if (w instanceof HasEnabled) {
                ((HasEnabled) w).setEnabled(enabled);
            }
        }
    }
}
