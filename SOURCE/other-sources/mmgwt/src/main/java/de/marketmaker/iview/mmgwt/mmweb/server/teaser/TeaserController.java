package de.marketmaker.iview.mmgwt.mmweb.server.teaser;


import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneResolver;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * servlet for teaser management
 * - handle uploading of teaser image and properties like linkUrl, target
 * - serve the image raw data
 */
public class TeaserController extends MultiActionController implements InitializingBean {
    private final Log logger = LogFactory.getLog(getClass());

    public static final String VERSION_KEY = "version";

    private TeaserDaoDb teaserDao;

    private ZoneResolver zoneResolver;


    public void setZoneResolver(ZoneResolver zoneResolver) {
        this.zoneResolver = zoneResolver;
    }

    public void setTeaserDao(TeaserDaoDb teaserDao) {
        this.teaserDao = teaserDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(zoneResolver);
        Assert.notNull(teaserDao);
    }

    @SuppressWarnings("unused")
    public void doRead(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        final String version = readVersion(request);
        final Zone zone = zoneResolver.resolveZone(request);
        final String json = GsonUtil.toJson(teaserDao.findRecord(zone, version));
        //avoid caching, especially in IE 9 and 10 if HTTP method GET is used.
        //see: http://stackoverflow.com/questions/49547/making-sure-a-web-page-is-not-cached-across-all-browsers
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setIntHeader("Expires", 0); // Proxies
        response.setContentType("application/json");
        final PrintWriter writer = response.getWriter();
        writer.print(json);
    }

    @SuppressWarnings("unused")
    public void doWrite(HttpServletRequest request,
            HttpServletResponse response) throws IOException, FileUploadException {
        final Zone zone = zoneResolver.resolveZone(request);
        final ServletFileUpload upload = new ServletFileUpload();

        FileItemIterator fileItems = upload.getItemIterator(request);
        TeaserRecordImpl record = new TeaserRecordImpl();
        record.setModuleName(zone.getName());

        while (fileItems.hasNext()) {
            // CAUTION: side effect: next() discards all data from the previous item!
            final FileItemStream item = fileItems.next();
            final String fieldName = item.getFieldName();

            switch (fieldName) {
                case "teaserUpload":
                    // this is the file upload,
                    // we need to cache the stream, we also could read the data from the image,
                    // but we had to fiddle with the image format since it seems not possible to
                    // recreate the exact same input stream once we have a ImageObject...
                    // also commons-io doesn't allow to read the stream again after next() has been called
                    // so storing the raw data in a byte array seems the best solution, let me know
                    // if you have a better idea
                    byte[] imageData = FileCopyUtils.copyToByteArray(item.openStream());
                    record.setImageData(imageData);
                    record.setSize(imageData.length);
                    record.setFilename(item.getName());
                    record.setContentType(item.getContentType());
                    break;
                case "teaserEnabled":
                    record.setTeaserEnabled(true);
                    break;
                case "linkEnabled":
                    record.setLinkEnabled(true);
                    break;
                case "linkUrl":
                    record.setLinkUrl(Streams.asString(item.openStream()));
                    break;
                case "linkTarget":
                    record.setLinkTarget(Streams.asString(item.openStream()));
                    break;
                case "version":
                    record.setVersion(Streams.asString(item.openStream()));
                    break;
                default:
                    logger.error("<doWrite> ignoring unknown field: '" + fieldName
                            + "', value: '" + Streams.asString(item.openStream()) + "'");
                    break;
            }
        }

        final BufferedImage imageBuffer = ImageIO.read(new ByteArrayInputStream(record.getImageData()));
        if (imageBuffer != null) {
            record.setHeight(imageBuffer.getData().getHeight());
            record.setWidth(imageBuffer.getData().getWidth());
            teaserDao.storeRecord(zone, record.getVersion(), record);
        }
        else {
            // user didn't upload an image, we try to keep the preview image since that image is shown to the user
            // and we disable the teaser if there is no image
            TeaserRecordImpl oldRecord = teaserDao.findRecord(zone, "next");
            if (oldRecord.getImageData() == null) {
                record.setTeaserEnabled(false);
            }
            oldRecord.setVersion(record.getVersion());
            oldRecord.setTeaserEnabled(record.getTeaserEnabled());
            oldRecord.setLinkEnabled(record.getLinkEnabled());
            oldRecord.setLinkUrl(record.getLinkUrl());
            oldRecord.setLinkTarget(record.getLinkTarget());
            teaserDao.storeRecord(zone, record.getVersion(), oldRecord);
        }
        flushMessage(response, "success");
    }

    @SuppressWarnings("unused")
    public void doView(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        final String version = readVersion(request);
        final Zone zone = zoneResolver.resolveZone(request);

        final TeaserRecordImpl record = teaserDao.findRecord(zone, version);
        if (record == null || record.getImageData() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return; // 404
        }

        response.setContentType(record.getContentType());
        response.setHeader("Content-Length", Integer.toString(record.getSize()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + record.getFilename() + "\"");
        response.setStatus(HttpServletResponse.SC_OK);

        byte[] imageData = record.getImageData();
        if (imageData != null) {
            FileCopyUtils.copy(imageData, response.getOutputStream());
        }
    }

    private void flushMessage(HttpServletResponse response, String message) throws IOException {
        String content = "<html><head></head><body>" + message + "</body></html>";
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().append(content).flush();
    }

    private String readVersion(HttpServletRequest request) {
        final String version = request.getParameter(VERSION_KEY);
        if (version == null || version.trim().isEmpty()) {
            return TeaserDaoDb.CURRENT_VERSION;
        }
        return version;
    }
}

