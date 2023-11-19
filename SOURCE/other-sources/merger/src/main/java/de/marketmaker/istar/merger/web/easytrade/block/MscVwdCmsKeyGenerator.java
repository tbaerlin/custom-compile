/*
 * BndRenditekennzahlen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.validator.RestrictedSet;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@MmInternal
public class MscVwdCmsKeyGenerator extends EasytradeCommandController implements InitializingBean {
    private static final byte[] VWD_CMS_DIGEST_KEY = "A5Bwr!$hu)84cXfRsw+#9tgq4e146".getBytes(US_ASCII);

    private static final byte[] ATTRAX_KEY_SUFFIX =
            "hktn8g3IlFx6eWCk4rgtlQ677FK8I7kMBW373e30XZXhQvNk2Zf3B24T26WdW24Qh80CU2H2GZ1tiY463q9O".getBytes(US_ASCII);

    private static final MessageDigest MD5;

    static {
        try {
            MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    protected URL attraxUrl;

    protected String attraxPubKeyURL = "http://dzbank.vwd.com/fondsadvisor/?getpubkey=q7yG66f";

    public static class Command {
        private String type;

        @RestrictedSet("platow,attrax")
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public MscVwdCmsKeyGenerator() {
        super(Command.class);
    }

    public void setAttraxPubKeyURL(String attraxPubKeyURL) {
        this.attraxPubKeyURL = attraxPubKeyURL;
    }

    public void afterPropertiesSet() throws Exception {
        this.attraxUrl = new URL(this.attraxPubKeyURL);
        this.logger.info("<afterPropertiesSet> attrax url = " + this.attraxUrl);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final String result = createResult(errors, ((Command) o).getType());
        return (result != null) ? new ModelAndView("mscvwdcmskeygenerator", "result", result) : null;
    }

    private String createResult(BindException errors, final String type) {
        if ("platow".equals(type)) {
            return getVwdCmsQueryParameters();
        }
        if ("attrax".equals(type)) {
            return createAttraxResult(errors);
        }
        errors.reject("invalid.type", "invalid: '" + type + "'");
        return null;
    }

    private String createAttraxResult(BindException errors) {
        final byte[] pubKey;

        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) this.attraxUrl.openConnection();
            conn.setReadTimeout(4000);
            conn.setConnectTimeout(4000);
            conn.connect();
            final int rc = conn.getResponseCode();
            if (rc != HttpServletResponse.SC_OK) {
                this.logger.warn("<createAttraxResult> invalid response code " + rc
                        + ", " + headers(conn));
                errors.reject("internal.error");
                return null;
            }
            is = conn.getInputStream();
            pubKey = getBytes(is, 32);
        } catch (IOException e) {
            this.logger.warn("<createAttraxResult> failed " + headers(conn), e);
            errors.reject("internal.error");
            return null;
        } finally {
            IoUtils.close(is);
        }

        return hexDigest(pubKey, ATTRAX_KEY_SUFFIX);
    }

    /**
     * Reads at most <tt>max</tt> bytes from is
     * @throws IOException if no or more than max bytes can be read from is
     */
    private byte[] getBytes(InputStream is, int max) throws IOException {
        byte[] tmp = new byte[max + 1];
        int i = 0;
        int n;
        while (i < tmp.length && (n = is.read(tmp, i, tmp.length - i)) != -1) {
            i += n;
        }
        if (i == 0) {
            throw new IOException("no data");
        }
        if (i == tmp.length) {
            throw new IOException("expected <= " + max + " bytes, got more!?");
        }
        return Arrays.copyOf(tmp, i);
    }

    private String headers(HttpURLConnection conn) {
        return (conn != null) ? conn.getHeaderFields().toString() : "null";
    }

    public static String getVwdCmsQueryParameters() {
        final long time = System.currentTimeMillis() / 1000;
        final byte[] input = Long.toString(time).getBytes(US_ASCII);
        return "unixtime=" + time + "&md5key=" + hexDigest(input, VWD_CMS_DIGEST_KEY);
    }

    private static String hexDigest(byte[]... parts) {
        return ByteUtil.toHexString(digest(parts));
    }

    private static byte[] digest(byte[]... parts) {
        synchronized (MD5) {
            MD5.reset();
            for (byte[] part : parts) {
                MD5.update(part);
            }
            return MD5.digest();
        }
    }
}