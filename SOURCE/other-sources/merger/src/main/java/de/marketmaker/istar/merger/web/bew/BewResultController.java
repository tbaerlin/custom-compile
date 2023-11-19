/*
 * BewStatusController.java
 *
 * Created on 27.05.2010 13:22:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;

import de.marketmaker.istar.common.validator.ClassValidator;
import de.marketmaker.istar.common.validator.ClassValidatorFactory;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.merger.web.easytrade.Error;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * Returns bew result status and result files.
 * @author oflege
 */
@Controller
public class BewResultController {
    public static class Command extends BewCommand {
        private String jobId;

        private boolean messages = false;

        @NotNull
        @Pattern(regex = "[0-9]{8}_[0-9]{6}")
        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId.trim();
        }

        public boolean isMessages() {
            return messages;
        }

        public void setMessages(boolean messages) {
            this.messages = messages;
        }
    }

    private BewExecutor executor;

    private ClassValidator validator = ClassValidatorFactory.forClass(Command.class);

    public void setExecutor(BewExecutor executor) {
        this.executor = executor;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(this.validator);
    }

    /**
     * Returns a document that consists of a single line that indicates the request completion status
     * for a BewTask. The line contains an int value with the following semantic:
     * <dl>
     * <dt>-1</dt><dd>The result is permanently unavailable. Resubmit required</dd>
     * <dt>0</dt><dd>The task is scheduled for execution</dd>
     * <dt>1..99</dt><dd>The task is being executed, the number represents the percentage that
     * has been completed already</dd>
     * <dt>100</dt><dd>The task has been executed successfully, the result can be obtained under the
     * well-known URL</dd>
     * </dl>
     */
    @RequestMapping("**/query.bew")
    public void query(HttpServletRequest request,
            HttpServletResponse response, @Valid Command c, BindingResult e) throws IOException {

        if (e.hasErrors()) {
            sendError(response, e);
        }
        else if (!c.isWithValidCredentials(request)) {
            response.sendError(SC_FORBIDDEN);
        }
        else {
            handleQuery(response, c);
        }
    }

    private void handleQuery(HttpServletResponse response,
            Command c) throws IOException {
        final File userDir = this.executor.getUserDir(c);
        final int pctComplete = this.executor.getStatus(userDir.getName(), c.getJobId());

        response.setStatus(SC_OK);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().println(Integer.toString(pctComplete));
        response.getWriter().close();
    }

    @RequestMapping("**/result.bew")
    protected void result(HttpServletRequest request,
            HttpServletResponse response, @Valid Command c, BindingResult e) throws Exception {

        if (e.hasErrors()) {
            sendError(response, e);
        }
        else {
            if (!c.isWithValidCredentials(request)) {
                response.sendError(SC_FORBIDDEN);
            }
            else {
                handleResult(response, c);
            }
        }
    }

    private void handleResult(HttpServletResponse response,
            Command c) throws IOException {
        final File userDir = this.executor.getUserDir(c);
        final File f = this.executor.getFile(userDir.getName(), c.getJobId(), c.isMessages());

        if (f != null) {
            response.setStatus(SC_OK);
            response.setContentLength((int) f.length());
            FileCopyUtils.copy(new FileInputStream(f), response.getOutputStream());
        }
        else {
            response.sendError(SC_NOT_FOUND);
        }
    }

    @ExceptionHandler(BindException.class)
    public void handleException(HttpServletResponse response, BindException e) throws IOException {
        sendError(response, e);
    }

    private void sendError(HttpServletResponse response, BindingResult e) throws IOException {
        final DefaultMessageSourceResolvable resolvable = e.getAllErrors().get(0);
        response.sendError(SC_BAD_REQUEST,
                Error.error(resolvable.getCode(), resolvable.getDefaultMessage()).toString());
    }
}