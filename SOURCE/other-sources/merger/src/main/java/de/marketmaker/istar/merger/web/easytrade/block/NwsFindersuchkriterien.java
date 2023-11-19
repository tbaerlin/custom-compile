/*
 * NwsFindersuchkriterien.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaElement;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaElementList;
import de.marketmaker.istar.news.frontend.NewsRequest;

/**
 * Returns data describing categories and criteria for searching news. For each category (which
 * may be client specific but generally includes s.th. like topic, industry sector, or region),
 * the response list contains elements with a key and a name, the former is to be used in
 * news searches, the latter is supposed to be displayed to end users.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class NwsFindersuchkriterien extends EasytradeCommandController implements InitializingBean {

    static final String DEFAULT_REALM = "default";

    public static class Command {
        private String realm = DEFAULT_REALM;

        @MmInternal
        public String getRealm() {
            return realm;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setRealm(String realm) {
            this.realm = realm;
        }
    }

    static class Realm {
        private List<ProfiledList> lists;

        private final Map<String, LocalizedString> branchenById;

        private final Map<String, LocalizedString> subjectsById;

        Realm(List<ProfiledList> lists) {
            this.lists = lists;
            this.branchenById = retrieveMetadata(ATTRIBUTE_BRANCHEN);
            this.subjectsById = retrieveMetadata(ATTRIBUTE_THEMENGEBIETE);
        }

        Map<String, LocalizedString> getBranchenById() {
            return this.branchenById;
        }

        Map<String, LocalizedString> getSubjectsById() {
            return this.subjectsById;
        }

        private Map<String, LocalizedString> retrieveMetadata(String atrribute) {
            final Map<String, LocalizedString> map = new HashMap<>();
            for (FinderMetaElement finderMetaElement : getMetaElements(atrribute)) {
                final String key = finderMetaElement.getKey();
                final String[] keys = key.split(NewsRequest.OR_CONSTRAINTS_SEPARATOR);
                for (String k : keys) {
                    map.put(k, finderMetaElement.getName());
                }
            }
            return Collections.unmodifiableMap(map);
        }

        private List<FinderMetaElement> getMetaElements(String key) {
            for (ProfiledList list : this.lists) {
                if (list.getName().getDefault().equals(key)) {
                    return list.getElements();
                }
            }
            return Collections.emptyList();
        }
    }

    static class ProfiledElement extends FinderMetaElement  {
        private Set<String> selectors;

        ProfiledElement(String key, LocalizedString value, Set<String> selectors) {
            super(key, value);
            this.selectors = selectors;
        }

        public String toString() {
            return getKey() + "=>" + getName() + (this.selectors != null ? this.selectors.toString() : "");
        }
    }

    static class ProfiledList extends FinderMetaElementList  {
        private Set<String> selectors;
        private boolean withProfiledElement = false;

        ProfiledList(LocalizedString name, String type, Set<String> selectors) {
            super(name, type);
            this.selectors = selectors;
        }

        public void add(ProfiledElement e) {
            super.add(e);
            this.withProfiledElement |= (e.selectors != null);
        }
    }

    private static final String ATTRIBUTE_BRANCHEN = "branchen";

    private static final String ATTRIBUTE_REGIONEN = "regionen";

    private static final String ATTRIBUTE_THEMENGEBIETE = "themengebiete";

    private static final Set<String> ATTRIBUTES = new HashSet<>(Arrays.asList(
            ATTRIBUTE_BRANCHEN, ATTRIBUTE_REGIONEN, ATTRIBUTE_THEMENGEBIETE
    ));

    private AtomicReference<Map<String, Realm>> realmsByName
            = new AtomicReference<>(Collections.<String, Realm>emptyMap());

    private Resource newsListSource;

    public NwsFindersuchkriterien() {
        super(Command.class);
    }

    public void afterPropertiesSet() throws Exception {
        reloadLists();
    }

    @ManagedOperation
    public void reloadLists() {
        try {
            final Map<String, Realm> map = new NwsFindersuchkriterienParser().parse(this.newsListSource);
            this.realmsByName.set(map);
            this.logger.info("<reloadLists> succeeded");
        } catch (Exception e) {
            this.logger.error("<reloadLists> failed", e);
        }
    }

    public void setNewsListSource(Resource newsListSource) {
        this.newsListSource = newsListSource;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command c = (Command) o;
        final Map<String, Object> model = createModel(c);
        return new ModelAndView("nwsfindersuchkriterien", model);
    }

    private Map<String, Object> createModel(Command c) {
        final Map<String, Object> model = new HashMap<>();
        final Realm realm = getRealm(c.getRealm());
        if (realm == null) {
            this.logger.warn("<doHandle> no lists for realm " + c.getRealm());
            return model;
        }

        final List<FinderMetaElementList> result = getLists(realm.lists);
        final Iterator<FinderMetaElementList> it = result.iterator();
        while(it.hasNext()) {
            final FinderMetaElementList list = it.next();
            if (ATTRIBUTES.contains(list.getName().getDefault())) {
                model.put(list.getName().getDefault(), list.getElements());
                it.remove();
            }
        }
        if (!result.isEmpty()) {
            model.put("lists", result);
        }
        return model;
    }

    Realm getRealm(String name) {
        final Map<String, Realm> map = this.realmsByName.get();
        Realm result;
        if (FeatureFlags.isEnabled(FeatureFlags.Flag.VWD_RELEASE_2014)) {
            result = map.get(name + "-neu");
            if (result != null) {
                return result;
            }
        }
        result = map.get(name);
        return (result != null) ? result : map.get(DEFAULT_REALM);
    }

    private List<FinderMetaElementList> getLists(List<ProfiledList> lists) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final BitSet bitSet = profile.toEntitlements(Profile.Aspect.NEWS, null);

        List<FinderMetaElementList> result = new ArrayList<>();

        for (ProfiledList list : lists) {
            if (!isAllowed(bitSet, list.selectors)) {
                continue;
            }
            if (!list.withProfiledElement) { // nothing to profile
                result.add(list);
                continue;
            }
            final FinderMetaElementList profiledList = new FinderMetaElementList(list.getName(), list.getType());
            for (FinderMetaElement element : list.getElements()) {
                final ProfiledElement pe = (ProfiledElement) element;
                if (!isAllowed(bitSet, pe.selectors)) {
                    continue;
                }
                profiledList.add(element);
            }
            if (!profiledList.getElements().isEmpty()) {
                result.add(profiledList);
            }
        }
        return result;
    }

    private boolean isAllowed(BitSet bs, Set<String> keys) {
        if (keys == null) {
            return true;
        }
        for (String key : keys) {
            if (bs.get(EntitlementsVwd.toValue(key))) {
                return true;
            }
        }
        return false;
    }
}
