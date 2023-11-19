package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SimpleStandaloneEngine;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCheckBox;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCombo;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsDateTimeEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsDecimalEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsShellMMInfoPicker;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.DecimalBox;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.ShellMMInfoPicker;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMDateTime;
import de.marketmaker.iview.pmxml.ParameterDesc;
import de.marketmaker.iview.pmxml.ParameterEnumDesc;
import de.marketmaker.iview.pmxml.ParameterShellMMDesc;
import de.marketmaker.iview.pmxml.ParameterSimpleDesc;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.ShellMMInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.ID_NAME_SUFFIX;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asBoolean;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asString;

/**
 * @author umaurer
 * @author Markus Dick
 */
public class AnalysisMetadataForm extends Composite {
    private final List<ParameterWidget> parameterWidgets;

    abstract static class ParameterWidget<T, H extends ParameterDesc> implements TakesValue<T>, ChangeHandler, ValueChangeHandler, SelectionHandler<MenuItem> {
        protected final Widget widget;
        protected final Widget captionWidget;
        protected final H parameter;
        protected final Image resetWidget;
        protected T value;

        public ParameterWidget(Widget widget, Widget captionWidget, final H parameter) {
            this.widget = widget;
            this.captionWidget = captionWidget;
            this.parameter = parameter;

            this.resetWidget = IconImage.get("mm-reset-icon").createImage(); // $NON-NLS$
            this.resetWidget.addStyleName("mm-pointer");
            this.resetWidget.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    resetToDefault();
                }
            });
        }

        protected void setInitialValue(T v, boolean doNotSetDefaultIfValueIsNull) {
            if (v != null || doNotSetDefaultIfValueIsNull) {
                setValue(v);
            } else if (getDefaultValue() != null) {
                setValue(getDefaultValue());
            }
            onChange(null);
        }

        public Widget getWidget() {
            return this.widget;
        }

        public Widget getCaptionWidget() {
            return this.captionWidget;
        }

        public Widget getResetWidget() {
            return this.resetWidget;
        }

        public void addLayoutParameter(Map<String, String> map) {
            final T value = getValue();
            final T defaultValue = getDefaultValue();
            if (!equals(value, defaultValue)) {
                if(value == null) {
                    map.put(this.parameter.getName(), null);
                    return;
                }
                map.put(this.parameter.getName(), value.toString());
            }
        }

        @Override
        public void onChange(ChangeEvent event) {
            final T value = getValue();
            this.resetWidget.setVisible(!equals(value, getDefaultValue()));
        }

        @Override
        public void onValueChange(ValueChangeEvent valueChangeEvent) {
            onChange(null);
        }

        @Override
        public void onSelection(SelectionEvent<MenuItem> event) {
            onChange(null);
        }

        private void resetToDefault() {
            setValue(getDefaultValue());
            onChange(null);
        }

        protected boolean equals(T o1, T o2) {
            return CompareUtil.equals(o1, o2);
        }

        public abstract T getDefaultValue();
    }

    private static abstract class SpsPropertyParameterWidget<H extends ParameterDesc> extends ParameterWidget<String, H> {
        protected final SpsLeafProperty spsProperty;

        private SpsPropertyParameterWidget(SpsBoundWidget<?, SpsLeafProperty> widget, H parameter) {
            super(widget.getWidget(), widget.getCaptionWidget(), parameter);
            this.spsProperty = widget.getBindFeature().getSpsProperty();
            this.spsProperty.addChangeHandler(this);
        }

        @Override
        public void setValue(String value) {
            this.spsProperty.setValue(value);
        }

        @Override
        public String getValue() {
            return this.spsProperty.getStringValue();
        }
    }

    private static class SpsShellMMInfoPropertyParameterWidget extends ParameterWidget<ShellMMInfo, ParameterShellMMDesc> {
        protected final SpsLeafProperty spsProperty;

        private SpsShellMMInfoPropertyParameterWidget(SpsBoundWidget<?, SpsLeafProperty> widget, ParameterShellMMDesc parameter) {
            super(widget.getWidget(), widget.getCaptionWidget(), parameter);
            this.spsProperty = widget.getBindFeature().getSpsProperty();
            this.spsProperty.addChangeHandler(this);
        }

        @Override
        public void setValue(ShellMMInfo value) {
            this.spsProperty.setValue(value);
        }

        @Override
        public ShellMMInfo getValue() {
            return this.spsProperty.getShellMMInfo();
        }

        @Override
        public ShellMMInfo getDefaultValue() {
            return parameter.getDefaultValue();
        }

        @Override
        protected boolean equals(ShellMMInfo o1, ShellMMInfo o2) {
            return AnalysisMetadataForm.equals(o1, o2);
        }

        @Override
        public void addLayoutParameter(Map<String, String> map) {
            final ShellMMInfo value = getValue();
            final ShellMMInfo defaultValue = getDefaultValue();

            if (!AnalysisMetadataForm.equals(value, defaultValue)) {
                if(value == null) {
                    map.put(this.parameter.getName(), null);
                    map.put(this.parameter.getName() + ID_NAME_SUFFIX, null);
                    return;
                }

                map.put(this.parameter.getName(), value.getBezeichnung());
                map.put(this.parameter.getName() + ID_NAME_SUFFIX, value.getId());
            }
        }
    }

    public AnalysisMetadataForm(LayoutDesc metadata, Map<String, String> layoutParameters, PopupPanel parentPopupPanel) {
        this(metadata, layoutParameters, true, parentPopupPanel);
    }

    public AnalysisMetadataForm(LayoutDesc metadata, Map<String, String> layoutParameters, boolean withReportNameHeadline, PopupPanel parentPopupPanel) {
        Firebug.logAsGroup("<AnalysisMetadataForm.AnalysisMetadataForm>", layoutParameters); // $NON-NLS$

        final Panel panel = new FlowPanel();
        panel.setStyleName("mm-form"); // $NON-NLS$
        panel.addStyleName("sps-taskView");  // $NON-NLS$
        final String layoutName = metadata.getLayout().getLayoutName();
        if (withReportNameHeadline && layoutName != null) {
            panel.add(new Label(layoutName));
        }
        final FlexTable table = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        final List<ParameterDesc> parameters = metadata.getParameters();
        this.parameterWidgets = new ArrayList<>();
        for (final ParameterDesc parameter : parameters) {
            final String parameterName = parameter.getName();
            final String value = layoutParameters.get(parameterName);

            final ParameterWidget parameterWidget;
            // A null value may be set explicitly by the user, which is the case if it is available in layoutParameters
            // In such a case, creating the parameter widget with a null value means, that the null value must
            // be set instead of retrieving the default value
            final boolean valueDifferentFromDefault = layoutParameters.containsKey(parameterName);
            switch (parameter.getTypeInfo().getTypeId()) {
                case TI_NUMBER:
                    parameterWidget = createParameterWidgetNumber((ParameterSimpleDesc) parameter, value,
                            valueDifferentFromDefault);
                    break;
                case TI_DATE:
                    parameterWidget = createParameterWidgetDate((ParameterSimpleDesc) parameter, value,
                            valueDifferentFromDefault);
                    break;
                case TI_STRING:
                    parameterWidget = createParameterWidgetString((ParameterSimpleDesc) parameter, value,
                            valueDifferentFromDefault);
                    break;
                case TI_BOOLEAN:
                    //Allow a three way boolean to be set to null
                    parameterWidget = createParameterWidgetBoolean((ParameterSimpleDesc) parameter, value,
                            valueDifferentFromDefault);
                    break;
                case TI_ENUMERATION:
                    parameterWidget = createParameterWidgetEnum((ParameterEnumDesc) parameter, value,
                            valueDifferentFromDefault);
                    break;
                case TI_FOLDER:
                case TI_SHELL_MM:
                    final String id = layoutParameters.get(parameterName + ID_NAME_SUFFIX);
                    parameterWidget = createParameterWidgetShellMM((ParameterShellMMDesc) parameter, value, id,
                            parentPopupPanel, valueDifferentFromDefault);
                    break;
                default:
                    throw new IllegalArgumentException("unknown parameter type: " + parameter.getTypeInfo().getTypeId()); // $NON-NLS-0$
            }

            this.parameterWidgets.add(parameterWidget);
            final int size = this.parameterWidgets.size() - 1;

            table.setWidget(size, 0, parameterWidget.getCaptionWidget());
            formatter.setStyleName(size, 0, "sps-form-label");
            table.setWidget(size, 1, parameterWidget.getWidget());
            table.setWidget(size, 2, parameterWidget.getResetWidget());
        }
        panel.add(table);
        initWidget(panel);
    }

    private static String toCaption(String displayName) {
        return (displayName != null ? displayName.replaceAll("_", " ") : null);  // $NON-NLS$
    }

    private ParameterWidget createParameterWidgetShellMM(ParameterShellMMDesc parameter, String label, String id, PopupPanel parentPopupPanel, final boolean doNotSetDefaultIfValueIsNull) {
        final SpsShellMMInfoPicker spsPicker = new SpsShellMMInfoPicker();
        spsPicker.setCaption(toCaption(parameter.getDisplayname()));
        spsPicker.setBaseStyle("sps-edit-shellMMInfo"); // $NON-NLS$

        switch(parameter.getTypeInfo().getTypeId()) {
            case TI_SHELL_MM:
                spsPicker.setSelectSymbolFormStyle(ShellMMInfoPicker.SelectSymbolFormStyle.SYMBOL);
                spsPicker.setShellMMTypes(ShellMMTypeUtil.getSecurityTypes());
                break;
            case TI_FOLDER:
            default:
                spsPicker.setSelectSymbolFormStyle(ShellMMInfoPicker.SelectSymbolFormStyle.FOLDER);
                spsPicker.setShellMMTypes(parameter.getTypeInfo().getFolderTypes());
                break;
        }

        final SpsBoundWidget<ShellMMInfoPicker, SpsLeafProperty> spsWidget = SimpleStandaloneEngine.configureSpsWidget(spsPicker, parameter.getTypeInfo());

        final ShellMMInfo shellMMInfoValue;
        if (label != null && id != null) {
            shellMMInfoValue = new ShellMMInfo();
            shellMMInfoValue.setBezeichnung(label);
            shellMMInfoValue.setId(id);
        } else {
            shellMMInfoValue = null;
        }

        final SpsShellMMInfoPropertyParameterWidget widget = new SpsShellMMInfoPropertyParameterWidget(spsWidget, parameter);
        widget.setInitialValue(shellMMInfoValue, doNotSetDefaultIfValueIsNull);
        return widget;
    }

    private ParameterWidget createParameterWidgetNumber(final ParameterSimpleDesc parameter, String value, final boolean doNotSetDefaultIfValueIsNull) {
        final ParsedTypeInfo typeInfo = parameter.getTypeInfo();

        final SpsDecimalEdit spsDecimalEdit = new SpsDecimalEdit()
                .withCaption(parameter.getDisplayname())
                .withPercent(typeInfo.isNumberProcent())
                .withDemanded(typeInfo.isDemanded())
                .withMin(typeInfo.getMin())
                .withMax(typeInfo.getMax())
                .withVUnit(typeInfo.getVUnit())
                .withSpin(typeInfo.getNumberSpin());

        spsDecimalEdit.setBaseStyle("sps-edit"); // $NON-NLS$
        final SpsBoundWidget<DecimalBox, SpsLeafProperty> spsWidget =
                SimpleStandaloneEngine.configureSpsWidget(spsDecimalEdit, typeInfo);

        final ParameterWidget<String, ParameterSimpleDesc> pw =
                new SpsPropertyParameterWidget<ParameterSimpleDesc>(spsWidget, parameter) {
                    @Override
                    public String getDefaultValue() {
                        final String defaultValue = asString(this.parameter.getDefaultValue());
                        if(StringUtil.hasText(defaultValue)) {
                            return String.valueOf(spsWidget.getWidget().applyScale(new BigDecimal(defaultValue)));
                        }
                        return defaultValue;
                    }
                };
        pw.setInitialValue(value, doNotSetDefaultIfValueIsNull);
        return pw;
    }

    private ParameterWidget createParameterWidgetDate(final ParameterSimpleDesc parameter, final String value, final boolean doNotSetDefaultIfValueIsNull) {
        final ParsedTypeInfo pti = parameter.getTypeInfo();
        final SpsDateTimeEdit spsDateTimeEdit = new SpsDateTimeEdit()
                .withDateKind(pti.getDateKind())
                .withSeconds(pti.isIsTimeSeconds())
                .withCaption(toCaption(parameter.getDisplayname()));

        final SpsBoundWidget<Widget, SpsLeafProperty> spsWidget =
                SimpleStandaloneEngine.configureSpsWidget(spsDateTimeEdit, pti);

        final ParameterWidget<String, ParameterSimpleDesc> pw =
                new SpsPropertyParameterWidget<ParameterSimpleDesc>(spsWidget, parameter) {
                    @Override
                    public String getValue() {
                        return asString(this.spsProperty.getDate());
                    }

                    @Override
                    public void setValue(String value) {
                        final MMDateTime diDateTime = new MMDateTime();
                        diDateTime.setValue(value);
                        this.spsProperty.setValue(diDateTime);
                    }

                    @Override
                    public String getDefaultValue() {
                        return asString(this.parameter.getDefaultValue());
                    }

                    protected boolean equals(String o1, String o2) {
                        return CompareUtil.equals(o1, o2);
                    }
                };
        pw.setInitialValue(value, doNotSetDefaultIfValueIsNull);
        return pw;
    }

    private ParameterWidget createParameterWidgetString(final ParameterSimpleDesc parameter, final String value, final boolean doNotSetDefaultIfValueIsNull) {
        final ParsedTypeInfo pti = parameter.getTypeInfo();

        final SpsEdit spsEdit = new SpsEdit()
                .withCaption(toCaption(parameter.getDisplayname()))
                .withMaxLength(pti.getMemoCharacterLimit());
        spsEdit.setBaseStyle("sps-edit");  // $NON-NLS$

        final SpsBoundWidget<TextBox, SpsLeafProperty> spsWidget = SimpleStandaloneEngine.configureSpsWidget(spsEdit, pti);

        final ParameterWidget<String, ParameterSimpleDesc> pw =
                new SpsPropertyParameterWidget<ParameterSimpleDesc>(spsWidget, parameter) {
                    @Override
                    public String getDefaultValue() {
                        return asString(this.parameter.getDefaultValue());
                    }
                };
        pw.setInitialValue(value, doNotSetDefaultIfValueIsNull);
        return pw;
    }

    private ParameterWidget createParameterWidgetBoolean(final ParameterSimpleDesc parameter, final String value, final boolean doNotSetDefaultIfValueIsNull) {
        final ParsedTypeInfo pti = parameter.getTypeInfo();

        final SpsBoundWidget<SpsCheckBox.DecoratedCheckBox, SpsLeafProperty> spsWidget
                = SimpleStandaloneEngine.configureSpsWidget(new SpsCheckBox()
                .withCaption(toCaption(parameter.getDisplayname()))
                .withThreeValueBoolean(!pti.isBooleanIsKindOption()), pti);

        final ParameterWidget<String, ParameterSimpleDesc> pw =
                new SpsPropertyParameterWidget<ParameterSimpleDesc>(spsWidget, parameter) {
                    @Override
                    public String getDefaultValue() {
                        final Boolean aBoolean = asBoolean(this.parameter.getDefaultValue());
                        return aBoolean != null ? aBoolean.toString() : null;
                    }
                };

        pw.setInitialValue(value, doNotSetDefaultIfValueIsNull);
        return pw;
    }

    private ParameterWidget createParameterWidgetEnum(final ParameterEnumDesc parameter, final String value, final boolean doNotSetDefaultIfValueIsNull) {
        final ParsedTypeInfo pti = parameter.getTypeInfo();

        final Map<String, String> enumMap = SpsUtil.createEnumMap(pti.getEnumElements());

        Firebug.debug("<AnalysisMetadataForm.createParameterWidgetEnum> enumerationNullValue=" + pti.getEnumerationNullValue());

        final SpsCombo spsCombo = new SpsCombo(enumMap, pti.getEnumerationNullValue())
                .withCaption(toCaption(parameter.getDisplayname()));
        spsCombo.setMandatory(pti.isDemanded());
        spsCombo.setStyle("combo"); // $NON-NLS$
        final SpsBoundWidget<SelectButton, SpsLeafProperty> spsWidget =
                SimpleStandaloneEngine.configureSpsWidget(spsCombo, pti);

        final ParameterWidget<String, ParameterEnumDesc> pw =
                new SpsPropertyParameterWidget<ParameterEnumDesc>(spsWidget, parameter) {
                    @Override
                    public String getDefaultValue() {
                        final MM mm = this.parameter.getDefaultValue();
                        if (mm == null) {
                            if(StringUtil.hasText(pti.getEnumerationNullValue())) {
                                Firebug.debug("getEnumerationNullValue: " + pti.getEnumerationNullValue() + " defaultValue=" + MmTalkHelper.toLogString(mm));
                                return pti.getEnumerationNullValue();
                            }
                            return null;
                        }
                        return MmTalkHelper.asCode(mm);
                    }
                };
        pw.setInitialValue(value, doNotSetDefaultIfValueIsNull);
        return pw;
    }

    public HashMap<String, String> getLayoutParameters() {
        final HashMap<String, String> map = new HashMap<>();
        for (ParameterWidget parameterWidget : this.parameterWidgets) {
            parameterWidget.addLayoutParameter(map);
        }
        Firebug.logAsGroup("<AnalysisMetadataForm.getLayoutParameters>", map); // $NON-NLS$
        return map;
    }

    public void resetToDefaults() {
        for (ParameterWidget parameterWidget : this.parameterWidgets) {
            parameterWidget.resetToDefault();
        }
    }

    private static boolean equals(ShellMMInfo s1, ShellMMInfo s2) {
        return s1 == s2 || (s1 != null && s2 != null) && CompareUtil.equals(s1.getId(), s2.getId());
    }
}