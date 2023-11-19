/*
 * VwdEntProfileProvider.java
 *
 * Created on 30.06.2008 11:35:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTimeConstants;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.marketmaker.istar.common.http.RestTemplateFactory;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.domainimpl.profile.ResourcePermissionProvider;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.domainimpl.profile.VwdProfileFactory;

import static de.marketmaker.istar.merger.provider.profile.VwdEntAccessMethod.*;

/**
 * ProfileProvider that uses the vwd entitlement server (vwd-ent:1968) to request profile
 * information. Supported methods are Ent_ByLogin, Ent_ByUGenoId, and Ent_ByVwdId.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class VwdEntProfileProvider implements ProfileProvider {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String baseUri = "http://vwd-ent:1968/vwdPermissions.asmx/";

    private String defaultAppId = "7";

    private VwdEntProfileProviderFile providerFile;

    private RestTemplate restTemplate;

    private File dumpDir;

    public VwdEntProfileProvider() {
        String p = System.getProperty("istar.home");
        if (p != null) {
            File dir = new File(p, "temp");
            if (dir.isDirectory()) {
                dumpDir = dir;
            }
        }
    }

    public void setDumpDir(File dumpDir) {
        this.dumpDir = dumpDir;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void setProviderFile(VwdEntProfileProviderFile providerFile) {
        this.providerFile = providerFile;
    }

    public ProfileResponse getProfile(ProfileRequest request) {
        final VwdEntAccessMethod method = VwdEntAccessMethod.getMethod(request);
        return getProfile(request, method);
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "genoId", description = "genoId"),
            @ManagedOperationParameter(name = "appId", description = "appId")
    })
    public String getProfileByGenoId(String genoId, String appId) {
        return getProfileAsString(getProfileRequest(genoId, null, appId), BY_GENO_ID);
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "login", description = "login"),
            @ManagedOperationParameter(name = "clientId", description = "clientId"),
            @ManagedOperationParameter(name = "appId", description = "appId")
    })
    public String getProfileByLogin(String login, String clientId, String appId) {
        return getProfileAsString(getProfileRequest(login, clientId, appId), BY_LOGIN);
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "vwdId", description = "vwdId"),
            @ManagedOperationParameter(name = "appId", description = "appId")
    })
    public String getProfileByVwdId(String vwdId, String appId) {
        return getProfileAsString(getProfileRequest(vwdId, null, appId), BY_VWD_ID);
    }

    private String getProfileAsString(final ProfileRequest request,
            final VwdEntAccessMethod method) {
        return String.valueOf(getProfile(request, method).getProfile());
    }

    public void setDefaultAppId(String defaultAppId) {
        this.defaultAppId = defaultAppId;
        this.logger.info("<setDefaultAppId> " + this.defaultAppId);
    }

    private String getAppId(ProfileRequest request) {
        if (request.getApplicationId() != null) {
            return request.getApplicationId();
        }
        this.logger.warn("<getAppId> no appId set -> using defaultAppId for " + request);
        return this.defaultAppId;
    }


    private ProfileResponse getProfile(ProfileRequest request, VwdEntAccessMethod method) {
        final String appId = getAppId(request);

        if (this.providerFile != null) {
            final ProfileResponse response = this.providerFile.getProfile(request, method, appId);
            if (response != null) {
                this.logger.info("<getProfile> for " + request + " from file");
                return response;
            }
        }

        if (!method.isValid(request)) {
            this.logger.warn("<getProfile> invalid: " + request + " for " + method);
            return ProfileResponse.invalid();
        }

        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(this.baseUri + method.getUri());
        method.configure(b, appId, request);
        URI uri = b.build().toUri();

        final TimeTaker tt = new TimeTaker();
        String xml = null;
        try {
            xml = this.restTemplate.getForObject(uri, String.class);
            VwdProfile result = new VwdProfileFactory().read(xml);
            result.setAppId(appId);
            if (result.getVwdId() == null) {
                if (request.isOftenFailingGenobrokerRequest()) {
                    this.logger.info("<getProfile> no vwdId in result for " + request);
                }
                else {
                    this.logger.warn("<getProfile> no vwdId in result for " + request);
                }
                return ProfileResponse.invalid();
            }
            tt.stop();
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getProfile> " + request + " => " + result + ", took " + tt);
            }
            else if (tt.getElapsedMs() > DateTimeConstants.MILLIS_PER_SECOND) {
                this.logger.warn("<getProfile> slow " + request + " took " + tt);
            }
            else {
                this.logger.info("<getProfile> for " + request + " took " + tt);
            }
            return new ProfileResponse(result);
        } catch (Exception e) {
            String path = storeXmlOnException(xml);
            if (path != null) {
                this.logger.error("<getProfile> failed for " + request
                        + ", uri=" + uri + " : " + e.getMessage() + ", stored response in " + path);
            }
            else {
                this.logger.error("<getProfile> failed for " + request
                        + ", uri=" + uri + " : " + e.getMessage());
            }
        }
        return ProfileResponse.invalid();
    }

    private String storeXmlOnException(String xml) {
        if (xml == null) {
            return null;
        }
        try {
            File tempFile = File.createTempFile("profile-", ".xml", this.dumpDir);
            FileCopyUtils.copy(xml, new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8));
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            this.logger.warn("<storeXmlOnException> failed", e);
            return null;
        }
    }

    private ProfileRequest getProfileRequest(String authentication, String clientId, String appId) {
        final ProfileRequest request = new ProfileRequest(null, authentication);
        if (StringUtils.hasText(clientId)) {
            request.setClientId(clientId);
        }
        if (StringUtils.hasText(appId)) {
            request.setApplicationId(appId);
        }
        return request;
    }

    public static void main(String[] args) {
        final String resourceProfileName = "dtring";
//        final String appId = "34";
        final String vwdId = "120137";

        VwdEntProfileProvider pp = new VwdEntProfileProvider();
        pp.setRestTemplate(new RestTemplateFactory().getObject());
        final ProfileRequest request = new ProfileRequest("vwd-ent:ByVwdId", vwdId);
//        request.setApplicationId(appId);
        final ProfileResponse response = pp.getProfile(request);

        final Profile vwdEnt2006 = response.getProfile();
        final Profile resourceProfile = ProfileFactory.createInstance(ResourcePermissionProvider.getInstance(resourceProfileName));

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println(vwdId + " <==> " + resourceProfileName);
        System.out.println("######################################");
        System.out.println(vwdEnt2006);
        System.out.println(resourceProfile);
        for (Profile.Aspect aspect : Profile.Aspect.values()) {
            for (PriceQuality priceQuality : PriceQuality.values()) {
                final BitSet vwdEnt2006BS = vwdEnt2006.toEntitlements(aspect, priceQuality);
                final BitSet resourceBS = resourceProfile.toEntitlements(aspect, priceQuality);

                final BitSet notInLocal = diff(vwdEnt2006BS, resourceBS);
                final BitSet notInCentral = diff(resourceBS, vwdEnt2006BS);

                if (notInLocal.isEmpty() && notInCentral.isEmpty()) {
                    continue;
                }
                System.out.println();
                System.out.println(aspect.name() + "/" + priceQuality.name());
                System.out.println("  in vwdEnt2006, but not resourceProfile: " + bitsetToString(notInLocal));
                System.out.println("  in resourceProfile, but not vwdEnt2006: " + bitsetToString(notInCentral));
            }
        }

    }

    private static String bitsetToString(BitSet bs) {
        final StringBuilder sb = new StringBuilder();
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(EntitlementsVwd.toEntitlement(i));
        }
        return sb.toString();
    }

    private static BitSet diff(BitSet a, BitSet b) {
        final BitSet c = (BitSet) a.clone();
        c.andNot(b);
        return c;
    }
}
