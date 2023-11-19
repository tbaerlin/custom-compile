/*
 * FndFindersuchkriterien.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.mer.MerDataProvider;
import de.marketmaker.istar.merger.provider.mer.MerItem;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;

/**
 * Provides available countries and macroeconomic aspects that can be used in {@see MER_Items}.
 * <p>
 * Restrictions can be set on country and macroeconomic aspect.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MerItemsMetadata extends EasytradeCommandController {
    public static class Command extends ListCommand {
        private String[] country;

        private String[] type;

        /**
         * @return country names in German, e.g. Argentinien, Brasilien
         */
        public String[] getCountry() {
            return country;
        }

        public void setCountry(String[] country) {
            this.country = country;
        }

        /**
         * @return macroeconomic aspects in German, e.g. Auftragseingang, BIP etc..
         */
        public String[] getType() {
            return type;
        }

        public void setType(String[] type) {
            this.type = type;
        }
    }

    private MerDataProvider merDataProvider;

    public MerItemsMetadata() {
        super(Command.class);
    }

    public void setMerDataProvider(MerDataProvider merDataProvider) {
        this.merDataProvider = merDataProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        final Command cmd = (Command) o;
        final Map<String, Object> model = new HashMap<>();

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (cmd.getType() == null && cmd.getCountry() == null) {
            final Map<String, List<String>> metadata = this.merDataProvider.getMetadata(profile);
            model.put("country", metadata.get("country"));
            model.put("type", metadata.get("type"));
        }
        else {
            final List<MerItem> items = this.merDataProvider.getItems(profile,
                    cmd.getType() != null ? Arrays.asList(cmd.getType()) : Collections.<String>emptyList(),
                    cmd.getCountry() != null ? Arrays.asList(cmd.getCountry()) : Collections.<String>emptyList());
            final Set<String> types = new TreeSet<>();
            final Set<String> countries = new TreeSet<>();
            for (final MerItem item : items) {
                types.add(item.getType());
                countries.add(item.getCountry());
            }

            types.remove("Zins");
            types.remove("Sonstige");
            types.remove("Leading Indicator");
            types.remove("Index");
            types.remove("Devisen");

            model.put("country", countries);
            model.put("type", types);
        }

        return new ModelAndView("meritemsmetadata", model);
    }
}