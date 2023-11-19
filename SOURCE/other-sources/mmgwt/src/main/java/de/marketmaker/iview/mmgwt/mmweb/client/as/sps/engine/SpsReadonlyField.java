package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.input.ValidatingBigDecimalBox;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmPlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.MainInput;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.TiType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Author: umaurer
 * Created: 11.04.14
 */
public class SpsReadonlyField extends SpsBoundWidget<HTML, SpsLeafProperty> implements NoValidationPopup, HasStaleDataIndicator {
    private final HTML label = new HTML();
    private final ValueType valueType;
    private int scale = -1;
    private boolean percent = false;
    private Map<String, String> mapEnumDescriptions;
    private String enumNullValue;
    private boolean renderTrailingZeros;

    private ShellMMInfoAttribute shellAttribute;
    private HandlerRegistration shellClickHandlerRegistration;

    private Supplier<HistoryContext> historyContextSupplier;
    private MainInput mainInput = null;

    public enum ValueType {
        TEXT, DECIMAL, PERCENT, ENUM, CHECK, DATE, PRE, SHELL
    }

    public enum ShellMMInfoAttribute {
        DESCRIPTION("description"), NUMBER("number"), TYPE("type"), ISIN("isin"), ISIN_NUMBER("isin-number"); // $NON-NLS$

        private final String style;

        ShellMMInfoAttribute(String style) {
            this.style = style;
        }

        public String getStyle() {
            return style;
        }
    }

    public SpsReadonlyField(ValueType valueType) {
        Tooltip.addAutoCompletion(this.label);

        this.valueType = valueType;
        this.label.setStyleName("sps-ro-field");
        switch (this.valueType) {
            case CHECK:
                this.label.addStyleName("sps-ro-check");
                break;
            case PRE:
                this.label.addStyleName("sps-ro-pre");
                break;
            case PERCENT:
                this.percent = true;
                // falls through
            case DECIMAL:
                this.label.addStyleName("sps-ro-decimal");
                break;
            case SHELL:
                Tooltip.addAutoCompletion(this.label);
                // falls through
            default:
                this.label.addStyleName("sps-ro-default");
                break;
        }
    }

    public SpsReadonlyField withMainInput(MainInput mainInput) {
        this.mainInput = mainInput;
        return this;
    }

    public SpsReadonlyField withVUnit(String vUnit) {
        this.scale = vUnit == null || "0".equals(vUnit) ? -1 : vUnit.length() - 1; // $NON-NLS$
        return this;
    }

    public SpsReadonlyField withEnumDescriptions(String enumNullValue, Map<String, String> mapEnumDescriptions) {
        this.enumNullValue = enumNullValue;
        this.mapEnumDescriptions = mapEnumDescriptions;
        return this;
    }

    public SpsReadonlyField withShellMMInfoAttribute(ShellMMInfoAttribute attribute) {
        this.shellAttribute = attribute;
        this.label.addStyleName(this.shellAttribute.getStyle());
        return this;
    }

    public SpsReadonlyField withShellMMInfoLink(final String historyContextName) {
        this.historyContextSupplier = () -> SpsUtil.extractShellMMInfoHistoryContext(historyContextName, getBindFeature().getSpsProperty());
        return this;
    }

    public SpsReadonlyField withRenderTrailingZeros(boolean renderTrailingZeros) {
        this.renderTrailingZeros = renderTrailingZeros;
        return this;
    }

    @Override
    public void onPropertyChange() {
        switch (this.valueType) {
            case TEXT:
            case DATE:
                this.label.setText(getBindFeature().getSpsProperty().getStringValue());
                break;
            case DECIMAL:
            case PERCENT:
                setDecimalValue(getBindFeature().getSpsProperty().getStringValue());
                break;
            case ENUM:
                setEnumValue(getBindFeature().getSpsProperty().getStringValue());
                break;
            case CHECK:
                setCheckValue(getBindFeature().getSpsProperty().getStringValue());
                break;
            case PRE:
                this.label.setText(getBindFeature().getSpsProperty().getStringValue());
                break;
            case SHELL:
                doPropertyChangeForShellMMInfo(getBindFeature().getSpsProperty().getShellMMInfo());
                break;
        }
    }

