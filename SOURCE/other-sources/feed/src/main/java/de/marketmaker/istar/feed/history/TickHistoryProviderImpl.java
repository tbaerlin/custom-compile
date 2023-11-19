/*
 * TickHistoryProviderImpl.java
 *
 * Created on 23.08.12 15:27
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.TickType;

/**
 * @author zzhao
 */
@ManagedResource
public class TickHistoryProviderImpl implements TickHistoryProvider, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<TickType, TickHistoryGarner> readers =
            new EnumMap<>(TickType.class);

    private Path symbolAliasFilePath;

    private final AtomicReference<Properties> aliasMapRef = new AtomicReference<>();

    public void setSymbolAliasFilePath(String symbolAliasFilePath) throws IOException {
        this.symbolAliasFilePath = Paths.get(symbolAliasFilePath);
        loadSymbolAliasFile();
    }

    @ManagedOperation(description = "Reload symbol alias file")
    public void loadSymbolAliasFile() throws IOException {
        if (this.symbolAliasFilePath == null) {
            this.logger.warn("<loadSymbolAliasFile> no symbol alias file set");
            return;
        }
        if (Files.exists(this.symbolAliasFilePath) && Files.isRegularFile(this.symbolAliasFilePath)
                && Files.isReadable(this.symbolAliasFilePath)) {
            try (final InputStream is = Files.newInputStream(this.symbolAliasFilePath,
                    StandardOpenOption.READ)) {
                this.aliasMapRef.set(PropertiesLoader.load(is));
                this.logger.info("<loadSymbolAliasFile> succeeded");
            }
        }
        else {
            throw new IllegalStateException("cannot read symbol alias file: " +
                    this.symbolAliasFilePath.toString());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        final Map map = ac.getBeansOfType(TickHistoryGarnerImpl.class);
        for (Object obj : map.values()) {
            final TickHistoryGarner reader = (TickHistoryGarner) obj;
            this.readers.put(reader.getTickType(), reader);
        }
    }

    @Override
    public TickHistoryResponse query(final TickHistoryRequest req) {
        try {
            final TickHistoryGarner reader = this.readers.get(req.getTickType());
            if (reader == null) {
                return TickHistoryResponse.INVALID;
            }
            final AggregatedHistoryTickRecord record = new AggregatedHistoryTickRecord(
                    req.getDuration(), req.getTickType());
            final DateTime historyEnd = reader.gatherTicks(record, withAlias(req), req);
            return new TickHistoryResponse(record, historyEnd);
        } catch (IllegalArgumentException e) {
            this.logger.warn("<query> failed for: {}, details: {}", req, e.getMessage());
        } catch (Exception e) {
            this.logger.error("<query> failed for: {}", req, e);
        }

        return TickHistoryResponse.INVALID;
    }

    private List<String> withAlias(TickHistoryRequest req) {
        final String alias = getAlias(req.getVwdCode());
        return StringUtils.isBlank(alias)
                ? Collections.singletonList(req.getVwdCode())
                : Arrays.asList(alias, req.getVwdCode());
    }

    private String getAlias(String vwdCode) {
        return this.aliasMapRef.get().getProperty(vwdCode);
    }

    @ManagedOperation(description = "invoke to ticks for a given symbol and date")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "symbol", description = "a VWD symbol"),
            @ManagedOperationParameter(name = "from", description = "a day yyyyMMdd"),
            @ManagedOperationParameter(name = "to", description = "a day yyyyMMdd"),
            @ManagedOperationParameter(name = "tickType",
                    description = "tick type: TRADE,ASK,BID or SYNTHETIC_TRADE")
    })
    public String queryJmx(String symbol, int from, int to, String tickType) {
        final TimeTaker tt = new TimeTaker();
        final DateTime start = (from == 0)
                ? new DateTime().withTimeAtStartOfDay().minusDays(1)
                : DateUtil.yyyymmddToDateTime(from);
        final DateTime end = (to == 0 || from == to)
                ? start.plusDays(1) : DateUtil.yyyymmddToDateTime(to);
        final Interval interval = new Interval(start, end);
        final TickType tickTypeEnum = TickType.valueOf(tickType);
        final TickHistoryRequest req = new TickHistoryRequest(symbol, interval,
                TickHistoryRequest.AGGREGATION, 0, false, tickTypeEnum);
        final TickHistoryResponse resp = query(req);
        return !resp.isValid() ? "Invalid tick history response" :
                HistoryGathererTickBase.toQueryResult(symbol, start, end,
                        (AggregatedHistoryTickRecord) resp.getRecord(), tt.toString(), tickTypeEnum,
                        resp.getHistoryEnd());
    }
}
