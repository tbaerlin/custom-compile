package de.marketmaker.iview.mmgwt.mmweb.server.teaser;



public class TeaserRecordImpl {

    private String moduleName;
    private String version;

    private int size;
    private String contentType;
    private String linkUrl;
    private String filename;
    private Boolean linkEnabled = false;
    private Boolean teaserEnabled = false;
    private int width;
    private int height;
    private String linkTarget;
    // we need transient here to exclude this blob from json serialization
    private transient byte[] imageData;


    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getTeaserEnabled() {
        return teaserEnabled;
    }

    public void setTeaserEnabled(Boolean teaserEnabled) {
        this.teaserEnabled = teaserEnabled;
    }

    public Boolean getLinkEnabled() {
        return linkEnabled;
    }

    public void setLinkEnabled(Boolean linkEnabled) {
        this.linkEnabled = linkEnabled;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getLinkTarget() {
        return linkTarget;
    }

    public void setLinkTarget(String linkTarget) {
        this.linkTarget = linkTarget;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String toString() {
        return "TeaserRecordImpl{" +
                "moduleName='" + moduleName + '\'' +
                ", version='" + version + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                ", filename='" + filename + '\'' +
                ", linkEnabled=" + linkEnabled +
                ", teaserEnabled=" + teaserEnabled +
                ", width=" + width +
                ", height=" + height +
                ", linkTarget='" + linkTarget + '\'' +
                '}';
    }

}
