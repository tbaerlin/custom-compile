package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import de.marketmaker.iview.pmxml.LayoutDesc;


/**
 * User: umaurer
 * Date: 29.11.13
 * Time: 13:16
 */
public class ArchiveData {
    public enum ContentType {
        TABLE, PDF, CHART, GADGET
    }

    private final String handle;
    private final LayoutDesc layoutDescEx;
    private final String objectName;
    private int progress = -1;
    private final ContentType type;
    private boolean inBackground;

    public ArchiveData(ContentType type, String handle, LayoutDesc layoutDescEx, String objectName) {
        this.type = type;
        this.handle = handle;
        this.layoutDescEx = layoutDescEx;
        if (this.layoutDescEx == null) {
            throw new IllegalArgumentException("layoutDescEx must not be null!"); // $NON-NLS$
        }
        this.objectName = objectName;
    }

    public String getHandle() {
        return handle;
    }

    public LayoutDesc getLayoutDesc() {
        return this.layoutDescEx;
    }

    public String getObjectName() {
        return objectName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public ContentType getType() {
        return this.type;
    }

    public boolean isInBackground() {
        return inBackground;
    }

    public void setInBackground(boolean inBackground) {
        this.inBackground = inBackground;
    }

    @Override
    public String toString() {
        return "ArchiveData{" + // $NON-NLS$
                "handle='" + handle + '\'' + // $NON-NLS$
                ", layoutDescEx.guid=" + (layoutDescEx.getLayout() != null ? layoutDescEx.getLayout().getGuid() : "null") + // $NON-NLS$
                ", objectName='" + objectName + '\'' + // $NON-NLS$
                ", progress=" + progress + // $NON-NLS$
                ", type=" + type + // $NON-NLS$
                ", inBackground=" + inBackground + // $NON-NLS$
                '}';
    }
}