/*
 * ReportServiceImpl.java
 *
 * Created on 21.05.12 14:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.report;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.merger.provider.ReportProvider;

import static de.marketmaker.istar.domain.data.DownloadableItem.Source;

/**
 * @author zzhao
 */
public class ReportServiceImpl implements ReportService, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<DownloadableItem.Source, ReportProvider> providers =
            new EnumMap<>(Source.class);

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        final Map<String, ReportProvider> map = ac.getBeansOfType(ReportProvider.class);
        for (Map.Entry<String, ReportProvider> entry : map.entrySet()) {
            final Source src = entry.getValue().getSource();
            if (null == src) {
                continue;
            }
            this.providers.put(src, entry.getValue());
            this.logger.info("<setApplicationContext> found report provider '" + entry.getKey() + "'"
                    + " for: " + src);
        }
        this.logger.info("<setApplicationContext> " + this.providers.size() + " report providers found");
    }

    @Override
    public ReportResponse getReports(ReportRequest req) {
        final Map<Source, List<DownloadableItem>> reports = req.getProviderPref().stream()
                .filter(pref -> this.providers.containsKey(pref))
                .collect(Collectors.toMap(Function.identity(),
                        pref -> this.providers.get(pref).getReports(req.getInstrumentId()),
                        (p, u) -> p, () -> new EnumMap<>(Source.class)));

        return reports.isEmpty() ? ReportResponse.EMPTY : new ReportResponse(reports);
    }
}
