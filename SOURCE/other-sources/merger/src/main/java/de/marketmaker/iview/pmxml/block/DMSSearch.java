package de.marketmaker.iview.pmxml.block;

import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.block.SortSupport;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.iview.dmxml.Sort;
import de.marketmaker.iview.pmxml.Comparators;
import de.marketmaker.iview.pmxml.DMSSearchDocumentsRequest;
import de.marketmaker.iview.pmxml.DMSSearchDocumentsResponse;
import de.marketmaker.iview.pmxml.DocumentMetadata;
import de.marketmaker.iview.pmxml.DocumentOrigin;
import de.marketmaker.iview.pmxml.MMString;
import de.marketmaker.iview.pmxml.MetadataQuery;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellMMTypeDesc;
import de.marketmaker.iview.pmxml.internaltypes.DMSSearchResult;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created on 20.04.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
@SuppressWarnings("unused")
public class DMSSearch extends AbstractInternalBlock<DMSSearchResult, DMSSearch.DMSSearchCmd> {

    @SuppressWarnings("unused")
    public static class DMSSearchCmd extends ListCommand implements AbstractInternalBlock.InternalCommand, Serializable {
        private String correlationId;
        private String atomname;
        private String templateName;
        private String documentName;
        private String comment;
        private String shellMMType;
        private String id;
        private String[] documentTypes;
        private String dateFrom;
        private String dateTo;
        private String pagingHandle;
        private boolean customerDesktopActive = false;
        private boolean postboxFeatureActive = false;

        public void setId(String id) {
            this.id = id;
        }

        public void setType(String shellMMType) {
            this.shellMMType = shellMMType;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public void setDocumentTypes(String[] documentTypes) {
            this.documentTypes = documentTypes;
        }

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }

        public void setAtomname(String atomname) {
            this.atomname = atomname;
        }

        public void setDateFrom(String dateFrom) {
            this.dateFrom = dateFrom;
        }

        public void setDateTo(String dateTo) {
            this.dateTo = dateTo;
        }

        public void setPagingHandle(String pagingHandle) {
            this.pagingHandle = pagingHandle;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public void setDocumentName(String documentName) {
            this.documentName = documentName;
        }

        @RestrictedSet("true,false")
        public boolean isCustomerDesktopActive() {
            return this.customerDesktopActive;
        }

        public void setCustomerDesktopActive(boolean customerDesktopActive) {
            this.customerDesktopActive = customerDesktopActive;
        }

        public void setCustomerDesktopActive(String customerDesktopActive) {
            if (StringUtils.hasText(customerDesktopActive)) {
                this.customerDesktopActive = Boolean.parseBoolean(customerDesktopActive);
            }
        }

        @RestrictedSet("true,false")
        public boolean isPostboxFeatureActive() {
            return this.postboxFeatureActive;
        }

        public void setPostboxFeatureActive(boolean postboxFeatureActive) {
            this.postboxFeatureActive = postboxFeatureActive;
        }

        public void setPostboxFeatureActive(String postboxFeatureActive) {
            if (StringUtils.hasText(postboxFeatureActive)) {
                this.postboxFeatureActive = Boolean.parseBoolean(postboxFeatureActive);
            }
        }

        @Override
        public AbstractInternalBlock.InternalCommandFeature getInternalCmdFeature() {
            return new AbstractInternalBlock.InternalCommandFeature()
                    .withAtomname(this.atomname)
                    .withCorrelationId(this.correlationId)
                    .withTtl("PT0S");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            final DMSSearchCmd that = (DMSSearchCmd) o;
            return this.customerDesktopActive == that.customerDesktopActive &&
                    this.postboxFeatureActive == that.postboxFeatureActive &&
                    Objects.equals(this.correlationId, that.correlationId) &&
                    Objects.equals(this.atomname, that.atomname) &&
                    Objects.equals(this.templateName, that.templateName) &&
                    Objects.equals(this.documentName, that.documentName) &&
                    Objects.equals(this.comment, that.comment) &&
                    Objects.equals(this.shellMMType, that.shellMMType) &&
                    Objects.equals(this.id, that.id) &&
                    Arrays.equals(this.documentTypes, that.documentTypes) &&
                    Objects.equals(this.dateFrom, that.dateFrom) &&
                    Objects.equals(this.dateTo, that.dateTo) &&
                    Objects.equals(this.pagingHandle, that.pagingHandle);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(super.hashCode(), this.correlationId, this.atomname, this.templateName, this.documentName, this.comment, this.shellMMType, this.id, this.dateFrom, this.dateTo, this.pagingHandle, this.customerDesktopActive, this.postboxFeatureActive);
            result = 31 * result + Arrays.hashCode(this.documentTypes);
            return result;
        }
    }

