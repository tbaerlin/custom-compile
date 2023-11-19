/*
 * OrderValidationMessageView.java
 *
 * Created on 18.01.13 10:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.DialogButton;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ScrollPanel;
import de.marketmaker.iview.pmxml.OrderValidationServerityType;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.ValidationMessage;

import java.util.List;

/**
 * @author Markus Dick
 */
class OrderValidationMessageView implements OrderValidationMessageDisplay {
    private OrderValidationMessageDisplay.Presenter presenter;
    private final DialogIfc dialog;
    private List<ValidationMessage> messages;
    private final DialogButton okButton;
    private FlowPanel content;

    public OrderValidationMessageView() {
        this.content = new FlowPanel();
        final ScrollPanel scrollPanel = new ScrollPanel(this.content);
        scrollPanel.setWidth("400px");  // $NON-NLS$
        scrollPanel.setHeight("250px");  // $NON-NLS$

        this.dialog = Dialog.getImpl().createDialog()
                .withStyle("as-oe-dlg")   // $NON-NLS$
                .withTitle(I18n.I.orderEntryOrderValidationMessages())
                .withWidget(scrollPanel);

        this.okButton = this.dialog.addDefaultButton(I18n.I.proceed(), new Command() {
            @Override
            public void execute() {
                OrderValidationMessageView.this.dialog.keepOpen();
                OrderValidationMessageView.this.presenter.onOkButtonClicked();
            }
        });

        final Command cancelCommand = new Command() {
            @Override
            public void execute() {
                OrderValidationMessageView.this.dialog.keepOpen();
                OrderValidationMessageView.this.presenter.onCancelButtonClicked();
            }
        };
        this.dialog.withButton(I18n.I.cancel(), cancelCommand);

        this.dialog.withEscapeCommand(cancelCommand);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show() {
        this.content.clear();
        for(int i = 0; i < this.messages.size(); i++) {
            this.content.add(new Message(i, this.messages.get(i)));
        }

        this.dialog.show();
    }

    @Override
    public void hide() {
        this.dialog.closePopup();
    }

    @Override
    public void setMessages(List<ValidationMessage> messages) {
        this.messages = messages;
    }

    @Override
    public void setOkButtonVisible(boolean visible) {
        this.okButton.setVisible(visible);
    }

    private class Message extends Composite {
        private static final String MESSAGE_ANSWER_RADIO_GROUP = "messageAnswer";  //$NON-NLS$
        private final int index;
        private ThreeValueBoolean answer = null;

        Message(final int index, final ValidationMessage message) {
            this.index = index;

            final FlexTable layout = new FlexTable();
            layout.setCellSpacing(3);

            final FlexTable.FlexCellFormatter formatter = layout.getFlexCellFormatter();

            layout.setWidget(0, 0, createIconImage(message));
            formatter.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
            formatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);

            layout.setWidget(0, 1, new Label(message.getMsg()));
            formatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);
            formatter.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);

            if(OrderValidationServerityType.VST_QUESTION.equals(message.getServerity())) {
                layout.setWidget(1, 1, createAnswerWidgets(index, message));
                formatter.setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_MIDDLE);
                formatter.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_LEFT);
            }

            initWidget(layout);
        }

        private Image createIconImage(ValidationMessage message) {
            final String name;
            switch(message.getServerity()) {
                case VST_QUESTION:
                    name="dialog-question"; //$NON-NLS$
                    break;
                case VST_WARNING:
                    name="dialog-warning"; //$NON-NLS$
                    break;
                case VST_ERROR:
                    name="dialog-error"; //$NON-NLS$
                    break;
                case VST_INFO:
                default:
                    name="dialog-info"; //$NON-NLS$
                    break;
            }
            return IconImage.get(name).createImage();
        }

        private Widget createAnswerWidgets(int index, ValidationMessage message) {
            final HorizontalPanel buttonPanel = new HorizontalPanel();
            buttonPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
            buttonPanel.setSpacing(3);

            final ThreeValueBoolean answer = message.getAnswer();
            final String radioGroup = MESSAGE_ANSWER_RADIO_GROUP + index;

            final RadioButton buttonNull = new RadioButton(radioGroup);
            if(answer == null) {
                buttonNull.setText(I18n.I.orderEntryOrderValidationMessageNullAnswer());
                buttonNull.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        if(Boolean.TRUE.equals(event.getValue())) {
                            onAnswerSelected(null);
                        }
                    }
                });
                buttonPanel.add(buttonNull);
            }
            buttonNull.setVisible(false);

            final RadioButton buttonFalse = new RadioButton(radioGroup);
            buttonFalse.setText(I18n.I.no());
            buttonFalse.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    if(Boolean.TRUE.equals(event.getValue())) {
                        onAnswerSelected(ThreeValueBoolean.TV_FALSE);
                    }
                }
            });
            buttonPanel.add(buttonFalse);

            final RadioButton buttonTrue = new RadioButton(radioGroup);
            buttonTrue.setText(I18n.I.yes());
            buttonTrue.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    if(Boolean.TRUE.equals(event.getValue())) {
                        onAnswerSelected(ThreeValueBoolean.TV_TRUE);
                    }
                }
            });
            buttonPanel.add(buttonTrue);

            //Init selected button
            if(answer == null) {
                buttonNull.setValue(true, false);
                return buttonPanel;
            }

            switch(answer) {
                case TV_TRUE:
                    buttonTrue.setValue(true, false);
                    break;
                case TV_FALSE:
                    buttonFalse.setValue(true, false);
                    break;
                case TV_NULL:
                default:
                    buttonNull.setValue(true, false);
            }

            return buttonPanel;
        }

        private void onAnswerSelected(ThreeValueBoolean answer) {
            this.answer = answer;
            OrderValidationMessageView.this.presenter.onAnswer(this.index, this.answer);
        }

        public int getIndex() {
            return index;
        }
    }
}
