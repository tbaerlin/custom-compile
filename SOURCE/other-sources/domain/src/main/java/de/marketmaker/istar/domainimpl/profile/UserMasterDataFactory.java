/*
 * UserMasterDataFactory.java
 *
 * Created on 14.07.2008 14:25:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.domain.profile.UserMasterData;

/**
 * Creates a UserMasterData object from an xml input stream<p>
 * See UserStamm... requests in <a href="http://vwd-ent.market-maker.de:1968/">vwd-ent</a>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserMasterDataFactory {

    public UserMasterData read(InputStream is) throws Exception {
        return getUserMasterData(FileCopyUtils.copyToByteArray(is));
    }

    UserMasterData getUserMasterData(byte[] data) throws JDOMException, IOException {
        final SAXBuilder builder = new SAXBuilder();
        final Document document = builder.build(new ByteArrayInputStream(data));
        final Element root = document.getRootElement();
        return (isAcceptable(root) ? new UserMasterDataImpl(data) : null);
    }

    private boolean isAcceptable(Element root) {
        return root.getChild("vwdUser") != null || root.getChild("DZBUser") != null;
    }

    public static void main(String[] args) throws Exception {
        final UserMasterDataImpl user = read("vwdUserStamm_ByVwdId");
        System.out.println(user);
        System.out.println(user.nodeText("Mandator/Services/ServiceType[@id='10']/Service/Name"));

        final UserMasterDataImpl user2 = read("UserStamm_ByVwdId");
        System.out.println(user2);
        System.out.println(user2.nodeText("Services/ServiceArt[@id='10']/Service/Name"));
    }

    private static UserMasterDataImpl read(final String method) throws Exception {
        URL u = new URL("http://vwd-ent:1968/vwdPermissions.asmx/" + method + "?AppID=7&vwdID=46678");
        final BufferedInputStream is = new BufferedInputStream(u.openStream());
        return (UserMasterDataImpl) new UserMasterDataFactory().read(is);
    }
}
