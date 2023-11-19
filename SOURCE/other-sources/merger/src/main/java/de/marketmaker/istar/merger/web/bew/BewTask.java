/*
 * BewTask.java
 *
 * Created on 18.05.2010 13:27:30
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.feed.vwd.EndOfDayProvider;
import de.marketmaker.istar.instrument.InstrumentRequest;
import de.marketmaker.istar.instrument.InstrumentResponse;
import de.marketmaker.istar.instrument.InstrumentServerImpl;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import de.marketmaker.istar.instrument.search.InstrumentSearcherImpl;
import de.marketmaker.istar.merger.PriceRecordFactory;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayData;
import de.marketmaker.istar.merger.provider.ProfiledSnapRecordFactory;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.ratios.opra.OpraRatioSearchResponse;

import static de.marketmaker.istar.instrument.InstrumentUtil.isOpraInstrument;

/**
 * Performs the actual task of identifying requested quotes, retrieving the prices and writing
 * the result file.
 * @author oflege
 */
class BewTask {
    private static final Pattern CROSSRATE_PATTERN = Pattern.compile("([A-Z]{3})/([A-Z]{3})");

    static final String TASKID_FILE_NAME = "taskid.txt";

    static final String RESULT_FILE_NAME = "result.csv";

    static final String MESSAGES_FILE_NAME = "unknown.txt";

    static final String PROBLEMS_FILE_NAME = "problems.log";

    private static final String EOL = "\r\n";

    private static final String ISO_8859_15 = "ISO-8859-15";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final File dir;

    private final String vwdId;

    private PrintWriter resultWriter;

    private PrintWriter unknownWriter;

    private Map<String, String> mappings;

    /**
     * lines from input file, assume all lines fit into memory
     */
    private List<String> symbols = new ArrayList<>(1000);

    private List<String> externalFields = new ArrayList<>(1000);

    private int numProcessed = 0;

    private final BewController.Command command;

    private final RequestContext context;

    private BewFields fields;

    private final BewController controller;

    private boolean withPrices;

    private boolean withLastMonthPrices;

    private boolean withLastYearPrices;

    private int taskId;

    private List<BewFunction> functions;

    private String lastItem;

    private int pctComplete = 0;

    BewTask(Profile profile, BewController controller, BewController.Command command, String vwdId,
            File dir) {
        this.controller = controller;
        this.command = command;
        this.dir = dir;
        this.vwdId = vwdId;
        this.context = RequestContextHolder.getRequestContext().withProfile(profile);
    }

    @Override
    public String toString() {
        return "BewTask{" + getCustomer() + "/" + getJobId()
                + ", tid=" + this.taskId
                + (size() > 0 ? (", #" + size()) : "")
                + ", " + this.pctComplete + "%}";
    }

    String getCustomer() {
        return this.dir.getParentFile().getName();
    }

    String getJobId() {
        return this.dir.getName();
    }

    File getDir() {
        return this.dir;
    }

    String getVwdId() {
        return this.vwdId;
    }

    int size() {
        return this.symbols.size();
    }

    RequestContext getContext() {
        return this.context;
    }

    void execute() throws Exception {
        ackProgress(1);
        final File resultFile = new File(this.dir, RESULT_FILE_NAME);
        final File messagesFile = new File(this.dir, MESSAGES_FILE_NAME);
        boolean success = false;
        try (PrintWriter rw = new PrintWriter(resultFile, ISO_8859_15);
             PrintWriter uw = new PrintWriter(messagesFile, ISO_8859_15)) {
            this.resultWriter = rw;
            this.unknownWriter = uw;
            this.mappings = readMappings();
            processRequest();
            success = true;
        } finally {
            ackProgress(success ? 100 : -1);
        }
    }

    private void ackProgress(int percentage) {
        if (percentage != this.pctComplete) {
            this.pctComplete = percentage;
            this.controller.getDao().ackTaskProgress(this.taskId, percentage);
        }
    }

    void initTaskId() throws IOException {
        this.taskId = this.controller.getDao().createTask(this.command.getCustomer());
        final File f = new File(this.dir, TASKID_FILE_NAME);
        try (PrintWriter pw = new PrintWriter(f)) {
            pw.println(this.taskId);
        }
        this.logger.info("<initTaskId> stored id " + this.taskId + " in " + f.getAbsolutePath());
    }

