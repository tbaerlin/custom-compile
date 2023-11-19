/*
 * DmxmlDocuServiceImpl.java
 *
 * Created on 16.03.2012 13:14:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.dmxmldocu;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.iview.dmxml.RequestType;
import de.marketmaker.iview.dmxmldocu.DmxmlBlockDocumentation;
import de.marketmaker.iview.dmxmldocu.DmxmlDocumentationRepository;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.AuthenticationFailedException;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.BlocksDocumentation;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.DmxmlDocuService;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.InternalDmxmlServerError;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.InvalidDmxmlRequestException;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.InvalidDmxmlResponseException;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.LoginData;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.WrappedDmxmlRequest;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.WrappedDmxmlResponse;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Element;
import de.marketmaker.iview.mmgwt.mmweb.server.GwtService;
import de.marketmaker.iview.mmgwt.mmweb.server.MmwebServiceImpl;
import de.marketmaker.iview.mmgwt.mmweb.server.ServletRequestHolder;
import de.marketmaker.iview.mmgwt.mmweb.server.UserServiceImpl;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class DmxmlDocuServiceImpl extends GwtService implements DmxmlDocuService {

    public static final String LOGIN_DATA_ATTRIBUTE = "loginData";

    private Resource blocksFilesPath;

    private Resource zoneXsdPath;

    private Unmarshaller dictionaryUnmarshaller;

    private Unmarshaller blockDocuUnmarshaller;

    private Unmarshaller dmxmlRequestUnmarshaller;

    private MmwebServiceImpl mmwebService;

    private final ConcurrentMap<String, XmlDocuAugmenter> xmlAugmenter =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, BlocksDocumentation> blockDocu =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Schema> zoneSchemas = new ConcurrentHashMap<>();

    @Override
    public void login(LoginData loginData) throws AuthenticationFailedException {
        final HttpServletRequest servletRequest = ServletRequestHolder.getHttpServletRequest();
        closeExistingSession(servletRequest);
        try {
            final Zone zone = checkAuthentication(loginData, servletRequest);
            final HttpSession session = servletRequest.getSession();
            prepareSession(session, loginData, zone);
            this.logger.info("<login> Logged in " + loginData
                    + " with session id " + session.getId());
        } catch (AuthenticationFailedException e) {
            this.logger.warn("<login> failed: " + e.getMessage());
            throw e;
        }
    }

    private HttpSession getSession() {
        final HttpServletRequest servletRequest = ServletRequestHolder.getHttpServletRequest();
        return (servletRequest != null) ? servletRequest.getSession(false) : null;
    }

    @Override
    public void logout() {
        final HttpSession session = getSession();
        if (session != null) {
            if (session.getAttribute(LOGIN_DATA_ATTRIBUTE) == null) {
                this.logger.warn("<logout> Called with NON-dmxml-docu session; ignoring it");
            }
            else {
                this.logger.info("<logout> invalidating session " + session.getId());
                session.invalidate();
            }
        }
    }

    public BlocksDocumentation getRepository() throws AuthenticationFailedException {
        try {
            return doGetRepository();
        } catch (AuthenticationFailedException e) {
            this.logger.warn("<getRepository> Exception thrown in frontend: ", e);
            throw e;
        } catch (RuntimeException e) {
            this.logger.warn("<getRepository> Exception thrown in frontend: ", e);
            throw e;
        }
    }

    public WrappedDmxmlResponse sendDmxmlRequest(WrappedDmxmlRequest request)
            throws InvalidDmxmlRequestException, AuthenticationFailedException, InvalidDmxmlResponseException {
        try {
            return doSendDmxmlRequest(request);
        } catch (AuthenticationFailedException e) {
            this.logger.warn("<getRepository> Exception thrown in frontend: ", e);
            throw e;
        } catch (InvalidDmxmlRequestException e) {
            this.logger.warn("<getRepository> Exception thrown in frontend: ", e);
            throw e;
        } catch (InvalidDmxmlResponseException e) {
            this.logger.warn("<getRepository> Exception thrown in frontend: ", e);
            throw e;
        } catch (RuntimeException e) {
            this.logger.warn("<getRepository> Exception thrown in frontend: ", e);
            throw e;
        } catch (Exception e) {
            this.logger.warn("<getRepository> Exception thrown in frontend: ", e);
            throw new RuntimeException(e);
        }
    }

    private void closeExistingSession(HttpServletRequest servletRequest) {
        final HttpSession existingSession = servletRequest.getSession(false);
        if (existingSession != null) {
            this.logger.warn("<login> Closing existing session " + existingSession.getId());
            existingSession.invalidate();
        }
    }

    private void prepareSession(HttpSession session, LoginData loginData, Zone zone) {
        session.setAttribute(LOGIN_DATA_ATTRIBUTE, loginData);
        session.setAttribute(UserServiceImpl.SESSION_KEY_ZONENAME, zone.getName());
    }

    private BlocksDocumentation doGetRepository() throws AuthenticationFailedException {
        final HttpSession session = getSession();
        if (session == null) {
            throw new AuthenticationFailedException("no session");
        }
        final String zoneName = (String) session.getAttribute(UserServiceImpl.SESSION_KEY_ZONENAME);
        if (zoneName == null) {
            throw new AuthenticationFailedException("invalid session");
        }
        return getDocuRepository(this.mmwebService.getZone(zoneName));
    }

    private WrappedDmxmlResponse doSendDmxmlRequest(WrappedDmxmlRequest request)
            throws AuthenticationFailedException, InvalidDmxmlRequestException, InvalidDmxmlResponseException {
        final String rawRequest = request.getDmxmlRequest();
        final RequestType dmxmlRequest = unmarshallRequest(rawRequest);

        final DmxmlDocuServiceMethod c = createMethod(dmxmlRequest);

        final WrappedDmxmlResponse result = new WrappedDmxmlResponse();

        try {
            validateXmlAgainstZoneSchema(rawRequest, c.zone);
        } catch (ValidationException e) {
            throw new InvalidDmxmlRequestException("Request is not valid with respect to XML Schema:\n" + e.getCause().getMessage(), rawRequest);
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<sendDmxmlRequest> rawRequest = " + rawRequest);
        }

        c.sendDmxmlRequestToMmwebService();
        result.setDmxmlResponse(c.rawResponse);

        try {
            validateXmlAgainstZoneSchema(c.rawResponse, c.zone);
        } catch (ValidationException e) {
            throw new InvalidDmxmlResponseException("Response is not valid with respect to XML Schema:\n" + e.getCause().getMessage(), c.rawResponse);
        }

        // Wrap and add docu
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<sendDmxmlRequest> rawResponse = " + c.rawResponse);
        }
        try {
            final XmlDocuAugmenter augmenter = getZoneSpecificAugmenter(c.zone);
            final Element xmlTreeRoot = augmenter.parseAndAugment(new StreamSource(new StringReader(c.rawResponse)));
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<sendDmxmlRequest> xmlTreeRoot:\n" + xmlTreeRoot.getSubtreeAsString());
            }
            result.setXmlTreeRoot(xmlTreeRoot);
        } catch (Exception e) {
            throw new InvalidDmxmlResponseException("Error parsing response.", c.rawResponse);
        }
        return result;
    }

    private DmxmlDocuServiceMethod createMethod(
            RequestType dmxmlRequest) throws AuthenticationFailedException {
        return new DmxmlDocuServiceMethod(this, dmxmlRequest);
    }

    private void fillAuthenticationWithSampleIndexFixedValues(BlocksDocumentation docu, Zone zone) {
        final Map<String, String[]> sampleFormParams =
                zone.getParameterMap(Collections.<String, String[]>emptyMap(), "sample-form.html");
        docu.setDefaultAuthentication(firstOrNull(sampleFormParams.get("authentication")));
        docu.setDefaultAuthenticationType(firstOrNull(sampleFormParams.get("authenticationType")));
    }

    /**
     * Checks whether the zone name and password in loginData matches the zone found by zoneResolver.
     * @param loginData login data from client (after login stored in session)
     * @param servletRequest the current request
     * @return the found zone upon granted access
     * @throws AuthenticationFailedException if one of the checks fails
     */
    Zone checkAuthentication(LoginData loginData,
            HttpServletRequest servletRequest) throws AuthenticationFailedException {
        Zone zone = this.mmwebService.getZone(loginData.getZone());
        // TODO ZoneResolver prefers resolution by session first; so this is still vulnerable to cross-zone requests
        final Zone requestZone = this.mmwebService.resolveZone(servletRequest);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<checkAuthentication> logged into zone " + zone.getName());
            this.logger.debug("<checkAuthentication> request for zone " + requestZone.getName());
        }
        if (zone == null) {
            throw new AuthenticationFailedException(
                    "Authorization needed: Client HTML file was accessed from illegal URL.");
        }
        if (requestZone == null) {
            throw new AuthenticationFailedException(
                    "Authorization needed: Could not resolve request zone.");
        }
        if (!zone.getName().equals(requestZone.getName())) {
            throw new AuthenticationFailedException(
                    "Zone mismatch between client HTML access URL zone and request URL");
        }
        final String zonePassword = getZonePassword(zone);
        if (zonePassword == null) {
            throw new AuthenticationFailedException(
                    "Authorization needed: Zone " + zone.getName() + " does not allow dmxml documentation.");
        }
        if (!zonePassword.equals(loginData.getPassword())) {
            throw new AuthenticationFailedException(
                    "Authorization needed: Wrong password for zone  " + zone.getName() + ".");
        }
        return zone;
    }

    private String getZonePassword(Zone zone) {
        final String[] passwords = zone.getParameterMap(
                Collections.<String, String[]>emptyMap(), "dmxml-docu").get("password");
        return passwords != null && passwords.length > 0 ? passwords[0] : null;
    }

    private void validateXmlAgainstZoneSchema(final String xml, Zone zone)
            throws ValidationException {
        try {
            Schema schema = getZoneSchema(zone);
            final Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    // validation errors should not produce warnings
                    // TODO maybe handle warnings anyway?
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    onError(exception);
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    onError(exception);
                }

                private void onError(SAXParseException exception) {
                    // TODO manually use exception.getLineNumber() if needed
                    throw new IllegalArgumentException(exception);
                }
            });
            validator.validate(new StreamSource(new StringReader(xml)));
        } catch (IllegalArgumentException e) {
            // catch and re-throw non-runtime exception
            throw new ValidationException(e.getCause());
        } catch (Exception e) {
            throw new InternalDmxmlServerError("Failed validating request xml", e);
        }
    }

    private Schema getZoneSchema(Zone zone) {
        if (!zoneSchemas.containsKey(zone.getName())) {
            try {
                this.logger.info("<getZoneSchema> Loading schema for zone " + zone.getName());
                final File schemaFile = getZoneSchemaFile(zone);
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                final Schema schema = schemaFactory.newSchema(schemaFile);
                this.zoneSchemas.put(zone.getName(), schema);
            } catch (Exception e) {
                this.logger.warn("<getZoneSchema> Failed creating schema for zone " + zone.getName(), e);
            }
        }
        return zoneSchemas.get(zone.getName());
    }

    private File getZoneSchemaFile(Zone zone) throws IOException {
        final Map<String, Object> contextMap = zone.getContextMap("");
        this.logger.info("<getZoneSchemaFile> contextMap=" + contextMap);
        final String xsdName = (String) contextMap.get("xsdName");
        this.logger.info("<getZoneSchemaFile> xsdName=" + xsdName);
        return new File(zoneXsdPath.getFile(), xsdName);
    }


    private RequestType unmarshallRequest(String rawRequest) throws InvalidDmxmlRequestException {
        try {
            final RequestType request;
            synchronized (this.dmxmlRequestUnmarshaller) {
                @SuppressWarnings("unchecked") // $NON-NLS$
                final JAXBElement<RequestType> element =
                        (JAXBElement<RequestType>) this.dmxmlRequestUnmarshaller.unmarshal(
                                new StreamSource(new StringReader(rawRequest)));
                request = element.getValue();
            }
            return request;
        } catch (Exception e) {
            logger.warn("Cannot unmarshal request xml", e);
            throw new InvalidDmxmlRequestException("Error unmarshalling request xml ", rawRequest); // $NON-NLS$
        }
    }

    private BlocksDocumentation getDocuRepository(Zone zone) {
        final BlocksDocumentation cached = blockDocu.get(zone.getName());
        if (cached == null) {
            this.logger.info("<getDocuRepository> Creating block documentation repository for zone " + zone);
            try {
                final DmxmlDocumentationRepository repo;
                synchronized (this.blockDocuUnmarshaller) {
                    @SuppressWarnings("unchecked")
                    final JAXBElement<DmxmlDocumentationRepository> element =
                            (JAXBElement<DmxmlDocumentationRepository>) blockDocuUnmarshaller.unmarshal(
                                    new StreamSource(new BufferedInputStream(new FileInputStream(
                                            new File(blocksFilesPath.getFile(), zone.getName() + "-blocks.xml")))));
                    repo = element.getValue();
                }
                final BlocksDocumentation newRepo = new BlocksDocumentation();
                final ArrayList<String> sortedNames = new ArrayList<>();
                final HashMap<String, DmxmlBlockDocumentation> map = new HashMap<>(repo.getDmxmlBlocks().getBlock().size());
                for (DmxmlBlockDocumentation block : repo.getDmxmlBlocks().getBlock()) {
                    sortedNames.add(block.getBlockName());
                    map.put(block.getBlockName(), block);
                }
                // TODO sort names according to some fancy ordering
                Collections.sort(sortedNames);
                newRepo.setBlockNames(sortedNames);
                newRepo.setBlockDocu(map);
                fillAuthenticationWithSampleIndexFixedValues(newRepo, zone);
                this.blockDocu.put(zone.getName(), newRepo);
            } catch (Exception e) {
                this.logger.warn("<getDocuRepository> Could not load documentation repository for zone " + zone, e);
            }
        }
        return blockDocu.get(zone.getName());
    }

    private XmlDocuAugmenter getZoneSpecificAugmenter(Zone zone) {
        final XmlDocuAugmenter cached = this.xmlAugmenter.get(zone.getName());
        if (cached == null) {
            try {
                XmlDocuAugmenter newAugmenter = new XmlDocuAugmenter();
                newAugmenter.setXsdFile(new FileSystemResource(getZoneSchemaFile(zone)));
                newAugmenter.setXsdTypeDocuProvider(newXsdDocuProvider(zone.getName()));
                newAugmenter.afterPropertiesSet();
                this.xmlAugmenter.put(zone.getName(), newAugmenter);
            } catch (Exception e) {
                throw new RuntimeException("Could not load dmxml response dictionary for zone " + zone.getName(), e);
            }
        }
        return this.xmlAugmenter.get(zone.getName());
    }

    private XsdDocuProvider newXsdDocuProvider(String zone) throws Exception {
        final XsdDocuProvider docuProvider = new XsdDocuProvider();
        docuProvider.setDictionaryFile(new ClassPathResource("/zones/" + zone + "-dict.xml"));
        docuProvider.setDictionaryUnmarshaller(getDictionaryUnmarshaller());
        docuProvider.afterPropertiesSet();
        return docuProvider;
    }

    private static <T> T firstOrNull(T[] array) {
        return array == null || array.length == 0 ? null : array[0];
    }


    public Resource getBlocksFilesPath() {
        return blocksFilesPath;
    }

    public void setBlocksFilesPath(Resource blocksFilesPath) {
        this.blocksFilesPath = blocksFilesPath;
    }

    public Resource getZoneXsdPath() {
        return zoneXsdPath;
    }

    public void setZoneXsdPath(Resource zoneXsdPath) {
        this.zoneXsdPath = zoneXsdPath;
    }

    public Unmarshaller getBlockDocuUnmarshaller() {
        return blockDocuUnmarshaller;
    }

    public void setBlockDocuUnmarshaller(Unmarshaller blockDocuUnmarshaller) {
        this.blockDocuUnmarshaller = blockDocuUnmarshaller;
    }

    public MmwebServiceImpl getMmwebService() {
        return mmwebService;
    }

    public void setMmwebService(MmwebServiceImpl mmwebService) {
        this.mmwebService = mmwebService;
    }

    public Unmarshaller getDmxmlRequestUnmarshaller() {
        return dmxmlRequestUnmarshaller;
    }

    public void setDmxmlRequestUnmarshaller(Unmarshaller dmxmlRequestUnmarshaller) {
        this.dmxmlRequestUnmarshaller = dmxmlRequestUnmarshaller;
    }

    public Unmarshaller getDictionaryUnmarshaller() {
        return dictionaryUnmarshaller;
    }

    public void setDictionaryUnmarshaller(Unmarshaller dictionaryUnmarshaller) {
        this.dictionaryUnmarshaller = dictionaryUnmarshaller;
    }
}
