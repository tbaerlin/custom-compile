package de.marketmaker.iview.pmxml;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.List;

/**
 * Created on 23.07.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class Comparators {


    public static final Comparator<String> STRING_COMPARATOR = (str1, str2) -> {
        if (str1 != null && str2 != null) {
            return str1.compareTo(str2);
        }
        if (str1 == null && str2 == null) {
            return 0;
        }
        if (str1 == null) {
            return 1;
        }
        return -1;

    };

    public static final Comparator<ShellMMInfo> SHELL_BY_NAME = (o1, o2) -> {
        String name1 = null;
        String name2 = null;
        if (o1 != null) {
            name1 = o1.getBezeichnung().toUpperCase();
        }
        if (o2 != null) {
            name2 = o2.getBezeichnung().toUpperCase();
        }
        return STRING_COMPARATOR.compare(name1, name2);
    };

    public static final Comparator<DocumentMetadata> DMSDOC_NAME_COMPARATOR = (o1, o2) -> {
        String name1 = null;
        String name2 = null;
        if (o1 != null) {
            name1 = o1.getDocumentName().toUpperCase();
        }
        if (o2 != null) {
            name2 = o2.getDocumentName().toUpperCase();
        }
        return STRING_COMPARATOR.compare(name1, name2);
    };

    public static final Comparator<DocumentMetadata> DMSDOC_TYPE_COMPARATOR = (o1, o2) -> {
        String type1 = null;
        String type2 = null;
        if (o1 != null) {
            type1 = o1.getDocumentType().toUpperCase();
        }
        if (o2 != null) {
            type2 = o2.getDocumentType().toUpperCase();
        }
        return STRING_COMPARATOR.compare(type1, type2);
    };

    public static final Comparator<DocumentMetadata> DMSDOC_LOGIN_COMPARATOR = (o1, o2) -> {
        String login1 = null;
        String login2 = null;
        if (o1 != null) {
            login1 = o1.getCreatedBy();
        }
        if (o2 != null) {
            login2 = o2.getCreatedBy();
        }
        return STRING_COMPARATOR.compare(login1, login2);
    };

    public static final Comparator<DocumentMetadata> DMSDOC_DATE_COMPARATOR = (o1, o2) -> {
        if (o1 != null && o2 != null) {
            if (o1.dateCreated.length() > 0 && o2.dateCreated.length() > 0) {
                final DateTime date1 = DateTime.parse(o1.dateCreated);
                final DateTime date2 = DateTime.parse(o2.dateCreated);
                return date1.compareTo(date2);
            }
            if (o1.dateCreated.length() == 0) {
                return 1;
            } else {
                return -1;
            }
        }
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        } else {
            return -1;
        }
    };

    public static final Comparator<DocumentMetadata> DMSDOC_POSTBOX_READ_TIMESTAMP_COMPARATOR =
            new AbstractDocumentMetadataPostboxComparator() {

                @Override
                public int compare(DocumentMetadata o1, DocumentMetadata o2) {
                    final boolean hasUploadedDocs1 = hasUploadedPostboxDocuments(o1);
                    final boolean hasUploadedDocs2 = hasUploadedPostboxDocuments(o2);

                    final int comparedHasUploadedDocuments = Boolean.compare(hasUploadedDocs1, hasUploadedDocs2);
                    if(comparedHasUploadedDocuments != 0) {
                        return comparedHasUploadedDocuments;
                    }

                    if (o1 != null && o2 != null) {
                        if (o1.getPostboxReadTimestamp().length() > 0 && o2.getPostboxReadTimestamp().length() > 0) {
                            final DateTime date1 = DateTime.parse(o1.getPostboxReadTimestamp());
                            final DateTime date2 = DateTime.parse(o2.getPostboxReadTimestamp());
                            return date1.compareTo(date2);
                        }
                        if (o1.getPostboxReadTimestamp().length() == 0) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    if (o1 == null) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            };

    public static final Comparator<DocumentMetadata> DMSDOC_HAS_POSTBOX_UPLOAD_COMPARATOR =
            new AbstractDocumentMetadataPostboxComparator() {
                @Override
                public int compare(DocumentMetadata o1, DocumentMetadata o2) {
                    final boolean b1 = hasUploadedPostboxDocuments(o1);
                    final boolean b2 = hasUploadedPostboxDocuments(o2);
                    return Boolean.compare(b1, b2);
                }
            };

    public static abstract class AbstractDocumentMetadataPostboxComparator implements Comparator<DocumentMetadata> {
        public boolean hasUploadedPostboxDocuments(DocumentMetadata dm) {
            if (dm == null) {
                return false;
            }

            final List<MMString> list = dm.getPostboxUUIDList();
            return list != null && list.size() > 0;
        }
    }
}
