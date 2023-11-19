/*
 * ViewDirectoryController.java
 *
 * Created on 29.04.2005 11:40:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.InputSource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.delay.DelayProvider;
import de.marketmaker.istar.feed.delay.DelayProviderUtil;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.DpHistoricUpdateBuilder;
import de.marketmaker.istar.feed.ordered.FieldDataMerger;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.SnapFieldIteratorFactory;
import de.marketmaker.istar.feed.snap.SnapData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class DpPricesController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final static SAXParserFactory SPF = SAXParserFactory.newInstance();

    /**
     * how many elements to process before sleeping sleepTimeMillis
     */
    private int atOnce = 1000;

    /**
     * how long to sleep every atOnce elements
     */
    private long sleepTimeMillis = 75;

    private FeedDataRepository feedDataRepository;

    private DpHistoricUpdateBuilder dpHistoricUpdateBuilder;

    private DelayProvider delayProvider;

    private File form;

    private File historicPricesDir;

    private DpPricesFormatter dpPricesFormatter;

    private final FieldDataMerger merger = new FieldDataMerger();

    public void setHistoricPricesDir(File historicPricesDir) {
        this.historicPricesDir = historicPricesDir;
    }

    public void setDpHistoricUpdateBuilder(DpHistoricUpdateBuilder dpHistoricUpdateBuilder) {
        this.dpHistoricUpdateBuilder = dpHistoricUpdateBuilder;
    }

    public void setFeedDataRepository(FeedDataRepository feedDataRepository) {
        this.feedDataRepository = feedDataRepository;
    }

    public void setDelayProvider(DelayProvider delayProvider) {
        this.delayProvider = delayProvider;
    }

    public void setDpPricesFormatter(DpPricesFormatter dpPricesFormatter) {
        this.dpPricesFormatter = dpPricesFormatter;
    }

    public void setAtOnce(int atOnce) {
        this.atOnce = atOnce;
    }

    public void setSleepTimeMillis(long sleepTimeMillis) {
        this.sleepTimeMillis = sleepTimeMillis;
    }

    public void setForm(File form) {
        this.form = form;
    }

    @RequestMapping(value = "dp-prices.xml", method = RequestMethod.GET)
    protected ModelAndView showForm(HttpServletResponse response) throws Exception {
        FileCopyUtils.copy(new FileInputStream(this.form), response.getOutputStream());
        return null;
    }

    @RequestMapping(value = "dp-prices.xml", method = RequestMethod.POST)
    protected ModelAndView onSubmit(HttpServletRequest request,
            HttpServletResponse response, DpPricesCommand command) throws Exception {
        final TimeTaker tt = new TimeTaker();

        final String requestStr = command.getRequest();

/*
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            this.logger.info("<onSubmit> header: " + headerName + " -> " + request.getHeader(headerName));
        }
*/


        DpPricesQuery query;
        final String userAgent = request.getHeader("User-Agent");
        final String remoteAddr = request.getRemoteAddr();
        final String remoteHost = request.getRemoteHost();
        try {
            final DpPricesInputHandler handler = new DpPricesInputHandler(this.feedDataRepository);

            final SAXParser parser = SPF.newSAXParser();
            parser.parse(new InputSource(new StringReader(requestStr)), handler);
            query = handler.getQuery();
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<onSubmit>"
                        + " User-Agent: " + userAgent
                        + " query: "
                        + requestStr.substring(0, Math.min(requestStr.length(), 2000)));
            }
        } catch (Exception e) {
            this.logger.warn("<onSubmit> failed: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "failed parsing the request: " + e.getMessage());
            return null;
        }

        PrintWriter pw = null;
        try {
            setStatus(response, query.isZipped() ? "application/x-gzip" : "text/xml");

            pw = getWriter(response.getOutputStream(), query.isZipped(), query.getEncoding());
            process(query, pw);
        } catch (Throwable t) {
            this.logger.warn("<doPost> failed for"
                    + " User-Agent: " + userAgent
                    + " request: "
                    + requestStr.substring(0, Math.min(requestStr.length(), 2000)), t);
            // we cannot write an error message or even an error return code to the stream
            // because it is likely that some content was written already.
            t.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        this.logger.info("<onSubmit> DpPrices"
                + " RemoteAddr: " + remoteAddr
                + " RemoteHost: " + remoteHost
                + " User-Agent: " + userAgent
                + ", request.length()=" + requestStr.length()
                + ", #keys=" + query.getFeedDatas().size() + ", #fields=" + query.getFieldIds().length
                + ", took " + tt);
        return null;
    }

    void process(DpPricesQuery query, PrintWriter pw) throws IOException, InterruptedException {
        header(pw, query.getRootElement(), query.getEncoding());

        int counter = 0;
        for (final FeedData fd : query.getFeedDatas()) {
            final Boolean realtime = isRealtime(fd, query.isRealtime());
            if (realtime == null) {
                continue;
            }

            append((OrderedFeedData) fd, query, pw, realtime);

            counter++;
            if (counter % this.atOnce == 0) {
                Thread.sleep(this.sleepTimeMillis);
            }
        }

        appendHistoricUpdates(query, pw);

        footer(pw, query.getRootElement());
    }

    private void append(OrderedFeedData fd, DpPricesQuery query, PrintWriter pw, boolean realtime) {
        int[] timestamp = new int[1];
        final byte[] data = getFieldData(fd, query, realtime, timestamp);
        dpPricesFormatter.write(pw, fd, query.getFieldsForQuery(data), query.isWithTimeOfArrival(), query.isWithDateOfArrival(), query.isUseFieldids(), timestamp[0]);
    }

    private byte[] getFieldData(OrderedFeedData fd, DpPricesQuery query, boolean realtime,
            int[] timestamp) {
        if (!query.isWithFields()) {
            return null;
        }
        synchronized (fd) {
            SnapData sd = fd.getSnapData(realtime);
            if (!sd.isInitialized()) {
                return null;
            }
            timestamp[0] = ((OrderedSnapData) sd).getLastUpdateTimestamp();
            final byte[] data = sd.getData(true);
            if (!realtime && query.isWithDynamicFields() && query.isWithNonDynamicFields()) {
                SnapData sdRt = fd.getSnapData(true);
                if (!sdRt.isInitialized()) {
                    return null;
                }
                return merge(sdRt.getData(true), data);
            }
            return data;
        }
    }

    private byte[] merge(byte[] existing, byte[] update) {
        synchronized (this.merger) {
            byte[] merged = this.merger.merge(new BufferFieldData(existing), new BufferFieldData(update));
            return (merged != null) ? merged : existing;
        }
    }

    private void appendHistoricUpdates(DpPricesQuery query,
            PrintWriter pw) throws InterruptedException {
        final SnapFieldIteratorFactory factory = query.getIteratorFactory();

        int counter = 0;
        for (Integer date : query.getDates()) {
            final Iterator<OrderedUpdate> it
                    = this.dpHistoricUpdateBuilder.read(this.historicPricesDir, date.toString());
            while (it.hasNext()) {
                final OrderedUpdate update = it.next();
                final FeedData data = getFeedData(update.getVwdcode());
                if (data == null) {
                    continue;
                }
                dpPricesFormatter.write(pw, data, factory.iterator(update), query.isWithTimeOfArrival(), query.isWithDateOfArrival(), query.isUseFieldids(), update.getTimestamp());

                counter++;
                if (counter % this.atOnce == 0) {
                    Thread.sleep(this.sleepTimeMillis);
                }
            }
        }
    }

    /**
     * If this component has a reference to a delayProvider and the delay for data is 0, then
     * the delayed data is stored in the realtime snap as well, so we return true regardless of
     * the realtime parameter. Otherwise, we return realtime.
     */
    private Boolean isRealtime(FeedData data, boolean realtime) {
        return DelayProviderUtil.isRealtime(delayProvider, data, realtime);
    }


    protected FeedData getFeedData(ByteString vwdcode) {
        return this.feedDataRepository.get(vwdcode);
    }

    /**
     * set the status for the servlet response
     * @param response response to set the status for
     * @param mimetype mimetype to use
     */
    protected void setStatus(HttpServletResponse response, String mimetype) {
        response.setContentType(mimetype);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * write data header.
     */
    void header(PrintWriter pw, String rootElement, Charset charSet) throws IOException {
        pw.println("<?xml version=\"1.0\" encoding=\"" + charSet.displayName() + "\"?>");
        pw.println("<" + rootElement + ">");
    }

    /**
     * write data footer.
     */
    void footer(PrintWriter pw, String rootElement) throws IOException {
        pw.println("</" + rootElement + ">");
    }

    protected PrintWriter getWriter(OutputStream os, boolean isZipped,
            Charset charSet) throws IOException {
        return new PrintWriter(new OutputStreamWriter(isZipped ? new GZIPOutputStream(os) : os, charSet));
    }
}
