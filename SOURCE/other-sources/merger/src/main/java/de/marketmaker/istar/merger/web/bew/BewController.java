/*
 * BewController.java
 *
 * Created on 17.05.2010 11:34:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import de.marketmaker.istar.common.validator.ClassValidator;
import de.marketmaker.istar.common.validator.ClassValidatorFactory;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.feed.vwd.EndOfDayProvider;
import de.marketmaker.istar.feed.vwd.EntitlementProvider;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProviderImpl;
import de.marketmaker.istar.merger.web.NoProfileException;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.ratios.opra.OpraSearchEngine;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * Controller for the BEWertungsservice. Creates a directory for each request in which it stores
 * the submitted files and delegates request processing to a
 * {@link de.marketmaker.istar.merger.web.bew.BewTask} object which it submits for execution to
 * its {@link de.marketmaker.istar.merger.web.bew.BewExecutor}.<p>
 * If setting up the task fails, the controller will return an <tt>SC_INTERNAL_SERVER_ERROR</tt>
 * ({@value javax.servlet.http.HttpServletResponse#SC_INTERNAL_SERVER_ERROR}). On success, the
 * controller returns a single line of text that will be the name of the directory in which the
 * result of the task can be found later. The same name can also be used to query the execution
 * status using the {@link de.marketmaker.istar.merger.web.bew.BewResultController}.
 * <p>
 * <b>Sample Upload</b><pre>
 * curl -vv -F "request=@request.txt" -F customer='mm-test' -F license='1234-5678-9abc-def0' 'http://dmdev.vwd.com/dmxml-1/bew/submit.bew'
 * </pre>
 * </p>
 * @author oflege
 */
@Controller
public class BewController {

    public static class Command extends BewCommand {
        private MultipartFile request;

        private MultipartFile mappings;

        private boolean debug = false;

        public void setRequest(MultipartFile request) {
            this.request = request;
        }

        public void setMappings(MultipartFile mappings) {
            this.mappings = mappings;
        }

        public MultipartFile getRequest() {
            return request;
        }

        public MultipartFile getMappings() {
            return mappings;
        }

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ClassValidator validator = ClassValidatorFactory.forClass(Command.class);

    private BewExecutor executor;

    private BewDao dao;

    private InstrumentServer instrumentServer;

    private IntradayProvider intradayProvider;

    private BewHistoricPriceProvider bewHistoricPriceProvider;

    private EndOfDayProvider endOfDayProvider;

    private IsoCurrencyConversionProviderImpl isoCurrencyConversionProvider;

    private HistoricRatiosProvider historicRatiosProvider;

    private EntitlementProvider entitlementProvider;

    private OpraSearchEngine opraSearchEngine;

    private int batchSize = 50;

    private ProfileProvider profileProvider;

    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    public void setDao(BewDao dao) {
        this.dao = dao;
    }

    BewDao getDao() {
        return this.dao;
    }

    public void setOpraSearchEngine(OpraSearchEngine opraSearchEngine) {
        this.opraSearchEngine = opraSearchEngine;
    }

    OpraSearchEngine getOpraSearchEngine() {
        return this.opraSearchEngine;
    }

    public void setInstrumentServer(InstrumentServer instrumentServer) {
        this.instrumentServer = instrumentServer;
    }

