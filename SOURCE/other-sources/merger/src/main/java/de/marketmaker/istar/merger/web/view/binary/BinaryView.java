/*
 * ImageView.java
 *
 * Created on 13.11.2006 12:55:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.view.binary;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jcip.annotations.Immutable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;

import de.marketmaker.istar.merger.web.HttpException;

/**
 * Stateless, immutable view that renders binary content. In order to use this view, the ModelAndView
 * rendered by the DispatcherServlet has to meet the following conditions:<ul>
 * <li> the view's name has to be {@link #VIEW_NAME}
 * <li> the model has to contain a byte[]-value for the key {@link #CONTENT_KEY}
 * </ul>
 * The contentType of the response can be specified as a model element with the key
 * {@link #CONTENT_TYPE_KEY}
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class BinaryView implements View {
    public static final String VIEW_NAME = BinaryView.class.getName() + ".viewName";

    public static final String CONTENT_TYPE_KEY = BinaryView.class.getName() + ".contentType";

    public static final String CONTENT_KEY = BinaryView.class.getName() + ".content";

    static final View INSTANCE = new BinaryView();

    private BinaryView() {
    }


    public String getContentType() {
        return null;
    }

    public void render(Map model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        final byte[] content = (byte[]) model.get(CONTENT_KEY);
        if (content == null) {
            throw new HttpException(HttpServletResponse.SC_NO_CONTENT);
        }

        response.setStatus(HttpServletResponse.SC_OK);

        if (response.getContentType() == null) {
            final String contentType = (String) model.get(CONTENT_TYPE_KEY);
            if (StringUtils.hasText(contentType)) {
                response.setContentType(contentType);
            }
        }

        response.setContentLength(content.length);
        response.getOutputStream().write(content);
    }
}
