/*
 * FinderFormElements.java
 *
 * Created on 10.06.2008 13:32:06
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.FinderFormElementEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.LiveFinderElementConfigurator;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.AbstractFinderForm.eqOp;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.SymbolOption.SymbolField.IID_WITHOUT_SUFFIX;
import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.SymbolOption.SymbolField.QID;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Michael Lösch
 */
public class FinderFormElements {
    static final Item ONE_DAY = new Item(I18n.I.nDays(1), "1d");  // $NON-NLS-0$

    static final Item TODAY = new Item(I18n.I.today(), "0d");  // $NON-NLS-0$

    static final Item YESTERDAY = new Item(I18n.I.yesterday(), "1d");  // $NON-NLS-0$

    static final Item ONE_WEEK = new Item(I18n.I.nWeeks(1), "1w");  // $NON-NLS-0$

    static final Item ONE_MONTH = new Item(I18n.I.nMonths(1), "1m");  // $NON-NLS-0$

    static final Item THREE_MONTH = new Item(I18n.I.nMonths(3), "3m");  // $NON-NLS-0$

    static final Item SIX_MONTHS = new Item(I18n.I.nMonths(6), "6m");  // $NON-NLS-0$

    static final Item ONE_YEAR = new Item(I18n.I.nYears(1), "1y");  // $NON-NLS-0$

    static final Item THREE_YEARS = new Item(I18n.I.nYears(3), "3y");  // $NON-NLS-0$

    static final Item FIVE_YEARS = new Item(I18n.I.nYears(5), "5y");  // $NON-NLS-0$

    static final Item TEN_YEARS = new Item(I18n.I.nYears(10), "10y");  // $NON-NLS-0$

    public static final List<Item> LIST_EXPIRATION_DATES =
            Arrays.asList(ONE_WEEK, ONE_MONTH, THREE_MONTH, SIX_MONTHS, ONE_YEAR);

    public static final List<Item> LIST_ISSUE_DATES =
            Arrays.asList(TODAY, YESTERDAY, ONE_WEEK, ONE_MONTH);

    public static final List<Item> LIST_ISSUE_DATES2 =
            Arrays.asList(ONE_WEEK, ONE_MONTH, THREE_MONTH, SIX_MONTHS, ONE_YEAR, THREE_YEARS);

    private static final char SINGLE_QUOTE = '\'';

    public static String IGNORE_VALUE = "NaV"; // $NON-NLS-0$

    public static final Item BELOW_PAR = new Item(I18n.I.belowPar(), "<"); // $NON-NLS$

    public static final Item PAR = new Item(I18n.I.par(), "=="); // $NON-NLS$

    public static final Item ABOVE_PAR = new Item(I18n.I.abovePar(), ">"); // $NON-NLS$

    public static final List<Item> LIST_PAR = Arrays.asList(BELOW_PAR, PAR, ABOVE_PAR);

    private static final String EXACT_MATCH_PREFIX = "+";

    private static final NewsSearchParser NEWS_SEARCH_PARSER = new NewsSearchParser();

    public static MmJsDate add(MmJsDate date, String interval, DateTimeUtil.PeriodMode mode) {
        final MmJsDate result = new MmJsDate(date.atMidnight());
        int count = Integer.parseInt(interval.substring(0, interval.length() - 1));
        if (mode == DateTimeUtil.PeriodMode.PAST) {
            count = -count;
        }
        final char iv = interval.charAt(interval.length() - 1);
        switch (iv) {
            case 'd':
                return result.addDays(count);
            case 'w':
                return result.addDays(7 * count);
            case 'm':
                return result.addMonths(count);
            case 'y':
                return result.addYears(count);
            default:
                throw new RuntimeException("unsupported interval: " + iv); // $NON-NLS-0$
        }
    }

    /**
     * Base class for all other elements
     */
    static abstract class AbstractOption implements FinderFormElement,
            HasValueChangeHandlers<String>, ChangeHandler {
        protected final CheckBox cb;  // whether this element is enabled

        protected final String field; // used in query expressions

        protected String id; // id of this element, used to store/retrieve configuration

        protected final String label;

        private boolean enabled = false;

        protected boolean alwaysEnabled = false;

        private boolean silent = false;

        private boolean active = true;

        protected String style = null;

        private HandlerManager manager;

        private Integer defaultOrder;

        protected AbstractOption(String id, String field, String label) {
            this.id = id;
            this.label = label;
            this.field = field;
            this.cb = new CheckBox(this.label, true);
            this.cb.addClickHandler(event -> checkActivation());
            this.cb.addKeyPressHandler(event -> checkActivation());
        }

        public String getLabel() {
            return this.label;
        }

        public String getId() {
            return this.id;
        }

        public void onChange(ChangeEvent event) {
            //implement in subclass
        }

        public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
            if (this.manager == null) {
                this.manager = new HandlerManager(this);
            }
            return this.manager.addHandler(ValueChangeEvent.getType(), handler);
        }

        public void fireEvent(GwtEvent<?> gwtEvent) {
            if (this.manager != null) {
                this.manager.fireEvent(gwtEvent);
            }
        }

        public void addChangeHandler(ChangeHandler ch) {
            if (this.manager == null) {
                this.manager = new HandlerManager(this);
            }
            this.manager.addHandler(ChangeEvent.getType(), ch);
        }

        public void setAlwaysEnabled() {
            this.alwaysEnabled = true;
            this.cb.setValue(true);
            this.cb.setEnabled(false);
        }

        public void setSilent() {
            this.silent = true;
        }

        public boolean getValue() {
            return this.cb.getValue();
        }

        public void setValue(boolean checked) {
            this.cb.setValue(checked);
            checkActivation();
        }

        public AbstractOption withStyle(String style) {
            this.style = style;
            return this;
        }

        public Integer getDefaultOrder() {
            return defaultOrder;
        }

        public AbstractOption withConf(String[] conf, Integer defaultValue) {
            this.defaultOrder = defaultValue;
            if (conf == null) {
                setActive(defaultValue != null);
                return this;
            }
            setActive(false);
            for (String s : conf) {
                if (this.id.equals(s)) {
                    setActive(true);
                }
            }
            return this;
        }

        public AbstractOption withConf(String[] conf) {
            return withConf(conf, null);
        }

        public void addClickHandler(ClickHandler ch) {
            this.cb.addClickHandler(ch);
        }

        protected AbstractOption(String field, String label) {
            this(field, field, label);
        }

        public void addConfigTo(FinderFormConfig config) {
            if (this.cb.getValue()) {
                config.put(this.id, "true"); // $NON-NLS-0$
            }
        }

        protected void checkActivation() {
            if (this.cb.getValue() != this.enabled) {
                this.enabled = this.cb.getValue();
                setEnabled(this.enabled);
            }
        }

        abstract void setEnabled(boolean enabled);

        public boolean isEnabled() {
            return this.enabled;
        }

        public void addTo(FlexTable flexTable, int row) {
            if (!isActive()) {
                return;
            }
            flexTable.setWidget(row, 1, this.cb);
            //noinspection GWTStyleCheck
            flexTable.getFlexCellFormatter().setStyleName(row, 1, "mm-finder-element title"); // $NON-NLS-0$
            final Widget w = getInnerWidget();
            if (w != null) {
                setEnabled(this.cb.getValue());
                flexTable.setWidget(row, 2, w);
                //noinspection GWTStyleCheck
                flexTable.getFlexCellFormatter().setStyleName(row, 2, "mm-finder-element values"); // $NON-NLS-0$
            }
        }

        public Widget getInnerWidget() {
            return null;
        }

        public void initialize(Map<String, FinderMetaList> map) {
            // empty
        }

        public void apply(FinderFormConfig config) {
            this.cb.setValue("true".equals(config.get(this.id)) || this.alwaysEnabled); // $NON-NLS-0$
            checkActivation();
        }

        public Grid createInner(int cols) {
            return createInner(1, cols);
        }

        protected Grid createInner(int rows, int cols) {
            Grid result = new Grid(rows, cols);
            result.setStyleName("mm-formElement"); // $NON-NLS-0$
            return result;
        }

        protected String getFieldname() {
            return this.field;
        }

        public final String getQuery() {
            return this.cb.getValue() && !this.silent ? doGetQuery() : null;
        }

/*
        private String doGetInactiveQuery() {
            return StringUtil.hasText(this.inactiveQuery)
                    ? this.inactiveQuery
                    : null;
        }

        public FinderFormElement withInactiveQuery(String inactiveQuery) {
            this.inactiveQuery = inactiveQuery;
            return this;
        }
*/

        public void addExplanation(final FlowPanel panel) {
            if (this.cb.getValue()) {
                doAddExplanation(panel);
            }
        }

        public void reset() {
            this.cb.setValue(this.alwaysEnabled);
            checkActivation();
        }

        protected String key(boolean query) {
            return query ? this.field : this.label;
        }

        protected abstract String doGetQuery();

        protected abstract void doAddExplanation(final FlowPanel panel);

        protected void addExplanation(FlowPanel panel, String explanation) {
            if (panel.getWidgetCount() > 0) {
                final InlineLabel span = new InlineLabel(I18n.I.and());
                span.setStyleName("mm-operator"); // $NON-NLS-0$
                panel.add(span);
            }
            final InlineLabel span = new InlineLabel(explanation);
            span.setStyleName("mm-explanation"); // $NON-NLS-0$
            panel.add(span);
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.silent = !active;
            this.active = active;
        }

