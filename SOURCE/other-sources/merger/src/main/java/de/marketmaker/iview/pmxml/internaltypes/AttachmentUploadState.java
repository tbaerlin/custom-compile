/*
 * AttachmentUploadState.java
 *
 * Created on 03.09.2014 09:14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.pmxml.internaltypes;

/**
 * @author mdick
 */
@SuppressWarnings("unused")
public enum AttachmentUploadState {
    OK("ok"),
    /*errors/exceptions:*/
    ERROR("error"),
    NO_ACTIVITY_ID_ERROR("noActivityIdError"),
    NO_FILE_ERROR("noFileError"),
    NO_CONTENT_TYPE_ERROR("noContentTypeError"),
    NO_FILE_NAME_ERROR("noFileNameError"),
    NO_FILE_EXTENSION_ERROR("noFileExtensionError"),
    TECHNICAL_FILE_SIZE_LIMIT_EXCEEDED_ERROR("technicalFileSizeLimitExceededError"), //2 GiB - 1 Byte
    CONFIGURED_FILE_SIZE_LIMIT_EXCEEDED_ERROR("configuredFileSizeLimitExceededError"),
    CONTENT_TYPE_NOT_ALLOWED("contentTypeNotAllowed"),
    FILE_EXTENSION_NOT_ALLOWED("fileExtensionNotAllowed"),
    EMPTY_FILES_NOT_ALLOWED("emptyFilesNotAllowed");

    private final String value;

    AttachmentUploadState(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
