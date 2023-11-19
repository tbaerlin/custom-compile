/*
 * ProfileBeanFactory.java
 *
 * Created on 19.02.2009 13:53:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import static de.marketmaker.istar.domainimpl.profile.ProfileRequest.VWD_ENT_BY_VWD_ID;
import static org.springframework.util.StringUtils.hasText;

import de.marketmaker.istar.domain.profile.Profile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * To be used in application contexts where a single profile is needed that does not need to
 * be updated while the program is running. Supported configurations:<br>
 * <table border="1">
 * <tr><th>authenticationType</th><th>authentication</th></tr>
 * <tr><td>resource</td><td><em>name of resource profile</em></td></tr>
 * <tr><td>{@value de.marketmaker.istar.domainimpl.profile.ProfileRequest#VWD_ENT_BY_VWD_ID}</td><td><em>appID:vwdID</em> or <em>vwdID</em></td></tr>
 * </table>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ProfileFactoryBean extends AbstractFactoryBean<Profile> {
    private static final Pattern APP_ID_AND_VWD_ID = Pattern.compile("(?:(\\d+):)?(\\d+)");

    private String authentication;

    private String authenticationType;

    private final String vwdEntUrlPrefix = "http://vwd-ent:1968/vwdPermissions.asmx/";

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public Class<? extends Profile> getObjectType() {
        return Profile.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.authenticationType == null) {
            throw new NullPointerException("authenticationType");
        }
        if (this.authentication == null) {
            throw new NullPointerException("authentication");
        }
        super.afterPropertiesSet();
    }

    protected Profile createInstance() throws Exception {
        if ("resource".equals(this.authenticationType)) {
            return ProfileFactory.createInstance(
                ResourcePermissionProvider.getInstance(this.authentication));
        }

        if (VWD_ENT_BY_VWD_ID.equals(this.authenticationType)) {
            final HttpClient c = HttpClientBuilder.create().build();
            final HttpUriRequest method = createVwdEntGetMethod();
            final HttpResponse resp = c.execute(method);
            final StatusLine statusLine = resp.getStatusLine();
            final int status = statusLine.getStatusCode();
            if (status != HttpStatus.SC_OK) {
                throw new IllegalStateException(statusLine.toString());
            }
            return new VwdProfileFactory().read(EntityUtils.toString(resp.getEntity()));
        }

        throw new IllegalArgumentException("cannot handle authenticationType '"
            + this.authenticationType + "'");
    }

    private HttpUriRequest createVwdEntGetMethod() {
        final Matcher m = APP_ID_AND_VWD_ID.matcher(this.authentication);
        if (!m.matches()) {
            throw new IllegalArgumentException(
                "invalid authentication '" + this.authentication + "'");
        }
        final RequestBuilder builder = RequestBuilder.get();
        if (hasText(m.group(1))) {
            builder.setUri(this.vwdEntUrlPrefix + "Ent_ByVwdId");
            builder.addParameter("appID", m.group(1))
                .addParameter("vwdID", m.group(2));

        } else {
            builder.setUri(this.vwdEntUrlPrefix + "Ent_ByVwdId_NOAPP");
            builder.addParameter("vwdID", m.group(2));
        }
        return builder.build();
    }

    public static void main(String[] args) throws Exception {
        ProfileFactoryBean b = new ProfileFactoryBean();
        b.setAuthenticationType(ProfileRequest.VWD_ENT_BY_VWD_ID);
        b.setAuthentication("34:120043");
        b.afterPropertiesSet();

        System.out.println(b.getObject());
    }
}
