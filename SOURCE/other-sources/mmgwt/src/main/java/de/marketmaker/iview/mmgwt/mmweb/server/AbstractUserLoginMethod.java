/*
 * AbstractUserLoginMethod.java
 *
 * Created on 16.11.2012 15:30:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppProfile;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Base class for user login methods. Encapsulates some common methods.
 * @author Markus Dick
 */
abstract class AbstractUserLoginMethod {
    final UserServiceImpl service;

    final UserRequest userRequest;

    AbstractUserLoginMethod(UserServiceImpl service, UserRequest userRequest) {
        this.service = service;
        this.userRequest = userRequest;
    }

    abstract UserResponse invoke();

    AppProfile toAppProfile(Profile profile) {
        final AppProfile result = new AppProfile();
        final List<String> functions = new ArrayList<>();
        functions.addAll(toList(profile, Profile.Aspect.FUNCTION));
        functions.addAll(toList(profile, Profile.Aspect.PRICE));
        result.setFunctions(functions);
        result.setProducts(toList(profile, Profile.Aspect.PRODUCT));
        result.setNews(toList(profile, Profile.Aspect.NEWS));
        result.setPages(toList(profile, Profile.Aspect.PAGE));
        if (profile instanceof VwdProfile) {
            result.setProduktId(((VwdProfile) profile).getProduktId());
        }
        return result;
    }

    List<String> toList(Profile p, Profile.Aspect aspect) {
        final BitSet bs = p.toEntitlements(aspect, null);
        // HACK: we do not want to test for web_investor and web_investor_push and whatever else
        // is invented by mr. krausch, so just set web_investor bit for all derivative products:
        // since we have a lot of places where we check whether a web_investor is used,
        if (aspect == Profile.Aspect.PRODUCT && bs.get(Selector.DZBANK_WEB_INVESTOR_PUSH.getId())) {
            bs.set(Selector.DZBANK_WEB_INVESTOR.getId());
        }
        return toList(bs);
    }

    List<String> toList(BitSet bs) {
        final List<String> result = new ArrayList<>();
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            result.add(Integer.toString(i));
        }
        return result;
    }
}
