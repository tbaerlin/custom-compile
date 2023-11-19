/*
 * GisIpoInstruments.java
 *
 * Created on 24.10.2008 13:56:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.domain.profile.ProfileUtil;
import de.marketmaker.istar.domainimpl.data.DownloadableItemImpl;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.istar.merger.web.easytrade.misc.DynamicPibController;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.DerivativeIpoData;
import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domainimpl.data.DerivativeIpoDataImpl;
import de.marketmaker.istar.merger.provider.CustomerDataDelegateProvider;
import de.marketmaker.istar.merger.web.easytrade.ListCommandWithOptionalPaging;
import de.marketmaker.istar.merger.web.easytrade.ListHelper;
import de.marketmaker.istar.merger.web.easytrade.ListResult;

import static de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.REQUEST_ATTRIBUTE_NAME;

/**
 * Provides instruments of initial public offering for GIS application.
 * <p>
 * Currently only supports IPOs from either <i>dz</i> or <i>wgz</i> bank.
 * </p>
 * <p>
 * The results can be sorted on different fields. See response for a complete list of sortable fields.
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GisIpoInstruments extends EasytradeCommandController {

    public static class Command extends ListCommandWithOptionalPaging {
        private String type;

        /**
         * @return IPO provider type.
         * @sample dz
         */
        @NotNull
        @RestrictedSet("dz,wgz")
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    private static final String PIB_DESCRIPTION = "Produktinfo";

    private static final Map<String, Comparator> SORTERS = new HashMap<>();

    static {
        SORTERS.put("sort", new Comparator<DerivativeIpoData>() {
            public int compare(DerivativeIpoData o1, DerivativeIpoData o2) {
                return o1.getSort() - o2.getSort();
            }
        });
        SORTERS.put("wkn", new Comparator<DerivativeIpoData>() {
            public int compare(DerivativeIpoData o1, DerivativeIpoData o2) {
                return GisIpoInstruments.compare(o1.getWkn(), o2.getWkn());
            }
        });
        SORTERS.put("name", new Comparator<DerivativeIpoData>() {
            public int compare(DerivativeIpoData o1, DerivativeIpoData o2) {
                return GisIpoInstruments.compare(o1.getName(), o2.getName());
            }
        });
        SORTERS.put("subscriptionStart", new DateComparator() {
            protected LocalDate doGetLocalDate(DerivativeIpoData d) {
                return d.getSubscriptionStart();
            }
        });
        SORTERS.put("subscriptionEnd", new DateComparator() {
            protected LocalDate doGetLocalDate(DerivativeIpoData d) {
                return d.getSubscriptionEnd();
            }
        });
        SORTERS.put("valutaDate", new DateComparator() {
            protected LocalDate doGetLocalDate(DerivativeIpoData d) {
                return d.getValutaDate();
            }
        });
        SORTERS.put("expirationDate", new DateComparator() {
            protected LocalDate doGetLocalDate(DerivativeIpoData d) {
                return d.getExpirationDate();
            }
        });
    }

    private static int compare(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return 0;
        }
        if (str1 == null) {
            return -1;
        }
        if (str2 == null) {
            return -1;
        }
        return str1.compareTo(str2);
    }

    abstract private static class DateComparator implements Comparator<DerivativeIpoData> {
        private static final LocalDate OLD = new LocalDate(1970, 1, 1);

        public int compare(DerivativeIpoData o1, DerivativeIpoData o2) {
            return getDate(o1).compareTo(getDate(o2));
        }

        private LocalDate getDate(DerivativeIpoData d) {
            if (d == null) {
                return OLD;
            }
            final LocalDate result = doGetLocalDate(d);
            return result != null ? result : OLD;
        }

        protected abstract LocalDate doGetLocalDate(DerivativeIpoData q);
    }

    private CustomerDataDelegateProvider provider;

    public GisIpoInstruments() {
        super(Command.class);
    }

    public void setProvider(CustomerDataDelegateProvider provider) {
        this.provider = provider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        final Command cmd = (Command) o;

        final List<DerivativeIpoData> list = this.provider.getDerivateIposDzbank(cmd.getType());

        final List<DerivativeIpoData> instruments = new ArrayList<>();
        for (final DerivativeIpoData did : list) {
            final DerivativeIpoDataImpl data = new DerivativeIpoDataImpl(did.getWkn(), did.getName(),
                    did.getSubscriptionStart(), did.getSubscriptionEnd(), did.getValutaDate(),
                    did.getExpirationDate(), did.getSort(), did.isDzPib());

            List<DownloadableItem> reports = filterForUnique(did.getReports()).stream().filter(it -> isEnabledItem(it)).collect(Collectors.toList());
            if ("dz".equalsIgnoreCase(cmd.getType()) && hasPermission(Selector.PRODUCT_WITH_PIB)) {
                reports = replacePibLinkForDzAdaptor(did.getWkn(), reports, credentials(request));
            }

            data.addReports(reports);
            instruments.add(data);
        }

        final ListResult listResult = ListResult.create(cmd, new ArrayList<>(SORTERS.keySet()),
                "name", instruments.size());

        //noinspection unchecked
        Comparator<DerivativeIpoData> sorter = SORTERS.get(listResult.getSortedBy());
        if (!listResult.isAscending()) {
            sorter = Collections.reverseOrder(sorter);
        }
        instruments.sort(sorter);
        ListHelper.clipPage(cmd, instruments);
        listResult.setCount(instruments.size());

        final Map<String, Object> model = new HashMap<>();
        model.put("instruments", instruments);
        model.put("listinfo", listResult);
        return new ModelAndView("gisipoinstruments", model);
    }

    private boolean isEnabledItem(final DownloadableItem item) {
        return !PIB_DESCRIPTION.equals(item.getDescription()) || hasPermission(Selector.PRODUCT_WITH_PIB);
    }

    private boolean hasPermission(final Selector selector) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return profile.isAllowed(selector);
    }

    private String credentials(HttpServletRequest request) {
        MoleculeRequest mr = (MoleculeRequest) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
        if (mr == null) {
            return null;
        } else {
            return ProfileUtil.encodeCredential(mr.getAuthentication(), mr.getAuthenticationType());
        }
    }

    private List<DownloadableItem> replacePibLinkForDzAdaptor(String wkn, List<DownloadableItem> items, String credentials) {
        List<DownloadableItem> result = new ArrayList<>();

        for (DownloadableItem item : items) {
            if (!PIB_DESCRIPTION.equals(item.getDescription())) {
                result.add(item);
            }
        }
        result.add(createAdaptorLinkItem(wkn, credentials)); // default margin, no dialog
        return result;
    }

    static ArrayList<DownloadableItem> filterForUnique(List<DownloadableItem> items) {
        final Map<String, DownloadableItem> result = new HashMap<>();

        for (final DownloadableItem item : items) {
            final String key = item.getDescription() == null ? "DEFAULT" : item.getDescription();

            if (result.containsKey(key) && item.getDate() != null) {
                final DownloadableItem cmp = result.get(key);

                if (cmp.getDate() != null && cmp.getDate().isAfter(item.getDate())) {
                    continue;
                }
            }
            result.put(key, item);
        }
        return new ArrayList<>(result.values());
    }

    private DownloadableItem createAdaptorLinkItem(String wkn, String encodedCredentials) {
        final DateTime now = new DateTime();
        DynamicPibController.PibAdaptorDownloadLink link = new DynamicPibController.PibAdaptorDownloadLink();
        link.setMargin("default");
        link.setWkn(wkn);
        link.setEncodedCredentials(encodedCredentials);
        link.setTyp("Zeichnungsprodukt");
        return new DownloadableItemImpl(
            now.getYear(),
            DownloadableItem.Type.PIB,
            PIB_DESCRIPTION,
            link.asString(),
            now,
            null, null, null, null, null, null);
    }

}