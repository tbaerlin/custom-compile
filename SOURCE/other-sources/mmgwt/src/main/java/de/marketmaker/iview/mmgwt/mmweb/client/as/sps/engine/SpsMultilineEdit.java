package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.user.client.ui.TextArea;
import de.marketmaker.itools.gwtutil.client.widgets.input.LimitedTextArea;

/**
 * Author: umaurer
 * Created: 14.01.14
 */
public class SpsMultilineEdit extends SpsEditBase<SpsMultilineEdit, TextArea> implements HasCaption, RequiresPropertyUpdateBeforeSave, HasEditWidget {
    public SpsMultilineEdit() {
    }

    @Override
    public SpsMultilineEdit withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    @Override
    protected TextArea createGwtWidget() {
        final int maxLength = getMaxLength();
        return maxLength > 0 ? new LimitedTextArea(maxLength) : new TextArea();
    }

    @Override
    public String getStringValue() {
        return TextUtil.trimMultilineEnds(getWidget().getValue());
    }

    public void setValue(String value, boolean fireEvent) {
        super.setValue(value == null
                ? ""
                : TextUtil.trimMultilineEnds(value), fireEvent);
    }
}
