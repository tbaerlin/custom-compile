package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.input.Radio;
import de.marketmaker.itools.gwtutil.client.widgets.input.RadioGroup;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: umaurer
 * Created: 23.01.14
 */
public class SpsRadios extends SpsBoundWidget<Widget, SpsLeafProperty> implements HasCaption, HasFocusHandlers, HasBlurHandlers {
    private final Map<String, String> mapEnum;
    private final String enumNullValue;
    private String layout;
    private String[] labels;
    private RadioGroup<String> radioGroup = new RadioGroup<>();
    private boolean floating = false;
    private List<Widget> radioList;

    public SpsRadios(Map<String, String> mapEnum, String enumNullValue) {
        this.mapEnum = mapEnum;
        this.enumNullValue = enumNullValue;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getLayout() {
        return layout;
    }

    public void setLabels(String[] labels) {
        if (labels.length != this.mapEnum.size()) {
            throw new IllegalArgumentException("enum count: " + this.mapEnum.size() + " != label count: " + labels.length); // $NON-NLS$
        }
        this.labels = labels;
    }

    public String[] getLabels() {
        return this.labels;
    }

    public SpsRadios withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    @Override
    public void setStyle(String clazz) {
        super.setStyle(clazz);
        if ("float".equals(clazz)) { // $NON-NLS$
            this.floating = true;
        }
    }

    @Override
    protected Widget createWidget() {
        this.radioList = createWidgetList();
        return null;
    }

    private FlowPanel getWidgetsInPanel() {
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("sps-radios");
        for (Widget widget : radioList) {
            if (this.floating) {
                final SimplePanel sp = new SimplePanel(widget);
                sp.setStyleName("sps-radios-float");
                panel.add(sp);
            }
            else {
                panel.add(widget);
            }
        }
        if (this.floating) {
            final Label clear = new Label();
            clear.getElement().getStyle().setClear(Style.Clear.BOTH);
            panel.add(clear);
        }
        return panel;
    }

    private List<Widget> getWidgetsAsList() {
        final ArrayList<Widget> listWidgets = new ArrayList<>(this.radioList.size() + 2);
        final HTML captionWidget = getCaptionWidget();
        final Image tooltipHelp = createTooltipHelp();

        if (captionWidget != null) {
            listWidgets.add(captionWidget);
        }
        listWidgets.addAll(this.radioList);
        if (tooltipHelp != null) {
            listWidgets.add(tooltipHelp);
        }
        return listWidgets;
    }

    protected Widget[] createWidgets() {
        if (isForceCaptionWidget()) {
            setWidget(getWidgetsInPanel(), true);
            return super.createWidgets();
        }
        else {
            checkWidgetInitialized();
            final List<Widget> list = getWidgetsAsList();
            return list.toArray(new Widget[list.size()]);
        }
    }

    private List<Widget> createWidgetList() {
        final boolean hasNullValue = StringUtil.hasText(this.enumNullValue);

        final List<Widget> list = new ArrayList<>(this.labels == null ? this.mapEnum.size() : (2 * this.mapEnum.size()));
        final List<Map.Entry<String, String>> listEnumEntries = new ArrayList<>(this.mapEnum.entrySet());
        this.radioGroup.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getBindFeature().getSpsProperty().setValue(event.getValue());
            }
        });
        for (int i = 0; i < listEnumEntries.size(); i++) {
            final Map.Entry<String, String> entry = listEnumEntries.get(i);
            final Radio rb = this.radioGroup.add(entry.getKey(), false);
            if ("noLabel".equals(this.layout)) { // $NON-NLS$
                rb.setStyleName(getBaseStyle());
                list.add(rb);
            }
            else {
                final boolean entryIsEnumNullValue = isEnumNullValue(hasNullValue, entry);

                if(entryIsEnumNullValue && isMandatory()) {
                    continue;
                }
                final FlowPanel panel = new FlowPanel();
                panel.setStyleName(getBaseStyle());
                panel.add(rb);
                panel.add(rb.createSpan(TextUtil.toSafeHtml(getEnumLabel(hasNullValue, entry))));

                if(entryIsEnumNullValue) {
                    list.add(0, panel);
                }
                else {
                    list.add(panel);
                }
            }
            if (this.labels != null) {
                final Label description = new HTML(TextUtil.toSafeHtml(this.labels[i]));
                description.setStyleName(getBaseStyle() + "-description");
                list.add(description);
            }
        }
        return list;
    }

    private String getEnumLabel(boolean hasNullValue, Map.Entry<String, String> entry) {
        if(isEnumNullValue(hasNullValue, entry)) {
            return I18n.I.noSelection();
        }
        return entry.getValue();
    }

    private boolean isEnumNullValue(boolean hasNullValue, Map.Entry<String, String> entry) {
        return hasNullValue && this.enumNullValue != null && this.enumNullValue.equals(entry.getKey());
    }

    @Override
    public void onPropertyChange() {
        setValue(getBindFeature().getSpsProperty().getStringValue(), false);
    }

    private void setValue(String value, boolean fireEvent) {
        this.radioGroup.setValue(value, fireEvent);
    }

    @Override
    public boolean focusFirst() {
        return this.radioGroup != null && this.radioGroup.focusFirst();
    }

    @Override
    protected boolean hasFocus() {
        return this.radioGroup != null && this.radioGroup.hasFocus();
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.radioGroup.addFocusHandler(handler);
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.radioGroup.addBlurHandler(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.radioGroup.fireEvent(event);
    }
}