    private void processRequest() throws Exception {
        try (Scanner sc = new Scanner(new File(this.dir, "request.txt"), ISO_8859_15)) {
            final String fieldsStr = sc.hasNextLine() ? sc.nextLine().trim() : null;
            appendResultHeader(readFieldNames(fieldsStr));
            while (sc.hasNextLine()) {
                addLine(sc.nextLine().trim());
            }
        }
        processSymbols();
    }

    private Collection<String> readFieldNames(String fieldsStr) {
        final Collection<String> fieldNames = readFields(fieldsStr);
        if (this.controller.getIntradayProvider() != null) {
            this.withPrices = BewFields.isWithPrices(fieldNames);
        }
        if (this.controller.getBewHistoricPriceProvider() != null) {
            this.withLastMonthPrices = BewFields.isWithPreviousMonthFields(fieldNames);
            this.withLastYearPrices = BewFields.isWithPreviousYearFields(fieldNames);
        }

        this.fields = BewFields.getFields(fieldNames);

        if (hasFunctions(fieldNames)) {
            this.functions = new ArrayList<>();
            this.functions.add(BewFunction.create("LAST", false)); // for TIC
            this.functions.add(BewFunction.create("LAST", false)); // for EXC
            for (final String fieldname : fieldNames) {
                final int index = fieldname.indexOf("[");
                final String definition = index < 0
                        ? "LIST"
                        : fieldname.substring(index + 1, fieldname.length() - 1);
                this.functions.add(BewFunction.create(definition, BewFields.isDateField(fieldname)));
            }
        }

        return fieldNames;
    }

    private boolean hasFunctions(Collection<String> fieldNames) {
        return fieldNames.stream().anyMatch(fieldName -> fieldName.contains("["));
    }

    private LinkedHashSet<String> readFields(String fieldsStr) {
        if (!StringUtils.hasText(fieldsStr)) {
            this.logger.warn("<readFields> empty request file");
            return new LinkedHashSet<>();
        }
        final LinkedHashSet<String> fields
                = new LinkedHashSet<>(Arrays.asList(fieldsStr.toUpperCase().split(";")));
        fields.remove("TIC");
        fields.remove("EXC");
        return fields;
    }

    private void appendResultHeader(Collection<String> fieldNames) {
        this.resultWriter.print("TIC;EXC;");
        this.resultWriter.print(StringUtils.collectionToDelimitedString(fieldNames, ";"));
        this.resultWriter.print(EOL);
    }

    private void processSymbols() {
        final List<RequestItem> items = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            final String symbol = this.symbols.get(i);
            final String efs = this.externalFields.get(i);
            final RequestItem item = RequestItem.create(symbol, efs);
            if (item != null) {
                items.add(item);
            }
            if (items.size() == this.controller.getBatchSize()) {
                processBatch(items);
                items.clear();
            }
        }
        if (!items.isEmpty()) {
            processBatch(items);
        }

