/*
 * AttachmentUploadHandler.java
 *
 * Created on 01.09.2014 17:36
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.ActivityInstanceRequest;
import de.marketmaker.iview.pmxml.ActivityInstanceResponse;
import de.marketmaker.iview.pmxml.AsyncStoreDocumentRequest;
import de.marketmaker.iview.pmxml.DocumentMetadata;
import de.marketmaker.iview.pmxml.DocumentOrigin;
import de.marketmaker.iview.pmxml.VoidResponse;
import de.marketmaker.iview.pmxml.internaltypes.AttachmentUploadState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static de.marketmaker.iview.pmxml.internaltypes.AttachmentUploadState.*;

/**
 * @author mdick
 */
public class AttachmentUploadHandler implements HttpRequestHandler {
    private static final String FUNCTION_KEY = "Async_StoreDocument";
    private static final String FUNCTION_KEY_ACTIVITIES_GET_INSTANCE = "Activities_GetInstance";

    protected final Log logger = LogFactory.getLog(getClass());

    private PmxmlHandler pmxmlAsyncHandler;
    private PmxmlHandler pmxmlHandler;

    private Set<String> excludedContentTypes = Collections.emptySet();
    private Set<String> excludedFileTypes = Collections.emptySet();

    @Override
    public void handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.setContentType("text/html;charset=UTF-8");

        final String activityId = httpServletRequest.getParameter("activityId");
        final String taskId = httpServletRequest.getParameter("taskId");
        final GetFilePart filePartGetter = new GetFilePart(httpServletRequest, httpServletResponse).invoke();
        if (!filePartGetter.isOk()) {
            return;
        }
        final Part part = filePartGetter.getPart();

        if (!isOk(httpServletResponse, activityId, part)) {
            return;
        }