    private void indicateStaleData() {
        switch (this.valueType) {
            case TEXT:
            case DATE:
                this.label.setText(null);
                break;
            case DECIMAL:
            case PERCENT:
                setDecimalValue(null);
                break;
            case ENUM:
                setEnumValue(null);
                break;
            case CHECK:
                setCheckValue(null);
                break;
            case PRE:
                this.label.setText(null);
                break;
            case SHELL:
                doPropertyChangeForShellMMInfo(null);
                break;
        }
    }

    private void doPropertyChangeForShellMMInfo(final ShellMMInfo value) {
        this.label.setText(getAttributeValue(this.shellAttribute, value));
        if(this.historyContextSupplier != null && ShellMMInfoAttribute.TYPE != this.shellAttribute) {
            if(this.shellClickHandlerRegistration != null) {
                this.shellClickHandlerRegistration.removeHandler();
            }

            if(canGoTo(value)) {
                this.label.addStyleName("mm-link");
                this.shellClickHandlerRegistration = this.label.addClickHandler(
                        event -> SpsReadonlyField.this.onClick(value));
            }
            else {
                this.label.removeStyleName("mm-link");  // $NON-NLS$
            }
        }
    }

    private boolean canGoTo(ShellMMInfo value) {
        if (this.mainInput == null) {
            return PmPlaceUtil.canGoTo(value);
        }
        final ShellMMInfo shellMMInfo = this.mainInput.asShellMMInfo();
        return shellMMInfo != null
                && !(MmTalkHelper.isSameShellMMType(shellMMInfo, value) && MmTalkHelper.equals(TiType.TI_SHELL_MM, shellMMInfo, value))
                && PmPlaceUtil.canGoTo(value);
    }

    private void onClick(ShellMMInfo value) {
        if(this.historyContextSupplier == null) {
            return;
        }
        PmPlaceUtil.goTo(value, this.historyContextSupplier.get());
    }

    private String getAttributeValue(ShellMMInfoAttribute attribute, ShellMMInfo info) {
        if(info == null) {
            return null;
        }
        switch(attribute) {
            case ISIN:
                return info.getISIN();
            case TYPE:
                return PmRenderers.SHELL_MM_TYPE.render(info.getTyp());
            case NUMBER:
                return info.getNumber();
            case ISIN_NUMBER:
                final String isin = info.getISIN();
                final String wkn = info.getNumber();
                String text = "";
                if(StringUtil.hasText(isin)) {
                    text += isin + " / ";  // $NON-NLS$
                }
                if(StringUtil.hasText(wkn)) {
                    text += wkn;
                }
                return text;
            case DESCRIPTION:
            default:
                return info.getBezeichnung();
        }
    }

    private void setDecimalValue(String value) {
        if (!StringUtil.hasText(value)) {
            this.label.setHTML(TextUtil.NO_SELECTION_VALUE);
            return;
        }
        BigDecimal bd = new BigDecimal(value);
        if (this.scale != -1) {
            bd = bd.setScale(this.scale, RoundingMode.HALF_UP);
        }
        if (this.percent) {
            bd = bd.movePointRight(2);
        }
        final String rendered = this.renderTrailingZeros
                ? ValidatingBigDecimalBox.BigDecimalRenderer.instanceTrailingZeros().render(bd)
                : ValidatingBigDecimalBox.BigDecimalRenderer.instance().render(bd);
        this.label.setText(this.percent ? rendered + "%" : rendered);
    }

    private void setEnumValue(String value) {
        if (!StringUtil.hasText(value) || (StringUtil.hasText(this.enumNullValue) && this.enumNullValue.equals(value))) {
            this.label.setHTML(TextUtil.NO_SELECTION_TEXT);
            return;
        }
        String rendered = null;
        if (this.mapEnumDescriptions != null) {
            rendered = this.mapEnumDescriptions.get(value);
        }
        this.label.setText(rendered == null ? value : rendered);
    }

    private void setCheckValue(String value) {
        if (StringUtil.hasText(value)) {
            this.label.getElement().setAttribute("checked", value); // $NON-NLS$
        }
        else {
            this.label.getElement().removeAttribute("checked"); // $NON-NLS$
        }
    }

    @Override
    protected void onWidgetConfigured() {
        if (this.valueType != ValueType.PRE) {
            forceCaptionWidget();
        }
        super.onWidgetConfigured();
    }

    @Override
    protected HTML createWidget() {
        return this.label;
    }

    @Override
    public void setStaleDataIndicator(boolean staleData) {
        if(staleData) {
            indicateStaleData();
        }
    }
}
