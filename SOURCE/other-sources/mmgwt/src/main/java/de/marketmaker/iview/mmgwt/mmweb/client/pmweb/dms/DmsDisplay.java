package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.pmxml.DocumentMetadata;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.internaltypes.DMSSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 12.03.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public interface DmsDisplay {
    void updateData(DMSSearchResult result);

    void layout();

    interface DmsMetadataCallback {
        void metadataAvailable(DmsMetadata metadata);
    }

    interface Presenter {
        static class Config {
            MmJsDate dateFrom;
            MmJsDate dateTo;
            String name = "";
            String comment = "";
            final List<String> documentTypes = new ArrayList<>();

            public Config(MmJsDate from, MmJsDate to) {
                this.dateFrom = from;
                this.dateTo = to;
            }

            public Config(Config config) {
                this.dateFrom = config.dateFrom;
                this.dateTo = config.dateTo;
                this.name = config.name;
                this.comment = config.comment;
                this.documentTypes.addAll(config.documentTypes);
            }
        }


        interface SearchResultListener {
            void onSearchResult(DMSSearchResult result);
        }

        void setDisplay(DmsDisplay display);

        void requestDmsMetadata(String objectId, final DmsMetadataCallback callback);

        void layoutWhenUpdateDone();

        void updateForPaging();

        void update();

        void update(Config config);

        void update(MmJsDate dateFrom, MmJsDate dateTo);

        void update(DmsMetadata metadata, LayoutDesc layoutDesc);

        void update(DmsMetadata metadata, LayoutDesc layoutDesc, MmJsDate dateFrom, MmJsDate dateTo);

        void download(DocumentMetadata dm);

        void addSearchResultListener(SearchResultListener listener);
    }
}
