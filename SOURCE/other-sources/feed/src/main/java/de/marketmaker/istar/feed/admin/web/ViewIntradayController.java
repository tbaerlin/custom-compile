/*
 * ViewIntradayController.java
 *
 * Created on 28.04.2005 11:09:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.domainimpl.instrument.ContentFlagsDp2;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.IntradayServer;
import de.marketmaker.istar.feed.IntradayServerMBean;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.vwd.SnapFieldVwd;
import de.marketmaker.istar.feed.vwd.SnapFieldVwdFormatter;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import static de.marketmaker.istar.domain.data.SnapFieldComparators.BY_ID;
import static de.marketmaker.istar.domain.data.SnapFieldComparators.BY_NAME;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.DUMP3;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.DUMPZ;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class ViewIntradayController extends AdminController {

    private final static String REQUIRED_BEAN_CLASSNAME =
            ClassUtils.getShortName(IntradayServer.class);

    private static class MySnapFieldVwdFormatter extends SnapFieldVwdFormatter {
        @Override
        public String formatValue(SnapField sf) {
            final String s = super.formatValue(sf);
            if (sf.getId() != VwdFieldDescription.ADF_ContentFlags.id()) {
                //noinspection NumberEquality
                if (sf.getPrice() != sf.getLastPrice()) {
                    return s + "/" + formatPrice(sf.getLastPrice());
                }
                return s;
            }
            final StringBuilder sb = new StringBuilder(s).append("<ul>");
            for (String name : getFlagNames(s)) {
                sb.append("<li>").append(name).append("</li>");
            }
            return sb.append("</ul>").toString();
        }

        private Set<String> getFlagNames(String s) {
            final TreeSet<String> result = new TreeSet<>();
            for (ContentFlags.Flag flag : new ContentFlagsDp2(decode(s)).flags()) {
                result.add(flag.name());
            }
            return result;
        }

        // logic copied from de.marketmaker.istar.merger.mdpsexport.MdpsQuoteMetadataExporter()
        private BitSet decode(String s) {
            final byte[] bytes = Base64.decodeBase64(s);
            final BitSet result = new BitSet();
            for (int i = 0; i < bytes.length * 8; i++) {
                if ((bytes[i / 8] & (1 << (i % 8))) > 0) {
                    result.set(i);
                }
            }
            return result;
        }
    }

    protected boolean isRequiredType(String type) {
        return REQUIRED_BEAN_CLASSNAME.equals(type);
    }

    @RequestMapping("/IntradayServer.html")
    protected ModelAndView doHandle(HttpServletRequest request, ViewIntradayCommand cmd) throws Exception {
        final ModelAndView mav = prepareCommand(request, cmd);
        if (cmd.getInfo() == null || cmd.getObjectName() == null) {
            return mav;
        }

        if (!StringUtils.hasText(cmd.getKey())) {
            mav.setViewName("main");
            return mav;
        }

        final Map<String, Object> m = mav.getModel();

        final IntradayServerMBean bean =
                (IntradayServerMBean) getService(IntradayServerMBean.class, cmd);

        if (isUntypedKey(cmd.getKey())) {
            final String typedKey = bean.getTypedVendorkey(cmd.getKey());
            if (typedKey != null) {
                cmd.setKey(typedKey);
            }
        }

        if ("snap".equals(cmd.getView())) {
            IntradayRequest iRequest = new IntradayRequest();
            final String key = getUntypedKey(cmd.getKey());
            iRequest.add(new IntradayRequest.Item(key));
            final IntradayResponse iResponse = bean.getIntradayDataJmx(iRequest);
            final IntradayResponse.Item responseItem = iResponse.getItem(key);

            if (responseItem != null) {
                SnapRecord psr = responseItem.getPriceSnapRecord();
                SnapRecord dsr = responseItem.getRawDelaySnapRecord();
                m.put("price", getSortedFields(psr, dsr, cmd));
                if (psr instanceof OrderedSnapRecord) {
                    OrderedSnapRecord osr = (OrderedSnapRecord) psr;
                    m.put("size", osr.size());
                    m.put("length", osr.length());
                    m.put("withDelay", dsr != null);
                    m.put("delay", getFieldMap(dsr));
                    if (dsr != null) {
                        OrderedSnapRecord dosr = (OrderedSnapRecord) dsr;
                        m.put("delayLength", dosr.length());
                        m.put("nominalDelay", dosr.getNominalDelayInSeconds() + "s");
                    }
                }
                m.put("formatter", new MySnapFieldVwdFormatter());
                m.put("created", formatTimestamp(responseItem.getCreatedTimestamp()));
            }
        }
        else if ("ticks".equals(cmd.getView())) {
            final AbstractTickRecord.TickItem ticks;
            try {
                final TimeTaker tt = new TimeTaker();
                ticks = getTicks(cmd, bean);
                m.put("took", tt.toString());

                if (ticks != null) {
                    if (ticks.getEncoding() == DUMP3 || ticks.getEncoding() == DUMPZ) {
                        m.put("feeddump", new ViewableRecords(ticks, cmd));
                    }
                    else {
                        m.put("result", new ViewableTicks(ticks, cmd));
                    }
                }
            } catch (Throwable t) {
                this.logger.warn("<processFormSubmission> failed", t);
            }

        }

        return mav;
    }

    private String formatTimestamp(final int ts) {
        return (ts != 0) ? DateTimeProvider.Timestamp.toDateTime(ts).toString() : "n/a";
    }

    private boolean isUntypedKey(final String key) {
        final Matcher m = VendorkeyVwd.KEY_PATTERN.matcher(key);
        return m.matches() && m.group(1) == null && m.group(5) == null;
    }

    private String getUntypedKey(final String key) {
        final Matcher m = VendorkeyVwd.KEY_PATTERN.matcher(key);
        if (m.matches()) {
            if (m.group(1) != null) {
                return key.substring(m.group(1).length());
            }
            else if (m.group(5) != null){
                return key.substring(0, key.length() - m.group(5).length());
            }
        }
        return key;
    }

    private Map<Integer, SnapField> getFieldMap(SnapRecord sr) {
        if (sr == null) {
            return Collections.emptyMap();
        }
        HashMap<Integer, SnapField> result = new HashMap<>();
        for (SnapField sf: getSnapFields(sr)) {
            result.put(sf.getId(), sf);
        }
        return result;
    }

    private Collection<SnapField> getSortedFields(SnapRecord sr, SnapRecord dsr,
            ViewIntradayCommand command) {
        if (sr == null) {
            return Collections.emptyList();
        }

        Collection<SnapField> fields = getSnapFields(sr);

        final List<SnapField> result
                = (fields instanceof List) ? (List<SnapField>) fields : new ArrayList<>(fields);
        if (dsr != null) {
            // ids of RT fields
            Set<Integer> fids = fields.stream().map(SnapField::getId).collect(Collectors.toSet());
            // add undefined fields for NT fields that are not present in RT (e.g., due to deletion etc)
            getSnapFields(dsr).stream()
                    .map(SnapField::getId)
                    .filter(fid -> !fids.contains(fid))
                    .forEach(fid -> result.add(new SnapFieldVwd(fid, null)));
        }
        result.sort(command.isNamesort() ? BY_NAME : BY_ID);
        return result;
    }

    private Collection<SnapField> getSnapFields(SnapRecord sr) {
        return (sr instanceof OrderedSnapRecord)
                    ? ((OrderedSnapRecord)sr).getSnapFields(true) : sr.getSnapFields();
    }
}
