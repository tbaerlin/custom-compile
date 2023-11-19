/*
 * DocPdfController.java
 *
 * Created on 30.03.11 14:36
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

/**
 * @author zzhao
 */
public class EconodayImageController extends EasytradeCommandController {

    public static class Command {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    private static final Pattern P_IMAGE_ID = Pattern.compile("(\\d+)_(\\d)");

    private EconodayProvider provider;

    public EconodayImageController() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final String id = ((Command) o).getId();
        final Matcher matcher = P_IMAGE_ID.matcher(id);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("invalid image id '" + id + "'");
        }

        final byte[] data = this.provider.getImage(Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)));
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("image/jpg");
        response.setContentLength(data.length);
        response.getOutputStream().write(data);

        return null;
    }

    public void setProvider(EconodayProvider provider) {
        this.provider = provider;
    }
}
