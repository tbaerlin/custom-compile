/*
 * VwdProfileFactory.java
 *
 * Created on 26.06.2008 10:56:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.ProfileAdapter;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Creates a profile adapter that can be used to adapt a profile based on access counting information.
 * These counters can be used to restrict the number of times realtime data can be access for a
 * certain timeperiod. vwd provides a count service that allows to increment a counter and returns
 * information for an adapted profile. That information is read by this class and a ProfileAdapter
 * will be created.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VwdProfileAdapterFactory {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Pattern NEWLINE_WHITESPACE = Pattern.compile("\\s*[\\r\\n]+\\s*");

    public ProfileAdapter read(InputStream is) throws Exception {
        final byte[] response = FileCopyUtils.copyToByteArray(is);

        final SAXBuilder builder = new SAXBuilder();
        final Document document = builder.build(new ByteArrayInputStream(response));
        final Element root = document.getRootElement();

        final Element cntAccount = root.getChild("CntAccount");
        if (cntAccount == null) {
            this.logger.warn("<read> no CntAccount in response:");
            this.logger.warn(new String(response, Charset.forName("UTF-8")));
            throw new Exception("CntAccount missing");
        }

        final Element cntSets = cntAccount.getChild("CounterSets");

        if (cntSets == null) {
            final String status = getStatus(cntAccount);
            if ("-1".equals(status) || "-4".equals(status)) {
                return IdentityProfileAdapter.INSTANCE;
            }
            String msg = (status != null)
                    ? ("no CounterSets in response with Status " + status)
                    : ("no CounterSets in response: "
                    + NEWLINE_WHITESPACE.matcher(new String(response, UTF_8)).replaceAll(""));
            throw new ProfileAdapterException(msg);
        }

        final List<Element> counterSets = cntSets.getChildren("CounterSet");

        final List<VwdProfileAdapter.SelectorItem> items = new ArrayList<>();

        for (Element counterSet : counterSets) {
            final List<Element> counters = counterSet.getChildren("Counter");
            for (Element counter : counters) {
                final List<Element> selectors = counter.getChildren("Selectors");
                for (Element selector : selectors) {
                    final Element sel = selector.getChild("Sel");
                    final int id = getSelectorId(sel);
                    final Element mq = sel.getChild("mq");
                    final PriceQuality pq = toPriceQuality(Integer.parseInt(mq.getAttributeValue("qlt")));
                    int mqMode = Integer.parseInt(mq.getAttributeValue("mode"));
//                    System.out.printf("%4d %s%2d%n", id, pq, mqMode);
                    items.add(new VwdProfileAdapter.SelectorItem(id, pq, mqMode == 2));
                }
            }
        }

        return new VwdProfileAdapter(items, response);
    }

    private String getStatus(Element cntAccount) {
        final Element header = cntAccount.getChild("Header");
        if (header == null) {
            return null;
        }
        // it seems that the child is called Status now
        final String status = header.getChildTextTrim("Status");
        if (status != null) {
            return status;
        }
        // but it used to be called StatusId, so try that, too
        return header.getChildTextTrim("StatusId");
    }

    private int getSelectorId(Element selector) {
        final String s = selector.getAttributeValue("id");
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return Integer.parseInt(EntitlementsVwd.toNumericSelector(s));
        }
    }

    private PriceQuality toPriceQuality(int n) {
        switch (n) {
            case 0:
                return PriceQuality.NONE;
            case 1:
                return PriceQuality.REALTIME;
            case 2:
                return PriceQuality.DELAYED;
            case 3:
                return PriceQuality.END_OF_DAY;
            default:
                this.logger.warn("<toPriceQuality> unknown: " + n);
                return null;
        }
    }

    public static void main(String[] args) throws Exception {
        final File file = new File("d:/ReadAccount_ByCustomerUser.xml");
        final BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
        final ProfileAdapter adapter = new VwdProfileAdapterFactory().read(is);

        final Profile p = adapter.adapt(ProfileFactory.valueOf(true));
        System.out.println(p);
        System.out.println(p.isAllowed(Profile.Aspect.PRICE, "175"));
        System.out.println(p.getPriceQuality("175", KeysystemEnum.VWDFEED));
    }
}
