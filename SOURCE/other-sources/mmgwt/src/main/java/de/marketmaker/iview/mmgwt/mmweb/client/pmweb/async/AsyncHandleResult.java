package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import de.marketmaker.iview.tools.i18n.NonNLS;

import java.io.Serializable;

/**
 * User: umaurer
 * Date: 19.12.13
 * Time: 15:31
 */
public class AsyncHandleResult implements Serializable {
    private String handle;
    private String objectName;

    public AsyncHandleResult() {
        this.handle = null;
        this.objectName = null;
    }

    public AsyncHandleResult(String handle, String objectName) {
        this.handle = handle;
        this.objectName = objectName;
    }

    public String getHandle() {
        return handle;
    }

    public String getObjectName() {
        return objectName;
    }

    @Override
    @NonNLS
    public String toString() {
        return "AsyncHandleResult{" +
                "handle='" + handle + '\'' +
                ", objectName='" + objectName + '\'' +
                '}';
    }
}