    private static final SortSupport<DocumentMetadata> SORT_SUPPORT;

    private static final String DEFAULT_SORT_BY = "default";

    private static final List<String> SORT_FIELDS;

    static {
        SORT_SUPPORT = SortSupport.createBuilder("default", Comparators.DMSDOC_DATE_COMPARATOR)
                .add("documentName", Comparators.DMSDOC_NAME_COMPARATOR)
                .add("type", Comparators.DMSDOC_TYPE_COMPARATOR)
                .add("login", Comparators.DMSDOC_LOGIN_COMPARATOR)
                .add("date", Comparators.DMSDOC_DATE_COMPARATOR)
                .add("postbox", Comparators.DMSDOC_HAS_POSTBOX_UPLOAD_COMPARATOR)
                .add("postboxStatus", Comparators.DMSDOC_POSTBOX_READ_TIMESTAMP_COMPARATOR)
                .build();

        SORT_FIELDS = SORT_SUPPORT.getSortNames();
    }


    private Ehcache responseCache;

    public DMSSearch() {
        super(DMSSearchCmd.class);
    }

    public void setResponseCache(Ehcache responseCache) {
        this.responseCache = responseCache;
    }

    private DMSSearchDocumentsResponse getResFromCache(String searchString) {
        final Element element = this.responseCache.get(searchString);
        return (DMSSearchDocumentsResponse) ((element != null) ? element.getValue() : null);
    }


    @Override
    protected DMSSearchResult internalDoHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, DMSSearchCmd cmd, BindException e) {
        final DMSSearchDocumentsResponse resFromCache = StringUtils.hasText(cmd.pagingHandle) ? getResFromCache(cmd.pagingHandle) : null;
        final DMSSearchDocumentsResponse res;
        final String pagingHandle;

        if (resFromCache == null) {
            res = doSearch(cmd);
            pagingHandle = UUID.randomUUID().toString();
            this.responseCache.put(new Element(pagingHandle, res));
        }
        else {
            pagingHandle = cmd.pagingHandle;
            res = resFromCache;
        }

        final List<DocumentMetadata> objects = new ArrayList<>(res.getMetaData());

        final ListResult listResult
                = ListResult.create(cmd, SORT_FIELDS, DEFAULT_SORT_BY, objects.size());

        listResult.setTotalCount(objects.size());
        SORT_SUPPORT.apply(listResult.getSortedBy(), cmd, objects);
        listResult.setCount(objects.size());

        final Sort sort = new Sort();
        final Sort.SortedBy sortedBy = new Sort.SortedBy();
        sortedBy.setValue(listResult.getSortedBy());
        sortedBy.setAscending(listResult.isAscending());
        sort.setSortedBy(sortedBy);
        sort.getField().addAll(listResult.getSortFields());

        return new DMSSearchResult(objects, cmd.dateFrom, cmd.dateTo, String.valueOf(listResult.getCount()),
                String.valueOf(listResult.getOffset()), String.valueOf(listResult.getTotalCount()), sort, pagingHandle);
    }

    private DMSSearchDocumentsResponse doSearch(DMSSearchCmd cmd) {
        final DMSSearchDocumentsRequest req = new DMSSearchDocumentsRequest();
        req.setCustomerDesktopActive(cmd.customerDesktopActive);
        //Postbox timestamps are only processed by PM server via postbox cloud service, if this flat is set
        req.setProcessPostboxTimestamps(cmd.postboxFeatureActive);

        final MMString identifier = new MMString();
        identifier.setValue(cmd.id);
        final MetadataQuery mq = new MetadataQuery();
        mq.getIDs().add(identifier);

        final ShellMMTypeDesc shell = new ShellMMTypeDesc();
        shell.setT(ShellMMType.fromValue(cmd.shellMMType));
        mq.getIDTypes().add(shell);
        mq.setDateFrom(cmd.dateFrom);
        mq.setDateTo(cmd.dateTo);
        mq.setOrigin(DocumentOrigin.DO_UNKNOWN);
        mq.setTemplateName(cmd.templateName);
        mq.setDocumentName(cmd.documentName);
        mq.setComment(cmd.comment);
        if (cmd.documentTypes != null && cmd.documentTypes.length > 0) {
            for (String documentType : cmd.documentTypes) {
                final MMString mms = new MMString();
                mms.setValue(documentType);
                mq.getDocumentTypes().add(mms);
            }
        }
        req.getQueries().add(mq);

        final DMSSearchDocumentsResponse res;
        try {
            res = this.pmxmlImpl.exchangeData(req, "DMS_SearchDocuments", DMSSearchDocumentsResponse.class);
        }
        catch (PmxmlException ex) {
            throw new IllegalStateException(ex);
        }
        return res;
    }
}