        if (this.functions != null) {
            final StringBuilder sb = new StringBuilder();
            for (final BewFunction function : this.functions) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(function.getResult());
                function.reset();
            }
            this.resultWriter.print(sb.toString());
            this.resultWriter.print(EOL);
        }
    }

    private void addLine(String line) {
        final int first = line.indexOf(";");
        final int second = first >= 0 ? line.indexOf(";", first + 1) : -1;

        if (second < 0) {
            this.symbols.add(line);
            this.externalFields.add(null);
            return;
        }

        final String symbol = line.substring(0, second);
        this.symbols.add(symbol);
        this.externalFields.add(line.substring(second));
    }

    private void processBatch(List<RequestItem> items) {
        final List<ResultItem> resultItems = createResultItems(items);

        assignTimeZones(resultItems);

        if (this.withPrices || this.withLastMonthPrices || this.withLastYearPrices) {
            assignPrices(resultItems);
        }
        appendResult(resultItems);

        this.controller.getDao().addItems(this.taskId, resultItems);
        updatePctComplete(items.size());

        RequestContextHolder.getRequestContext().clearIntradayContext();
    }

    private void assignTimeZones(List<ResultItem> resultItems) {
        final EndOfDayProvider eodp = this.controller.getEndOfDayProvider();
        for (final ResultItem resultItem : resultItems) {
            final Quote quote = resultItem.getQuote();
            if (quote != null) {
                resultItem.setTimeZone(eodp.getTimeZone(quote.getSymbolVwdfeedMarket()));
            }
        }
    }

    private void updatePctComplete(final int num) {
        numProcessed += num;
        ackProgress(Math.min(99, Math.max(1, (this.numProcessed * 100) / size())));
    }

    private void appendResult(List<ResultItem> resultItems) {
        final StringBuilder sb = new StringBuilder(100);
        NEXT_ITEM:
        for (ResultItem item : resultItems) {
            sb.setLength(0);
            append(item, sb);
            if (sb.length() > 0) {
                final String line = sb.toString();
                if (this.functions != null) {
                    final String currentItem = item.getSymbol() + ";" + item.getExchange();
                    if (!currentItem.equals(this.lastItem)) {
                        if (this.lastItem != null) {
                            sb.setLength(0);
                            for (final BewFunction function : this.functions) {
                                if (sb.length() > 0) {
                                    sb.append(";");
                                }
                                sb.append(function.getResult());
                                function.reset();
                            }
                            this.resultWriter.print(sb.toString());
                            this.resultWriter.print(EOL);

                            this.functions.get(0).handle(item.getSymbol());
                            this.functions.get(1).handle(item.getExchange());
                        }
                        this.lastItem = currentItem;
                    }
                    final String[] tokens = StringUtils.delimitedListToStringArray(line, ";");
                    if (tokens.length != this.functions.size()) {
                        this.logger.warn("<appendResult> #tokens != #functions, skip line for " + currentItem);
                        continue;
                    }

                    // first the filters only
                    for (int i = 0; i < tokens.length; i++) {
                        final String token = tokens[i];
                        final BewFunction function = this.functions.get(i);
                        if (function.isFilter() && !function.isValid(token)) {
                            continue NEXT_ITEM;
                        }
                    }

                    // now content
                    for (int i = 0; i < tokens.length; i++) {
                        final String token = tokens[i];
                        final BewFunction function = this.functions.get(i);
                        function.handle(token);
                    }
                }
                else {
                    this.resultWriter.print(line);
                    this.resultWriter.print(EOL);
                }
            }

            if ((item.getQuote() == null || item.isFailed()) && this.unknownWriter != null) {
                this.unknownWriter.print(item.toStringInUnknownFile(true));
                this.unknownWriter.print(EOL);
            }
        }
    }

    private void append(ResultItem item, StringBuilder sb) {
        sb.append(item.getSymbol());
        if (item.getExchange() == null) {
            fillAndAppendExternalContent(item, true, sb);
            return;
        }
        sb.append(';').append(item.getExchange());
        if (item.getQuote() == null) {
            fillAndAppendExternalContent(item, false, sb);
            return;
        }

        final int i = sb.length();
        try {
            this.fields.append(item, sb);
        } catch (Exception e) {
            item.setFailed();
            logProblem(item, e);
            sb.setLength(i);
            if (item.getExternalFields() != null) {
                this.fields.appendEmptyContent(sb);
            }
        }

        if (item.getExternalFields() != null) {
            sb.append(item.getExternalFields());
        }
    }

    private void fillAndAppendExternalContent(ResultItem item, boolean withoutExchange,
            StringBuilder sb) {
        if (item.getExternalFields() != null) {
            if (withoutExchange) {
                sb.append(";");
            }
            this.fields.appendEmptyContent(sb);
            sb.append(item.getExternalFields());
        }
    }

    private void logProblem(ResultItem item, Exception e) {
        final File f = new File(this.dir, PROBLEMS_FILE_NAME);
        if (!f.exists()) {
            this.logger.error("<logProblem> new problem file " + f.getAbsolutePath());
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(f, true))) {
            pw.println("append failed for " + item + ":");
            e.printStackTrace(pw);
        } catch (IOException e1) {
            this.logger.error("<logProblem> failed ", e1);
            this.logger.error("<logProblem> ... when trying to log ", e);
        }
    }

    private void assignPrices(List<ResultItem> items) {
        final List<Quote> quotes = getQuotes(items);
        final List<IntradayData> datas
                = this.controller.getIntradayProvider().getIntradayData(quotes, null);
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setIntradayData(datas.get(i));
        }

        fixWeekendPrices(items);

        assignHistoricPrices(items);
    }

    private void fixWeekendPrices(List<ResultItem> items) {
        for (final ResultItem item : items) {
            if (item.getQuote() == null || item.getValuationPrice().getDate() == null) {
                continue;
            }

            final Price price = getNonWeekendPrice(item.getQuote(),
                    item.getValuationPrice().getDate().toLocalDate());

            if (price != null) {
                item.setValuationPrice(price, "close");
            }
        }
    }

    private Price getNonWeekendPrice(Quote quote, LocalDate date) {
        if (quote == null || quote.getInstrument().getInstrumentType() != InstrumentTypeEnum.CUR) {
            return null;
        }

        if (date == null || date.getDayOfWeek() <= DateTimeConstants.FRIDAY) {
            return null;
        }

        final LocalDate friday = getNonWeekendDay(date);
        final Price price = requestHistoricPriceFromPM(quote, friday);
        if (price == null || price.getValue() == null || price.getDate() == null) {
            return null;
        }
        return price;
    }

    private LocalDate getNonWeekendDay(LocalDate date) {
        if (date.getDayOfWeek() == DateTimeConstants.SUNDAY) {
            return date.minusDays(2);
        }
        if (date.getDayOfWeek() == DateTimeConstants.SATURDAY) {
            return date.minusDays(1);
        }
        return date;
    }

    private void assignHistoricPrices(List<ResultItem> items) {
        final LocalDate today = new LocalDate();
        final LocalDate lastMonthUltimo = today.withDayOfMonth(1).minusDays(1);
        final LocalDate lastYearUltimo = today.withDayOfYear(1).minusDays(1);
        final boolean monthEqualsYear = lastMonthUltimo.equals(lastYearUltimo);

        if (this.withLastMonthPrices) {
            final List<Price> lastMonthPrices = getHistoricPrices(items, lastMonthUltimo);

            for (int i = 0; i < items.size(); i++) {
                final ResultItem item = items.get(i);
                final Price monthsUltimo = lastMonthPrices.get(i);

                if (monthsUltimo == null || monthsUltimo.getDate() == null) {
                    continue;
                }

                final Price nonWeekendPrice = getNonWeekendPrice(item.getQuote(),
                        monthsUltimo.getDate().toLocalDate());
                final Price finalPrice = nonWeekendPrice != null ? nonWeekendPrice : monthsUltimo;

                item.setLastMonthsUltimo(finalPrice);
                if (monthEqualsYear) {
                    item.setLastYearsUltimo(finalPrice);
                }
            }
            if (monthEqualsYear) {
                return;
            }
        }

        if (this.withLastYearPrices) {
            final List<Price> lastYearPrices = getHistoricPrices(items, lastYearUltimo);
            for (int i = 0; i < items.size(); i++) {
                final ResultItem item = items.get(i);
                final Price yearsUltimo = lastYearPrices.get(i);

                if (yearsUltimo == null || yearsUltimo.getDate() == null) {
                    continue;
                }

                final Price nonWeekendPrice = getNonWeekendPrice(item.getQuote(),
                        yearsUltimo.getDate().toLocalDate());
                final Price finalPrice = nonWeekendPrice != null ? nonWeekendPrice : yearsUltimo;

                items.get(i).setLastYearsUltimo(finalPrice);
            }
        }
    }

    /**
     * @param items for which prices are requested
     * @param date return no prices later than date
     * @return valuation prices at or before date for all items; may contain null values
     */
    private List<Price> getHistoricPrices(List<ResultItem> items, LocalDate date) {
        final BewHistoricPriceRequest request = createHistoricPriceRequest(items, date);
        final BewHistoricPriceResponse response
                = this.controller.getBewHistoricPriceProvider().getPrices(request);
        if (!response.isValid()) {
            // TODO: throw exception?
            this.logger.warn("<getPrices> invalid response from bewHistoricPriceProvider!?");
            return Collections.nCopies(items.size(), null);
        }

        final DateTime earliestDate = date.withDayOfMonth(1).toDateTimeAtStartOfDay();

        final List<Price> prices = new ArrayList<>(items.size());
        for (ResultItem item : items) {
            if (item.getQuote() != null) {
                final BewHistoricPriceResponse.Item responseItem
                        = response.getItem(item.getQuote().getSymbolVwdcode());
                final Price price = asPrice(item, responseItem, date);
                if (price != null && price.getDate() != null && price.getDate().isBefore(earliestDate)) {
                    prices.add(null);
                }
                else {
                    prices.add(price);
                }
            }
            else {
                prices.add(null);
            }
        }

        // override prices from historic price provider with more recent valuation price from
        // intraday provider if available; the historic price provider might not know about the
        // most recent prices yet.
        for (int i = 0; i < items.size(); i++) {
            final ResultItem item = items.get(i);
            for (final Price vp : new Price[]{item.getValuationPrice(), item.getPreviousValuationPrice()}) {
                if (!vp.isDefined() || vp.getDate() == null
                        || vp.getDate().toLocalDate().isAfter(date)
                        || vp.getDate().isBefore(earliestDate)) {
                    continue;
                }
                final Price price = prices.get(i);
                if (price == null
                        || !price.isDefined()
                        || price.getDate().toLocalDate().isBefore(vp.getDate().toLocalDate())) {
                    prices.set(i, vp);
                }
            }
        }

        final DateTime toCheck = getNonWeekendDay(date).toDateTimeAtStartOfDay();

        for (int i = 0; i < items.size(); i++) {
            final ResultItem item = items.get(i);
            final Price price = prices.get(i);

            if (item.getQuote() == null) {
                continue;
            }

            if (InstrumentUtil.isVwdFund(item.getQuote())
                    || item.getQuote().getInstrument().getInstrumentType() == InstrumentTypeEnum.CUR) {
                if (price == null || price.getDate() == null
                        || price.getDate().isBefore(toCheck)) {
                    final Price hprice = requestHistoricPriceFromPM(item.getQuote(), date);
                    if (hprice != null && hprice.getValue() != null && hprice.getDate() != null
                            && (price == null || price.getDate() == null
                            || hprice.getDate().isAfter(price.getDate()))) {
                        prices.set(i, hprice);
                    }
                }
            }
        }

        return prices;
    }

    private Price requestHistoricPriceFromPM(Quote quote, LocalDate date) {
        final List<SymbolQuote> sqs = Collections.singletonList(SymbolQuote.create(quote));
        final List<LocalDate> dates = Collections.singletonList(date);
        final List<List<Price>> historicPrices = this.controller.getHistoricRatiosProvider().getHistoricPrices(sqs, dates);
        if (historicPrices == null || historicPrices.isEmpty()) {
            this.logger.warn("<requestHistoricPriceFromPM> failed to get price for " + quote
                    + " on " + date + ", task = " + this.taskId);
            return null;
        }
        return historicPrices.get(0).get(0);
    }

    private BewHistoricPriceRequest createHistoricPriceRequest(List<ResultItem> items,
            LocalDate date) {
        final BewHistoricPriceRequest result = new BewHistoricPriceRequest(date);
        items.stream()
                .filter(item -> item.getQuote() != null)
                .forEach(item -> result.addVwdcode(item.getQuote().getSymbolVwdcode()));
        return result;
    }

    private Price asPrice(ResultItem result, BewHistoricPriceResponse.Item item, LocalDate date) {
        if (item == null) {
            return null;
        }
        if (item.getSnapRecord() != null) {
            final Profile profile = RequestContextHolder.getRequestContext().getProfile();
            final BitSet allowedFields = this.controller.getEntitlementProvider().getAllowedFields(result.getQuote(), profile);
            final SnapRecord record = new ProfiledSnapRecordFactory(allowedFields, null).applyProfileTo(item.getSnapRecord());

            final PriceRecord pr = PriceRecordFactory.create(result.getQuote(), record, PriceQuality.END_OF_DAY, false);
            return new ValuationPrice(result.getQuote(), item.getSnapRecord(), pr, result.getValuationPriceField(), date)
                    .getValuationPrice();
        }
        else {
            return new PriceImpl(item.getPrice(), 0L, null, item.getPriceDate().toDateTimeAtStartOfDay(),
                    PriceQuality.END_OF_DAY);
        }
    }

    private List<Quote> getQuotes(List<ResultItem> items) {
        final List<Quote> result = new ArrayList<>(items.size());
        result.addAll(items.stream().map(ResultItem::getQuote).collect(Collectors.toList()));
        return result;
    }

    private List<ResultItem> createResultItems(List<RequestItem> items) {
        final InstrumentRequest ir = createInstrumentRequest(items);
        final InstrumentResponse response = identify(ir);

        final List<ResultItem> result = new ArrayList<>(items.size() * 2);
        for (int i = 0; i < items.size(); i++) {
            result.addAll(getResultItems(items.get(i), response.getInstruments().get(i)));
        }

        return result;
    }

    private List<ResultItem> getResultItems(final RequestItem item, final Instrument instrument) {
        if (!item.isOption()) {
            return item.createResultItems(instrument);
        }
        final Instrument option = getVwdOption(item, instrument);
        if ((option != null || item.isOptionExchange())
                && !(option != null && isOpraInstrument(option))) {
            return item.createResultItems(option);
        }
        return item.createResultItems(getOpraItems(item));
    }

    private Instrument getOpraItems(RequestItem item) {
        final OpraRatioSearchResponse response = searchOpraItems(item);
        return response.isValid() ? new OpraInstrument(item.getSymbol(), response.getItems()) : null;
    }

    private OpraRatioSearchResponse searchOpraItems(RequestItem item) {
        return this.controller.getOpraSearchEngine().getOpraItems(OptionFutureMapping.getOpraSearchRequest(item));
    }

    private Instrument getVwdOption(RequestItem item, Instrument instrument) {
        final List<String> symbols = OptionFutureMapping.getVwdCodesForOptionOrFuture(item, instrument);
        if (symbols == null || symbols.isEmpty()) {
            return null;
        }
        final InstrumentRequest request = new InstrumentRequest();
        for (final String symbol : symbols) {
            request.addItem(symbol, InstrumentRequest.KeyType.VWDCODE);
        }
        InstrumentResponse response = identify(request);
        for (final Instrument ri : response.getInstruments()) {
            if (ri != null) {
                return ri;
            }
        }
        return null;
    }

    private InstrumentResponse identify(final InstrumentRequest ir) {
        return this.controller.getInstrumentServer().identify(ir);
    }

    private InstrumentRequest createInstrumentRequest(List<RequestItem> items) {
        assignQueries(items);
        final InstrumentRequest result = new InstrumentRequest();
        for (RequestItem item : items) {
            result.addItem(item.getQueryTerm(), item.getKeyType());
        }
        return result;
    }

    private void assignQueries(List<RequestItem> items) {
        for (RequestItem item : items) {
            item.assignQuery(getMappedSymbol(item.getSymbol()), this.mappings.get(item.getSymbol()));
        }
    }

    private String getMappedSymbol(final String s) {
        final String result = this.mappings.get(s);
        if (result != null) {
            return result;
        }

        final Matcher cMatcher = CROSSRATE_PATTERN.matcher(s);
        if (cMatcher.matches()) {
            final String from = cMatcher.group(1);
            final String to = cMatcher.group(2);
            final String vwdfeed = this.controller.getIsoCurrencyConversionProvider()
                    .getCrossRateSymbol(from, to);
            if (vwdfeed != null) {
                return vwdfeed.substring(vwdfeed.indexOf(".") + 1);
            }
        }

        return s;
    }

    private Map<String, String> readMappings() throws Exception {
        final File mappings = new File(this.dir, "mappings.txt");
        if (!mappings.canRead()) {
            return Collections.emptyMap();
        }
        final HashMap<String, String> result = new HashMap<>();

        try (Scanner sc = new Scanner(mappings, ISO_8859_15)) {
            while (sc.hasNextLine()) {
                final String[] strings = sc.nextLine().split(";");
                if (strings.length != 2) {
                    continue;
                }
                result.put(strings[0].trim().toUpperCase(), strings[1].trim().toUpperCase());
            }
            this.logger.info("<readMappings> read " + result.size() + " mappings");
        } catch (IOException e) {
            this.logger.error("<readMappings> failed", e);
            // todo: return? throw? or what?
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        File dir = new File("d:/produktion/var/data/instrument/work0/");

        final InstrumentDirDao dao = new InstrumentDirDao(new File(dir, "data/objects.dat"));

        final InstrumentSearcherImpl searcher = new InstrumentSearcherImpl(dir, dao, null);

        final InstrumentServerImpl is = new InstrumentServerImpl();
        is.setInstrumentBackends(false, dao, searcher, null);

        Profile p = ProfileFactory.valueOf(true);
        RequestContextHolder.setRequestContext(new RequestContext(p, MarketStrategy.STANDARD));

        final File[] dirs = new File("d:/temp/bew/").listFiles(f -> {
//                return f.isDirectory() && "test".equals(f.getName());
            return f.isDirectory() && f.getName().length() == 6;
        });


        for (File d : dirs) {
            System.err.println(d.getAbsolutePath());
            final BewController c = new BewController();
            c.setBatchSize(50);
            c.setInstrumentServer(is);
            c.setIntradayProvider(null);
            final BewTask t = new BewTask(p, c, new BewController.Command(), null, d);
            t.execute();
        }

        searcher.close();
        dao.close();
    }
}
