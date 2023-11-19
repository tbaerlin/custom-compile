package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.LayoutDocumentType;

import java.util.List;

/**
 * Created on 04.03.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class ArchiveDialog {

    interface Save {
        void save(String title, String comment, String docType);
    }

    private ArchiveDialog(final Save callback, final DmsMetadata metadata, final LayoutDesc layoutDesc) {
        final Panel panel = new FlowPanel();

        final Label lbTitle = new Label(I18n.I.title());
        panel.add(lbTitle);
        final TextBox tbTitle = new TextBox();
        tbTitle.setWidth("250px"); // $NON-NLS$
        final String text = createTitleSuggestion(metadata, layoutDesc);
        tbTitle.setText(text);
        tbTitle.setSelectionRange(0, text.length());
        tbTitle.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                tbTitle.selectAll();
            }
        });
        panel.add(tbTitle);

        final Label lbDocType = new Label(I18n.I.type());
        lbDocType.getElement().getStyle().setMarginTop(6, Style.Unit.PX);
        panel.add(lbDocType);
        //TODO: replace ListBox by SelectButton
        final ListBox boxDocTypes = createDocTypesBox(findDocumentTypeName(layoutDesc));
        boxDocTypes.setWidth("250px"); // $NON-NLS$
        panel.add(boxDocTypes);

        final Label lbComment = new Label(I18n.I.comment());
        lbComment.getElement().getStyle().setMarginTop(6, Style.Unit.PX);
        lbComment.getElement().getStyle().setMarginBottom(3, Style.Unit.PX);
        panel.add(lbComment);
        final TextArea ta = new TextArea();
        ta.setWidth("250px"); // $NON-NLS$
        ta.setHeight("200px"); // $NON-NLS$
        panel.add(ta);

        Dialog.getImpl().createDialog()
                .withTitle(I18n.I.dmsArchiveDocument())
                .withWidget(panel)
                .withFocusWidget(tbTitle)
                .withDefaultButton(I18n.I.ok(), new Command() {
                    @Override
                    public void execute() {
                        callback.save(tbTitle.getValue(), ta.getValue(), boxDocTypes.getItemText(boxDocTypes.getSelectedIndex()));
                    }
                })
                .withButton(I18n.I.cancel())
                .show();
    }

    private String findDocumentTypeName(LayoutDesc layoutDesc) {
        final List<LayoutDocumentType> documentTypes = PmWebSupport.getInstance().getGlobalLayoutMetadata().getDocumentTypes();
        final String id = layoutDesc.getLayout().getDocumentTypeId();
        for (LayoutDocumentType documentType : documentTypes) {
            if (id.equals(documentType.getId())) {
                return documentType.getName();
            }
        }
        return null;
    }

    private ListBox createDocTypesBox(String defaultValue) {
        final ListBox box = new ListBox();
        box.addItem("");
        int selectedIdx = -1;
        final List<LayoutDocumentType> documentTypes = PmWebSupport.getInstance().getGlobalLayoutMetadata().getDocumentTypes();
        for (int i = 0, documentTypesSize = documentTypes.size(); i < documentTypesSize; i++) {
            final LayoutDocumentType documentType = documentTypes.get(i);
            box.addItem(documentType.getName());
            if (documentType.getName().equals(defaultValue)) {
                selectedIdx = i;
            }
        }
        if (selectedIdx > -1) {
            box.setSelectedIndex(selectedIdx);
        }
        return box;
    }

    private String createTitleSuggestion(DmsMetadata metadata, LayoutDesc layoutDesc) {
        return metadata.getName() + " " + layoutDesc.getLayout().getLayoutName();
    }

    public static void show(Save callback, DmsMetadata metadata, LayoutDesc layoutDesc) {
        new ArchiveDialog(callback, metadata, layoutDesc);
    }
}