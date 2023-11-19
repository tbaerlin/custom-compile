package de.marketmaker.istar.merger.provider.gis;

import de.marketmaker.istar.domain.data.DownloadableItem;

public enum GisDocumentType {
    PIB("Produktinformationsblatt", DownloadableItem.Type.PIB), // Produktinfo??
    PIF("Produktblatt", DownloadableItem.Type.PIF),
    BIB("Basisinformationsblatt", DownloadableItem.Type.BIB);

    public static final GisDocumentType DEFAULT = PIB;

    private final String description;

    private final DownloadableItem.Type downloadableItemType;

    GisDocumentType(String description, DownloadableItem.Type downloadableItemType) {
        this.description = description;
        this.downloadableItemType = downloadableItemType;
    }

    public String getDescription() {
        return description;
    }

    public DownloadableItem.Type getDownloadableItemType() {
        return downloadableItemType;
    }
}
