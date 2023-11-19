/*
 * AnalysesNewsHandler.java
 *
 * Created on 10.04.12 12:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import static de.marketmaker.istar.analyses.backend.AnalysesProvider.INVALID_ANALYSIS_ID;

import java.util.Optional;

import com.google.protobuf.Descriptors.FieldDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.analyses.backend.Protos.Analysis;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Builder;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Rating;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription.Field;
import de.marketmaker.istar.news.backend.NewsRecordHandler;
import de.marketmaker.istar.news.backend.ObjectAdapter;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;

/**
 * base class for processing news/analyses feed, incoming data are processed as NewsRecordImpl
 *
 * @author oflege
 */
abstract class AnalysesNewsHandler implements NewsRecordHandler {

    protected final String HEARTBEAT_HEADER = "TIME CHECK";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final AnalysesProvider provider;

    protected final String selectorStr;

    protected AnalysesNewsHandler(Selector selector, AnalysesProvider provider) {
        this.provider = provider;
        this.selectorStr = Integer.toString(selector.getId());
    }

    protected Builder buildAnalysis(NewsRecordImpl nr) {
        final Builder builder = Analysis.newBuilder()
                .setProvider(this.provider.getProvider())
                .setHeadline(getHeadline(nr.getHeadline()))
                .addText(nr.getText());

        Optional.ofNullable(nr.getAgencyTimestamp())
                .ifPresent(dateTime -> builder.setAgencyDate(dateTime.getMillis()));

        Optional.ofNullable(nr.getTimestamp())
                .ifPresent(dateTime -> builder.setDate(dateTime.getMillis()));

        addFields(nr, builder, VwdFieldDescription.NDB_ISINList, "symbol");
        addFields(nr, builder, VwdFieldDescription.NDB_Wpknlist, "symbol");
        addFields(nr, builder, VwdFieldDescription.MMF_Iid_List, "iid", ObjectAdapter.LONG_ADAPTER);

        addProviderSpecificFields(nr, builder);
        return builder;
    }

    /**
     * implement this method for an analysis provider specific feed field to protobuf field mapping
     */
    abstract void addProviderSpecificFields(NewsRecordImpl nr, Builder builder);

    protected String getHeadline(final String headline) {
        return headline;
    }

    protected void addFields(NewsRecordImpl nr, Builder builder,
            final Field field, final String fieldName) {
        addFields(nr, builder, field, fieldName, ObjectAdapter.IDENTITY_ADAPTER);
    }

    protected void addFields(NewsRecordImpl nr, Builder builder,
            final Field field, final String fieldName, ObjectAdapter adapter) {
        final SnapField sf = nr.getSnapRecord().getField(field.id());
        try {
            if (sf.getValue() != null) {
                FieldDescriptor fd = Analysis.getDescriptor().findFieldByName(fieldName);
                String[] values = ((String) sf.getValue()).split(",");
                for (String value : values) {
                    Object adapted = adapter.adapt(value.trim());
                    if (adapted != null) {
                        builder.addRepeatedField(fd, adapted);
                    }
                }
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException("addFields failed: "
                    + " Field: " + field + "/" + fieldName
                    + " SnapField: " + sf.getType() + "/" + sf.getId() + "/" + sf.getName()
                    + " value: '" + sf.getValue() + "'"
                    , ex);
        }
    }

    protected void addField(NewsRecordImpl nr, Builder builder,
            final Field field, final String fieldName) {
        addField(nr, builder, field, fieldName, ObjectAdapter.IDENTITY_ADAPTER);
    }

    protected void addField(NewsRecordImpl nr, Builder builder,
            final Field field, final String fieldName, ObjectAdapter adapter) {
        final SnapField sf = nr.getSnapRecord().getField(field.id());
        try {
            if (sf.getValue() != null) {
                FieldDescriptor fd = Analysis.getDescriptor().findFieldByName(fieldName);
                if (fd == null) {
                    throw new IllegalArgumentException("Unknown field: '" + fieldName + "'");
                }
                Object adapted = adapter.adapt(sf.getValue().toString().trim());
                if (adapted != null) {
                    builder.setField(fd, adapted);
                }
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException("addField failed: "
                    + " Field: " + field + "/" + fieldName
                    + " SnapField: " + sf.getType() + "/" + sf.getId() + "/" + sf.getName()
                    + " value: '" + sf.getValue() + "'"
                    + "", ex);
        }
    }

    /**
     * inbound data from feed,
     * this method is called for each incoming feed record
     *
     * @param newsRecord incoming news/analysis
     */
    @Override
    public void handle(NewsRecordImpl newsRecord) {
        // permission check
        if (!newsRecord.getSelectors().contains(this.selectorStr)) {
            return;
        }
        // validation
        final Builder builder = buildAnalysis(newsRecord);
        if (builder.getRating() == Rating.NONE) {
            final String headline = builder.getHeadline();
            if (!HEARTBEAT_HEADER.equals(headline)) {
                this.logger.debug("<handle> incoming record ignored, reason: without rating: " + builder.getHeadline());
            }
            return;
        }
        if (builder.getIidCount() == 0) {
            this.logger.info("<handle> incoming record ignored, reason: without iid " + builder.getHeadline()
                    + ", " + newsRecord.getAttributes(NewsAttributeEnum.ISIN));
            return;
        }
        if (builder.getIidCount() > 1) {
            this.logger.info("<handle> incoming record ignored, reason: multiple iids " + builder.getHeadline()
                    + ", " + builder.getIidList());
            return;
        }
        // process
        final long id = this.provider.addAnalysis(builder);
        if (id == INVALID_ANALYSIS_ID) {
            this.logger.warn("<handle> analysis with id {} from agency {} not inserted",
                builder.getId(), newsRecord.getAgency());
        }
        else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<handle> inserted analysis with id {} from agency {}",
                    builder.getId(), newsRecord.getAgency());
            }
        }
    }

}
