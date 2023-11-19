/*
 * PmCli.java
 *
 * Created on 08.10.14 09:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

import java.util.Arrays;
import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import de.marketmaker.istar.common.util.DateUtil;

/**
 * @author oflege
 */
public class PmTester {
    public static void main(String[] args) throws MMTalkException {
        double x = DateUtil.javaDateToComDate(new DateTime(2099, 12, 31, 23, 59).toDate());
        System.out.println(x);
        System.out.println(DateUtil.comDateToDate(x));


        final RmiProxyFactoryBean proxy = new RmiProxyFactoryBean();
        proxy.setServiceUrl("rmi://tepm1:9880/pmserver");
        proxy.setServiceInterface(MMService.class);
        proxy.afterPropertiesSet();

        MMService s = (MMService) proxy.getObject();

        MMServiceResponse r = s.getMMTalkTable(new MMTalkTableRequest(MMKeyType.SECURITY)
//                        .withKeys(Arrays.asList("710000", "_", "514000"))
                        .withKeys(Arrays.asList("43583", "_", "45102"))
                        .withFormulas(Arrays.asList("name"))
        );

        Object[] table = r.getData();
//        Object[] table = s.getMMTalkTable(new int[] { 43583, 4711, 45102}, null, new String[] { "id", "name"}, "");
        for (Object o : table) {
            System.out.printf("%-25s %s\n", String.valueOf(o), o != null ? o.getClass().getName() : "null");
        }
        System.out.println(Arrays.deepToString(table));
    }
}
