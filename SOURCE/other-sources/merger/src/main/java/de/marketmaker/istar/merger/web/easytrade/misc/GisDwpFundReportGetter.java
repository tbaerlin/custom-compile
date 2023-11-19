/*
 * GisDwpFundReportMailer.java
 *
 * Created on 12.03.2009 12:12:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.ClassValidator;
import de.marketmaker.istar.common.validator.ClassValidatorFactory;
import de.marketmaker.istar.common.validator.NotNull;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class GisDwpFundReportGetter {
    public static class Command {
        private String url;

        private String cust_id;

        private String basePath = "wpdirect";

        @NotNull
        public String getCust_id() {
            return cust_id;
        }

        public void setCust_id(String cust_id) {
            this.cust_id = cust_id;
        }

        @NotNull
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }

    private FundReportCounter fundReportCounter;

    private FundReportCounter fundReportCounter69;

    private final ClassValidator validator = ClassValidatorFactory.forClass(Command.class);

    public void setFundReportCounter(FundReportCounter fundReportCounter) {
        this.fundReportCounter = fundReportCounter;
    }

    public void setFundReportCounter69(FundReportCounter fundReportCounter69) {
        this.fundReportCounter69 = fundReportCounter69;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(this.validator);
    }

    @RequestMapping(value = "**/wpdirect/fundreport.frm")
    protected ModelAndView handle(@Valid Command cmd, HttpServletResponse response) throws Exception {
        return handle(cmd, this.fundReportCounter.countAccess(cmd.getCust_id()), response);
    }

    @RequestMapping(value = "**/wpdirect_end/fundreport.frm")
    protected ModelAndView handle69(@Valid Command cmd, HttpServletResponse response) throws Exception {
        return handle(cmd, this.fundReportCounter69.countAccess(cmd.getCust_id()), response);
    }

    private ModelAndView handle(Command cmd, boolean success, HttpServletResponse response)
            throws Exception {
        if (success) {
            response.sendRedirect(cmd.getUrl());
            return null;
        }
        return new ModelAndView(cmd.getBasePath() + "/wpdirect_get_result", "success", false);
    }
}