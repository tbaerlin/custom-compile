/*
 * SpsAttachmentsWidget.java
 *
 * Created on 01.09.2014 11:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.DefaultFocusKeyHandler;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.MultiWidgetFocusSupport;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.attachments.FileUploadResult;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.pmxml.AsyncGetDocumentListRequest;
import de.marketmaker.iview.pmxml.AsyncGetDocumentListResponse;
import de.marketmaker.iview.pmxml.AsyncListItem;
import de.marketmaker.iview.pmxml.DeleteRequest;
import de.marketmaker.iview.pmxml.DocumentMetadata;
import de.marketmaker.iview.pmxml.DocumentOrigin;
import de.marketmaker.iview.pmxml.Handle;
import de.marketmaker.iview.pmxml.VoidResponse;
import de.marketmaker.iview.pmxml.internaltypes.AttachmentUploadState;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author mdick
 */
public class SpsAttachmentsWidget extends SpsBoundWidget<FlowPanel, SpsGroupProperty> implements AsyncCallback<ResponseType>, HasFocusHandlers, HasBlurHandlers {
    private static final Comparator<AsyncListItem> ASYNC_LIST_ITEM_COMPARATOR = new Comparator<AsyncListItem>() {
        @Override
        public int compare(AsyncListItem o1, AsyncListItem o2) {
            if (o1 == o2 || o1 == null || o2 == null || o1.getDocMetaData() == null || o2.getDocMetaData() == null ||
                    o1.getDocMetaData().getDocumentName() == null) {
                return 0;
            }
            return o1.getDocMetaData().getDocumentName().compareToIgnoreCase(o2.getDocMetaData().getDocumentName());
        }
    };

    private final FlowPanel layout = new FlowPanel();
    private final FormPanel formPanel = new FormPanel();
    private final FileUpload hiddenFileUpload;
    private final Hidden hiddenActivityInstanceId = new Hidden();
    private final Hidden hiddenTaskId = new Hidden();
    private final FlexTable attachmentsTable = new FlexTable();

    private final DmxmlContext dmxmlContext = new DmxmlContext();

    private final DmxmlContext.Block<AsyncGetDocumentListResponse> listBlock =
            this.dmxmlContext.addBlock("PM_AsyncGetDocumentList");  // $NON-NLS$
    private final AsyncGetDocumentListRequest listRequest;

    private final DmxmlContext.Block<VoidResponse> deleteBlock =
            this.dmxmlContext.addBlock("PM_AsyncDelete");  // $NON-NLS$
    private final DeleteRequest deleteRequest = new DeleteRequest();
    private final MultiWidgetFocusSupport multiWidgetFocusSupport = new MultiWidgetFocusSupport();