        try(final ByteArrayOutputStream out = new ByteArrayOutputStream((int) part.getSize());
                final InputStream in = part.getInputStream()) {

            final DocumentMetadata metadata = getMetadata(activityId, taskId, getFileName(part));
            if(this.logger.isDebugEnabled()) {
                this.logger.debug("<handleRequest> " + toString(metadata));
            }

            FileCopyUtils.copy(in, out);
            final AsyncStoreDocumentRequest request = createRequest(metadata, out.toByteArray());
            this.pmxmlAsyncHandler.exchangeData(request, FUNCTION_KEY, VoidResponse.class);
            writeMessage(httpServletResponse, OK);
        }
        catch(Throwable t) {
            this.logger.error("<handleRequest> activityId=" + activityId + " taskId=" + taskId, t);
            writeError(httpServletResponse, ERROR, t.getClass().getSimpleName(), t.getMessage());
        }
    }

    private boolean isOk(HttpServletResponse httpServletResponse, String activityId, Part part) throws IOException {
        if(!StringUtils.hasText(activityId)) {
            writeError(httpServletResponse, NO_ACTIVITY_ID_ERROR);
            return false;
        }

        if(part == null) {
            writeError(httpServletResponse, NO_FILE_ERROR);
            return false;
        }

        if(part.getSize() <= 0) {
            // A quick fix for Chrome, which fires a change event and uploads an empty file even if the user clicked
            // cancel.
            final String fileName = getFileName(part);
            if(!StringUtils.hasText(fileName)) {
                writeError(httpServletResponse, OK);
                return false;
            }

            // Sort out requests of strange users who want to attach an empty file.
            writeError(httpServletResponse, EMPTY_FILES_NOT_ALLOWED);
            return false;
        }

        final String contentType = part.getContentType();
        if(!StringUtils.hasText(contentType)) {
            writeError(httpServletResponse, NO_CONTENT_TYPE_ERROR);
            return false;
        }
        //content-type field possibly contains also an encoding; see http://tools.ietf.org/html/rfc2616#section-14.17
        final String contentTypeOnly = contentType.split(";")[0].trim().toLowerCase();
        if(this.excludedContentTypes.contains(contentTypeOnly)) {
            writeError(httpServletResponse, CONTENT_TYPE_NOT_ALLOWED);
            return false;
        }

        if(part.getSize() > Integer.MAX_VALUE) {
            writeError(httpServletResponse, TECHNICAL_FILE_SIZE_LIMIT_EXCEEDED_ERROR);
            return false;
        }

        final String fileName = getFileName(part);
        if(fileName == null) {
            writeError(httpServletResponse, NO_FILE_NAME_ERROR);
            return false;
        }

        final String fileType = getFileType(fileName);
        if(!StringUtils.hasText(fileType)) {
            writeError(httpServletResponse, NO_FILE_EXTENSION_ERROR);
            return false;
        }
        if(this.excludedFileTypes.contains(fileType)) {
            writeError(httpServletResponse, FILE_EXTENSION_NOT_ALLOWED);
            return false;
        }
        return true;
    }

    private AsyncStoreDocumentRequest createRequest(DocumentMetadata metadata, byte[] content) throws IOException {
        final AsyncStoreDocumentRequest request = new AsyncStoreDocumentRequest();
        request.setHandle(UUID.randomUUID().toString().toUpperCase());
        request.setDocMetaData(metadata);
        request.setContent(content);
        return request;
    }

    private DocumentMetadata getMetadata(String activityId, String taskId, String fileName) {
        final ActivityInstanceInfo info = getActivityInstanceInfo(activityId, taskId);
        if (info == null) {
            throw new IllegalArgumentException("failed to get ActivityInstanceInfo for task " + taskId + " of activity " + activityId);
        }
        // The ID, IDType, AdvisorNumber, WriteOnlyZoneName are set by PM,
        // if the attached file is committed to the DMS
        final DocumentMetadata metadata = new DocumentMetadata();
        metadata.setOrigin(DocumentOrigin.DO_ACTIVITY_ATTACHMENT);
        metadata.setActivityInstanceGuid(StringUtils.trimWhitespace(info.getGUID()));
        metadata.setDocumentType(""); // Steffen: hier nicht "Aktivitäten-Anhang" schicken!
        metadata.setDocumentName(getFileNameWithoutExtension(fileName));
        metadata.setFileType(getFileType(fileName));
        metadata.setDateCreated(ISODateTimeFormat.dateTime().print(System.currentTimeMillis()));
        return metadata;
    }

    private ActivityInstanceInfo getActivityInstanceInfo(String activityId, String taskId) {
        final ActivityInstanceRequest req = new ActivityInstanceRequest();
        req.setInstanceId(activityId);
        req.setCurrentTask(taskId);

        try {
            final ActivityInstanceResponse res = this.pmxmlHandler.exchangeData(req, FUNCTION_KEY_ACTIVITIES_GET_INSTANCE, ActivityInstanceResponse.class);
            return res.getInfo();
        } catch (PmxmlException e) {
            this.logger.error("<getActivityInstanceInfo> activityId=" + activityId + " taskId=" + taskId, e);
        }
        return null;
    }

    private String getFileType(String fileName) {
        final int beginIndex = fileName.lastIndexOf(".");
        if(beginIndex < 0) {
            return null;
        }
        return fileName.substring(beginIndex + 1).toLowerCase();
    }

    private String getFileNameWithoutExtension(String fileName) {
        final int endIndex = fileName.lastIndexOf(".");
        if(endIndex < 0) {
            return null;
        }
        return fileName.substring(0, endIndex);
    }

    private void writeError(HttpServletResponse httpServletResponse, AttachmentUploadState state, String... message) throws IOException {
        if(ERROR == state) {
            if(this.logger.isErrorEnabled()) {
                this.logger.error("<writeError> " + state.name() + ":" + StringUtils.arrayToDelimitedString(message, "; "));
            }
        }
        else {
            if(this.logger.isDebugEnabled()) {
                this.logger.debug("<writeError> " + state.name() + ":" + StringUtils.arrayToDelimitedString(message, "; "));
            }
        }
        writeMessage(httpServletResponse, state, message);
    }

    private void writeMessage(HttpServletResponse httpServletResponse, AttachmentUploadState state, String... message) throws IOException {
        final String s = toJsonHtmlResult(state, message);
        if(this.logger.isDebugEnabled()) {
            this.logger.debug("<writeMessage> " + s);
        }
        httpServletResponse.getWriter().print(s);
    }

    private String toJsonHtmlResult(AttachmentUploadState state, String[] message) {
        final StringBuilder sb = new StringBuilder().append("<html><head><head/><body>")
                .append("{\"State\":\"").append(state.name()).append("\",\"Messages\":[");
        for (int i = 0; i < message.length; i++) {
            if(i != 0) {
                sb.append(",");
            }
            sb.append("\"").append(HtmlUtils.htmlEscape(StringUtils.replace(message[i], "\"", "\\\""))).append("\"");
        }
        return sb.append("]}").append("</body></html>").toString();
    }

    private String getFileName(final Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(
                        content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    public void setPmxmlAsyncHandler(PmxmlHandler pmxmlAsyncHandler) {
        this.pmxmlAsyncHandler = pmxmlAsyncHandler;
    }

    public void setPmxmlHandler(PmxmlHandler pmxmlHandler) {
        this.pmxmlHandler = pmxmlHandler;
    }

    public void setExcludedContentTypes(Set<String> excludedContentTypes) {
        this.excludedContentTypes = excludedContentTypes;
    }

    public void setExcludedFileTypes(Set<String> excludedFileTypes) {
        this.excludedFileTypes = excludedFileTypes;
    }

    private static String toString(DocumentMetadata metadata) {
        return "DocumentMetadata{" +
                "documentType='" + metadata.getDocumentType() + '\'' +
                ", documentName='" + metadata.getDocumentName() + '\'' +
                ", origin=" + metadata.getOrigin() +
                ", fileType='" + metadata.getFileType() + '\'' +
                ", dateCreated='" + metadata.getDateCreated() + '\'' +
                ", idType=" + metadata.getIDType() +
                ", id='" + metadata.getID() + '\'' +
                ", advisorNumber='" + metadata.getAdvisorNumber() + '\'' +
                ", activityInstanceGuid='" + metadata.getActivityInstanceGuid() + '\'' +
                '}';
    }

    private class GetFilePart {
        private final HttpServletRequest httpServletRequest;
        private final HttpServletResponse httpServletResponse;
        private boolean result;
        private Part part;

        public GetFilePart(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
            this.httpServletRequest = httpServletRequest;
            this.httpServletResponse = httpServletResponse;
        }

        boolean isOk() {
            return this.result;
        }

        public Part getPart() {
            return this.part;
        }

        public GetFilePart invoke() throws IOException {
            final Log logger = AttachmentUploadHandler.this.logger;

            try {
                this.part = this.httpServletRequest.getPart("file");
            }
            catch(IllegalStateException ise) {
                //Servlet 3 spec defines that ISE is thrown if anything with the file size is wrong
                this.result = false;
                logger.warn(ise);
                final Throwable cause = ise.getCause();
                // if the cause is a wrapped apache file upload util message (indicator is the
                // prefix "org.apache.tomcat"), then use the message of the cause otherwise
                // use the message of the IllegalStateException.
                final String message;
                if(cause != null && StringUtils.hasText(cause.getMessage()) &&
                        StringUtils.startsWithIgnoreCase(ise.getMessage(), "org.apache.tomcat")) {
                    message = StringUtils.capitalize(cause.getMessage());
                }
                else {
                    message = ise.getMessage();
                }
                writeError(this.httpServletResponse, CONFIGURED_FILE_SIZE_LIMIT_EXCEEDED_ERROR, message);
                return this;
            }
            catch(Exception e) {
                this.result = false;
                logger.error(e);
                writeError(this.httpServletResponse, ERROR, e.getClass().getSimpleName(), e.getMessage());
                return this;
            }
            this.result = true;
            return this;
        }
    }
}
