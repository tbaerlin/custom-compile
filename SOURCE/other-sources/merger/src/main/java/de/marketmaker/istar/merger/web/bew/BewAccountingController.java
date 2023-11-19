/*
 * BewAccountingController.java
 *
 * Created on 06.10.2010 11:02:30
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.PrintWriter;
import java.net.BindException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.DateTimeEditor;
import de.marketmaker.istar.common.validator.ClassValidator;
import de.marketmaker.istar.common.validator.ClassValidatorFactory;
import de.marketmaker.istar.common.validator.NotNull;

/**
 * @author oflege
 */
@Controller
@RequestMapping("**/accounting.csv")
public class BewAccountingController {

    public static class Command {
        private String customer;

        private DateTime from;

        private DateTime to;

        public String getCustomer() {
            return customer;
        }

        public void setCustomer(String customer) {
            this.customer = customer;
        }

        @NotNull
        public DateTime getFrom() {
            return from;
        }

        public void setFrom(DateTime from) {
            this.from = from;
        }

        @NotNull
        public DateTime getTo() {
            return to;
        }

        public void setTo(DateTime to) {
            this.to = to;
        }
    }

    private static final DateTimeFormatter DTF
            = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ClassValidator validator = ClassValidatorFactory.forClass(Command.class);

    private BewDao dao;

    public void setDao(BewDao dao) {
        this.dao = dao;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(validator);
        binder.registerCustomEditor(DateTime.class, new DateTimeEditor());
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showForm(HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        return new ModelAndView("bew/accounting", "customers", this.dao.getCustomerNames());
    }

    @RequestMapping(method = RequestMethod.POST)
    public void onSubmit(HttpServletResponse response, @Valid Command cmd, BindingResult e)
            throws Exception {

        if (e.hasErrors()) {
            throw new BindException(e.getAllErrors().get(0).toString());
        }

        final List<TaskInfo> tasks
                = this.dao.getTasks(cmd.getCustomer(), cmd.getFrom(), cmd.getTo());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/csv");

        final PrintWriter pw = response.getWriter();
        pw.println("customer;date;runid;symbol;exchange;vwd_symbol;vwd_exchange;iid");

        final StringBuilder line = new StringBuilder(100);

        for (TaskInfo task : tasks) {
            line.setLength(0);
            line.append(task.getCustomer()).append(";");
            line.append(DTF.print(task.getRequestdate())).append(";");
            line.append(task.getId()).append(";");
            final int headerLen = line.length();

            final List<SymbolInfo> infos = this.dao.getSymbols(task.getId());
            for (SymbolInfo info : infos) {
                line.setLength(headerLen);
                line.append(info.getSymbol()).append(";");
                if (info.getExchange() != null) {
                    line.append(info.getExchange());
                }
                if (info.getVwdSymbol() != null) {
                    line.append(";").append(info.getVwdSymbol())
                            .append(";").append(info.getVwdExchange())
                            .append(";").append(info.getIid());
                }
                pw.println(line);
            }
        }
        pw.close();
    }
}
