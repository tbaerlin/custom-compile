/*
 * UserMasterDataFactory.java
 *
 * Created on 14.07.2008 14:13:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.File;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.marketmaker.istar.domain.profile.UserMasterData;

/**
 * UserMasterDataProvider that retrieves UserMasterData objects from the vwd entitlement system.<p>
 * See UserStamm... requests in <a href="http://vwd-ent.market-maker.de:1968/">vwd-ent</a>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserMasterDataSource implements UserMasterDataProvider {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * AppId used in backend requests, UserMasterDataRequest can override this value
     */
    private String appId = "7";

    /**
     * Directory in which xml-Files are stored for users that cannot be retrieved from the
     * entitlement service
     */
    private File userDirectory;

    private String baseUri = "http://vwd-ent:1968/vwdPermissions.asmx/";

    private final UserMasterDataFactory userFactory = new UserMasterDataFactory();

    private RestTemplate restTemplate;

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public UserMasterDataResponse getUserMasterData(UserMasterDataRequest request) {
        final UserMasterData user = readFromFile(request.getId());
        if (user != null) {
            return new UserMasterDataResponse(user);
        }

        return getUserMasterDataFromWebservice(request);
    }

    public void setAppId(String appId) {
        this.appId = appId;
        this.logger.info("<setAppId> " + this.appId);
    }

    public void setUserDirectory(String userDirectory) {
        if (StringUtils.hasText(userDirectory)) {
            this.userDirectory = new File(userDirectory);
        }
    }

    private UserMasterDataResponse getUserMasterDataFromWebservice(UserMasterDataRequest request) {
        UriComponentsBuilder b;
        switch (request.getType()) {
            case LOGIN:
                b = UriComponentsBuilder.fromHttpUrl(this.baseUri + "vwdUserStamm_ByLogin")
                        .queryParam("AppId", getAppId(request.getAppId()))
                        .queryParam("Login", request.getId())
                        .queryParam("MandantId", request.getClientId());
                break;
            case VWD:
                b = UriComponentsBuilder.fromHttpUrl(this.baseUri + "vwdUserStamm_ByVwdId")
                        .queryParam("AppId", getAppId(request.getAppId()))
                        .queryParam("vwdId", request.getId());
                break;
            case GENO:
                return UserMasterDataResponse.createInvalid();
            default:
                this.logger.error("<getUserMasterData> unknown type: " + request.getType());
                return UserMasterDataResponse.createInvalid();
        }

        URI uri = b.build().toUri();
        try {
            byte[] xml = this.restTemplate.getForObject(uri, byte[].class);
            UserMasterData data = this.userFactory.getUserMasterData(xml);
            if (data == null) {
                return UserMasterDataResponse.createInvalid();
            }
            return new UserMasterDataResponse(data);
        } catch (Exception e) {
            this.logger.error("<getUserMasterData> failed for " + request
                    + ", uri=" + uri + " : " + e.getMessage());
        }
        return UserMasterDataResponse.createInvalid();
    }

    private String getAppId(final String id) {
        return (id != null) ? id : this.appId;
    }

    private UserMasterData readFromFile(String name) {
        if (this.userDirectory == null) {
            return null;
        }
        final File f = new File(this.userDirectory, name + ".xml");
        if (!f.exists()) {
            return null;
        }
        try {
            this.logger.debug("<readFromFile> reading " + f.getAbsolutePath());
            return this.userFactory.getUserMasterData(FileCopyUtils.copyToByteArray(f));
        } catch (Exception e) {
            this.logger.warn("<readFromFile> failed to read " + f.getAbsolutePath(), e);
        }
        return null;
    }
}
