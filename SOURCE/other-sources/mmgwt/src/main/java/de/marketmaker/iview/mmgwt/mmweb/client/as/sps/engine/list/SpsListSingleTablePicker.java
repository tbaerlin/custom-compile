/*
 * SpsListSingleTablePicker.java
 *
 * Created on 22.05.2015 10:28
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
 * @author mdick
 */
@NonNLS
public class SpsListSingleTablePicker<P extends SpsProperty> extends AbstractSpsListSingleTable<Button, P> implements PopupTableSelectionHelper.Callback, HasFocusHandlers, HasBlurHandlers {
    private String iconMappingName = "pm-icon-16";

    public SpsListSingleTablePicker(Context context, BindToken parentToken, BindToken itemsBindToken, String columnsKeyField, List<ListWidgetDescColumn> columns, String iconMappingName) {
        super(context, parentToken, itemsBindToken, columnsKeyField, columns);
        if(StringUtil.hasText(iconMappingName)) {
            this.iconMappingName = iconMappingName;
        }
    }

    @Override
    protected Button createWidget() {
        final Button button = new PickerButton(new Command() {
            @Override
            public void execute() {
                showList();
            }
        }).withIcon(this.iconMappingName)
                .withClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        showList();
                    }
                });

        initFocusSupport(button);

        button.setEnabled(!isReadonly());

        return button;
    }

    @Override
    public void onPropertyChange() {
        //nothing to do here! we are only setting the value but do not show the selected value.
    }

    public static class PickerButton extends Button {
        private final Command rightContentClicked;

        public PickerButton(Command rightContentClicked) {
            super(false);
            this.rightContentClicked = rightContentClicked;
        }

        @Override
        protected void fireClickEvent(ClickEvent event) {
            if (!isEnabled()) {
                return;
            }
            if (event.getX() > getRightContentX() + 2) {
                if(rightContentClicked != null) {
                    rightContentClicked.execute();
                }
            }
            else if (event.getX() < getRightContentX()) {
                super.fireClickEvent(event);
            }
        }

        @Override
        protected Widget createRightContentWidget() {
            final Image image = new Image("clear.cache.gif");
            image.setStyleName("menu-trigger");
            return image;
        }
    }
}
