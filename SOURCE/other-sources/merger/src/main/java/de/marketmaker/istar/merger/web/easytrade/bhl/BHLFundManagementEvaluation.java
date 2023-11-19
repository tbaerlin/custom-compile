/*
 * BHLHighLowEvaluation.java
 *
 * Created on 15.02.2011 11:34:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.bhl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;

/**
 * Sample: curl -o response.csv -F "request=@bhl-request.csv" http://localhost/dmxml-1/bew/bhl-fondsverwaltung.csv
 * @author Thomas Kiesgen
 */
@Controller
public class BHLFundManagementEvaluation {
    public static class Command {
        private MultipartFile request;

        private MultipartFile response;

        @SuppressWarnings("UnusedDeclaration")
        public void setRequest(MultipartFile request) {
            this.request = request;
        }

        public MultipartFile getRequest() {
            return request;
        }

        public MultipartFile getResponse() {
            return response;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setResponse(MultipartFile response) {
            this.response = response;
        }
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    private InstrumentServer instrumentServer;

    private IntradayProvider intradayProvider;

    private ProfileProvider profileProvider;

    private HistoricRatiosProvider historicRatiosProvider;

    private BHLExecutor executor;

    public void setInstrumentServer(InstrumentServer instrumentServer) {
        this.instrumentServer = instrumentServer;
    }

    public InstrumentServer getInstrumentServer() {
        return instrumentServer;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public IntradayProvider getIntradayProvider() {
        return intradayProvider;
    }

    public HistoricRatiosProvider getHistoricRatiosProvider() {
        return historicRatiosProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setExecutor(BHLExecutor executor) {
        this.executor = executor;
    }

    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    @RequestMapping("/bew/bhl-fondsverwaltung.csv")
    public void handle(HttpServletRequest request,
            HttpServletResponse response, Command c) throws Exception {
        if (c.getRequest() == null && c.getResponse() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            this.logger.warn("<handle> no request/id file for " + request.getRequestURI());
            return;
        }

        if (c.getRequest() != null) {
            final String version = request.getParameter("version"); // version=test to solve T-43122
            this.logger.info("<handle> version: " + version);
            handleRequest(request, response, c, "test".equals(version));
            return;
        }

        final String taskId = new String(c.getResponse().getBytes()).trim();

        final int status = this.executor.getStatus(taskId);
        if (status < 0) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "failed");
            return;
        }

        if (status < 100) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final File result = this.executor.getFile(taskId);

        if (result == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "failed");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain;charset=UTF-8");
        FileCopyUtils.copy(new FileInputStream(result), response.getOutputStream());
        this.logger.info("<handle> delivered file " + result.getAbsolutePath());
    }

    private Profile getProfile(boolean testVersion) {
        final ProfileRequest profileRequest = testVersion
                ? ProfileRequest.byVwdId("120354", "34")
                : ProfileRequest.byVwdId("120119", "34");
        final ProfileResponse pr = this.profileProvider.getProfile(profileRequest);
        if (!pr.isValid()) {
            this.logger.warn("<handle> invalid response for " + profileRequest);
            return null;
        }
        return pr.getProfile();
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response,
            Command c, boolean testVersion) throws InterruptedException, IOException {
        final Profile profile = getProfile(testVersion);
        if (profile == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        final File taskDir = this.executor.getTaskDir();

        if (!taskDir.exists() && !taskDir.mkdirs()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.logger.warn("<handle> failed to create directory " + taskDir.getAbsolutePath());
            return;
        }

        try {
            c.getRequest().transferTo(new File(taskDir, "request.txt"));
        } catch (Exception e2) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.logger.warn("<handle> failed to store request ", e2);
            return;
        }

        // create session to bind requests to one server (reason: files are written to local disk)
        request.getSession(true);
        final Zone z = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
        this.executor.submit(new BHLTask(this, z.getRequestContext(request, profile), taskDir, testVersion));

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().println(taskDir.getName());
        response.getWriter().close();
    }
}