    InstrumentServer getInstrumentServer() {
        return this.instrumentServer;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    IntradayProvider getIntradayProvider() {
        return this.intradayProvider;
    }

    public void setIsoCurrencyConversionProvider(
            IsoCurrencyConversionProviderImpl isoCurrencyConversionProvider) {
        this.isoCurrencyConversionProvider = isoCurrencyConversionProvider;
    }

    IsoCurrencyConversionProvider getIsoCurrencyConversionProvider() {
        return this.isoCurrencyConversionProvider;
    }

    public HistoricRatiosProvider getHistoricRatiosProvider() {
        return historicRatiosProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    BewHistoricPriceProvider getBewHistoricPriceProvider() {
        return bewHistoricPriceProvider;
    }

    public void setBewHistoricPriceProvider(BewHistoricPriceProvider bewHistoricPriceProvider) {
        this.bewHistoricPriceProvider = bewHistoricPriceProvider;
    }

    EndOfDayProvider getEndOfDayProvider() {
        return endOfDayProvider;
    }

    public void setEndOfDayProvider(EndOfDayProvider endOfDayProvider) {
        this.endOfDayProvider = endOfDayProvider;
    }

    public EntitlementProvider getEntitlementProvider() {
        return entitlementProvider;
    }

    public void setEntitlementProvider(EntitlementProvider entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchSize() {
        return this.batchSize;
    }

    public void setExecutor(BewExecutor executor) {
        this.executor = executor;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(this.validator);
    }

    @RequestMapping("**/submit.bew")
    protected void handle(HttpServletRequest request,
            HttpServletResponse response, @Valid Command c,
            BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            response.sendError(SC_BAD_REQUEST);
            this.logger.warn("<handle> request with errors for " + request.getRequestURI()
                    + ", bindingResult=" + bindingResult);
            return;
        }

        if (!c.isWithValidCredentials(request)) {
            response.sendError(SC_FORBIDDEN);
            return;
        }

        final File userDir = this.executor.getUserDir(c);
        final File taskDir = this.executor.getTaskDir(userDir);

        final String taskKey = userDir.getName() + "/" + taskDir.getName();

        String vwdId = getVwdId(request, c);
        final Profile p;
        try {
            p = getProfile(vwdId, taskKey);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<handle> using " + p + " for " + taskKey);
            }
        } catch (Exception e) {
            response.sendError(SC_INTERNAL_SERVER_ERROR);
            this.logger.warn("<handle> failed to get profile", e);
            return;
        }

        if (!taskDir.exists() && !taskDir.mkdirs()) {
            response.sendError(SC_INTERNAL_SERVER_ERROR);
            this.logger.warn("<handle> failed to create directory " + taskDir.getAbsolutePath());
            return;
        }

        File dest = new File(taskDir, "request.txt");
        try {
            c.getRequest().transferTo(dest);
            if (c.getMappings() != null) {
                dest = new File(taskDir, "mappings.txt");
                c.getMappings().transferTo(dest);
            }
        } catch (Exception e) {
            response.sendError(SC_INTERNAL_SERVER_ERROR);
            this.logger.warn("<handle> failed to store " + dest.getAbsolutePath(), e);
            return;
        }

        final BewTask task = new BewTask(p, this, c, vwdId, taskDir);
        try {
            task.initTaskId();
        } catch (IOException e) {
            response.sendError(SC_INTERNAL_SERVER_ERROR);
            this.logger.warn("<handle> failed to init taskId ", e);
            return;
        }

        // create session to bind requests to one server, just in case we have multiple servers
        // with local file storage (usually, we should have a shared volume, as the session trick
        // depends on clients processing cookies correctly)
        request.getSession(true);

        this.executor.submit(task);

        response.setStatus(SC_OK);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().println(taskDir.getName());
        response.getWriter().close();
    }

    public Profile getProfile(String vwdId, String taskKey) {
        final ProfileRequest request = getProfileRequest(vwdId);
        final ProfileResponse response = this.profileProvider.getProfile(request);
        if (!response.isValid()) {
            this.logger.warn("<getProfile> invalid profile for " + request + ", task " + taskKey);
            throw new NoProfileException("no valid profile for task " + taskKey);
        }
        final Profile profile = response.getProfile();
        this.logger.info("<getProfile> using profile " + profile + " for task " + taskKey);
        return profile;
    }

    private ProfileRequest getProfileRequest(String vwdId) {
        return (vwdId != null)
                ? ProfileRequest.byVwdId(vwdId, null)
                : new ProfileRequest("resource", "bew-produktiv");
    }

    private String getVwdId(HttpServletRequest request, Command cmd) {
        final Zone z = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
        final Map<String, Object> map = z.getContextMap(cmd.getCustomer());
        return (String) map.get("vwdId");
    }
}
