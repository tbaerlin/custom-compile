package de.marketmaker.istar.merger.web.easytrade.multiplex;

import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.iview.dmxml.BlockOrError;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * Created on 06.05.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class RemoteTarget implements MultiplexerTarget{
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final Pattern ZONE_PATTERN = Pattern.compile(Pattern.quote("$zone$"));

    protected RestTemplate restTemplate;
    protected String serviceUri;
    protected JaxbHandler jaxb;

    @Required
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Required
    public void setServiceUri(String serviceUri) {
        this.serviceUri = serviceUri;
    }

    @Required
    public void setJaxb(JaxbHandler jaxb) {
        this.jaxb = jaxb;
    }

    private String doRequest(String service, Zone zone, String json) throws Exception {
        final String uri = ZONE_PATTERN.matcher(service).replaceAll(zone.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return this.restTemplate.postForObject(uri, entity, String.class);
    }

    protected List<ModelAndView> handleRequestRemote(HttpServletRequest request, HttpServletResponse response, Zone zone, MoleculeRequest mr, boolean mixed) throws Exception {
        prepareRequestType(mr, request.getSession());
        final String json = GsonUtil.toJson(mr);
        try {
            final String res = doRequest(this.serviceUri, zone, json);
            if (mixed) {
                return createMavs(jaxb.unmarshal(res, ResponseType.class));
            }
            else {
                FileCopyUtils.copy(res, response.getWriter());
            }
        }
        catch (HttpStatusCodeException e) {
            this.logger.warn("<doRequest> failed with " + e.getStatusCode() + " for " + json);
            response.sendError(e.getStatusCode().value(), e.getStatusCode().getReasonPhrase());
        }
        catch (RestClientException e) {
            this.logger.error("<doRequest> failed", e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        }

        return null;
    }

    private List<ModelAndView> createMavs(ResponseType res) {
        final List<BlockOrError> blockOrError = res.getData().getBlockOrError();
        final ArrayList<ModelAndView> mavs = new ArrayList<>();
        for (BlockOrError bor : blockOrError) {
            final String data;
            if (bor instanceof ErrorType) {
                data = this.jaxb.marshal(ErrorType.class, (ErrorType) bor, "error");
            }
            else if (bor instanceof BlockType) {
                data = this.jaxb.marshal(BlockType.class, (BlockType) bor, "block");
            }
            else {
                throw new IllegalStateException("unhandled subtype of BlockOrError: " + bor.getClass().getSimpleName());
            }
            final ModelAndView mav = new ModelAndView("remoteatom", "data", data);
            mavs.add(mav);
        }
        return mavs;
    }

    @Override
    public ModelAndView handleStraightRequest(HttpServletRequest request, HttpServletResponse response, Zone zone, MoleculeRequest mr) throws Exception {
        handleRequestRemote(request, response, zone, mr, false);
        return null;
    }

    @Override
    public Future<List<ModelAndView>> handleMixedRequest(final HttpServletRequest request, final HttpServletResponse response, final Zone zone, final MoleculeRequest mr, ExecutorService executorService) throws Exception {
        return executorService.submit(() -> handleRequestRemote(request, response, zone, mr, true));
    }

    protected void prepareRequestType(MoleculeRequest mr, HttpSession session) {
        mr.setAuthenticationType((String) session.getAttribute(ProfileResolver.AUTHENTICATION_TYPE_KEY));
        mr.setAuthentication((String) session.getAttribute(ProfileResolver.AUTHENTICATION_KEY));
    }

    @Override
    public boolean supports(Zone zone, MoleculeRequest.AtomRequest atom) {
        return true;
    }
}