    public SpsAttachmentsWidget() {
        this.dmxmlContext.setCancellable(false);
        this.listRequest = createAsyncGetDocumentRequest();
        this.listBlock.setParameter(this.listRequest);
        this.listBlock.setEnabled(true);

        this.deleteBlock.setParameter(this.deleteRequest);
        this.deleteBlock.setEnabled(false);
        this.deleteRequest.setOrigin(DocumentOrigin.DO_ACTIVITY_ATTACHMENT);
        this.deleteRequest.setAll(false);

        this.formPanel.setMethod("POST");  // $NON-NLS$
        this.formPanel.setEncoding("multipart/form-data");  // $NON-NLS$
        this.formPanel.setAction(AbstractMainController.INSTANCE.contextPath + "/pmweb/attachmentUpload");  // $NON-NLS$
        this.formPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                SpsAttachmentsWidget.this.onSubmitComplete(event);
            }
        });

        final HorizontalPanel formPanelLayout = new HorizontalPanel();
        this.formPanel.setWidget(formPanelLayout);

        final Button fileUploadButton = Button.text(I18n.I.addActivityFileAttachment())
                .makeFocusable(false)
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        // Calling click is not HTML 5 standard!
                        // However, it is supported by IE8-11, FF, and Chrome
                        clickHiddenFileUpload();
                    }
                }).build();
        formPanelLayout.add(fileUploadButton);
        makeFocusable(fileUploadButton);
        this.multiWidgetFocusSupport.add(fileUploadButton);

        //hidden input fields on form panel
        this.hiddenFileUpload = new FileUpload();
        this.hiddenFileUpload.setName("file");  // $NON-NLS$
        this.hiddenFileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                formPanel.submit();
            }
        });
        this.hiddenFileUpload.setVisible(false);
        formPanelLayout.add(this.hiddenFileUpload);
        this.hiddenActivityInstanceId.setName("activityId");  // $NON-NLS$
        formPanelLayout.add(this.hiddenActivityInstanceId);
        this.hiddenTaskId.setName("taskId");  // $NON-NLS$
        formPanelLayout.add(this.hiddenTaskId);

        this.layout.setStyleName("sps-attachments");
        this.attachmentsTable.setStyleName("sps-attachments-table");
        this.attachmentsTable.setCellSpacing(0);
        this.attachmentsTable.setCellPadding(0);

        this.layout.add(this.formPanel);
        this.layout.add(this.attachmentsTable);
    }

    private void makeFocusable(Widget fileUploadButton) {
        // make focusable: WidgetUtil's impl. does not work here in FF, because
        // on key down the browser's file chooser dialog does not appear and
        // preventing the key down event seems to prevent also the key press event.
        // However, preventDefault must be called before the input click is handled.
        fileUploadButton.getElement().setTabIndex(0);
        fileUploadButton.addDomHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if(KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                    event.preventDefault();
                    clickHiddenFileUpload();
                }
            }
        }, KeyPressEvent.getType());
    }

    private void clickHiddenFileUpload() {
        //Calling click is not HTML 5 standard! However, it is supported by IE8-11, FF, and Chrome
        InputElement.as(this.hiddenFileUpload.getElement()).click();
    }

    private AsyncGetDocumentListRequest createAsyncGetDocumentRequest() {
        final AsyncGetDocumentListRequest request = new AsyncGetDocumentListRequest();
        request.setOrigin(DocumentOrigin.DO_ACTIVITY_ATTACHMENT);
        request.setOffset(Integer.toString(0));
        request.setCount(Integer.toString(Integer.MAX_VALUE));
        return request;
    }

    private void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
        final FileUploadResult result = JsonUtils.safeEval(event.getResults());
        final AttachmentUploadState state = result.getState();
        if(state != AttachmentUploadState.OK) {
            final SafeHtmlBuilder sb = new SafeHtmlBuilder().appendEscaped(getUploadErrorMessage(state));
            final List<String> messages = result.getMessages();
            if(!messages.isEmpty()) {
                sb.appendHtmlConstant("<br/><br/>").appendEscaped(I18n.I.systemMessage()).append(':');  // $NON-NLS$
            }
            for (String message : messages) {
                if(StringUtil.hasText(message)) {
                    sb.appendHtmlConstant("<br/>").appendEscaped(message);  // $NON-NLS$
                }
            }
            Dialog.error(sb.toSafeHtml());
        }
        refreshList();
    }

    private String getUploadErrorMessage(AttachmentUploadState state) {
        switch(state) {
            case NO_ACTIVITY_ID_ERROR:
                return I18n.I.attachmentUploadNoActivityIdError();
            case NO_FILE_ERROR:
                return I18n.I.attachmentUploadNoFileError();
            case NO_CONTENT_TYPE_ERROR:
                return I18n.I.attachmentUploadNoContentTypeError();
            case NO_FILE_NAME_ERROR:
                return I18n.I.attachmentUploadNoFileNameError();
            case NO_FILE_EXTENSION_ERROR:
                return I18n.I.attachmentUploadNoFileExtensionError();
            case TECHNICAL_FILE_SIZE_LIMIT_EXCEEDED_ERROR:
                return I18n.I.attachmentUploadTechnicalFileSizeLimitExceededError();
            case CONFIGURED_FILE_SIZE_LIMIT_EXCEEDED_ERROR:
                return I18n.I.attachmentUploadConfiguredFileSizeLimitExceededError();
            case CONTENT_TYPE_NOT_ALLOWED:
                return I18n.I.attachmentUploadContentTypeNotAllowed();
            case FILE_EXTENSION_NOT_ALLOWED:
                return I18n.I.attachmentUploadFileExtensionNotAllowed();
            case EMPTY_FILES_NOT_ALLOWED:
                return I18n.I.attachmentUploadEmptyFilesNotAllowed();
            case ERROR:
            default:
                return I18n.I.attachmentUploadError();
        }
    }

    @Override
    public void onPropertyChange() {
        //do nothing
    }

    public SpsAttachmentsWidget withActivityInstanceId(String activityInstanceId) {
        this.hiddenActivityInstanceId.setValue(activityInstanceId);
        return this;
    }

    public SpsAttachmentsWidget withActivityInstanceGuid(String activityInstanceGuid) {
        this.listRequest.setActivityInstanceGuid(activityInstanceGuid.trim());
        refreshList();
        return this;
    }

    private void refreshList() {
        this.listBlock.setToBeRequested();
        this.listBlock.setEnabled(true);
        this.deleteBlock.setEnabled(false);
        this.dmxmlContext.issueRequest(this);
    }

    public SpsAttachmentsWidget withTaskId(String taskId) {
        this.hiddenTaskId.setValue(taskId);
        return this;
    }

    @Override
    public void onFailure(Throwable caught) {
        if(this.listBlock.isEnabled()) {
            onListFailed();
            Firebug.warn("getting documents failed", caught);
        }
        else if(this.deleteBlock.isEnabled()) {
            onDeleteFailed();
            Firebug.warn("getting documents failed", caught);
        }
    }

    private void onListFailed() {
        Dialog.error(I18n.I.loadActivityFileAttachmentListFailed());
    }

    private void onDeleteFailed() {
        Dialog.error(I18n.I.deleteActivityFileAttachmentFailed());
    }

    @Override
    public void onSuccess(ResponseType response) {
        if(this.listBlock.isEnabled()) {
            onListSuccess();
        }
        else if(this.deleteBlock.isEnabled()) {
            onDeleteSuccess();
        }
    }

    private void onDeleteSuccess() {
        if(!this.deleteBlock.isResponseOk()) {
            onDeleteFailed();
            Firebug.warn("DeleteBlock response not ok!");
            return;
        }
        refreshList();
    }

    private void onListSuccess() {
        if (!this.listBlock.isResponseOk()) {
            this.attachmentsTable.clear();
            onListFailed();
            Firebug.warn("AsyncGetDocuments response not ok!");
            return;
        }

        final AsyncGetDocumentListResponse result = this.listBlock.getResult();
        int row = 0;
        final List<AsyncListItem> items = result.getItems();
        Collections.sort(items, ASYNC_LIST_ITEM_COMPARATOR);

        for (final AsyncListItem item : items) {
            addRow(row++, item);
        }
        while (this.attachmentsTable.getRowCount() - items.size() > 0) {
            this.attachmentsTable.removeRow(this.attachmentsTable.getRowCount()-1);
        }
    }

    private void addRow(int row, final AsyncListItem item) {
        final String handle = item.getTmpMetaData().getHandle();
        final DocumentMetadata docMetaData = item.getDocMetaData();
        final HorizontalPanel entry = new HorizontalPanel();
        entry.add(getLink(handle, docMetaData));
        entry.add(getDeleteButton(handle, docMetaData));

        this.attachmentsTable.setWidget(row, 0, entry);
    }

    private HTML getLink(final String handle, final DocumentMetadata docMetaData) {
        final HTML link = new HTML(new SafeHtmlBuilder()
                .append(IconImage.getImagePrototypeForFileType(docMetaData.getFileType()).getSafeHtml())
                .appendHtmlConstant("<span>")  // $NON-NLS$
                .appendEscaped(docMetaData.getDocumentName() + "." + docMetaData.getFileType())  // $NON-NLS$
                .appendHtmlConstant("</span>")  // $NON-NLS$
                .toSafeHtml());

        link.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showDocument(handle);
            }
        });
        link.setStyleName("sps-dmsLink");
        link.addStyleName("mm-link");

        WidgetUtil.makeFocusable(link, new DefaultFocusKeyHandler() {
            @Override
            public boolean onFocusKeyClick() {
                showDocument(handle);
                return true;
            }

            @Override
            public boolean onFocusDelete() {
                deleteDocument(handle, docMetaData);
                return true;
            }
        });
        this.multiWidgetFocusSupport.add(link);
        return link;
    }

    private IconImageIcon getDeleteButton(final String handle, final DocumentMetadata docMetaData) {
        final IconImageIcon deleteButton = IconImage.getIcon("sps-minus").withClickHandler(new ClickHandler() {  // $NON-NLS$
            @Override
            public void onClick(ClickEvent event) {
                deleteDocument(handle, docMetaData);
            }
        });
        deleteButton.addStyleName("sps-attachments-delete");
        WidgetUtil.makeFocusable(deleteButton, new DefaultFocusKeyHandler(){
            @Override
            public boolean onFocusDelete() {
                deleteDocument(handle, docMetaData);
                return true;
            }

            @Override
            public boolean onFocusKeyClick() {
                return onFocusDelete();
            }
        });
        this.multiWidgetFocusSupport.add(deleteButton);
        return deleteButton;
    }

    private void showDocument(String handle) {
        if (StringUtil.hasText(handle)) {
            Window.open(UrlBuilder.forPmReport("pmweb/attachment?handle=" + handle).toURL(), "_blank", "");  // $NON-NLS$
        }
    }

    private void deleteDocument(final String handle, DocumentMetadata docMetaData) {
        Dialog.confirm(I18n.I.deleteActivityFileAttachmentTitle(),
                I18n.I.deleteActivityFileAttachmentQuestion(docMetaData.getDocumentName()),
                new Command() {
                    @Override
                    public void execute() {
                        doDeleteDocument(handle);
                    }
                }
        );
    }

    private void doDeleteDocument(String handle) {
        this.listBlock.setEnabled(false);
        this.deleteBlock.setEnabled(true);
        this.deleteBlock.setToBeRequested();
        final Handle handleObj = new Handle();
        handleObj.setHandle(handle);
        this.deleteRequest.getHandles().add(handleObj);
        this.dmxmlContext.issueRequest(this);
    }

    @Override
    protected FlowPanel createWidget() {
        return this.layout;
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.multiWidgetFocusSupport.addFocusHandler(handler);
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.multiWidgetFocusSupport.addBlurHandler(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.multiWidgetFocusSupport.fireEvent(event);
    }
}
