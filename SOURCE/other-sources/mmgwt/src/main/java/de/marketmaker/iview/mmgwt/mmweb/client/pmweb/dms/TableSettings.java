package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.itools.gwtutil.client.widgets.input.CheckBox;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.pmxml.LayoutDocumentType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 24.04.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class TableSettings implements IsWidget {
    private final FlowPanel panel;
    private final List<Image> resetWidgets = new ArrayList<>();

    public TableSettings(final DmsDisplay.Presenter.Config config, final DmsDisplay.Presenter.Config defaultConfig) {
        this.panel = new FlowPanel();
        this.panel.getElement().getStyle().setProperty("maxHeight", "400px"); // $NON-NLS$
        this.panel.getElement().getStyle().setOverflowY(Style.Overflow.AUTO);
        this.panel.setStyleName("mm-form"); // $NON-NLS$
        this.panel.addStyleName("sps-taskView");  // $NON-NLS$

        final FlexTable table = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        int row = -1;

        // from ////////////////////////////////////////////////////////////////////////////////////////////////////////
        final HTML fromLabel = createLabel(I18n.I.fromUpperCase());
        table.setWidget(++row, 0, fromLabel);
        formatter.setStyleName(row, 0, "sps-form-label");

        final DateBox fromBox = DateBox.factory().withDate(config.dateFrom)
                .withAllowNull().withIconWidet(IconImage.getIcon("sps-calendar")) // $NON-NLS$
                .build();
        final Image fromResetWidget = createResetWidget(new ClickHandler() {
            public void onClick(ClickEvent event) {
                fromBox.setDate(defaultConfig.dateFrom);
            }
        });
        fromResetWidget.setVisible(!config.dateFrom.equals(defaultConfig.dateFrom));

        fromBox.addValueChangeHandler(new ValueChangeHandler<MmJsDate>() {
            @Override
            public void onValueChange(ValueChangeEvent<MmJsDate> valueChangeEvent) {
                config.dateFrom = valueChangeEvent.getValue();
                fromResetWidget.setVisible(!config.dateFrom.equals(defaultConfig.dateFrom));
            }
        });
        table.setWidget(row, 1, fromBox);
        table.setWidget(row, 2, fromResetWidget);

        // to //////////////////////////////////////////////////////////////////////////////////////////////////////////
        final HTML toLabel = createLabel(I18n.I.toUpperCase());
        table.setWidget(++row, 0, toLabel);
        formatter.setStyleName(row, 0, "sps-form-label");

        final DateBox toBox = DateBox.factory().withDate(config.dateTo)
                .withAllowNull().withIconWidet(IconImage.getIcon("sps-calendar")) // $NON-NLS$
                .build();
        final Image toResetWidget = createResetWidget(new ClickHandler() {
            public void onClick(ClickEvent event) {
                toBox.setDate(defaultConfig.dateTo);
            }
        });
        toResetWidget.setVisible(!config.dateTo.equals(defaultConfig.dateTo));

        toBox.addValueChangeHandler(new ValueChangeHandler<MmJsDate>() {
            @Override
            public void onValueChange(ValueChangeEvent<MmJsDate> valueChangeEvent) {
                config.dateTo = valueChangeEvent.getValue();
                toResetWidget.setVisible(!config.dateTo.equals(defaultConfig.dateTo));
            }
        });
        table.setWidget(row, 1, toBox);
        table.setWidget(row, 2, toResetWidget);

        // Name ////////////////////////////////////////////////////////////////////////////////////////////////////////
        final HTML nameLabel = createLabel(I18n.I.name());
        table.setWidget(++row, 0, nameLabel);
        formatter.setStyleName(row, 0, "sps-form-label");

        final TextBox nameBox = new TextBox();
        nameBox.setStyleName("sps-edit");
        nameBox.setValue(config.name);
        final Image nameResetWidget = createResetWidget(new ClickHandler() {
            public void onClick(ClickEvent event) {
                nameBox.setValue(defaultConfig.name, true);
            }
        });
        nameResetWidget.setVisible(!config.name.equals(defaultConfig.name));
        nameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> valueChangeEvent) {
                config.name = valueChangeEvent.getValue();
                nameResetWidget.setVisible(!config.name.equals(defaultConfig.name));
            }
        });
        table.setWidget(row, 1, nameBox);
        table.setWidget(row, 2, nameResetWidget);

        // Comment /////////////////////////////////////////////////////////////////////////////////////////////////////
        final HTML commentLabel = createLabel(I18n.I.comment());
        table.setWidget(++row, 0, commentLabel);
        formatter.setStyleName(row, 0, "sps-form-label");

        final TextBox commentBox = new TextBox();
        commentBox.setStyleName("sps-edit");
        commentBox.setValue(config.comment);
        final Image commentResetWidget = createResetWidget(new ClickHandler() {
            public void onClick(ClickEvent event) {
                commentBox.setValue(defaultConfig.comment, true);
            }
        });
        commentResetWidget.setVisible(!config.comment.equals(defaultConfig.comment));

        commentBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> valueChangeEvent) {
                config.comment = valueChangeEvent.getValue();
                commentResetWidget.setVisible(!config.comment.equals(defaultConfig.comment));
            }
        });
        table.setWidget(row, 1, commentBox);
        table.setWidget(row, 2, commentResetWidget);

        createCheckBoxes(table, config, defaultConfig);

        this.panel.add(table);
    }

    private void createCheckBoxes(FlexTable table, final DmsDisplay.Presenter.Config config, final DmsDisplay.Presenter.Config defaultConfig) {
        final List<LayoutDocumentType> documentTypes = PmWebSupport.getInstance().getGlobalLayoutMetadata().getDocumentTypes();

        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        int row = table.getRowCount();

        for (LayoutDocumentType documentType : documentTypes) {
            final String docType = documentType.getName();
            final HTML label = createLabel(docType);
            table.setWidget(++row, 0, label);
            formatter.setStyleName(row, 0, "sps-form-label");

            final CheckBox cb = new CheckBox(config.documentTypes.contains(docType));
            final Image resetWidget = createResetWidget(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    cb.setChecked(defaultConfig.documentTypes.contains(docType), true);
                }
            });
            resetWidget.setVisible(defaultConfig.documentTypes.contains(docType) && !config.documentTypes.contains(docType)
                    || !defaultConfig.documentTypes.contains(docType) && config.documentTypes.contains(docType));

            cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {
                    if (config.documentTypes.contains(docType)) {
                        config.documentTypes.remove(docType);
                    }
                    else {
                        config.documentTypes.add(docType);
                    }
                    resetWidget.setVisible(defaultConfig.documentTypes.contains(docType) && !config.documentTypes.contains(docType)
                            || !defaultConfig.documentTypes.contains(docType) && config.documentTypes.contains(docType));
                }
            });
            table.setWidget(row, 1, cb);
            table.setWidget(row, 2, resetWidget);
        }
    }

    private HTML createLabel(String text) {
        final HTML label = new HTML(text);
        label.addStyleName("sps-caption");
        return label;
    }

    private Image createResetWidget(ClickHandler clickHandler) {
        final Image resetWidget = IconImage.get("mm-reset-icon").createImage();// $NON-NLS$
        resetWidget.addStyleName("mm-pointer");
        resetWidget.addClickHandler(clickHandler);
        this.resetWidgets.add(resetWidget);
        return resetWidget;
    }

    public void reset() {
        for (Image resetWidget : resetWidgets) {
            resetWidget.fireEvent(new GwtEvent<ClickHandler>() {
                @Override
                public DomEvent.Type<ClickHandler> getAssociatedType() {
                    return ClickEvent.getType();
                }

                @Override
                protected void dispatch(ClickHandler clickHandler) {
                    clickHandler.onClick(null);
                }
            });
        }
    }

    @Override
    public Widget asWidget() {
        return this.panel;
    }
}