/*
 * IpAddressFilterUtil.java
 *
 * Created on 30.11.15 15:34
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.http;

import java.beans.PropertyEditorSupport;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author tkiesgen
 */
public class IPv4AddressRangePredicate implements Predicate<InetAddress> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private List<IPMask> ipMasks;

    public void setIpMasks(List<IPMask> ipMasks) {
        this.ipMasks = ipMasks;
    }

    @Override
    public boolean test(InetAddress ipAddress) {
        if (!(ipAddress instanceof Inet4Address)) {
            this.logger.info("<test> no IPv4 address: " + ipAddress + " -> false");
            return false;
        }
        return this.ipMasks.stream().anyMatch(m -> m.matches((Inet4Address) ipAddress));
    }

    @SuppressWarnings("unused")
    public static class IPMaskEditor extends PropertyEditorSupport {
        public String getAsText() {
            final IPMask mask = (IPMask) getValue();
            if (mask == null) {
                return null;
            }
            return mask.print();
        }

        public void setAsText(String text) throws IllegalArgumentException {
            if (!StringUtils.hasText(text)) {
                return;
            }
            try {
                setValue(IPMask.getIPMask(text));
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Represents an IP range based on an address/mask.
     * @see "http://stackoverflow.com/questions/4209760/validate-an-ip-address-with-mask"
     */
    public static class IPMask {
        private Inet4Address i4addr;

        private byte maskCtr;

        private int addrInt;

        private int maskInt;

        public IPMask(Inet4Address i4addr, byte mask) {
            this.i4addr = i4addr;
            this.maskCtr = mask;

            this.addrInt = addrToInt(i4addr);
            this.maskInt = ~((1 << (32 - maskCtr)) - 1);
        }

        /**
         * IPMask factory method.
         * @param addrSlashMask IP/Mask String in format "nnn.nnn.nnn.nnn/mask".
         * If the "/mask" is omitted, "/32" (just the single address) is assumed.
         * @return a new IPMask
         * @throws UnknownHostException if address part cannot be parsed by InetAddress
         */
        public static IPMask getIPMask(String addrSlashMask) throws UnknownHostException {
            int pos = addrSlashMask.indexOf('/');
            String addr;
            byte maskCtr;
            if (pos == -1) {
                addr = addrSlashMask;
                maskCtr = 32;
            }
            else {
                addr = addrSlashMask.substring(0, pos);
                maskCtr = Byte.parseByte(addrSlashMask.substring(pos + 1));
            }
            return new IPMask((Inet4Address) InetAddress.getByName(addr), maskCtr);
        }

        /**
         * Test given IPv4 address against this IPMask object.
         * @param testAddr address to check.
         * @return true if address is in the IP Mask range, false if not.
         */
        public boolean matches(Inet4Address testAddr) {
            int testAddrInt = addrToInt(testAddr);
            return ((addrInt & maskInt) == (testAddrInt & maskInt));
        }

        /**
         * Convenience method that converts String host to IPv4 address.
         * @param addr IP address to match in nnn.nnn.nnn.nnn format or hostname.
         * @return true if address is in the IP Mask range, false if not.
         */
        public boolean matches(String addr) {
            final Inet4Address address;
            try {
                address = (Inet4Address) InetAddress.getByName(addr);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
            return matches(address);
        }

        /**
         * Converts IPv4 address to integer representation.
         */
        private static int addrToInt(Inet4Address i4addr) {
            byte[] ba = i4addr.getAddress();
            return (ba[0] << 24)
                    | ((ba[1] & 0xFF) << 16)
                    | ((ba[2] & 0xFF) << 8)
                    | (ba[3] & 0xFF);
        }

        public String print() {
            return i4addr.getHostAddress() + "/" + maskCtr;
        }

        @Override
        public String toString() {
            return "IPMask(" + i4addr.getHostAddress() + "/" + maskCtr + ")";
        }
    }
}