        public boolean isConfigurable() {
            return true;
        }
    }

    /**
     * Combines sevaral other options that it displays in a single row
     */
    static class MultiOption extends AbstractOption {
        private List<AbstractOption> options = new ArrayList<>();

        MultiOption(String id, String label, AbstractOption... options) {
            super(id, null, label);
            for (final AbstractOption option : options) {
                this.options.add(option);
                option.cb.setValue(true);
            }
        }

        protected String doGetQuery() {
            return AbstractFinderForm.getQuery(this.options);
        }

        protected void doAddExplanation(FlowPanel panel) {
            AbstractFinderForm.addExplanation(panel, this.options);
        }

        public void initialize(Map<String, FinderMetaList> map) {
            for (AbstractOption option : options) {
                option.initialize(map);
            }
        }

        void setEnabled(boolean enabled) {
            for (AbstractOption option : options) {
                option.setEnabled(enabled);
            }
        }

        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            for (AbstractOption option : options) {
                option.addConfigTo(config);
            }
        }

        public void apply(FinderFormConfig config) {
            super.apply(config);
            for (AbstractOption option : options) {
                option.apply(config);
                option.cb.setValue(true);
            }
        }

        public void reset() {
            super.reset();
            for (AbstractOption option : options) {
                option.reset();
                option.cb.setValue(true);
            }
        }

        public Widget getInnerWidget() {
            final Grid result = new Grid(1, this.options.size());
            for (int i = 0; i < options.size(); i++) {
                result.setWidget(0, i, options.get(i).getInnerWidget());
            }
            return result;
        }
    }

    /**
     * An option that just be enabled but takes no user configurable parameters
     */
    public static class BooleanOption extends AbstractOption {
        private final boolean reverse;

        protected BooleanOption(String id, String field, String label, boolean reverse) {
            super(id, field, label);
            this.reverse = reverse;
        }

        protected BooleanOption(String field, String label, boolean reverse) {
            this(field, field, label, reverse);
        }

        protected BooleanOption(String field, String label) {
            this(field, field, label, false);
        }

        protected void setEnabled(boolean enabled) {
            // nothing to do
        }

        protected String doGetQuery() {
            return this.field + (this.reverse ? "=='false'" : "=='true'"); // $NON-NLS-0$ $NON-NLS-1$
        }

        protected void doAddExplanation(FlowPanel panel) {
            addExplanation(panel, this.label);
        }
    }

    /**
     * Extends BooleanOption with configurable value.
     */
    @SuppressWarnings("unused")
    static class BooleanWithQuerySuffixOption extends BooleanOption {
        private final String querySuffix;

        protected BooleanWithQuerySuffixOption(String id, String field, String label,
                String querySuffix) {
            super(id, field, label, false);
            this.querySuffix = querySuffix;
        }

        protected BooleanWithQuerySuffixOption(String field, String label, String querySuffix) {
            this(field, field, label, querySuffix);
        }

        protected String doGetQuery() {
            return this.field + this.querySuffix;
        }
    }

    /**
     * A TextBox with a CheckBox; checking the CheckBox will override the fieldname used
     * in the query with some other fieldname.
     */
    @SuppressWarnings("unused")
    static class TwoFieldTextOption extends TextOption {
        private final CheckBox useSecondField;

        private final String field2;

        private final String label2;

        TwoFieldTextOption(String id, String field, String label, String field2, String label2) {
            super(id, field, label, null);
            this.field2 = field2;
            this.label2 = label2;
            this.useSecondField = new CheckBox(" " + label2); // $NON-NLS-0$
        }

        @Override
        void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            this.useSecondField.setEnabled(enabled);
        }

        @Override
        protected Widget doGetInnerWidget() {
            final Grid result = new Grid(2, 1);
            result.setStyleName("mm-formElement"); // $NON-NLS-0$
            result.setWidget(0, 0, this.textBox);
            result.setWidget(1, 0, this.useSecondField);
            return result;
        }

        @Override
        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            if (this.useSecondField.getValue()) {
                config.put(this.id + "-2", "true"); // $NON-NLS-0$ $NON-NLS-1$
            }
        }

        @Override
        public void apply(FinderFormConfig config) {
            super.apply(config);
            this.cb.setValue("true".equals(config.get(this.id + "-2"))); // $NON-NLS-0$ $NON-NLS-1$
        }

        @Override
        public void reset() {
            super.reset();
            this.cb.setValue(false);
        }

        @Override
        protected String getFieldLabel() {
            return this.useSecondField.getValue()
                    ? (super.getFieldLabel() + " (" + this.label2 + ")") // $NON-NLS-0$ $NON-NLS-1$
                    : super.getFieldLabel();
        }

        @Override
        protected String getFieldname() {
            return this.useSecondField.getValue() ? this.field2 : super.getFieldname();
        }
    }

    static class TextOption extends AbstractOption {
        protected TextBox textBox;

        private String suffix = null;

        protected String width = "100px"; // $NON-NLS-0$

        TextOption(String id, String field, String label, String textFieldSuffix) {
            super(id, field, label);
            this.suffix = textFieldSuffix;
        }

        TextOption(String field, String label) {
            this(field, field, label, null);
        }

        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            if (this.textBox.getText().trim().length() > 0) {
                config.put(this.id + "-text", this.textBox.getText().trim()); // $NON-NLS-0$
            }
        }

        public Widget getInnerWidget() {
            if (this.textBox == null) {
                initialize();
            }
            return doGetInnerWidget();
        }

        public void apply(FinderFormConfig config) {
            super.apply(config);
            final String f = config.get(this.id + "-text"); // $NON-NLS-0$
            this.textBox.setText(f != null ? f : ""); // $NON-NLS-0$
        }

        public TextOption initialize() {
            this.textBox = new TextBox();
            this.textBox.setWidth(this.width);
            return this;
        }

        void setEnabled(boolean enabled) {
            this.textBox.setEnabled(enabled);
        }

        public void reset() {
            super.reset();
            this.textBox.setText(""); // $NON-NLS-0$
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public TextOption withWidth(String width) {
            this.width = width;
            return this;
        }

        protected String doGetQuery() {
            return query(true);
        }

        private String query(boolean query) {
            final String text = this.textBox.getText().trim();
            if (text.length() == 0) {
                return null;
            }
            if (query) {
                return getFieldname() + "==" + quote(text); // $NON-NLS-0$
            }
            else {
                return getFieldLabel() + " = " + text; // $NON-NLS-0$
            }
        }

        protected String getFieldLabel() {
            return this.label;
        }

        protected void doAddExplanation(FlowPanel panel) {
            addExplanation(panel, query(false));
        }

        protected Widget doGetInnerWidget() {
            if (this.suffix != null) {
                return this.textBox;
            }
            final HorizontalPanel result = new HorizontalPanel();
            result.add(this.textBox);
            result.add(new Label(this.suffix));
            return result;
        }
    }

    static class NewsSearchTextOption extends TextOption {

        NewsSearchTextOption(String field, String label) {
            super(field, label);
        }

        protected String doGetQuery() {
            return NEWS_SEARCH_PARSER.parse(this.textBox.getText());
        }
    }

    /**
     * An option that allows to select a range of values between two specified values, both of
     * which are selected from a ListBox
     */
    static class FromToBoxOption extends AbstractOption {
        protected ListBox fromLb;

        protected ListBox toLb;

        protected boolean optimize = true;

        protected boolean sortReverse = false;

        /**
         * used to select a particular from-to configuration quickly
         */
        protected List<Button> buttons;

        protected FromToBoxOption(String id, String field, String label, List<Item> items) {
            super(id, field, label);
            if (items != null) {
                this.fromLb = Item.asListBox(items);
                this.toLb = Item.asListBox(items);
            }
        }

        FromToBoxOption(String field, String label, List<Item> items) {
            this(field, field, label, items);
        }

        public FromToBoxOption unoptimized() {
            this.optimize = false;
            return this;
        }

        public FromToBoxOption withButton(String label, final String from, final String to) {
            if (this.buttons == null) {
                this.buttons = new ArrayList<>();
            }

            final Button b = Button.text(label)
                    .clickHandler(event -> {
                        selectItem(fromLb, from);
                        selectItem(toLb, to);
                    })
                    .forceLegacyBorders()
                    .build();

            this.buttons.add(b);
            return this;
        }

        public FromToBoxOption withSortReverse(boolean sortReverse) {
            this.sortReverse = sortReverse;
            return this;
        }

        public void initialize(Map<String, FinderMetaList> map) {
            if (this.fromLb != null) {
                return;
            }
            final FinderMetaList fml = map.get(this.field);
            if (fml == null) {
                DebugUtil.logToServer(this.getClass().getName() + ".addSections: No values for FieldName " + this.field); // $NON-NLS$
            }
            this.fromLb = createListBox(fml, null);
            this.toLb = createListBox(fml, null);
            if (fml != null && fml.getElement().size() > 1) {
                this.toLb.setSelectedIndex(1);
            }
        }

        void setEnabled(boolean enabled) {
            this.fromLb.setEnabled(enabled);
            this.toLb.setEnabled(enabled);
            if (this.buttons != null) {
                for (Button button : buttons) {
                    button.setEnabled(enabled);
                }
            }
        }

        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            int fromSelectedIndex = this.fromLb.getSelectedIndex();
            if (fromSelectedIndex > -1) {
                config.put(this.id + "-from", this.fromLb.getValue(fromSelectedIndex)); // $NON-NLS-0$
            }
            int toSelectedIndex = this.toLb.getSelectedIndex();
            if (toSelectedIndex > -1) {
                config.put(this.id + "-to", this.toLb.getValue(toSelectedIndex)); // $NON-NLS-0$
            }
        }

        public Widget getInnerWidget() {
            final Grid result = createInner(4 + (this.buttons != null ? this.buttons.size() : 0));
            result.setText(0, 0, I18n.I.from());
            result.setWidget(0, 1, this.fromLb);
            result.setText(0, 2, I18n.I.to());
            result.setWidget(0, 3, this.toLb);
            if (this.buttons != null) {
                for (int i = 0; i < buttons.size(); i++) {
                    result.setWidget(0, 4 + i, buttons.get(i));
                }
            }
            return result;
        }

        public void apply(FinderFormConfig config) {
            super.apply(config);
            selectItem(this.fromLb, config.get(this.id + "-from")); // $NON-NLS-0$
            selectItem(this.toLb, config.get(this.id + "-to")); // $NON-NLS-0$
        }

        public void reset() {
            super.reset();
            this.fromLb.setSelectedIndex(0);
            this.toLb.setSelectedIndex(0);
        }

        protected String doGetQuery() {
            int min = Math.min(this.fromLb.getSelectedIndex(), this.toLb.getSelectedIndex());
            int max = Math.max(this.fromLb.getSelectedIndex(), this.toLb.getSelectedIndex());
            int from = this.sortReverse ? max : min;
            int to = this.sortReverse ? min : max;

            if (this.optimize && from == to) {
                return this.field + "==" + quote(selectedValue(this.fromLb)); // $NON-NLS-0$
            }

            return this.field + ">=" + quote(this.fromLb.getValue(from)) // $NON-NLS-0$
                    + "&&" + this.field + "<=" + quote(this.toLb.getValue(to)); // $NON-NLS-0$ $NON-NLS-1$
        }

        protected void doAddExplanation(FlowPanel panel) {
            int min = Math.min(this.fromLb.getSelectedIndex(), this.toLb.getSelectedIndex());
            int max = Math.max(this.fromLb.getSelectedIndex(), this.toLb.getSelectedIndex());
            int from = this.sortReverse ? max : min;
            int to = this.sortReverse ? min : max;

            if (this.optimize && from == to) {
                addExplanation(panel, this.label + " = " + selectedText(this.fromLb)); // $NON-NLS-0$
            }

            addExplanation(panel, this.label + " >= " + this.fromLb.getItemText(from)); // $NON-NLS-0$
            addExplanation(panel, this.label + " <= " + this.toLb.getItemText(to)); // $NON-NLS-0$
        }
    }

    /**
     * An option that allows to select a range of values between two specified values, both of
     * which can be entered by the user
     */
    public static class FromToTextOption extends AbstractOption {
        protected TextBox fromText;

        protected TextBox toText;

        private String defaultFrom = null;

        private String defaultTo = null;

        protected String textFieldSuffix = null;

        protected String width = "70px"; // $NON-NLS-0$

        protected FromToTextOption(String id, String field, String label, String textFieldSuffix) {
            super(id, field, label);
            this.textFieldSuffix = textFieldSuffix;
        }

        public FromToTextOption(String field, String label, String textFieldSuffix) {
            this(field, field, label, textFieldSuffix);
        }

        FromToTextOption(String field, String label) {
            this(field, field, label, null);
        }

        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            if (this.fromText.getText().trim().length() > 0) {
                config.put(this.id + "-from", this.fromText.getText().trim()); // $NON-NLS-0$
            }
            if (this.toText.getText().trim().length() > 0) {
                config.put(this.id + "-to", this.toText.getText().trim()); // $NON-NLS-0$
            }
        }

        protected String toQueryValue(String input, boolean query) {
            return input;
        }

        protected String toQueryValueMonths(String input, int min, int max, boolean query) {
            if (!StringUtil.hasText(input)) {
                return "";
            }
            try {
                final int n = Integer.parseInt(input);
                if ((min != -1 && n <= min) || (max != -1 && n > max)) {
                    return "";
                }
                final MmJsDate date = new MmJsDate().addMonths(n);
                return query ? JsDateFormatter.formatIsoDay(date) : input;
            } catch (NumberFormatException e) {
                return "";
            }

        }

        protected String toQueryValueYears(String input, boolean query) {
            if (!StringUtil.hasText(input)) {
                return "";
            }
            try {
                final int n = Integer.parseInt(input);
                final MmJsDate date = new MmJsDate().addYears(isNegative() ? -n : n);
                return query ? JsDateFormatter.formatIsoDay(date) : JsDateFormatter.formatDdmmyyyy(date);
            } catch (NumberFormatException e) {
                return "";
            }

        }

        public Widget getInnerWidget() {
            if (this.fromText == null) {
                initialize();
            }
            return createInnerGrid();
        }

        public void apply(FinderFormConfig config) {
            super.apply(config);
            final String f = config.get(this.id + "-from"); // $NON-NLS-0$
            this.fromText.setText(f != null ? f : ""); // $NON-NLS-0$
            final String t = config.get(this.id + "-to"); // $NON-NLS-0$
            this.toText.setText(t != null ? t : ""); // $NON-NLS-0$
        }

        public void setDefault(String defaultFrom, String defaultTo) {
            assert this.fromText == null;
            this.defaultFrom = defaultFrom;
            this.defaultTo = defaultTo;
        }

        public FromToTextOption initialize() {
            this.fromText = new TextBox();
            this.toText = new TextBox();
            this.fromText.setWidth(this.width);
            this.toText.setWidth(this.width);
            this.fromText.setEnabled(isEnabled());
            this.toText.setEnabled(isEnabled());
            resetTexts();
            return this;
        }

        void setEnabled(boolean enabled) {
            if (this.fromText == null) {
                return;
            }
            this.fromText.setEnabled(enabled);
            this.toText.setEnabled(enabled);
        }

        public void reset() {
            super.reset();
            resetTexts();
        }

        private void resetTexts() {
            if (this.fromText == null || this.toText == null) {
                return;
            }
            this.fromText.setText(this.defaultFrom != null ? this.defaultFrom : ""); // $NON-NLS-0$
            this.toText.setText(this.defaultTo != null ? this.defaultTo : ""); // $NON-NLS-0$
        }

        public void setWidth(String width) {
            this.width = width;
        }

        protected void addItems(Grid result, int row, int col) {
            result.setWidget(row, col++, createTextBox(" " + I18n.I.from() + " ", this.fromText, this.textFieldSuffix)); // $NON-NLS$
            result.setWidget(row, col, createTextBox(" " + I18n.I.to() + " ", this.toText, this.textFieldSuffix)); // $NON-NLS$
        }

        protected void addItems(Grid result, int col) {
            addItems(result, 0, col);
        }

        protected Grid createInnerGrid() {
            final Grid result = createInner(2);
            addItems(result, 0);
            return result;
        }

        protected boolean isNegative() {
            return false;
        }

        protected String doGetQuery() {
            final StringBuilder sb = new StringBuilder();
            final String from = toQueryValue(fromText.getText().trim(), true);
            final String to = toQueryValue(this.toText.getText().trim(), true);

            if (from.length() > 0) {
                if (from.equals(to)) {
                    sb.append(getFieldname()).append("==").append(quote(from)); // $NON-NLS-0$
                    return sb.toString();
                }
                if (isNegative()) {
                    sb.append(getFieldname()).append("<=").append(quote(from)); // $NON-NLS-0$
                }
                else {
                    sb.append(getFieldname()).append(">=").append(quote(from)); // $NON-NLS-0$
                }
            }
            if (to.length() > 0) {
                if (sb.length() > 0) {
                    sb.append(" && "); // $NON-NLS-0$
                }
                if (isNegative()) {
                    sb.append(getFieldname()).append(">=").append(quote(to)); // $NON-NLS-0$
                }
                else {
                    sb.append(getFieldname()).append("<=").append(quote(to)); // $NON-NLS-0$
                }
            }
            return sb.length() == 0 ? null : sb.toString();
        }

        protected void doAddExplanation(FlowPanel panel) {
            final String from = toQueryValue(this.fromText.getText().trim(), false);
            final String to = toQueryValue(this.toText.getText().trim(), false);

            doAddExplanation(panel, from, to);
        }

        protected void doAddExplanation(FlowPanel panel, String from, String to) {
            final String suffix = StringUtil.hasText(this.textFieldSuffix)
                    ? " " + this.textFieldSuffix
                    : "";
            if (from.length() > 0) {
                if (from.equals(to)) {
                    addExplanation(panel, getFieldLabel() + eqOp(false) + from + suffix);
                    return;
                }
                if (isNegative()) {
                    addExplanation(panel, getFieldLabel() + "<=" + from + suffix); // $NON-NLS-0$
                }
                else {
                    addExplanation(panel, getFieldLabel() + ">=" + from + suffix); // $NON-NLS-0$
                }
            }
            if (to.length() > 0) {
                if (isNegative()) {
                    addExplanation(panel, getFieldLabel() + ">=" + to + suffix); // $NON-NLS-0$
                }
                else {
                    addExplanation(panel, getFieldLabel() + "<=" + to + suffix); // $NON-NLS-0$
                }
            }
        }

        protected String getFieldLabel() {
            return this.label;
        }

        protected Widget createTextBox(String prefix, TextBox tb, String suffix) {
            final HorizontalPanel result = new HorizontalPanel();
            result.add(new Label(prefix));
            result.add(tb);
            if (suffix != null) {
                result.add(new Label(suffix));
            }
            return result;
        }
    }

    public static void fillListBox(ListBox lb, List<Item> initialItems) {
        lb.clear();
        for (Item initialItem : initialItems) {
            lb.addItem(initialItem.item, initialItem.value);
        }
    }

    public static class Item implements Comparable<Item> {
        static ListBox asListBox(List<Item> items, String defaultKey) {
            ListBox result = new ListBox();
            int n = 0;
            for (Item item : items) {
                result.addItem(item.value);
                result.setItemText(n++, item.item);
                if (item.value.equals(defaultKey)) {
                    result.setSelectedIndex(result.getItemCount() - 1);
                }
            }
            return result;
        }

        static ListBox asListBox(List<Item> items) {
            return asListBox(items, null);
        }

        final String item;

        final String value;

        public Item(String item, String value) {
            this.item = item;
            this.value = value;
        }

        public int compareTo(Item o) {
            final int result = this.item.compareTo(o.item);
            if (result == 0) {
                return this.value.compareTo(o.value);
            }
            return result;
        }

        public String getItem() {
            return item;
        }

        public String getValue() {
            return value;
        }
    }

    static class ItemGroup {
        final String name;

        final List<Item> items;

        ItemGroup(String name, List<Item> items) {
            this.name = name;
            this.items = items;
        }
    }

    /**
     * An option that allows to select a single value from a ListBox
     */
    public static class ListBoxOption extends AbstractOption implements DynamicValueElement {
        protected ListBox lb;

        protected final String defaultKey;

        private boolean metadataUpdateAllowed = true;

        private boolean isEnum = false;

        protected boolean useExactMatchPrefix = true;

        public ListBoxOption(String id, String field, String label, List<Item> items,
                String defaultKey) {
            super(id, field, label);
            this.defaultKey = defaultKey;
            if (items != null) {
                this.lb = Item.asListBox(items, defaultKey);
                this.lb.addChangeHandler(this);
                addStyle(this.lb);
            }
        }

        public String quote(String s) {
            return FinderFormElements.quote(s);
        }

        public ListBoxOption(String field, String label, List<Item> items, String defaultKey) {
            this(field, field, label, items, defaultKey);
        }

        public void onChange(ChangeEvent event) {
            if (!getValue()) {
                return;
            }
            ValueChangeEvent.fire(this, selectedValue(this.lb));
        }

        public void updateMetadata(Map<String, FinderMetaList> map, boolean force) {
            if ((getValue() && !force) || !this.metadataUpdateAllowed) {
                return;
            }
            final String selected = selectedValue(lb);
            this.lb.clear();
            fillListBox(lb, map.get(this.field), selected != null
                    ? selected
                    : this.defaultKey);
            this.cb.setEnabled(this.lb.getItemCount() > 0);
        }

        public ListBoxOption withoutMetadataUpdate() {
            this.metadataUpdateAllowed = false;
            return this;
        }

        @SuppressWarnings("unused")
        public ListBoxOption withoutMatchPrefix() {
            this.useExactMatchPrefix = false;
            return this;
        }

        public void addConfigTo(FinderFormConfig config) {
            if (!IGNORE_VALUE.equals(getValueStr())) {
                super.addConfigTo(config);
                config.put(getItemKey(), getValueStr());
            }
        }

        protected String getValueStr() {
            return selectedValue(this.lb);
        }

        public Widget getInnerWidget() {
            return this.lb;
        }

        public void apply(FinderFormConfig config) {
            super.apply(config);
            selectItem(this.lb, getListBoxValueFromConfig(config));
        }

        protected String getListBoxValueFromConfig(FinderFormConfig config) {
            return config.get(getItemKey());
        }

        public void initialize(Map<String, FinderMetaList> map) {
            final FinderMetaList metaList = map.get(this.field);
            if (metaList == null) {
                return;
            }
            if (this.lb == null) {
                this.lb = createListBox(metaList, this.defaultKey);
                addStyle(this.lb);
                this.lb.addChangeHandler(this);
            }
            if (this.isEnum != metaList.isEnum()) {
                this.isEnum = metaList.isEnum();
            }
        }

        private void addStyle(ListBox lb) {
            if (this.style != null) {
                lb.addStyleName(this.style);
            }
        }

        void setEnabled(boolean enabled) {
            this.lb.setEnabled(enabled);
        }

        public void reset() {
            super.reset();
            selectItem(this.lb, this.defaultKey);
        }

        protected String doGetQuery() {
            final String selectedValue = selectedValue(this.lb);
            if (IGNORE_VALUE.equals(selectedValue)) {
                return null;
            }
            return this.field + "==" + quote(((this.isEnum && this.useExactMatchPrefix) ? EXACT_MATCH_PREFIX : "") + selectedValue);
        }

        protected void doAddExplanation(FlowPanel panel) {
            if (IGNORE_VALUE.equals(selectedValue(this.lb))) {
                return;
            }
            final String text = getSelectedItemText().replaceAll("([0-9]*)", "").replace("()", ""); // $NON-NLS$
            addExplanation(panel, this.label + eqOp(false) + text);
        }

        String getSelectedItemText() {
            return selectedText(this.lb);
        }

        protected String getItemKey() {
            return this.id + "-item"; // $NON-NLS-0$
        }
    }

    static class MultiListBoxOption extends ListBoxOption {
        private final FlexTable table;

        private final List<String> listAlternativeValue = new ArrayList<>();

        private final List<String> listAlternativeText = new ArrayList<>();

        private static final char DIVIDER = '|';

        MultiListBoxOption(String id, String field, String label, List<Item> items,
                String defaultKey) {
            super(id, field, label, items, defaultKey);
            this.table = new FlexTable();
            this.table.setCellPadding(0);
            this.table.setCellSpacing(0);
            this.table.getColumnFormatter().setWidth(1, "18"); // $NON-NLS-0$
            if (this.lb != null) {
                addListBox(this.lb);
            }
        }

        MultiListBoxOption(String field, String label, List<Item> items, String defaultKey) {
            this(field, field, label, items, defaultKey);
        }

        @Override
        public void initialize(Map<String, FinderMetaList> map) {
            if (this.lb == null) {
                super.initialize(map);
                addListBox(this.lb);
            }
        }

        @Override
        public void reset() {
            super.reset();
            while (this.table.getRowCount() > 0) {
                this.table.removeRow(0);
            }
            this.listAlternativeText.clear();
            this.listAlternativeValue.clear();
            addListBox(this.lb);
        }

        protected String getValueStr(char divider) {
            if (this.listAlternativeValue.isEmpty()) {
                return super.getValueStr();
            }
            return StringUtil.join(divider, this.listAlternativeValue) + divider + super.getValueStr();
        }

        @Override
        protected String getValueStr() {
            return getValueStr(DIVIDER);
        }

        @Override
        protected String getListBoxValueFromConfig(FinderFormConfig config) {
            final String value = super.getListBoxValueFromConfig(config);
            if (value == null) {
                return null;
            }
            else {
                final int dividerPos = value.lastIndexOf(DIVIDER) + 1;
                return dividerPos > 0 ? value.substring(dividerPos) : value;
            }
        }

        @Override
        public void apply(FinderFormConfig config) {
            super.apply(config);
            final String s = config.get(getItemKey());
            final List<String> values = s == null ? null : StringUtil.split(s, DIVIDER);
            if (values == null || values.size() <= 1) {
                return;
            }
            for (int i = 0, n = values.size() - 1; i < n; i++) { // ignore last element, it is in listbox
                final String value = values.get(i);
                final String text = textForValue(this.lb, value);
                if (text != null) {
                    addAlternative(text, value);
                }
            }
        }

        private void addListBox(final ListBox lb) {
            this.table.setWidget(0, 0, lb);
            this.table.setWidget(0, 1, de.marketmaker.itools.gwtutil.client.widgets.Button.icon("mm-plus") // $NON-NLS$
                    .clickHandler(event -> addAlternative())
                    .build());
        }

        private void addAlternative() {
            addAlternative(selectedText(this.lb), selectedValue(this.lb));
        }

        private void addAlternative(String text, String value) {
            final int row = this.table.getRowCount() - 1;
            table.insertRow(row);
            final Label lbl1 = new Label(text);
            this.table.setWidget(row, 0, lbl1);
            final IconImageIcon iconDelete = IconImage.getIcon("mm-minus") // $NON-NLS$
                    .withClickHandler(event -> {
                        final int rowId = DOMUtil.getRowId(((Widget) event.getSource()).getElement());
                        listAlternativeText.remove(rowId);
                        listAlternativeValue.remove(rowId);
                        table.removeRow(rowId);
                        final ClickHandler adch = getAlternativeDeleteClickHandler();
                        if (adch != null) {
                            adch.onClick(event);
                        }
                    });
            iconDelete.addStyleName("mm-middle mm-link");
            this.table.setWidget(row, 1, iconDelete);
            this.listAlternativeText.add(text);
            this.listAlternativeValue.add(value);
        }

        protected ClickHandler getAlternativeDeleteClickHandler() {
            return null;
        }

        @Override
        public Widget getInnerWidget() {
            return this.table;
        }

        @Override
        protected String doGetQuery() {
            final Set<String> set = new HashSet<>(this.listAlternativeValue);
            set.add(selectedValue(this.lb));
            return this.field + "==" + quoteMultiple(set); // $NON-NLS-0$
        }

        @Override
        String getSelectedItemText() {
            final Set<String> set = new HashSet<>(this.listAlternativeText);
            set.add(selectedText(this.lb));
            if (set.size() == 1) {
                return set.iterator().next();
            }
            return StringUtil.join(" | ", new ArrayList<>(set)); // $NON-NLS-0$
        }
    }

    /**
     * A special option to specify a sort field and whether sorting is ascending or descending
     */
    static class OrderByOption extends ListBoxOption {
        protected final CheckBox desc = new CheckBox(I18n.I.descending());

        protected String defaultSortField = null;

        protected boolean defaultSortDescending = false;

        protected boolean turnedOff = false;

        OrderByOption(String field, String label, List<Item> items, String defaultKey) {
            super(field, label, items, defaultKey);
            setAlwaysEnabled();
        }

        public Widget getInnerWidget() {
            final Grid result = createInner(2);
            result.setWidget(0, 0, this.lb);
            result.setWidget(0, 1, this.desc);
            return result;
        }

        void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            this.desc.setEnabled(enabled);
        }

        void setTurnedOff(boolean turnedOff) {
            this.turnedOff = turnedOff;
        }

        public void setDefaultSortField(String defaultSortField) {
            this.defaultSortField = defaultSortField;
            selectItem(this.lb, this.defaultSortField);
        }

        public void setDefaultSortDescending(boolean defaultSortDescending) {
            this.defaultSortDescending = defaultSortDescending;
        }

        public void reset() {
            super.reset();
            this.desc.setValue(defaultSortDescending);
            selectItem(this.lb, this.defaultSortField);
        }

        public void apply(FinderFormConfig config) {
            super.apply(config);
            this.desc.setValue("true".equals(config.get(this.id + "-desc"))); // $NON-NLS-0$ $NON-NLS-1$
        }

        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            if (this.desc.getValue()) {
                config.put(this.id + "-desc", "true"); // $NON-NLS-0$ $NON-NLS-1$
            }
        }

        protected String doGetQuery() {
            return null;
        }

        protected void doAddExplanation(FlowPanel panel) {
            if (this.turnedOff) {
                return;
            }
            final String s = this.label + eqOp(false) + getSelectedItemText();
            addExplanation(panel, this.desc.getValue() ? s + desc.getText() : s);
        }

        protected String getOrderBy() {
            if (this.turnedOff) {
                return null;
            }
            return selectedValue(this.lb);
        }

        protected boolean isDescending() {
            return this.desc.getValue();
        }

        protected void setDescending(boolean checked) {
            this.desc.setValue(checked);
        }

        protected void setItem(String key) {
            selectItem(this.lb, key);
        }
    }

    /**
     * An option that allows to specify an associated instrument
     */
    static class SymbolOption extends AbstractOption implements ConfigurableSnippet {
        enum SymbolField {
            IID_WITHOUT_SUFFIX, QID
        }

        private final Button select = Button.text(I18n.I.selection())
                .clickHandler(event -> selectSymbol())
                .forceLegacyBorders()
                .build();

        private final Text name = new Text();

        private SymbolField symbolField;

        private String symbol = null;

        private final String[] filterTypes;

        private final boolean showMarketsPage;

        private final String filterForUnderlyingsForType;

        private Boolean filterForUnderlyingsOfLeveragProducts;

        SymbolOption(String field, String label, String[] filterTypes, boolean showMarketsPage) {
            this(field, label, filterTypes, IID_WITHOUT_SUFFIX, showMarketsPage, null, null);
        }

        SymbolOption(String field, String label, String[] filterTypes, SymbolField symbolField,
                boolean showMarketsPage) {
            this(field, label, filterTypes, symbolField, showMarketsPage, null, null);
        }

        SymbolOption(String field, String label, String[] filterTypes, SymbolField symbolField,
                boolean showMarketsPage,
                String filterForUnderlyingsForType, Boolean filterForUnderlyingsOfLeveragProducts) {
            super(field, label);
            this.name.addStyleName("mm-finder-instrumentName"); // $NON-NLS-0$
            this.filterTypes = filterTypes;
            this.symbolField = symbolField;
            this.showMarketsPage = showMarketsPage;
            this.filterForUnderlyingsForType = filterForUnderlyingsForType;
            this.filterForUnderlyingsOfLeveragProducts = filterForUnderlyingsOfLeveragProducts;
        }

        @SuppressWarnings("unused")
        public SymbolOption withSymbolField(SymbolField symbolField) {
            this.symbolField = symbolField;
            return this;
        }

        public Text getName() {
            return this.name;
        }

        public HashMap<String, String> getCopyOfParameters() {
            return new HashMap<>();
        }

        public void setParameters(HashMap<String, String> params) {
            final QuoteWithInstrument qwi = QuoteWithInstrument.getLastSelected();
            if (qwi != null) {
                setParameters(qwi);
            }
        }

        public void setParameters(QuoteWithInstrument item) {
            final String symbol;
            switch (this.symbolField) {
                case IID_WITHOUT_SUFFIX:
                    symbol = item.getIid(false);
                    break;
                case QID:
                    symbol = item.getId();
                    break;
                default:
                    symbol = null;
                    break;
            }
            setParameters(symbol, item.getName());
        }

        private void setParameters(String symbol, String title) {
            this.symbol = symbol;
            this.name.setText(title != null && this.symbol != null ? title : ""); // $NON-NLS-0$
            ValueChangeEvent.fire(this, this.symbol);
        }

        void setEnabled(boolean enabled) {
            this.select.setEnabled(enabled);
            this.name.setEnabled(enabled);
        }

        private void selectSymbol() {
            final SnippetConfigurationView configView = new SnippetConfigurationView(this);
            configView.addSelectSymbol(this.filterTypes, this.filterForUnderlyingsForType, this.filterForUnderlyingsOfLeveragProducts);
            configView.show(this.showMarketsPage);
        }

        public Widget getInnerWidget() {
            final Grid result = createInner(2);
            result.getColumnFormatter().setWidth(0, "90%"); // $NON-NLS-0$
            result.setWidget(0, 0, this.name);
            result.setWidget(0, 1, this.select);
            return result;
        }

        public void reset() {
            super.reset();
            this.name.setText(""); // $NON-NLS-0$
            this.symbol = null;
        }

        public void apply(FinderFormConfig config) {
            super.apply(config);
            this.symbol = config.get(this.id + "-symbol"); // $NON-NLS-0$
            final boolean enabled = this.symbol != null;
            this.name.setText(enabled ? config.get(this.id + "-name") : ""); // $NON-NLS-0$ $NON-NLS-1$
        }

        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            if (this.symbol != null) {
                config.put(this.id + "-symbol", this.symbol); // $NON-NLS-0$
            }
            config.put(this.id + "-name", this.name.getText()); // $NON-NLS-0$
        }

        protected String doGetQuery() {
            if (this.symbol == null) {
                return null;
            }
            return this.field + "==" + quote(this.symbol); // $NON-NLS-0$
        }

        protected void doAddExplanation(FlowPanel panel) {
            if (this.symbol != null) {
                addExplanation(panel, this.label + " = " + this.name.getText()); // $NON-NLS-0$
            }
        }
    }

    /**
     * An option that allows to select a range of values between two specified values, both of
     * which are entered by the user; the period field to which these values apply has to be selected
     * from a ListBox
     */
    static class PeriodFromToTextOption extends ListFromToTextOption {
        PeriodFromToTextOption(String id, String field, String label, String textFieldSuffix,
                List<Item> items, int selectedIndex) {
            super(id, field, label, items, selectedIndex, textFieldSuffix);
        }

        PeriodFromToTextOption(String id, String field, String label, String textFieldSuffix,
                List<Item> items) {
            super(id, field, label, items, textFieldSuffix);
        }

        protected String getFieldname() {
            return this.field + selectedValue(this.lb);
        }

        protected String getFieldLabel() {
            return this.label;
        }
    }

    /**
     * An option that allows to select a range of values between two specified values, both of
     * which are entered by the user; the field to which these values apply has to be selected
     * from a ListBox
     */
    static class ListFromToTextOption extends FromToTextOption {
        protected final ListBox lb;

        protected final List<Item> items;

        ListFromToTextOption(String id, String field, String label, List<Item> items,
                int selectedIndex, String textFieldSuffix) {
            super(id, field, label, textFieldSuffix);
            this.items = items;
            this.lb = Item.asListBox(items);
            if (selectedIndex >= 0 && items.size() > selectedIndex) {
                this.lb.setSelectedIndex(selectedIndex);
            }
        }

        ListFromToTextOption(String id, String field, String label, List<Item> items,
                String textFieldSuffix) {
            this(id, field, label, items, -1, textFieldSuffix);
        }

        void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            this.lb.setEnabled(enabled);
        }

        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            config.put(getLbKey(), selectedValue(this.lb));
        }

        public void apply(FinderFormConfig config) {
            super.apply(config);
            selectItem(this.lb, config.get(getLbKey()));
        }

        public void reset() {
            super.reset();
            this.lb.setSelectedIndex(0);
        }

        protected Grid createInnerGrid() {
            final Grid result = createInner(3);
            result.setWidget(0, 0, this.lb);
            addItems(result, 1);
            return result;
        }

        protected String getFieldname() {
            return selectedValue(this.lb);
        }

        protected String getFieldLabel() {
            return selectedText(this.lb);
        }

        protected String getLbKey() {
            return this.id + "-lb"; // $NON-NLS-0$
        }
    }

    static class StartEndOption extends ListFromToTextOption {
        private static int nextGroupId = 0;

        private int groupId = ++nextGroupId;

        protected RadioButton[] buttons = new RadioButton[2];

        protected DateBox[] dateBox = new DateBox[2];

        private DateTimeUtil.PeriodMode mode;

        protected MmJsDate defaultFrom = null;

        protected MmJsDate defaultTo = null;

        StartEndOption(String id, String field, String label, String textFieldSuffix,
                List<Item> items) {
            this(id, field, label, textFieldSuffix, items, DateTimeUtil.PeriodMode.PAST);
        }

        StartEndOption(String id, String field, String label, String textFieldSuffix,
                List<Item> items,
                DateTimeUtil.PeriodMode mode) {
            super(id, field, label, items, textFieldSuffix);
            setWidth("80px"); // $NON-NLS-0$
            this.mode = mode;
        }

        protected RadioButton createRadioButton(String label) {
            return new RadioButton(this.id + "_" + this.groupId, label); // $NON-NLS-0$
        }

        protected Grid addWidgets() {
            final Grid result = createInner(5);
            result.setWidget(0, 0, this.buttons[0]);
            result.setWidget(0, 1, this.lb);
            result.setWidget(0, 2, this.buttons[1]);
            result.setWidget(0, 3, this.dateBox[0]);
            result.setWidget(0, 4, this.dateBox[1]);
            return result;
        }

        protected Grid createInnerGrid() {
            this.buttons[0] = createRadioButton(" " + I18n.I.selection());  // $NON-NLS$
            this.buttons[0].addClickHandler(event -> setEnabled(true));
            this.buttons[0].setValue(true);
            this.buttons[1] = createRadioButton(" " + I18n.I.from()); // $NON-NLS$
            this.buttons[1].addClickHandler(event -> setEnabled(true));
            if (this.defaultFrom != null) {
                this.fromText.setText(JsDateFormatter.formatDdmmyyyy(this.defaultFrom));
            }
            if (this.defaultTo != null) {
                this.toText.setText(JsDateFormatter.formatDdmmyyyy(this.defaultTo));
            }
            this.dateBox[0] = new DateBox(null, parseDate(this.fromText), this.fromText);
            this.dateBox[1] = new DateBox(I18n.I.to(), parseDate(this.toText), this.toText);
            this.buttons[0].setEnabled(isEnabled());
            this.buttons[1].setEnabled(isEnabled());
            setTimeRange(new MmJsDate(), selectedValue(this.lb));
            this.lb.addChangeHandler(event -> setTimeRange(new MmJsDate(), selectedValue(lb)));
            this.dateBox[0].addValueChangeHandler(event -> {
                dateBox[1].setMinDate(event.getValue());
                dateBox[1].validate();
            });
            this.dateBox[1].addValueChangeHandler(event -> {
                dateBox[0].setMaxDate(event.getValue());
                dateBox[0].validate();
            });
            return addWidgets();
        }

        protected MmJsDate parseDate(TextBox textBox) {
            try {
                return JsDateFormatter.parseDdmmyyyy(textBox.getText());
            } catch (Exception e) {
                return null;
            }
        }

        private void setMinMax() {
            dateBox[1].setMinDate(dateBox[0].getDate());
            dateBox[0].setMaxDate(dateBox[1].getDate());
        }

        protected void setTimeRange(MmJsDate date, String interval) {
            final MmJsDate dateDiff = add(date, interval, this.mode);
            if (this.mode == DateTimeUtil.PeriodMode.PAST) {
                this.dateBox[0].setDate(dateDiff);
                this.dateBox[1].setDate(date);
            }
            else {
                this.dateBox[0].setDate(date);
                this.dateBox[1].setDate(dateDiff);
            }
            setMinMax();
        }

        public void setDefault(MmJsDate defaultFrom, MmJsDate defaultTo) {
            if (this.dateBox[0] == null) {
                this.defaultFrom = defaultFrom;
                this.defaultTo = defaultTo;
            }
            else {
                this.dateBox[0].setDate(defaultFrom);
                this.dateBox[1].setDate(defaultTo);
                setMinMax();
            }
        }

        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            if (this.buttons[1].getValue()) {
                config.put(this.id + "-checked", "1"); // $NON-NLS-0$ $NON-NLS-1$
            }
        }

        public void apply(FinderFormConfig c) {
            super.apply(c);
            RadioButton b = this.buttons["1".equals(c.get(this.id + "-checked")) ? 1 : 0]; // $NON-NLS-0$ $NON-NLS-1$
            b.setValue(true);
            if (c.get(this.id + "-from") == null && c.get(this.id + "-to") == null && c.get(getLbKey()) != null) { // $NON-NLS-0$ $NON-NLS-1$
                setTimeRange(new MmJsDate(), c.get(getLbKey()));
            }
        }

        public void reset() {
            super.reset();
            this.buttons[0].setValue(true);
            setTimeRange(new MmJsDate(), selectedValue(this.lb));
        }

        protected String getFieldname() {
            return this.field;
        }

        protected String getFieldLabel() {
            return this.label;
        }

        void setEnabled(boolean enabled) {
            if (buttons[0] == null) {
                super.setEnabled(enabled);
                return;
            }
            this.buttons[0].setEnabled(enabled);
            this.buttons[1].setEnabled(enabled);
            this.lb.setEnabled(enabled && this.buttons[0].getValue());
            final boolean fromToEnabled = enabled && this.buttons[1].getValue();
            this.dateBox[0].setEnabled(fromToEnabled);
            this.dateBox[1].setEnabled(fromToEnabled);
        }

        @Override
        protected String toQueryValue(String input, boolean query) {
            final MmJsDate date = JsDateFormatter.parseDdmmyyyy(input);
            if (date == null) {
                return input;
            }
            return query ? JsDateFormatter.formatIsoDay(date) : JsDateFormatter.formatDdmmyyyy(date);
        }

        protected String doGetQuery() {
            if (this.buttons[0].getValue()) {
                return query(true);
            }
            return super.doGetQuery();
        }

        protected void doAddExplanation(FlowPanel panel) {
            if (this.buttons[0].getValue()) {
                addExplanation(panel, query(false));
                return;
            }
            super.doAddExplanation(panel);
        }

        private String query(final boolean query) {
            final String pspec = selectedValue(this.lb);
            if (mode == DateTimeUtil.PeriodMode.PAST) {
                final String date = Formatter.formatDateAsISODay(DateTimeUtil.nowMinus(pspec));
                final String deDate = Formatter.LF.formatDate(DateTimeUtil.nowMinus(pspec));
                return key(query) + ">=" + quote(query ? date : deDate); // $NON-NLS-0$
            }
            else {
                final String today = Formatter.formatDateAsISODay(new Date());
                final String deToday = Formatter.LF.formatDate(new Date());
                final String date = Formatter.formatDateAsISODay(DateTimeUtil.nowPlus(pspec));
                final String deDate = Formatter.LF.formatDate(DateTimeUtil.nowPlus(pspec));
                if (query) {
                    return key(true) + ">=" + quote(today) + " && " + key(true) + "<" + quote(date); // $NON-NLS$
                }
                else {
                    return quote(deToday) + " <= " + key(false) + " < " + quote(deDate); // $NON-NLS$
                }
            }
        }
    }

    /**
     * An option that allows to select one of several values using RadioButtons; if there are many
     * values to choose from and only a few should be displayed as RadioButtons, it is also possible
     * the specify that one of the other items can be selected from a ListBox
     */
    public static class RadioOption extends AbstractOption implements ClickHandler {
        private static int nextGroupId = 0;

        private int groupId = ++nextGroupId;

        protected RadioButton[] buttons;

        protected List<Item> items;

        /**
         * these items are presented in the lb ListBox rather than as individual buttons
         */
        protected List<Item> otherItems;

        /**
         * allows to select one of the otherItems
         */
        protected ListBox lb;

        public RadioOption(String field, String label, List<Item> items) {
            this(field, field, label, items);
        }

        public RadioOption(String id, String field, String label, List<Item> items) {
            this(id, field, label, items, null);
        }

        protected RadioOption(String id, String field, String label, List<Item> items,
                List<Item> otherItems) {
            super(id, field, label);
            if (items != null) {
                initialize(items, otherItems);
            }
        }

        void updateNames(List<Item> items) {
            for (int i = 0; i < items.size(); i++) {
                this.buttons[i].setHTML(items.get(i).item);
            }
        }

        public void onClick(ClickEvent event) {
            ValueChangeEvent.fire(this, getSelectedValue());
        }

        private RadioButton createRadioButton(String label) {
            return new RadioButton(this.id + "_" + this.groupId, label); // $NON-NLS-0$
        }

        private void initialize(List<Item> items, List<Item> otherItems) {
            this.items = items;
            this.otherItems = otherItems;
            this.buttons = new RadioButton[items.size() + (otherItems != null ? 1 : 0)];
            for (int i = 0; i < items.size(); i++) {
                final Item item = items.get(i);
                this.buttons[i] = createRadioButton(item.item);
                this.buttons[i].setValue(i == 0);
                this.buttons[i].addClickHandler(this);
            }
            if (otherItems != null) {
                this.buttons[items.size()] = createRadioButton(I18n.I.selection());
                this.lb = Item.asListBox(otherItems);
            }
        }

        void setEnabled(boolean enabled) {
            if (this.buttons != null) {
                for (RadioButton button : buttons) {
                    button.setEnabled(enabled);
                }
            }
        }

        public void initialize(Map<String, FinderMetaList> map) {
            if (this.items == null) {
                initialize(toItems(map.get(this.field)), null);
            }
        }

        public void addConfigTo(FinderFormConfig config) {
            if (!isToBeIgnored()) {
                super.addConfigTo(config);
                config.put(getRadioKey(), getValue(getSelected()));
            }
        }

        private String getValue(int i) {
            //A value lesser than 0 (e.g. -1) is used by some other methods to indicate that nothing was selected.
            //This must be handled here in order to get the finder work correctly, in case any value lists returned by
            //the block are empty instead of xsd:nil!
            if (i < 0) {
                return null;
            }

            if (i < this.items.size()) {
                return this.items.get(i).value;
            }
            // must be one of otherItems
            return this.otherItems.get(this.lb.getSelectedIndex()).value;
        }

        private String getItem(int i) {
            if (i < this.items.size()) {
                return this.items.get(i).item;
            }
            // must be one of otherItems
            return this.otherItems.get(this.lb.getSelectedIndex()).item;
        }

        public Widget getInnerWidget() {
            final Grid result = createInner(this.buttons.length + (this.lb != null ? 1 : 0));
            for (int i = 0; i < buttons.length; i++) {
                result.setWidget(0, i, buttons[i]);
            }
            if (this.lb != null) {
                result.setWidget(0, this.buttons.length, lb);
            }
            return result;
        }

        public void apply(FinderFormConfig config) {
            super.apply(config);
            final String radioKey = getRadioKey();
            final String checked = config.get(radioKey);
            final int n1 = getSelectedValueIndex(this.items, checked);
            if (n1 != -1) {
                setSelected(n1);
//                DebugUtil.logToFirebugConsole("RadioOption.apply(" + radioKey + "=" + checked + ")  n1: "
// + n1 + "  items: " + toString(this.items) + "    otherItems: " + toString(this.otherItems));
                return;
            }
            final int n2 = getSelectedValueIndex(this.otherItems, checked);
            if (n2 != -1) {
                setSelected(this.buttons.length - 1);
                this.lb.setSelectedIndex(n2);
                return;
            }
            // not found, reset to use first
            setSelected(0);
        }

        private int getSelectedValueIndex(List<Item> aList, String value) {
            if (aList == null) {
                return -1;
            }
            for (int i = 0; i < aList.size(); i++) {
                if (aList.get(i).value.equals(value)) {
                    return i;
                }
            }
            return -1;
        }

        public void reset() {
            super.reset();
            updateNames(this.items);
            setSelected(0);
            if (this.lb != null) {
                this.lb.setSelectedIndex(0);
            }
        }

        public int getSelected() {
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].getValue()) {
                    return i;
                }
            }
            assert false;
            return -1;
        }

        public String getSelectedValue() {
            return getValue(getSelected());
        }

        public void setSelected(int i) {
            for (int j = 0; j < this.buttons.length; j++) {
                this.buttons[j].setValue(i == j);
            }
            ValueChangeEvent.fire(this, getSelectedValue());
        }

        protected String doGetQuery() {
            if (isToBeIgnored()) {
                return null;
            }
            return this.field + "==" + quote(getSelectedValue()); // $NON-NLS-0$
        }

        protected void doAddExplanation(FlowPanel panel) {
            if (isToBeIgnored()) {
                return;
            }
            addExplanation(panel, this.label + eqOp(false) + getItem(getSelected()));
        }

        private boolean isToBeIgnored() {
            return IGNORE_VALUE.equals(getSelectedValue());
        }

        private String getRadioKey() {
            return this.id + "-checked"; // $NON-NLS-0$
        }
    }

    /**
     * Abstract base class for options that allow to select a set of several values using CheckBoxes.
     * Subclasses have to create the CheckBoxes and to create the widget(s) that show them.
     */
    private abstract static class AbstractCheckBoxOption extends AbstractOption implements
            ClickHandler {
        protected CheckBox[] boxes;

        protected AbstractCheckBoxOption(String id, String field, String label) {
            super(id, field, label);
        }

        protected CheckBox createCheckBox(int i, String label, String name) {
            this.boxes[i] = new CheckBox(label);
            this.boxes[i].setName(name);
            this.boxes[i].addClickHandler(this);
            return this.boxes[i];
        }

        public void onClick(ClickEvent event) {
            // empty
        }

        protected boolean isAnyChecked() {
            for (CheckBox box : boxes) {
                if (box.getValue()) {
                    return true;
                }
            }
            return false;
        }

        void setEnabled(boolean enabled) {
            if (this.boxes != null) {
                for (CheckBox box : boxes) {
                    box.setEnabled(enabled);
                }
            }
        }

        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            for (int i = 0; i < this.boxes.length; i++) {
                if (this.boxes[i].getValue()) {
                    config.put(this.id + "-" + getValue(i), "true"); // $NON-NLS-0$ $NON-NLS-1$
                }
            }
        }

        private String getValue(int i) {
            return getNthItem(i).value;
        }

        private String getItem(int i) {
            return getNthItem(i).item;
        }

        protected abstract Item getNthItem(int i);

        public void apply(FinderFormConfig config) {
            super.apply(config);
            for (int i = 0; i < this.boxes.length; i++) {
                if ("true".equals(config.get(this.id + "-" + getValue(i)))) { // $NON-NLS-0$ $NON-NLS-1$
                    this.boxes[i].setValue(true);
                }
            }
        }

        public void reset() {
            super.reset();
            for (CheckBox box : this.boxes) {
                box.setValue(false);
            }
        }

        protected String doGetQuery() {
            return query(true);
        }

        protected String query(boolean query) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < this.boxes.length; i++) {
                if (this.boxes[i].getValue()) {
                    if (sb.length() > 0) {
                        sb.append(", "); // $NON-NLS-0$
                    }
                    sb.append(query ? quote(getValue(i)) : getItem(i));
                }
            }
            return sb.length() == 0 ? null : (key(query) + " IN (" + sb.toString() + ")"); // $NON-NLS-0$ $NON-NLS-1$
        }

        protected void doAddExplanation(FlowPanel panel) {
            addExplanation(panel, query(false));
        }
    }

    /**
     * An option that allows to select a set of several values using CheckBoxes
     */
    public static class CheckBoxOption extends AbstractCheckBoxOption {
        protected List<Item> items;

        private boolean equalColumns = false;

        public CheckBoxOption(String field, String label, List<Item> items) {
            this(field, field, label, items);
        }

        public CheckBoxOption(String id, String field, String label, List<Item> items) {
            super(id, field, label);
            if (items != null) {
                initialize(items);
            }
        }

        public CheckBoxOption withEqualColumns() {
            this.equalColumns = true;
            return this;
        }

        private void initialize(List<Item> items) {
            this.items = items;
            this.boxes = new CheckBox[items.size()];
            for (int i = 0; i < items.size(); i++) {
                createCheckBox(i, items.get(i).item, items.get(i).value);
            }
        }

        public void initialize(Map<String, FinderMetaList> map) {
            if (this.items == null) {
                initialize(toItems(map.get(this.field)));
            }
        }

        protected Item getNthItem(int i) {
            return this.items.get(i);
        }

        public Widget getInnerWidget() {
            final Grid result = createInner(this.boxes.length);
            for (int i = 0; i < this.boxes.length; i++) {
                result.setWidget(0, i, boxes[i]);
            }
            if (this.equalColumns) {
                final int columnWidth = 100 / this.boxes.length;
                final HTMLTable.ColumnFormatter formatter = result.getColumnFormatter();
                for (int i = 0; i < this.boxes.length; i++) {
                    formatter.setWidth(i, columnWidth + "%"); // $NON-NLS-0$
                }
            }
            return result;
        }
    }

    private static class CountingGrid extends Grid implements ValueChangeHandler<Boolean> {

        private int count = 0;

        private final int index;

        private final CountDisplayingStackPanel panel;

        CountingGrid(CountDisplayingStackPanel panel, int index, int rows, int columns) {
            super(rows, columns);
            this.index = index;
            this.panel = panel;
        }

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
            if (event.getValue()) {
                this.count++;
            }
            else {
                this.count--;
            }
            this.panel.updateCount(this.index, count);
        }

        void reset() {
            this.count = 0;
        }
    }

    private static class CountDisplayingStackPanel extends Composite {

        private final StackPanel stackPanel = new StackPanel();

        private final List<String> headings = new ArrayList<>();

        public CountDisplayingStackPanel() {
            initWidget(stackPanel);
        }

        private String renderText(String stackText, int count) {
            if (count == 0) {
                return stackText;
            }
            return stackText + " (" + I18n.I.selectedOptionsNumber(count) + ')'; // $NON-NLS$
        }

        void updateCount(int index, int count) {
            final String stackText = headings.get(index);
            if (stackText != null) {
                this.stackPanel.setStackText(index, renderText(stackText, count));
            }
        }

        void add(CountingGrid w, String stackText) {
            this.stackPanel.add(w, renderText(stackText, 0));
            this.headings.add(stackText);
        }

        void reset() {
            for (int i = 0; i < headings.size(); i++) {
                ((CountingGrid) this.stackPanel.getWidget(i)).reset();
                updateCount(i, 0);
            }
        }
    }

    /**
     * An option that allows to select a set of several values using CheckBoxes
     */
    static class CheckBoxGroupsOption extends AbstractCheckBoxOption {

        private Map<String, String> mapI18n;  // do we really need this any more?

        protected List<ItemGroup> groups;

        protected FinderSection fs;

        private final CountDisplayingStackPanel stackPanel = new CountDisplayingStackPanel();

        protected CheckBoxGroupsOption(String field, String label, List<ItemGroup> groups,
                Map<String, String> mapI18n) {
            this(field, field, label, groups, mapI18n);
        }

        protected CheckBoxGroupsOption(String id, String field, String label,
                List<ItemGroup> groups, Map<String, String> mapI18n) {
            super(id, field, label);
            this.mapI18n = mapI18n;
            setAlwaysEnabled();
            if (groups != null) {
                initialize(groups);
            }
        }

        @Override
        public void addClickHandler(ClickHandler ch) {
            // bloody hack, but we need to access section directly
            this.fs = (FinderSection) ch;
        }

        @Override
        public void onClick(ClickEvent event) {
            onModify();
        }

        @Override
        public void apply(FinderFormConfig config) {
            super.apply(config);
            onModify();
        }

        @Override
        public void reset() {
            super.reset();
            onModify();
            this.stackPanel.reset();
        }

        private void onModify() {
            final boolean any = isAnyChecked();
            this.cb.setValue(any);
            this.fs.setValue(any);
        }

        @Override
        void setEnabled(boolean enabled) {
            // empty, always enabled
        }

        private int itemCount() {
            int n = 0;
            for (ItemGroup group : groups) {
                n += group.items.size();
            }
            return n;
        }

        private void initialize(List<ItemGroup> groups) {
            this.groups = groups;
            this.boxes = new CheckBox[itemCount()];
            int n = 0;
            for (ItemGroup group : groups) {
                for (Item item : group.items) {
                    createCheckBox(n++, item.item, item.value);
                }
            }
        }

        public void initialize(Map<String, FinderMetaList> map) {
            if (this.groups == null) {
                initialize(toGroup(map, this.field, this.mapI18n));
            }
        }

        protected Item getNthItem(int i) {
            int n = i;
            for (ItemGroup group : groups) {
                if (n >= group.items.size()) {
                    n -= group.items.size();
                    continue;
                }
                return group.items.get(n);
            }
            assert false;
            return null;
        }

        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this, null, true));
        }

        public Widget getInnerWidget() {
            final CountDisplayingStackPanel stack = this.stackPanel;
            int offset = 0;
            for (int index = 0; index < this.groups.size(); index++) {
                final ItemGroup group = this.groups.get(index);
                stack.add(createGrid(index, offset, group.items.size()), group.name);
                offset += group.items.size();
            }
            return stack;
        }

        private CountingGrid createGrid(int index, int offset, final int length) {
            final int cols = FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() || FeatureFlags.Feature.DZ_RELEASE_2016.isEnabled() ? 1 : 3;
            final int rows = (length + cols - 1) / cols;
            final CountingGrid g = new CountingGrid(this.stackPanel, index, rows, cols);
            g.setStyleName("mm-formElement"); // $NON-NLS-0$
            for (int col = 0; col < cols; col++) {
                for (int row = 0; row < rows; row++) {
                    final int i = row * cols + col;
                    if (i < length) {
                        final CheckBox cb = this.boxes[i + offset];
                        cb.addValueChangeHandler(g);
                        g.setWidget(row, col, cb);
                    }
                }
            }
            return g;
        }
    }

    static class LiveCheckBoxGroupsOption extends CheckBoxGroupsOption {
        private final SearchHandler searchHandler;

        protected LiveCheckBoxGroupsOption(String field, String label, List<ItemGroup> groups,
                Map<String,
                        String> mapI18n, SearchHandler searchHandler) {
            this(field, field, label, groups, mapI18n, searchHandler);
        }

        protected LiveCheckBoxGroupsOption(String id, String field, String label,
                List<ItemGroup> groups, Map<String,
                String> mapI18n, SearchHandler searchHandler) {
            super(id, field, label, groups, mapI18n);
            this.searchHandler = searchHandler;
        }

        @Override
        public void onClick(ClickEvent event) {
            super.onClick(event);
            this.searchHandler.getClickHandler().onClick(event);
        }
    }

    private static String textForValue(final ListBox lb, String value) {
        final int i = indexForValue(lb, value);
        return (i != -1) ? lb.getItemText(i) : null;
    }

    private static int indexForValue(final ListBox lb, String value) {
        for (int i = 0; i < lb.getItemCount(); i++) {
            if (value.equals(lb.getValue(i))) {
                return i;
            }
        }
        return -1;
    }

    private static String selectedText(final ListBox lb) {
        final int selectedIndex = lb.getSelectedIndex();
        if (selectedIndex < 0) {
            return null;
        }
        return lb.getItemText(lb.getSelectedIndex());
    }

    public static String selectedValue(final ListBox lb) {
        if (lb.getItemCount() == 0) {
            return null;
        }
        return lb.getValue(lb.getSelectedIndex());
    }

    public static String selectedValue(final SuggestBox sb) {
        final String value = sb.getValue();
        return StringUtil.hasText(value) ? value : null;
    }

    private static void selectItem(final ListBox lb, String p) {
        if (p == null) {
            lb.setSelectedIndex(0);
        }
        else {
            lb.setSelectedIndex(Math.max(0, indexForValue(lb, p)));
        }
    }

    private static List<ItemGroup> toGroup(Map<String, FinderMetaList> map, String key,
            Map<String, String> mapI18n) {
        List<ItemGroup> result = new ArrayList<>();
        int n = 1;
        while (true) {
            // see AbstractFinder#addMetaLists for how keys are constructed
            final FinderMetaList metaList = map.get(key + "-" + n); // $NON-NLS-0$
            if (metaList == null || !(metaList instanceof FinderTypedMetaList)) {
                break;
            }
            final FinderTypedMetaList typedMetaList = (FinderTypedMetaList) metaList;
            String name = mapI18n.get(typedMetaList.getName());
            if (name == null) {
                name = typedMetaList.getName();
            }
            result.add(new ItemGroup(name, toItems(typedMetaList.getElement(), mapI18n)));
            n++;
        }
        return result;
    }

    static String quoteMultiple(Set<String> s) {
        if (s.size() == 1) {
            return quote(s.iterator().next());
        }
        final String joined = StringUtil.join("@", new ArrayList<>(s)); // $NON-NLS-0$
        return quote(joined);
    }


    private static void fillListBox(ListBox lb, FinderMetaList list, String selectedKey) {
        if (list == null || list.getElement() == null || list.getElement().isEmpty()) {
            return;
        }
        for (FinderMetaList.Element element : list.getElement()) {
            addElementToLb(lb, selectedKey, element);
        }
    }

    private static void addElementToLb(ListBox lb, String selectedKey,
            FinderMetaList.Element element) {
        if (element != null && StringUtil.hasText(element.getName())) {
            final String itemName = StringUtil.hasText(element.getCount())
                    ? (element.getName() + " (" + element.getCount() + ")")
                    : element.getName();
            lb.addItem(itemName, element.getKey());
            if (selectedKey != null && selectedKey.equals(element.getKey())) {
                lb.setSelectedIndex(lb.getItemCount() - 1);
            }
        }
    }

    private static void fillSuggestBox(SuggestBox sb, MultiWordSuggestOracle so,
            FinderMetaList list, Collection<String> itemValues, String selectedKey) {
        if (list == null || list.getElement() == null || list.getElement().isEmpty()) {
            return;
        }
        final Map<String, FinderMetaList.Element> sortedElements = new HashMap<>();
        final List<FinderMetaList.Element> elements = list.getElement();
        for (FinderMetaList.Element element : elements) {
            sortedElements.put(element.getKey(), element);
        }
        for (String value : itemValues) {
            addElementToSo(so, sortedElements.get(value));
        }

        sb.setValue(selectedKey == null ? "" : selectedKey);
    }

    private static void addElementToSo(MultiWordSuggestOracle so, FinderMetaList.Element element) {
        if (element != null && StringUtil.hasText(element.getName())) {
            so.add(element.getKey());
        }
    }

    private static void fillListBox(ListBox lb, FinderMetaList list, List<Item> reference,
            String selectedKey) {
        if (list == null || list.getElement() == null || list.getElement().isEmpty()) {
            return;
        }
        final Map<String, FinderMetaList.Element> sortedElements = new HashMap<>();
        final List<FinderMetaList.Element> elements = list.getElement();
        for (FinderMetaList.Element element : elements) {
            sortedElements.put(element.getKey(), element);
        }
        for (Item item : reference) {
            addElementToLb(lb, selectedKey, sortedElements.get(item.getValue()));
        }
    }

    private static ListBox createListBox(FinderMetaList list, String selectedKey) {
        final ListBox result = new ListBox();
        if (list == null) {
            return result;
        }
        fillListBox(result, list, selectedKey);
        return result;
    }

    static List<Item> toItems(FinderMetaList list) {
        if (list != null) {
            return toItems(list.getElement(), null);
        }
        return new ArrayList<>();
    }

    static List<Item> toItems(List<FinderMetaList.Element> list, Map<String, String> mapI18n) {
        final List<Item> result = new ArrayList<>();
        if (list != null) {
            for (FinderMetaList.Element element : list) {
                String name = null;
                if (mapI18n != null) {
                    name = mapI18n.get(element.getKey());
                }
                if (name == null) {
                    name = element.getName();
                }
                if (StringUtil.hasText(name)) {
                    result.add(new Item(name, element.getKey()));
                }
            }
        }
        return result;
    }

    public static String quote(String s) {
        if (s.indexOf(SINGLE_QUOTE) == -1) {
            return singleQuote(s);
        }
        return singleQuote(StringUtil.replace(s, "'", "''")); // $NON-NLS$
    }

    private static String singleQuote(String s) {
        return SINGLE_QUOTE + s + SINGLE_QUOTE;
    }

    static FromToTextOption createEdgFromToTextOption(List<Item> sortFields,
            String field, String label, String textFieldSuffix) {
        return createEdgFromToTextOption(sortFields, field, label, textFieldSuffix, null);
    }

    static FromToTextOption createEdgFromToTextOption(List<Item> sortFields,
            String field, String label, String textFieldSuffix,
            SearchHandler searchHandler) {
        final FromToTextOption result;
        result = searchHandler != null ? new LiveFromToTextOption(field, field, label, textFieldSuffix, searchHandler) :
                new FromToTextOption(field, label, textFieldSuffix);
        sortFields.add(new Item(label, field));
        result.setDefault("1", "5"); // $NON-NLS-0$ $NON-NLS-1$
        return result;
    }


    /// Live Finder Elements ///////////////////////////////////////////////////////////////////////////////

    static class LiveListBoxOption extends ListBoxOption implements MutableMetadata {

        private final List<Item> initialItems;

        private SearchHandler searchHandler;

        LiveListBoxOption(String id, String field, String label, List<Item> items,
                String defaultKey, SearchHandler searchHandler) {
            super(id, field, label, items, defaultKey);
            this.initialItems = items;
            setSearchHandler(searchHandler);
            addClickHandler(searchHandler.getClickHandler());
        }

        public void setSearchHandler(SearchHandler searchHandler) {
            if (searchHandler instanceof LiveFinder.DynamicSearchHandler) {
                ((LiveFinder.DynamicSearchHandler) searchHandler).withElement(this);
            }
            this.searchHandler = searchHandler;
        }

        @Override
        public void onChange(ChangeEvent event) {
            super.onChange(event);
            FinderFormUtils.checkForActivation(this, event);
            this.searchHandler.getChangeHandler().onChange(event);
        }

        @Override
        void setEnabled(boolean enabled) {
            // do nothing
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }

        @Override
        public void initialize(Map<String, FinderMetaList> map) {
            super.initialize(map);
            this.lb.setWidth("160px"); // $NON-NLS$
        }

        @Override
        public void reset() {
            this.cb.setValue(this.alwaysEnabled);
            checkActivation();
            if (this.initialItems != null) {
                fillListBox(this.lb, this.initialItems);
            }
            selectItem(this.lb, this.defaultKey);
        }

        public String getSelectedValue() {
            if (this.lb == null) {
                return "";
            }
            final String selectedItemText = getSelectedItemText();
            for (int i = 0; i < this.lb.getItemCount(); i++) {
                if (this.lb.getItemText(i).equals(selectedItemText)) {
                    return this.lb.getValue(i);
                }
            }
            return "";
        }
    }

    @SuppressWarnings({"unused", "Duplicates"})
    static class LiveListFromToTextOption extends ListFromToTextOption implements
            CloneableFinderFormElement<LiveListFromToTextOption> {
        private final SearchHandler searchHandler;

        LiveListFromToTextOption(String id, String field, String label, List<Item> items,
                String textFieldSuffix, SearchHandler searchHandler) {
            super(id, field, label, items, textFieldSuffix);
            setWidth("45px"); // $NON-NLS$
            addClickHandler(searchHandler.getClickHandler());
            this.searchHandler = searchHandler;
        }

        public LiveListFromToTextOption cloneElement(String newId) {
            return new LiveListFromToTextOption(newId, this.field, this.label, this.items, this.textFieldSuffix,
                    this.searchHandler);
        }

        public void fireCloneEvent() {
            EventBusRegistry.get().fireEvent(FinderFormElementEvent.newClone(this.id));
        }

        public void fireDeletedEvent() {
            EventBusRegistry.get().fireEvent(FinderFormElementEvent.cloneDeleted(this.id));
        }

        public boolean isClone() {
            return this.id.contains("-");
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }

        @Override
        public FromToTextOption initialize() {
            final FromToTextOption result = super.initialize();
            this.fromText.addChangeHandler(this);
            this.toText.addChangeHandler(this);
            this.fromText.setEnabled(true);
            this.toText.setEnabled(true);
            return result;
        }

        public void onChange(ChangeEvent event) {
            FinderFormUtils.checkForActivation(this, event);
            this.searchHandler.getChangeHandler().onChange(event);
        }

        @Override
        void setEnabled(boolean enabled) {
            // always enabled
        }

        @Override
        protected Grid createInnerGrid() {
            final Grid result = createInner(3, 3);
            result.setWidget(0, 0, this.lb);
            addItems(result, 1, 0);
            return result;
        }
    }

    static class LivePeriodFromToTextOption extends PeriodFromToTextOption implements
            CloneableFinderFormElement<LivePeriodFromToTextOption> {
        private final SearchHandler searchHandler;

        LivePeriodFromToTextOption(String id, String field, String label, String textFieldSuffix,
                List<Item> items, SearchHandler searchHandler) {
            super(id, field, label, textFieldSuffix, items);
            setWidth("45px"); // $NON-NLS$
            this.searchHandler = searchHandler;
            addClickHandler(this.searchHandler.getClickHandler());
        }

        public void onChange(ChangeEvent event) {
            FinderFormUtils.checkForActivation(this, event);
            this.searchHandler.getChangeHandler().onChange(event);
        }

        @Override
        void setEnabled(boolean enabled) {
            // always enabled
        }


        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }

        @Override
        protected Grid createInnerGrid() {
            final Grid result = createInner(2, 3);
            result.setWidget(0, 0, this.lb);
            addItems(result, 0);
            return result;
        }

        @Override
        protected void addItems(Grid result, int col) {
            result.setWidget(1, col++, createTextBox(" " + I18n.I.from() + " ", this.fromText, this.textFieldSuffix)); // $NON-NLS$
            result.setWidget(1, col, createTextBox(" " + I18n.I.to() + " ", this.toText, this.textFieldSuffix)); // $NON-NLS$
        }


        @Override
        public FromToTextOption initialize() {
            final FromToTextOption result = super.initialize();
            this.fromText.addChangeHandler(this);
            this.toText.addChangeHandler(this);
            this.lb.addChangeHandler(this.searchHandler.getChangeHandler());
            this.fromText.setEnabled(true);
            this.toText.setEnabled(true);
            this.lb.setEnabled(true);
            return result;
        }

        public LivePeriodFromToTextOption cloneElement(String newId) {
            return new LivePeriodFromToTextOption(newId, this.field, this.label, this.textFieldSuffix, this.items,
                    this.searchHandler);
        }

        public void fireCloneEvent() {
            EventBusRegistry.get().fireEvent(FinderFormElementEvent.newClone(this.id));
        }

        public void fireDeletedEvent() {
            EventBusRegistry.get().fireEvent(FinderFormElementEvent.cloneDeleted(this.id));
        }

        public boolean isClone() {
            return this.id.contains("-");
        }

    }

    static class LiveOrderByOption extends OrderByOption {
        private final SearchHandler searchHandler;

        LiveOrderByOption(String field, String label, List<Item> items, String defaultKey,
                SearchHandler searchHandler) {
            super(field, label, items, defaultKey);
            this.searchHandler = searchHandler;
            addClickHandler(this.searchHandler.getClickHandler());
            addValueChangeHandler(this.searchHandler.getStrValueChangHandler());
        }

        @Override
        public void initialize(Map<String, FinderMetaList> map) {
            super.initialize(map);
            this.desc.addClickHandler(this.searchHandler.getClickHandler());
            this.lb.addChangeHandler(this.searchHandler.getChangeHandler());
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }

        @Override
        public Widget getInnerWidget() {
            final Grid result = createInner(2, 1);
            result.setWidget(0, 0, this.lb);
            result.setWidget(1, 0, this.desc);
            return result;
        }
    }


    @SuppressWarnings("Duplicates")
    static class LiveMultiEnumOption extends CheckBoxOption implements MutableMetadata,
            DynamicValueElement {
        private static final int MAX_TOPITEM_COUNT = 3;

        private static final String ZERO_METADATA = "0"; // $NON-NLS$

        private final int fixBoxesCount;

        private SearchHandler searchHandler;

        private final FinderMetaList metaList;

        private final String metaName;

        private Widget toolBtn = null;

        private final String liveFinderId;

        protected ListBox lb;

        protected CheckBox lbCb;

        private boolean metadataUpdateAllowed = true;

        private final FlexTable subTable;

        private final String[] defaultConfig;

        private char divider = '|';

        private final String defaultKey;

        private final Map<String, String> keyAndOrigText = new LinkedHashMap<>();

        protected final boolean isEnum;

        protected boolean useExactMatchPrefix = true;

        private final List<Widget> alterBoxes = new ArrayList<>();

        public static LiveMultiEnumOption create(String liveFinderId, String elementId,
                String field, String label, String defaultKey,
                FinderMetaList metaList, String metaName, String[] defaultConfig,
                SearchHandler searchHandler) {
            return new LiveMultiEnumOption(liveFinderId, elementId, field, label, getItems(liveFinderId, elementId, metaList, defaultConfig),
                    getCheckBoxCount(liveFinderId, elementId, defaultConfig), defaultKey, defaultConfig, metaList, metaName, searchHandler);
        }

        public static LiveMultiEnumOption create(String liveFinderId, String elementId,
                String field, String label, List<Item> items,
                String defaultKey, String[] defaultConfig, SearchHandler searchHandler) {
            return new LiveMultiEnumOption(liveFinderId, elementId, field, label, getItems(liveFinderId, elementId, items, defaultConfig),
                    getCheckBoxCount(liveFinderId, elementId, defaultConfig), defaultKey, defaultConfig, null, null, searchHandler);
        }

        protected LiveMultiEnumOption(String liveFinderId, String elementId, String field,
                String label, List<Item> items,
                int checkBoxCount, String defaultKey, String[] defaultConfig,
                FinderMetaList metaList,
                String metaName, final SearchHandler searchHandler) {
            super(elementId, field, label, items);
            this.defaultConfig = defaultConfig;
            this.metaList = metaList;
            this.metaName = metaName;
            setSearchHandler(searchHandler);
            this.liveFinderId = liveFinderId;
            this.defaultKey = defaultKey;
            addClickHandler(this);
            this.fixBoxesCount = checkBoxCount;
            if (items != null) {
                for (Item item : items) {
                    this.keyAndOrigText.put(item.getValue(), item.getItem());
                }
                this.lb = Item.asListBox(removeFirstNItems(items, this.fixBoxesCount), defaultKey);
                this.lb.addChangeHandler(this);
                this.lbCb = new CheckBox();
                this.lbCb.addClickHandler(this);
                this.lb.setWidth("170px"); // $NON-NLS$
            }

            this.subTable = new FlexTable();
            if (this.lb != null) {
                addListAndCheckBox(this.lb, this.lbCb);
            }
            if (this.metaList == null || this.metaName == null) {
                withoutMetadataUpdate();
                this.isEnum = false;
            }
            else {
                this.isEnum = this.metaList.isEnum();
            }
        }

        public void onChange(ChangeEvent event) {
            if (!getValue() || !this.lbCb.getValue()) {
                setValue(true);
                this.lbCb.setValue(true);
                fireEvent(event);
            }
            this.searchHandler.getChangeHandler().onChange(event);
        }

        @Override
        void setEnabled(boolean enabled) {
            // always enabled
        }

        @Override
        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);
            if (this.lbCb.getValue()) {
                config.put(this.id + "-item", this.lb.getValue(this.lb.getSelectedIndex())); // $NON-NLS$
            }
        }

        @Override
        public void apply(FinderFormConfig config) {
            super.apply(config);
            final String key = this.id + "-item"; // $NON-NLS$
            if (config.contains(key)) {
                enableItemBoxes(StringUtil.split(config.get(key), this.divider));
            }
            addSelectedBoxes();
        }

        private void enableItemBoxes(List<String> items) {
            if (items.size() > 1) {
                for (int i = 0; i < items.size() - 1; i++) {
                    enableCheckBox(items.get(i));
                }
            }
            if (items.size() > 0) {
                final String lastItem = items.get(items.size() - 1);
                if (indexForValue(lb, lastItem) == -1) {
                    enableCheckBox(lastItem);
                }
                else {
                    this.lbCb.setValue(true);
                    selectItem(this.lb, lastItem);
                }
            }
        }

        private void enableCheckBox(final String namePrefix) {
            for (CheckBox box : this.boxes) {
                if (box.getName().startsWith(namePrefix)) {
                    box.setValue(true);
                }
            }
        }

        private void addSelectedBoxes() {
            for (CheckBox box : boxes) {
                if (box.getValue()) {
                    addAlternative(box.getName(), true, null);
                }
            }
        }

        public LiveMultiEnumOption withDivider(char divider) {
            this.divider = divider;
            return this;
        }

        public LiveMultiEnumOption withoutMatchPrefix() {
            this.useExactMatchPrefix = false;
            return this;
        }

        public void updateMetadata(Map<String, FinderMetaList> map, boolean force) {
            if ((getValue() && !force) || !this.metadataUpdateAllowed || !map.containsKey(this.metaName)) {
                return;
            }
            final Map<String, FinderMetaList.Element> keyAndElements = new HashMap<>();
            final FinderMetaList finderMetaList = map.get(this.metaName);
            final List<FinderMetaList.Element> elements = finderMetaList.getElement();
            for (FinderMetaList.Element element : elements) {
                keyAndElements.put(element.getKey(), element);
            }
            boolean anyCheckBoxEnabled = false;
            for (CheckBox box : this.boxes) {
                String countOf = ZERO_METADATA;
                if (keyAndElements.containsKey(box.getName())) {
                    countOf = keyAndElements.get(box.getName()).getCount();
                }
                box.setText(this.keyAndOrigText.get(box.getName()) + (countOf != null ? " (" + countOf + ")" : ""));
                box.setEnabled(!ZERO_METADATA.equals(countOf));
                if (box.isEnabled()) {
                    anyCheckBoxEnabled = true;
                }
            }
            final String key = this.lbCb.getValue() ? selectedValue(this.lb) : this.defaultKey;
            this.lb.clear();
            fillListBox(this.lb, map.get(this.metaName), removeFirstNItems(this.items, this.fixBoxesCount), key);
            this.lbCb.setEnabled(this.lb.getItemCount() != 0);
            this.lb.setEnabled(this.lb.getItemCount() != 0);
            this.cb.setEnabled(anyCheckBoxEnabled || this.lb.getItemCount() != 0);
        }

        public LiveMultiEnumOption withoutMetadataUpdate() {
            this.metadataUpdateAllowed = false;
            return this;
        }

        protected String getValueStr(char divider) {
            final List<String> values = new ArrayList<>();
            for (CheckBox checkBox : this.boxes) {
                if (checkBox.getValue()) {
                    values.add("+" + checkBox.getName()); // $NON-NLS$
                }
            }
            if (this.lbCb.getValue()) {
                values.add("+" + selectedValue(this.lb)); // $NON-NLS$
            }
            return StringUtil.join(divider, values);
        }

        protected String getValueStr() {
            return getValueStr(this.divider);
        }

        @Override
        public void reset() {
            super.reset();
            for (Widget alterBox : this.alterBoxes) {
                final int rowId = DOMUtil.getRowId(alterBox.getElement());
                this.subTable.removeRow(rowId);
            }
            this.alterBoxes.clear();
            this.lb.clear();
            fillListBox(this.lb, removeFirstNItems(this.items, this.fixBoxesCount));
            this.lbCb.setValue(false);
        }

        private void addListAndCheckBox(final ListBox lb, final CheckBox cb) {
            final FlowPanel lbCb = new FlowPanel();
            lbCb.add(cb);
            lbCb.add(lb);
            this.subTable.setWidget(0, 0, lbCb);
            final IconImageIcon iconAdd = IconImage.getIcon("mm-plus") // $NON-NLS$
                    .withClickHandler(this::addAlternative);
            iconAdd.addStyleName("mm-middle mm-link");
            this.subTable.setWidget(0, 1, iconAdd);
        }

        private void addAlternative(ClickEvent event) {
            addAlternative(selectedValue(this.lb), this.lbCb.getValue(), event);
        }

        private CheckBox getBoxByName(String value) {
            for (CheckBox box : boxes) {
                if (value.equals(box.getName())) {
                    return box;
                }
            }
            return null;
        }

        private void addAlternative(String value, boolean checkBoxvalue, ClickEvent event) {
            if (this.lb.getItemCount() == 0) {
                return;
            }
            final CheckBox box = getBoxByName(value);
            if (box == null || boxInTabCol(box, this.subTable, 0)) {
                return;
            }
            final int row = this.subTable.getRowCount() - 1;
            this.subTable.insertRow(row);
            this.subTable.setWidget(row, 0, box);
            this.alterBoxes.add(box);
            final IconImageIcon iconRemove = IconImage.getIcon("mm-minus") // $NON-NLS$
                    .withClickHandler(innerEvent -> {
                        final Widget source = (Widget) innerEvent.getSource();
                        final int rowId = DOMUtil.getRowId(source.getElement());
                        subTable.removeRow(rowId);
                        alterBoxes.remove(source);
                        if (box.getValue()) {
                            box.setValue(false);
                            LiveMultiEnumOption.this.onClick(innerEvent);
                        }
                    });
            iconRemove.addStyleName("mm-middle mm-link");
            this.subTable.setWidget(row, 1, iconRemove);
            box.setValue(checkBoxvalue);
            if (event != null && checkBoxvalue) {
                onClick(event);
            }
        }

        private boolean boxInTabCol(CheckBox box, FlexTable subTable, int focusOnCol) {
            for (int i = 0; i < subTable.getRowCount(); i++) {
                final int cellCount = subTable.getCellCount(i);
                if (cellCount <= focusOnCol) {
                    continue;
                }
                final Widget widget = subTable.getWidget(i, focusOnCol);
                if (widget == box) {
                    return true;
                }
            }
            return false;
        }

        private List<Item> removeFirstNItems(List<Item> items, int n) {
            if (items.size() <= n) {
                return Collections.emptyList();
            }
            return items.subList(n, items.size());
        }

        private static List<Item> getItems(String liveFinderId, String elementId,
                List<Item> allItems, String[] defaultConf) {
            final LinkedHashMap<String, Item> temp = new LinkedHashMap<>();
            for (Item item : allItems) {
                temp.put(item.value, item);
            }
            final LinkedHashMap<String, Item> elementKeys = new LinkedHashMap<>();
            final String[] conf = getItemConf(liveFinderId, elementId, defaultConf);
            if (conf != null && conf.length > 0) {
                for (String s : conf) {
                    if (temp.containsKey(s)) {
                        elementKeys.put(s, temp.get(s));
                        temp.remove(s);
                    }
                    else {
                        Firebug.log("<getItems> could not add configured item '" + s + // $NON-NLS$
                                "' to " + liveFinderId + "/" + elementId); // $NON-NLS$
                    }
                }
            }
            elementKeys.putAll(temp);
            final ArrayList<Item> result = new ArrayList<>();
            result.addAll(elementKeys.values());
            return result;
        }


        protected static List<Item> getItems(String liveFinderId, String elementId,
                FinderMetaList metaList, String[] defaultConf) {
            final LinkedHashMap<String, String> tempKeyName = new LinkedHashMap<>();
            final LinkedHashMap<String, String> tempNameKey = new LinkedHashMap<>();

            for (FinderMetaList.Element element : metaList.getElement()) {
                tempKeyName.put(element.getKey(), element.getName());
                tempNameKey.put(element.getName(), element.getKey());
            }
            final LinkedHashMap<String, String> elementKeys = new LinkedHashMap<>();
            final String[] conf = getItemConf(liveFinderId, elementId, defaultConf);
            if (conf != null && conf.length > 0) {
                for (String s : conf) {
                    if (tempKeyName.containsKey(s)) {
                        elementKeys.put(s, tempKeyName.get(s));
                        tempKeyName.remove(s);
                    }
                    else if (tempNameKey.containsKey(s)) {
                        String key = tempNameKey.get(s);
                        elementKeys.put(key, tempKeyName.get(key));
                        tempKeyName.remove(key);
                    }
                    else {
                        Firebug.log("<getItems> could not add configured item '" + s + // $NON-NLS$
                                "' to " + liveFinderId + "/" + elementId); // $NON-NLS$
                    }
                }
            }
            elementKeys.putAll(tempKeyName);
            final List<Item> items = new ArrayList<>();
            for (String s : elementKeys.keySet()) {
                items.add(new Item(elementKeys.get(s), s));
            }
            return items;
        }

        protected static int getCheckBoxCount(String liveFinderId, String elementId,
                String[] defaultConf) {
            final String[] conf = getItemConf(liveFinderId, elementId, defaultConf);
            if (conf != null && conf.length > 0) {
                return conf.length > MAX_TOPITEM_COUNT
                        ? MAX_TOPITEM_COUNT
                        : conf.length;
            }
            return 0;
        }

        private static String[] getItemConf(String liveFinderId, String elementId,
                String[] defaultConf) {
            final String tmp = SessionData.INSTANCE.getUser().getAppConfig()
                    .getProperty(AppConfig.LIVE_FINDER_ELEMENT_PREFIX + liveFinderId + elementId);
            if (!StringUtil.hasText(tmp)) {
                return defaultConf == null ? new String[]{} : defaultConf;
            }
            else if (SectionConfigUtil.EMPTY_CONF.equals(tmp)) {
                return new String[]{};
            }
            return tmp.split(SectionConfigUtil.SEPARATOR);
        }


        @Override
        public void setValue(boolean checked) {
            Firebug.log(getClass().getName() + " setValue " + checked);
            super.setValue(checked);
        }

        public void setSearchHandler(SearchHandler searchHandler) {
            if (searchHandler instanceof LiveFinder.DynamicSearchHandler) {
                ((LiveFinder.DynamicSearchHandler) searchHandler).withElement(this);
            }
            this.searchHandler = searchHandler;
        }

        @Override
        public void onClick(ClickEvent event) {
            handleBoxValues(event);
            this.searchHandler.onSearch();
        }

        private void handleBoxValues(ClickEvent event) {
            if (event.getSource() == this.cb) {
                return;
            }
            if (!this.cb.getValue() && isAnyChecked()) {
                this.cb.setValue(true);
                this.cb.fireEvent(event);
            }
            if (this.cb.getValue()) {
                if (isAnyChecked()) {
                    return;
                }
                this.cb.setValue(false);
            }
        }

        @Override
        protected boolean isAnyChecked() {
            return super.isAnyChecked() || this.lbCb.getValue();
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this, getConfigToolBtn()));
        }

        public Widget getInnerWidget() {
            final Panel result = new VerticalPanel();
            final Grid fixTable = createInner(this.fixBoxesCount, 1);
            for (int i = 0; i < this.fixBoxesCount; i++) {
                fixTable.setWidget(i, 0, this.boxes[i]);
            }
            result.add(fixTable);
            result.add(this.subTable);
            return result;
        }

        private Widget getConfigToolBtn() {
            if (this.toolBtn == null) {
                this.toolBtn = de.marketmaker.itools.gwtutil.client.widgets.Button.icon("x-tool-gear") // $NON-NLS$
                        .clickHandler(event -> {
                            final Command onOk = () -> EventBusRegistry.get().fireEvent(
                                    new ConfigChangedEvent(AppConfig.LIVE_FINDER_ELEMENT_PREFIX
                                            + liveFinderId + id, null, null));
                            final LiveFinderElementConfigurator d = metaList == null
                                    ? new LiveFinderElementConfigurator(I18n.I.configuration(), label, items, liveFinderId, id, defaultConfig, onOk)
                                    : new LiveFinderElementConfigurator(I18n.I.configuration(), label, metaList, liveFinderId, id, defaultConfig, onOk);
                            d.show();
                        })
                        .build();
            }
            return this.toolBtn;
        }

        @Override
        protected String query(boolean query) {
            if (!this.isActive()) {
                return null;
            }
            final Set<String> set = new HashSet<>();
            for (CheckBox checkBox : this.boxes) {
                if (checkBox.getValue()) {
                    addToQuery(set, checkBox.getName());
                }
            }
            if (this.lbCb.getValue()) {
                addToQuery(set, selectedValue(this.lb));
            }
            return set.isEmpty() ? "" : this.field + "==" + quoteMultiple(set);
        }

        private void addToQuery(Set<String> set, String value) {
            value = (this.isEnum && this.useExactMatchPrefix) ? (EXACT_MATCH_PREFIX + value) : value;
            set.add(value);
        }

        @Override
        protected void doAddExplanation(FlowPanel panel) {
            if (!isActive()) {
                return;
            }
            final StringBuilder sb = new StringBuilder();
            for (CheckBox box : this.boxes) {
                if (box.getValue()) {
                    addToExplanation(sb, this.keyAndOrigText.get(box.getName()));
                }
            }
            if (this.lbCb.getValue()) {
                addToExplanation(sb, this.keyAndOrigText.get(selectedValue(this.lb)));
            }
            if (sb.toString().isEmpty()) {
                return;
            }
            addExplanation(panel, this.label + " = " + sb.toString());
        }

        private void addToExplanation(StringBuilder sb, String text) {
            if (sb.toString().isEmpty()) {
                sb.append(text);
            }
            else {
                sb.append(" | ").append(text); // $NON-NLS$
            }
        }
    }

    @SuppressWarnings("Duplicates")
    static class LiveSuggestEnumOption extends AbstractOption implements ClickHandler,
            MutableMetadata, DynamicValueElement {
        private static final int MAX_TOPITEM_COUNT = 3;

        private static final String ZERO_METADATA = "0"; // $NON-NLS$

        private final int fixBoxesCount;

        protected List<Item> items;

        private SearchHandler searchHandler;

        private final String metaName;

        private FinderMetaList metaList;

        private Widget toolBtn = null;

        private final String liveFinderId;

        protected SuggestBox sb;

        protected MultiWordSuggestOracle suggestOracle;

        protected Set<String> suggestValues = Collections.emptySet();

        protected CheckBox lbCb;

        private boolean metadataUpdateAllowed = true;

        private final FlexTable subTable;

        private final String[] defaultConfig;

        private final String defaultKey;

        private final Map<String, String> keyAndOrigText = new LinkedHashMap<>();

        protected final boolean isEnum;

        protected boolean useExactMatchPrefix = true;

        private final List<CheckBox> alterBoxes = new ArrayList<>();

        private final List<CheckBox> fixedBoxes = new ArrayList<>();

        private Map<String, FinderMetaList.Element> keyAndElements = new HashMap<>();

        private Scheduler.ScheduledCommand currentFillSuggestBoxCommand;

        public static LiveSuggestEnumOption create(String liveFinderId, String elementId,
                String field, String label, String defaultKey,
                FinderMetaList metaList, String metaName, String[] defaultConfig,
                SearchHandler searchHandler) {
            return new LiveSuggestEnumOption(liveFinderId, elementId, field, label, getItems(liveFinderId, elementId, metaList, defaultConfig),
                    getCheckBoxCount(liveFinderId, elementId, defaultConfig), defaultKey, defaultConfig, metaList, metaName, searchHandler);
        }

        protected LiveSuggestEnumOption(String liveFinderId, String elementId, String field,
                String label, List<Item> items,
                int checkBoxCount, String defaultKey, String[] defaultConfig,
                FinderMetaList metaList,
                String metaName, final SearchHandler searchHandler) {
            super(elementId, field, label);
            this.items = items;
            this.fixBoxesCount = checkBoxCount;
            this.defaultConfig = defaultConfig;
            this.metaList = metaList;
            this.metaName = metaName;
            setSearchHandler(searchHandler);
            this.liveFinderId = liveFinderId;
            this.defaultKey = defaultKey;
            addClickHandler(this);

            for (int i = 0; i < this.fixBoxesCount; i++) {
                final Item item = items.get(i);
                final CheckBox box = createCheckBox(item.getItem(), item.getValue());
                this.fixedBoxes.add(box);
            }

            if (items != null) {
                for (Item item : items) {
                    this.keyAndOrigText.put(item.getValue(), item.getItem());
                }
                final List<Item> suggestItems = removeFirstNItems(items, this.fixBoxesCount);
                this.suggestValues = getSuggestValues(suggestItems);
                this.sb = createSuggestBox(defaultKey);
                this.suggestOracle = (MultiWordSuggestOracle) this.sb.getSuggestOracle();
                addChangeHandler(this.sb);
                this.lbCb = new CheckBox();
                this.lbCb.addClickHandler(this);
                this.sb.setWidth("170px"); // $NON-NLS$

                scheduleFillSuggestBox();
            }

            this.subTable = new FlexTable();
            if (this.sb != null) {
                addSuggestAndCheckBox(this.sb, this.lbCb);
            }
            if (this.metaList == null || this.metaName == null) {
                withoutMetadataUpdate();
                this.isEnum = false;
            }
            else {
                this.isEnum = this.metaList.isEnum();
            }
        }

        static SuggestBox createSuggestBox(String defaultKey) {
            final MultiWordSuggestOracle suggestOracle = new MultiWordSuggestOracle();

            final SuggestBox suggestBox = new SuggestBox(suggestOracle);
            suggestBox.setValue(defaultKey == null ? "" : defaultKey);

            return suggestBox;
        }

        protected CheckBox createCheckBox(String label, String name) {
            final CheckBox b = new CheckBox(label);
            b.setName(name);
            b.addClickHandler(this);
            return b;
        }

        private HashSet<String> getSuggestValues(List<Item> list) {
            final HashSet<String> set = new HashSet<>(list.size());
            for (Item item : list) {
                set.add(item.getValue());
            }
            return set;
        }

        private void addChangeHandler(SuggestBox sb) {
            sb.addSelectionHandler(event -> fireChangeEvent());
            sb.addKeyDownHandler(event -> {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    fireChangeEvent();
                }
            });
            sb.addDomHandler(this, ChangeEvent.getType());
        }

        private void fireChangeEvent() {
            if (this.keyAndOrigText.containsKey(this.sb.getValue())) {
                ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), this.sb);
                // TODO: show error popup
            }
        }

        long lastFireTime = -1;

        public void onChange(ChangeEvent event) {
            final long time = System.currentTimeMillis();
            if (time - this.lastFireTime < 500) {
                return;
            }
            if (!this.suggestValues.contains(this.sb.getValue())) {
                return;
            }
            this.lastFireTime = time;
            if (!getValue() || !this.lbCb.getValue()) {
                setValue(true);
                this.lbCb.setValue(true);
                fireEvent(event);
            }
            this.searchHandler.getChangeHandler().onChange(event);
        }

        @Override
        void setEnabled(boolean enabled) {
            // always enabled
        }

        @Override
        public void addConfigTo(FinderFormConfig config) {
            super.addConfigTo(config);

            addCheckBoxConfigTo(config, this.fixedBoxes, "f");  // $NON-NLS$
            addCheckBoxConfigTo(config, this.alterBoxes, "a");  // $NON-NLS$

            if (this.lbCb.getValue() && this.keyAndOrigText.containsKey(this.sb.getValue())) {
                config.put(this.id + "-item", this.sb.getValue()); // $NON-NLS$
            }
        }

        private void addCheckBoxConfigTo(FinderFormConfig config, List<CheckBox> checkBoxes,
                String listId) {
            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.getValue()) {
                    final String key = this.id + "-" + listId + "-" + checkBox.getName();  // $NON-NLS$
                    config.put(key, "true");  // $NON-NLS$
                }
            }
        }

        @Override
        public void apply(FinderFormConfig config) {
            super.apply(config);

            final String prefixFixed = this.id + "-f-";  // $NON-NLS$
            final String prefixAlter = this.id + "-a-";  // $NON-NLS$

            for (String key : config.getKeySet()) {
                if (key != null) {
                    if (key.startsWith(prefixFixed)) {
                        final String name = key.substring(prefixFixed.length());
                        final String value = config.get(key);
                        if ("true".equals(value)) {  // $NON-NLS$
                            final CheckBox checkBox = getBoxByName(name);
                            if (checkBox != null) {
                                checkBox.setValue(true);
                            }
                        }
                    }
                    else if (key.startsWith(prefixAlter)) {
                        final String name = key.substring(prefixAlter.length());
                        final String value = config.get(key);
                        if ("true".equals(value)) {  // $NON-NLS$
                            addAlternative(name, true, null);
                        }
                    }
                }
            }

            final String key = this.id + "-item"; // $NON-NLS$
            if (config.contains(key)) {
                this.lbCb.setValue(true);
                this.sb.setValue(config.get(key));
            }
        }

        @Override
        public void updateMetadata(final Map<String, FinderMetaList> map, boolean force) {
            if (!this.metadataUpdateAllowed || !map.containsKey(this.metaName)) {
                return;
            }
            if (getValue() && !force) {
                updateCounts();
                return;
            }

            this.metaList = map.get(this.metaName);
            final List<FinderMetaList.Element> elements = this.metaList.getElement();
            this.keyAndElements.clear();
            for (FinderMetaList.Element element : elements) {
                this.keyAndElements.put(element.getKey(), element);
            }
            updateCounts();
            this.suggestOracle.clear();
            final List<Item> suggestItems = removeFirstNItems(this.items, this.fixBoxesCount);
            this.suggestValues = getSuggestValues(suggestItems);
            scheduleFillSuggestBox();
            final boolean hasItems = !suggestItems.isEmpty();
            this.lbCb.setEnabled(hasItems);
            this.sb.setEnabled(hasItems);
            this.cb.setEnabled(anyCheckBoxEnabled() || hasItems);
        }

        private String getSelectedKey() {
            return this.lbCb.getValue() ? this.sb.getValue() : this.defaultKey;
        }

        private boolean anyCheckBoxEnabled() {
            boolean anyCheckBoxEnabled = false;
            for (CheckBox box : joinBoxes()) {
                String countOf = ZERO_METADATA;
                if (this.keyAndElements.containsKey(box.getName())) {
                    countOf = this.keyAndElements.get(box.getName()).getCount();
                }
                box.setEnabled(!ZERO_METADATA.equals(countOf));
                if (box.isEnabled()) {
                    anyCheckBoxEnabled = true;
                }
            }
            return anyCheckBoxEnabled;
        }

        private void updateCounts() {
            for (CheckBox box : joinBoxes()) {
                final FinderMetaList.Element element = this.keyAndElements.get(box.getName());
                if (element != null) {
                    final String countOf = element.getCount();
                    final String text = this.keyAndOrigText.get(box.getName()) + (countOf != null ? " (" + countOf + ")" : "");  // $NON-NLS$
                    box.setText(text);
                }
                else {
                    box.setText(this.keyAndOrigText.get(box.getName()));
                }
            }
        }

        private ArrayList<CheckBox> joinBoxes() {
            final ArrayList<CheckBox> allBoxes = new ArrayList<>(this.fixedBoxes);
            allBoxes.addAll(this.alterBoxes);
            return allBoxes;
        }

        public LiveSuggestEnumOption withoutMetadataUpdate() {
            this.metadataUpdateAllowed = false;
            return this;
        }

        @Override
        public void reset() {
            super.reset();
            for (Widget alterBox : this.alterBoxes) {
                final int rowId = DOMUtil.getRowId(alterBox.getElement());
                this.subTable.removeRow(rowId);
            }
            this.alterBoxes.clear();

            final List<Item> suggestItems = removeFirstNItems(this.items, this.fixBoxesCount);
            this.suggestValues = getSuggestValues(suggestItems);
            this.suggestOracle.clear();

            this.lbCb.setValue(false);

            scheduleFillSuggestBox();
        }

        private void scheduleFillSuggestBox() {
            if (this.currentFillSuggestBoxCommand == null) {
                final Scheduler.ScheduledCommand cmd = new Scheduler.ScheduledCommand() {

                    @Override
                    public void execute() {
                        fillSuggestBox(sb, suggestOracle, metaList, suggestValues, getSelectedKey());

                        if (currentFillSuggestBoxCommand == this) {
                            currentFillSuggestBoxCommand = null;
                        }
                    }
                };
                this.currentFillSuggestBoxCommand = cmd;
                Scheduler.get().scheduleDeferred(cmd);
            }
        }

        @Override
        protected String doGetQuery() {
            if (!this.isActive()) {
                return null;
            }
            final Set<String> set = new HashSet<>();
            for (CheckBox checkBox : joinBoxes()) {
                if (checkBox.getValue()) {
                    addToQuery(set, checkBox.getName());
                }
            }
            if (this.lbCb.getValue()) {
                addToQuery(set, selectedValue(this.sb));
            }
            return set.isEmpty() ? "" : this.field + "==" + quoteMultiple(set);
        }

        private void addSuggestAndCheckBox(final SuggestBox sb, final CheckBox cb) {
            final FlowPanel lbCb = new FlowPanel();
            lbCb.add(cb);
            lbCb.add(sb);
            this.subTable.setWidget(0, 0, lbCb);
            final IconImageIcon iconAdd = IconImage.getIcon("mm-plus") // $NON-NLS$
                    .withClickHandler(this::addAlternative);
            iconAdd.addStyleName("mm-middle mm-link");
            this.subTable.setWidget(0, 1, iconAdd);
        }

        private void addAlternative(ClickEvent event) {
            addAlternative(selectedValue(this.sb), this.lbCb.getValue(), event);
        }

        private CheckBox getBoxByName(String value) {
            for (CheckBox box : joinBoxes()) {
                if (value.equals(box.getName())) {
                    return box;
                }
            }
            return null;
        }

        private void addAlternative(String value, boolean checkBoxvalue, ClickEvent event) {
            if (this.suggestValues.isEmpty()) {
                return;
            }
            final CheckBox box1 = getBoxByName(value);
            if (boxInTabCol(box1, this.subTable, 0)) {
                return;
            }
            final int row = this.subTable.getRowCount() - 1;

            final CheckBox box;
            if (box1 == null) {
                box = createCheckBox(this.keyAndOrigText.get(value), value);
            }
            else {
                box = box1;
            }
            this.subTable.insertRow(row);
            this.subTable.setWidget(row, 0, box);
            this.alterBoxes.add(box);
            this.subTable.setWidget(row, 1, IconImage.getIcon("mm-minus").withStyleName("mm-link")  // $NON-NLS$
                    .withClickHandler(innerEvent -> {
                        final Widget source = (Widget) innerEvent.getSource();
                        final int rowId = DOMUtil.getRowId(source.getElement());
                        this.subTable.removeRow(rowId);
                        this.alterBoxes.remove(box);
                        if (box.getValue()) {
                            box.setValue(false);
                            LiveSuggestEnumOption.this.onClick(innerEvent);
                        }
                    }));
            box.setValue(checkBoxvalue);
            if (event != null && checkBoxvalue) {
                onClick(event);
            }
        }

        private boolean boxInTabCol(CheckBox box, FlexTable subTable, int focusOnCol) {
            for (int i = 0; i < subTable.getRowCount(); i++) {
                final int cellCount = subTable.getCellCount(i);
                if (cellCount <= focusOnCol) {
                    continue;
                }
                final Widget widget = subTable.getWidget(i, focusOnCol);
                if (widget == box) {
                    return true;
                }
            }
            return false;
        }

        private List<Item> removeFirstNItems(List<Item> items, int n) {
            if (items.size() <= n) {
                return Collections.emptyList();
            }
            return items.subList(n, items.size());
        }

        protected static List<Item> getItems(String liveFinderId, String elementId,
                FinderMetaList metaList, String[] defaultConf) {
            final LinkedHashMap<String, String> tempKeyName = new LinkedHashMap<>();
            final LinkedHashMap<String, String> tempNameKey = new LinkedHashMap<>();

            for (FinderMetaList.Element element : metaList.getElement()) {
                tempKeyName.put(element.getKey(), element.getName());
                tempNameKey.put(element.getName(), element.getKey());
            }
            final LinkedHashMap<String, String> elementKeys = new LinkedHashMap<>();
            final String[] conf = getItemConf(liveFinderId, elementId, defaultConf);
            if (conf != null && conf.length > 0) {
                for (String s : conf) {
                    if (tempKeyName.containsKey(s)) {
                        elementKeys.put(s, tempKeyName.get(s));
                        tempKeyName.remove(s);
                    }
                    else if (tempNameKey.containsKey(s)) {
                        String key = tempNameKey.get(s);
                        elementKeys.put(key, tempKeyName.get(key));
                        tempKeyName.remove(key);
                    }
                    else {
                        Firebug.log("<getItems> could not add configured item '" + s + // $NON-NLS$
                                "' to " + liveFinderId + "/" + elementId); // $NON-NLS$
                    }
                }
            }
            elementKeys.putAll(tempKeyName);
            final List<Item> items = new ArrayList<>();
            for (String s : elementKeys.keySet()) {
                items.add(new Item(elementKeys.get(s), s));
            }
            return items;
        }

        protected static int getCheckBoxCount(String liveFinderId, String elementId,
                String[] defaultConf) {
            final String[] conf = getItemConf(liveFinderId, elementId, defaultConf);
            if (conf != null && conf.length > 0) {
                return conf.length > MAX_TOPITEM_COUNT
                        ? MAX_TOPITEM_COUNT
                        : conf.length;
            }
            return 0;
        }

        private static String[] getItemConf(String liveFinderId, String elementId,
                String[] defaultConf) {
            final String tmp = SessionData.INSTANCE.getUser().getAppConfig()
                    .getProperty(AppConfig.LIVE_FINDER_ELEMENT_PREFIX + liveFinderId + elementId);
            if (!StringUtil.hasText(tmp)) {
                return defaultConf == null ? new String[]{} : defaultConf;
            }
            else if (SectionConfigUtil.EMPTY_CONF.equals(tmp)) {
                return new String[]{};
            }
            return tmp.split(SectionConfigUtil.SEPARATOR);
        }


        @Override
        public void setValue(boolean checked) {
            super.setValue(checked);
        }

        public void setSearchHandler(SearchHandler searchHandler) {
            if (searchHandler instanceof LiveFinder.DynamicSearchHandler) {
                ((LiveFinder.DynamicSearchHandler) searchHandler).withElement(this);
            }
            this.searchHandler = searchHandler;
        }

        @Override
        public void onClick(ClickEvent event) {
            handleBoxValues(event);
            this.searchHandler.onSearch();
        }

        private void handleBoxValues(ClickEvent event) {
            if (event.getSource() == this.cb) {
                return;
            }
            if (!this.cb.getValue() && isAnyChecked()) {
                this.cb.setValue(true);
                this.cb.fireEvent(event);
            }
            if (this.cb.getValue()) {
                if (isAnyChecked()) {
                    return;
                }
                this.cb.setValue(false);
            }
        }

        private boolean isAnyChecked() {
            for (CheckBox box : joinBoxes()) {
                if (box.getValue()) {
                    return true;
                }
            }
            return this.lbCb.getValue();
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this, getConfigToolBtn()));
        }

        public Widget getInnerWidget() {
            final Panel result = new VerticalPanel();
            final Grid fixTable = createInner(this.fixBoxesCount, 1);
            for (int i = 0; i < this.fixBoxesCount; i++) {
                fixTable.setWidget(i, 0, this.fixedBoxes.get(i));
            }
            result.add(fixTable);
            result.add(this.subTable);
            return result;
        }

        private Widget getConfigToolBtn() {
            if (this.toolBtn == null) {
                this.toolBtn = de.marketmaker.itools.gwtutil.client.widgets.Button.icon("x-tool-gear") // $NON-NLS$
                        .clickHandler(event -> {
                            final Command onOk = () -> EventBusRegistry.get().fireEvent(
                                    new ConfigChangedEvent(AppConfig.LIVE_FINDER_ELEMENT_PREFIX
                                            + liveFinderId + id, null, null));
                            final LiveFinderElementConfigurator d = metaList == null
                                    ? new LiveFinderElementConfigurator(I18n.I.configuration(), label, items, liveFinderId, id, defaultConfig, onOk)
                                    : new LiveFinderElementConfigurator(I18n.I.configuration(), label, metaList, liveFinderId, id, defaultConfig, onOk);
                            d.show();
                        })
                        .build();
            }
            return this.toolBtn;
        }

        private void addToQuery(Set<String> set, String value) {
            value = (this.isEnum && this.useExactMatchPrefix) ? (EXACT_MATCH_PREFIX + value) : value;
            set.add(value);
        }

        @Override
        protected void doAddExplanation(FlowPanel panel) {
            if (!isActive()) {
                return;
            }
            final StringBuilder sb = new StringBuilder();
            for (CheckBox box : joinBoxes()) {
                if (box.getValue()) {
                    addToExplanation(sb, this.keyAndOrigText.get(box.getName()));
                }
            }
            if (this.lbCb.getValue()) {
                addToExplanation(sb, this.keyAndOrigText.get(selectedValue(this.sb)));
            }
            if (sb.toString().isEmpty()) {
                return;
            }
            addExplanation(panel, this.label + " = " + sb.toString());
        }

        private void addToExplanation(StringBuilder sb, String text) {
            if (sb.toString().isEmpty()) {
                sb.append(text);
            }
            else {
                sb.append(" | ").append(text); // $NON-NLS$
            }
        }
    }


    @SuppressWarnings("Duplicates")
    static class LiveFromToTextOption extends FromToTextOption {
        private final SearchHandler searchHandler;

        LiveFromToTextOption(String id, String field, String label, String textFieldSuffix,
                SearchHandler searchHandler) {
            super(id, field, label, textFieldSuffix);
            setWidth("45px"); // $NON-NLS$
            this.searchHandler = searchHandler;
            addClickHandler(this.searchHandler.getClickHandler());
            addValueChangeHandler(this.searchHandler.getStrValueChangHandler());
        }

        @Override
        public FromToTextOption initialize() {
            final FromToTextOption result = super.initialize();
            this.fromText.addChangeHandler(this);
            this.toText.addChangeHandler(this);
            this.fromText.setEnabled(true);
            this.toText.setEnabled(true);
            return result;
        }

        public void onChange(ChangeEvent event) {
            FinderFormUtils.checkForActivation(this, event);
            this.searchHandler.getChangeHandler().onChange(event);
        }

        @Override
        void setEnabled(boolean enabled) {
            // always enabled
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }
    }

    static class LiveBooleanOption extends BooleanOption {
        LiveBooleanOption(String field, String label, SearchHandler searchHandler) {
            this(field, label, false, searchHandler);
        }

        LiveBooleanOption(String field, String label, boolean reverse, SearchHandler searchHandler) {
            super(field, label, reverse);
            addClickHandler(searchHandler.getClickHandler());
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }
    }

    static class LiveRadioOption extends RadioOption {
        private final SearchHandler searchHandler;

        LiveRadioOption(String field, String label, List<Item> items, SearchHandler searchHandler) {
            super(field, label, items);
            this.searchHandler = searchHandler;
            addClickHandler(searchHandler.getClickHandler());
        }

        @Override
        public void initialize(Map<String, FinderMetaList> map) {
            super.initialize(map);
            for (RadioButton button : this.buttons) {
                button.addClickHandler(this);
            }
        }

        @Override
        public void onClick(ClickEvent event) {
            if (!getValue()) {
                setValue(true);
                fireEvent(event);
            }
            else {
                super.onClick(event);
                searchHandler.getClickHandler().onClick(event);
            }
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }

        @Override
        void setEnabled(boolean enabled) {
            //nothing
        }
    }

    static class LiveFromToBoxOption extends FromToBoxOption {
        private final SearchHandler searchHandler;

        LiveFromToBoxOption(String id, String field, String label, List<Item> items,
                SearchHandler searchHandler) {
            super(id, field, label, items);
            this.searchHandler = searchHandler;
            addClickHandler(searchHandler.getClickHandler());
        }

        public void onChange(ChangeEvent event) {
            FinderFormUtils.checkForActivation(this, event);
            this.searchHandler.getChangeHandler().onChange(event);
        }

        @Override
        void setEnabled(boolean enabled) {
            // always enabled
        }


        @Override
        public void initialize(Map<String, FinderMetaList> map) {
            super.initialize(map);
            this.fromLb.addChangeHandler(this);
            this.toLb.addChangeHandler(this);
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }
    }


    static class LiveStartEndOption extends StartEndOption {
        private final SearchHandler searchHandler;

        private boolean hideFromTo = false;

        LiveStartEndOption(String id, String field, String label, String textFieldSuffix,
                List<Item> items,
                DateTimeUtil.PeriodMode mode, SearchHandler searchHandler) {
            super(id, field, label, textFieldSuffix, items, mode);
            this.searchHandler = searchHandler;
            addClickHandler(searchHandler.getClickHandler());
            addValueChangeHandler(searchHandler.getStrValueChangHandler());
        }

        public LiveStartEndOption withHideFromTo() {
            this.hideFromTo = true;
            return this;
        }

        @Override
        public FromToTextOption initialize() {
            final FromToTextOption result = super.initialize();
            this.fromText.addChangeHandler(this.searchHandler.getChangeHandler());
            this.toText.addChangeHandler(this.searchHandler.getChangeHandler());
            this.lb.addChangeHandler(this.searchHandler.getChangeHandler());
            return result;
        }

        @Override
        protected Grid createInnerGrid() {
            final Grid result = super.createInnerGrid();
            final DateBox[] boxes = this.dateBox;
            for (DateBox box : boxes) {
                box.addValueChangeHandler(this.searchHandler.getDateValueChangHandler());
            }
            return result;
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }

        @Override
        protected Grid addWidgets() {
            final Grid result = createInner(this.hideFromTo ? 1 : 3, 1);
            final Panel firstRow = new HorizontalPanel();
            firstRow.add(this.buttons[0]);
            firstRow.add(this.lb);
            result.setWidget(0, 0, firstRow);
            if (!this.hideFromTo) {
                final Panel secondRow = new HorizontalPanel();
                secondRow.add(this.buttons[1]);
                secondRow.add(this.dateBox[0]);
                result.setWidget(1, 0, secondRow);
                result.setWidget(2, 0, this.dateBox[1]);
                result.getWidget(2, 0).getElement().getStyle().setMarginLeft(22, Style.Unit.PX);
            }
            for (RadioButton button : buttons) {
                button.addClickHandler(this.searchHandler.getClickHandler());
            }
            return result;
        }
    }


    static class MoreLessLabel implements FinderFormElement {
        private final Label moreLess;

        private boolean more = false;

        private int lablesRowInFlexTable;

        private FlexTable flexTable;

        private final int lessMoreCount;

        private final List<FinderFormElement> elements;

        private final String sectionId;

        MoreLessLabel(List<FinderFormElement> elements, int lessMoreCount, String sectionId) {
            this.lessMoreCount = lessMoreCount;
            this.sectionId = sectionId;
            this.elements = elements;
            this.moreLess = new Label(I18n.I.more2());
            this.moreLess.addStyleName("mm-link"); // $NON-NLS$
            this.moreLess.addClickHandler(event -> switchState());
        }

        public void fireEvent(GwtEvent<?> event) {
            // nothing
        }

        public void addChangeHandler(ChangeHandler ch) {
            // nothing
        }

        public void switchState() {
            moreLess.setText(!more ? I18n.I.more2() : I18n.I.less2());
            if (this.more) {
                setAllElementsVisible(true);
            }
            else {
                setAllElementsVisible(false);
            }
            more = !more;
        }

        private void setAllElementsVisible(boolean visible) {
            for (int i = this.lablesRowInFlexTable + 1;
                    i < this.lablesRowInFlexTable + getActiveElementsCount() - this.lessMoreCount; i++) {
                this.flexTable.getRowFormatter().setVisible(i, visible);
            }
        }

        private int getActiveElementsCount() {
            int count = 0;
            for (FinderFormElement element : elements) {
                if (element.isActive()) {
                    count++;
                }
            }
            return count;
        }

        public void addTo(FlexTable flexTable, int row) {
            if (!isActive()) {
                return;
            }
            this.flexTable = flexTable;
            this.lablesRowInFlexTable = row;
            flexTable.setWidget(row, 1, this.moreLess);
        }

        public String getQuery() {
            return null;
        }

        public void addExplanation(FlowPanel panel) {
        }

        public void addConfigTo(FinderFormConfig config) {
            if (!this.more) {
                config.put(getId() + "-visible", "true"); // $NON-NLS$
            }
        }

        public void apply(FinderFormConfig config) {
            if ("true".equals(config.get(getId() + "-visible"))) { // $NON-NLS-0$ $NON-NLS-1$
                setValue(true);
            }
        }

        public void reset() {
        }

        public void initialize(Map<String, FinderMetaList> map) {
        }

        public void addClickHandler(ClickHandler ch) {
            this.moreLess.addClickHandler(ch);
        }

        public boolean getValue() {
            return this.more;
        }

        public void setValue(boolean checked) {
            this.more = checked;
            switchState();
        }

        public boolean isActive() {
            return true;
        }

        public void setActive(boolean active) {
            //is always active
        }

        public String getLabel() {
            return this.moreLess.getText();
        }

        public String getId() {
            return this.sectionId + "MorelessLabel"; // $NON-NLS$
        }

        public boolean isConfigurable() {
            return false;
        }

        public Integer getDefaultOrder() {
            return null;
        }
    }

    public static class LiveSymbolOption extends SymbolOption {

        public LiveSymbolOption(String field, String label, String[] filterTypes,
                boolean showMarketsPage,
                String filterForUnderlyingsForType, Boolean filterForUnderlyingsOfLeveragProducts,
                SearchHandler searchHandler) {
            super(field, label, filterTypes, isIidField(field) ? IID_WITHOUT_SUFFIX : QID,
                    showMarketsPage, filterForUnderlyingsForType, filterForUnderlyingsOfLeveragProducts);
            addClickHandler(searchHandler.getClickHandler());
            addValueChangeHandler(searchHandler.getStrValueChangHandler());
        }

        private static boolean isIidField(String field) {
            return FinderFormKeys.IID.equals(field) || FinderFormKeys.UNDERLYING.equals(field);
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }
    }

    public static class LiveTextOption extends TextOption implements KeyUpHandler {

        private final SearchHandler searchHandler;

        public LiveTextOption(String field, String label, SearchHandler searchHandler) {
            super(field, field, label, "");
            this.searchHandler = searchHandler;
            addValueChangeHandler(searchHandler.getStrValueChangHandler());
            addClickHandler(searchHandler.getClickHandler());
        }

        @Override
        public void addTo(FlexTable flexTable, int row) {
            setEnabled(FinderFormUtils.shrunkAddTo(getInnerWidget(), flexTable, this.cb, row, this));
        }

        @Override
        public TextOption initialize() {
            final TextOption result = super.initialize();
            this.textBox.addChangeHandler(this);
            // ChangeEvents don't bubble in IE (http://msdn.microsoft.com/en-us/library/ie/ms536912%28v=vs.85%29.aspx).
            // As a workaround, we are listening for KeyPressEvents, too. Unfortunately, this causes
            // duplicate update events in non-IE browsers, but that's the lesser of two evils
            this.textBox.addKeyUpHandler(this);
            this.textBox.setEnabled(true);
            return result;
        }

        public void onChange(ChangeEvent event) {
            FinderFormUtils.checkForActivation(this, event);
            this.searchHandler.getChangeHandler().onChange(event);
        }

        @Override
        public void onKeyUp(KeyUpEvent event) {
            FinderFormUtils.checkForActivation(this, event);
            this.searchHandler.getKeyUpHandler().onKeyUp(event);
        }

        @Override
        void setEnabled(boolean enabled) {
            // always enabled
        }

    }

    public static class NewsSearchLiveTextOption extends LiveTextOption {

        public NewsSearchLiveTextOption(String field, String label, SearchHandler searchHandler) {
            super(field, label, searchHandler);
        }

        protected String doGetQuery() {
            return NEWS_SEARCH_PARSER.parse(this.textBox.getText());
        }
    }

}