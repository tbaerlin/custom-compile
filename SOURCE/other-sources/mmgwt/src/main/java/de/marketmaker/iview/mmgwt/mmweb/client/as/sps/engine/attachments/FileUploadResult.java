/*
 * FileUploadResult.java
 *
 * Created on 03.09.2014 10:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.attachments;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.pmxml.internaltypes.AttachmentUploadState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JSON overlay type
 * @author mdick
 */
public class FileUploadResult extends JavaScriptObject {
    protected FileUploadResult() {
    }

    private native String _getState() /*-{return this.State; }-*/;
    private native JsArrayString _getMessages() /*-{ return this.Messages; }-*/;

    public final AttachmentUploadState getState() {
        Firebug.info("FileUploadResult: state=" + _getState());
        return AttachmentUploadState.valueOf(_getState());
    }

    public final List<String> getMessages() {
        Firebug.info("FileUploadResult: messages=" + _getMessages());

        final JsArrayString messages = _getMessages();
        if(messages == null || messages.length() == 0) {
            Firebug.info("FileUploadResult: messages null or empty");
            return Collections.emptyList();
        }

        final int length = messages.length();
        final ArrayList<String> arrayList = new ArrayList<>(length);
        for(int i = 0; i < length; i++) {
            arrayList.add(messages.get(i));
        }
        Firebug.info("FileUploadResult: arrayList=" + arrayList);
        return arrayList;
    }
}
