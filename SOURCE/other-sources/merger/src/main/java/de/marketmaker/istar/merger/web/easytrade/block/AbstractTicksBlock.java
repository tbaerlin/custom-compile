/*
 * AbstractTickData.java
 *
 * Created on 07.08.15 09:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler.Entitlement;
import de.marketmaker.istar.merger.web.easytrade.block.MscTickData.Command;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.TickList;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.feed.delay.DelayProvider;
import de.marketmaker.istar.feed.vwd.EntitlementProviderVwd;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import org.joda.time.Interval;

/**
 * @author oflege
 */
abstract class AbstractTicksBlock extends EasytradeCommandController {
    protected IntradayProvider intradayProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private EntitlementProviderVwd entitlementProvider;

    private DelayProvider delayProvider;

    private HistoricDataProfiler historicDataProfiler;

    protected AbstractTicksBlock(Class commandClass) {
        super(commandClass);
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setEntitlementProvider(EntitlementProviderVwd entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public void setDelayProvider(DelayProvider delayProvider) {
        this.delayProvider = delayProvider;
    }

    public void setHistoricDataProfiler(HistoricDataProfiler historicDataProfiler) {
        this.historicDataProfiler = historicDataProfiler;
    }

    protected Quote getQuote(DefaultSymbolCommand cmd) {
        return this.instrumentProvider.getQuote(cmd);
    }

    protected void checkAuthorization(Command cmd, Quote quote) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final Entitlement entitlement = this.historicDataProfiler.getEntitlement(profile, quote);
        final Interval interval = new Interval(cmd.getStart(), cmd.getEnd());

        if (!entitlement.validateInterval(profile, quote, interval)) {
            throw new PermissionDeniedException(entitlement.getMessage());
        }
    }

    protected BitSet getAllowedFields(Quote q) {
        final Profile p = RequestContextHolder.getRequestContext().getProfile();
        return this.entitlementProvider.getAllowedFields(q, p, PriceQuality.REALTIME_OR_DELAYED);
    }

    protected void checkFields(Quote q, boolean andFields,
            BitSet allowedFields, VwdFieldDescription.Field... fields) {

        final List<VwdFieldDescription.Field> notAllowed = new ArrayList<>();

        for (VwdFieldDescription.Field field : fields) {
            if (!allowedFields.get(field.id())) {
                notAllowed.add(field);
            }
        }

        if ((andFields && !notAllowed.isEmpty()) || notAllowed.size() == fields.length) {
            permissionDenied(q, notAllowed);
        }
    }

    protected void permissionDenied(Quote q, List<VwdFieldDescription.Field> notAllowed) {
        Map<String, String> missing = new HashMap<>();
        for (final VwdFieldDescription.Field field : notAllowed) {
            final int ent = this.entitlementProvider.getEntitlement(q.getSymbolVwdfeed(), field.id());
            String key = (ent == EntitlementProviderVwd.DEFAULT_ENTITLEMENT)
                    ? "not entitled" : "missing selector " + EntitlementsVwd.toEntitlement(ent);
            missing.put(key, join(missing.get(key), field.toString()));
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : missing.entrySet()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(e.getKey()).append(": ").append(e.getValue());
        }
        throw new PermissionDeniedException(sb.toString());
    }

    private String join(String s1, String s2) {
        return (s1 == null) ? s2 : (s1 + ", " + s2);
    }

    protected TickList.FieldPermissions createPermissions(BitSet allowedFields) {
        return TickList.FieldPermissions.create(
                allowedFields.get(VwdFieldDescription.ADF_Bezahlt.id()),
                allowedFields.get(VwdFieldDescription.ADF_Geld.id()),
                allowedFields.get(VwdFieldDescription.ADF_Brief.id()),
                allowedFields.get(VwdFieldDescription.ADF_Geld_Umsatz.id()),
                allowedFields.get(VwdFieldDescription.ADF_Brief_Umsatz.id()),
                allowedFields.get(VwdFieldDescription.ADF_Bezahlt_Umsatz.id()),
                allowedFields.get(VwdFieldDescription.ADF_Bezahlt_Kurszusatz.id()),
                allowedFields.get(VwdFieldDescription.ADF_Notierungsart.id()));
    }

    protected DateTime adaptEnd(Quote quote, DateTime end) {
        final Profile p = RequestContextHolder.getRequestContext().getProfile();
        if (p.getPriceQuality(quote) == PriceQuality.DELAYED) {
            final int delayInSeconds = getDelayInSeconds(quote);
            if (delayInSeconds > 0) {
                final DateTime allowedEnd = new DateTime().minusSeconds(delayInSeconds);
                if (allowedEnd.isBefore(end)) {
                    return allowedEnd;
                }
            }
        }
        return end;
    }

    private int getDelayInSeconds(Quote quote) {
        return this.delayProvider.getDelayInSeconds(VendorkeyVwd.getInstance(quote.getSymbolVwdfeed()));
    }
}
