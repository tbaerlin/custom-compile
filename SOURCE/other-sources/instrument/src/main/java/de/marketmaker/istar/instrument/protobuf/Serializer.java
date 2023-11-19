/*
 * Serializer.java
 *
 * Created on 19.06.12 18:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.protobuf;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domainimpl.DomainContext;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;

/**
 * @author oflege
 */
public class Serializer {
    private static final Logger logger = LoggerFactory.getLogger(Serializer.class);

    public static void main(String[] args) throws Exception {
        File dir = new File(args[0]);
        InstrumentDirDao dao = new InstrumentDirDao(dir);

        TimeTaker tt = new TimeTaker();

        long size = 0;

        DomainContextImpl dc = dao.getDomainContext();
        byte[] serialize = new DomainContextSerializer().serialize(dc);

        DomainContext deserialize = new DomainContextDeserializer().deserialize(serialize);
        assert(dc.getMarkets().size() == deserialize.getMarkets().size());
        assert(dc.getSectors().size() == deserialize.getSectors().size());
        assert(dc.getCurrencies().size() == deserialize.getCurrencies().size());
        assert(dc.getCountries().size() == deserialize.getCountries().size());

        size += serialize.length;

        int n = 0;
        InstrumentSerializer is = new InstrumentSerializer();
        InstrumentDeserializer ds = new InstrumentDeserializer(deserialize);

        for (Instrument instrument : dao) {
            byte[] insBytes = is.serialize((InstrumentDp2) instrument);
            Instrument deserialized = ds.deserialize(insBytes);
            size += insBytes.length;
            if (++n % 10000 == 0) System.out.println(n);
            if (!instrument.equals(deserialized)) {
                System.err.println("diff: " + instrument.getId());
                instrument.equals(deserialized);
                return;
            }
        }

        logger.info(String.valueOf(n));
        logger.info(String.valueOf(size));
        logger.info(String.valueOf(((double)size) / new File(dir, "instruments.dat").length()));
        logger.info(String.valueOf((double)tt.getElapsedMs() / n));
    }
}
