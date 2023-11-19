/*
 * ParameterMappingHandlerInterceptor.java
 *
 * Created on 29.01.2008 14:03:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * A KagidIDMSHandlerInterceptor that maps hardcoded IDMS kag ids to the respective issuername key.
 * <p/>
 * Note: Relies on the fact that it can actually add a parameter the the requests parameter
 * map. This will not be the case for genuine Tomcat request objects, so in that case the
 * original request will have to be wrapped with a custom request object (e.g., a
 * {@link RequestWrapper}).
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class KagidIDMSHandlerInterceptor extends KagidHandlerInterceptor {
    private static final Map<String, String> IDMS_TO_MS = new HashMap<>();
    private static final Map<String, String> IDMS_TO_VF = new HashMap<>();

    static {
        IDMS_TO_MS.put("1091181144.0", "1. SICAV Investmentgesellschaft"); //1. Sicav
        IDMS_TO_MS.put("10017", "3 Banken-Generali Investment GmbH"); //3 Banken Generali Investment-Gesellschaft mbH
        IDMS_TO_MS.put("24", "Aachener Grundvermögen KAG mbH");
        IDMS_TO_MS.put("25", "Aberdeen Global SICAV");
        IDMS_TO_MS.put("22", "ABN AMRO Asset Management");
        IDMS_TO_MS.put("109229831.0", "ABN AMRO"); //ABN AMRO Investment Funds SA
        IDMS_TO_MS.put("1092229831.0", "Abrias Investment Management AG");
        IDMS_TO_MS.put("1215465023.0", ""); //Activest Investmentgesellschaft Schweiz AG
        IDMS_TO_MS.put("981479324.0", ""); //Adviser I Funds SICAV
        IDMS_TO_MS.put("972050982.0", "Anglo Irish Bank Corp Plc"); //AIBC Anglo Irish Bank (Austria) KAG mbH
        IDMS_TO_MS.put("297", "AIG Investments Fund Management Ltd");
        IDMS_TO_MS.put("1193249465.1", "Alceda Fund Management SA");
        IDMS_TO_MS.put("451", "Alger SICAV");
        IDMS_TO_MS.put("358", "AllianceBernstein (Luxembourg) SA");
        IDMS_TO_MS.put("289", "Allianz Global Investors Ireland Ltd");
        IDMS_TO_MS.put("94", "Allianz Global Investors KAG mbH");
        IDMS_TO_MS.put("284", "Allianz Global Investors Luxembourg S.A."); //Allianz Global Investors Luxembourg SA
        IDMS_TO_MS.put("1048253343.0", "Allianz Invest Kapitalanlagegesellschaft mbH");
        IDMS_TO_MS.put("35", "ALTE LEIPZIGER Trust Investment-Gesellschaft mbH");
        IDMS_TO_MS.put("9", ""); //American Express Bank Asset Management (Lux) SA
        IDMS_TO_MS.put("1004624954.0", "AmpegaGerling Investment GmbH");
        IDMS_TO_MS.put("1195672257.0", ""); //Ashmore SICAV
        IDMS_TO_MS.put("1213996251.0", "Aviva Investors Luxembourg SA"); //Aviva Fund Services SA
        IDMS_TO_MS.put("38", "AXA Investment Managers Deutschland GmbH");
        IDMS_TO_MS.put("993480549.0", "AXA Investment Managers Paris"); //AXA Investment Managers Paris SA
        IDMS_TO_MS.put("1047053898.0", "Axxion"); //Axxion SA
        IDMS_TO_MS.put("54", "Baden-Württembergische Investmentgesellschaft mbH");
        IDMS_TO_MS.put("1092229802.0", "Banque et Caisse d'Epargne de l'Etat Luxembourg");
        IDMS_TO_MS.put("1192471855.0", "Barclays Global Investors"); //Barclays Global Investors (Deutschland) AG
        IDMS_TO_MS.put("292", "Baring Fund Managers Limited");
        IDMS_TO_MS.put("10009", "BAWAG PSK Invest GmbH");
        IDMS_TO_MS.put("1154956235.0", "BayernInvest Kapitalanlagegesellschaft mbH");
        IDMS_TO_MS.put("426", "BayernInvest Luxembourg SA");
        IDMS_TO_MS.put("1028808426.0", "Berenberg Lux Invest SA");
        IDMS_TO_MS.put("45", "BL SICAV");
        IDMS_TO_MS.put("165", "BlackRock"); //BlackRock Global Funds
        IDMS_TO_MS.put("1167401446.0", "BlueBay Asset Management Plc"); //BlueBay Funds SICAV
        IDMS_TO_MS.put("393", "BNP Paribas"); //BNP Paribas Asset Management Luxembourg SA
        IDMS_TO_MS.put("1112880768.0", "Bâloise Fund Invest"); //Bâloise Fund Invest (Lux) Sicav
        IDMS_TO_MS.put("1092229833.0", "C-Quadrat Kapitalanlage Aktiengesellschaft");
        IDMS_TO_MS.put("1141999838.0", "Callander Managers SA");
        IDMS_TO_MS.put("386", ""); //CAMCO Investment Management SA
        IDMS_TO_MS.put("1092229803.0", "Capital International"); //Capital International Fund
        IDMS_TO_MS.put("291", "Carl Spängler KAG mbH"); //Carl Spaengler Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("390", "CARLSON Fund Management Company SA");
        IDMS_TO_MS.put("1110538275.0", "Carmignac Gestion"); //Carmignac Gestion SA
        IDMS_TO_MS.put("1184091071.0", "Carnegie Fund Management Company S.A."); //Carnegie Fund AB
        IDMS_TO_MS.put("1171912226.0", "Cazenove International Fund PLC");
        IDMS_TO_MS.put("61", ""); //Citigroup Asset Management
        IDMS_TO_MS.put("1013448039.0", "Clariden Leu AG"); //Clariden Leu (Lux)
        IDMS_TO_MS.put("1013448039.1", "Clariden Leu AG"); //Clariden Leu (Lux) I
        IDMS_TO_MS.put("1188843041.0", "CMI Asset Management (Luxembourg) SA");
        IDMS_TO_MS.put("64", "Comgest"); //Comgest SA
        IDMS_TO_MS.put("5", "cominvest Asset Management GmbH");
        IDMS_TO_MS.put("27", "cominvest Asset Management S.A."); //cominvest Asset Management SA
        IDMS_TO_MS.put("290", "cominvest"); //cominvest SA
        IDMS_TO_MS.put("65", "Commerz Real Investment GmbH"); //Commerz Grundbesitz-Investment GmbH
        IDMS_TO_MS.put("1092229798.5", "Coutts Fund Managers Ltd</key>"); //Coutts & Co Fund Managers Ltd.
        IDMS_TO_MS.put("381", "Crédit Agricole"); //Credit Agricole Luxembourg SA
        IDMS_TO_MS.put("1186665096.0", "Credit Suisse Fund Management S.A."); //Credit Suisse Asset Management Fund Service (Lux)
        IDMS_TO_MS.put("378", "Credit Suisse Asset Management KAG mbH"); //Credit Suisse Asset Mgmt. KAG mbH
        IDMS_TO_MS.put("1048159432.0", "Crystal Fund Management AG");
        IDMS_TO_MS.put("1163686252.0", "Danske Fund Management Company SA");
        IDMS_TO_MS.put("77", "Davis Funds SICAV"); //Davis Selected Advisors LP
        IDMS_TO_MS.put("304", "DB Platinum Advisors");
        IDMS_TO_MS.put("1169579453.0", "db x-trackers Team"); //db x-trackers
        IDMS_TO_MS.put("82", "DEGI Dt. Gesellschaft f. Immobilienfonds"); //DEGI Deutsche Gesellschaft für Immobilienfonds mbH
        IDMS_TO_MS.put("84", "Deka Immobilien Investment GmbH");
        IDMS_TO_MS.put("280", "Deka International S.A."); //Deka International SA
        IDMS_TO_MS.put("83", "Deka Investment GmbH");
        IDMS_TO_MS.put("995551842.0", "Delta Lloyd Asset Management N.V."); //Delta Lloyd Investment Managers GmbH
        IDMS_TO_MS.put("373", "Deutsche Asset Management Investment-GmbH");
        IDMS_TO_MS.put("87", "Deutsche Postbank Privat Inv. KAG mbH"); //Deutsche Postbank Privat Investment KAG mbH
        IDMS_TO_MS.put("505", "Deutsche Postbnk Vermögens-Managem S.A"); //Deutsche Postbank Vermoegens-Management SA
        IDMS_TO_MS.put("100", ""); //Deutsche Vermoegensbildungsgesellschaft mbH
        IDMS_TO_MS.put("99", "Dexia Asset Management"); //Dexia Asset Management SA
        IDMS_TO_MS.put("1047051683.0", "DJE Investment SA");
        IDMS_TO_MS.put("1015241660.0", "DWS Investment GmbH");
        IDMS_TO_MS.put("238", "DWS Investment SA");
        IDMS_TO_MS.put("1200468555.0", "Edmond de Rothschild Asset Management"); //Edmond de Rothschild Fund SICAV
        IDMS_TO_MS.put("1044549172.0", "Ennismore Fund Management Limited"); //Ennismore Smaller Companies plc
        IDMS_TO_MS.put("306", "Erste Sparinvest KAG"); //Erste Sparinvest Kapitalanlagesellschaft mbH
        IDMS_TO_MS.put("452", ""); //F&C Portfolios Fund Sicav
        IDMS_TO_MS.put("1008666160.0", "Federated International Management Ltd");
        IDMS_TO_MS.put("1109157063.0", "Fidelity (FIL Investments International)"); //Fidelity Investment Management GmbH
        IDMS_TO_MS.put("111", "Fidelity (FIL (Luxembourg) S.A.)"); //Fidelity Investments Luxembourg SA
        IDMS_TO_MS.put("1174983060.0", "Financière de l'Echiquier"); //FINANCIERE DE L'ECHIQUIER SA
        IDMS_TO_MS.put("387", "Finter Fund Management Company SA");
        IDMS_TO_MS.put("1056357475.0", "First Private Investment Management KAG mbH");
        IDMS_TO_MS.put("1051084573.0", "First State Investments (UK) Ltd"); //First State Investment Ltd.
        IDMS_TO_MS.put("1170079867.0", "FISCH FUND SERVICES AG");
        IDMS_TO_MS.put("118", "Fonds Direkt Aktiengesellschaft"); //Fonds Direkt Sicav
        IDMS_TO_MS.put("1050413329.0", ""); //Fortis Investment Management Luxembourg SA
        IDMS_TO_MS.put("120", "FRANKFURT-TRUST Invest Luxemburg AG"); //Frankfurt-Trust Invest Luxemburg AG
        IDMS_TO_MS.put("1042470625.0", "FRANKFURT-TRUST Investment-GmbH"); //FRANKFURT-TRUST Investment-Gesellschaft mbH
        IDMS_TO_MS.put("1042470625.0", "Frankfurter Service Kapitalanlage-Gesellschaft mbH");
        IDMS_TO_MS.put("214", "Franklin Templeton Investment Funds"); //Franklin Templeton Investments (Luxembourg)
        IDMS_TO_MS.put("1092229825.6", ""); //Fund-Market Fund Management S.A.
        IDMS_TO_MS.put("302", "GAM Anlagefonds AG"); //GAM Star Fund plc
        IDMS_TO_MS.put("338", "GAMAX Management AG");
        IDMS_TO_MS.put("1108120243.0", "Gartmore SICAV");
        IDMS_TO_MS.put("1174932635.0", ""); //Gebser & Partner AG
        IDMS_TO_MS.put("1135098635.0", "Generali Investments Luxembourg S.A."); //Generali Asset Managers Luxembourg SA
        IDMS_TO_MS.put("421", "GLG Partners LP"); //GLG Partners Asset Management Ltd
        IDMS_TO_MS.put("1144138251.0", "Global Fund Selection SICAV");
        IDMS_TO_MS.put("131", "Goldman Sachs Asset Mngmt Intl"); //Goldman Sachs Funds Plc
        IDMS_TO_MS.put("1092229825.5", "Green Effects Investment plc.");
        IDMS_TO_MS.put("1005743877.0", "Griffin Capital Management Limited");
        IDMS_TO_MS.put("972638513.0", "Gutmann Kapitalanlageaktiengesellschaft");
        IDMS_TO_MS.put("1092229826.3", "Gutzwiller Fonds Management AG");
        IDMS_TO_MS.put("320", "HANSA-Nord-Lux Managementgesellschaft");
        IDMS_TO_MS.put("136", "HANSAINVEST - Hanseatische Inv. GmbH"); //HANSAINVEST Hanseatische Investment-GmbH
        IDMS_TO_MS.put("1149321784.0", ""); //Hasenbichler Investment AG mvK
        IDMS_TO_MS.put("428", "Hauck & Aufhäuser Inv. Gesellschaft S.A."); //Hauck & Aufhaeuser Investment Gesellschaft SA
        IDMS_TO_MS.put("395", "HelabaInvest KAG mbH"); //Helaba Invest Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("143", ""); //Henderson Horizon Fund SICAV
        IDMS_TO_MS.put("154", "HSBC Investment Funds (Luxembourg) S.A."); //HSBC Global Investment Funds SICAV
        IDMS_TO_MS.put("299", "HSBC Trinkaus Investment Managers S.A.");
        IDMS_TO_MS.put("10002", "Hypo-Kapitalanlage Gesellschaft mbH");
        IDMS_TO_MS.put("1092229824.4", "IFM Independent Fund Management AG");
        IDMS_TO_MS.put("1054026902.0", "IFOS Internationale Fonds Service AG");
        IDMS_TO_MS.put("157", "ING Investment Management Luxembourg");
        IDMS_TO_MS.put("1010571649.0", "International Fund Management S.A.");
        IDMS_TO_MS.put("149", "Internationale Kapitalanlage GmbH"); //Internationale Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("1092229828.3", "INVESCO Asset Management Ireland Ltd");
        IDMS_TO_MS.put("150", "INVESCO Asset Management SA"); //INVESCO Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("1022586038.0", "IPConcept Fund Management S.A."); //IPConcept Fund Management SA
        IDMS_TO_MS.put("448", "Janus Capital Funds Plc");
        IDMS_TO_MS.put("1013449539.0", "Jefferies Switzerland Ltd"); //Jefferies Umbrella Fund
        IDMS_TO_MS.put("1177351843.0", "JO Hambro Capital Management Limited");
        IDMS_TO_MS.put("430", "JPMorgan Asset Management (Europe) Sarl");
        IDMS_TO_MS.put("1165932622.0", "Julius Baer (Luxembourg) SA");
        IDMS_TO_MS.put("10005", "Julius Meinl Investmentgesellschaft mbH");
        IDMS_TO_MS.put("1028543388.0", "Jupiter Asset Management Ltd."); //Jupiter Global SICAV
        IDMS_TO_MS.put("1184157566.0", "Jyske Invest International Investeringsforening");
        IDMS_TO_MS.put("1058875724.0", "KanAm Grund KAG mbH"); //KanAm Grund Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("1003227165.0", "KBC Asset Management SA");
        IDMS_TO_MS.put("10014", "KEPLER-FONDS KAG"); //Kepler-Fonds Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("1092229823.9", "Kredietrust Luxembourg SA");
        IDMS_TO_MS.put("23", "Landesbank Berlin Investment GmbH");
        IDMS_TO_MS.put("442", "Lazard Asset Management (Deutschland) GmbH");
        IDMS_TO_MS.put("19", "LB(Swiss) Investment Fund SICAV");
        IDMS_TO_MS.put("1092229826.0", "Lemanik Asset Management SA"); //LEMANIK SICAV
        IDMS_TO_MS.put("54048.5", "LGT Capital Management AG");
        IDMS_TO_MS.put("1155319835.0", "Living Planet Fund Management Company S.A.");
        IDMS_TO_MS.put("1212125065.0", "LLB Fund Services AG");
        IDMS_TO_MS.put("1158066626.0", "Löwenfonds AG"); //Loewenfonds AG
        IDMS_TO_MS.put("200", "Lombard Odier Darier Hentsch Invest");
        IDMS_TO_MS.put("380", "LRI Invest SA");
        IDMS_TO_MS.put("986395899.0", "Lupus alpha Investment SA");
        IDMS_TO_MS.put("1184157297.0", "Lupus alpha KAGmbH"); //Lupus alpha Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("391", "Luxemburger Kapitalanlagegesellschaft SA");
        IDMS_TO_MS.put("1047053393.0", "Lyxor International Asset Management SA");
        IDMS_TO_MS.put("1031126919.0", "M&G Group"); //M&G Investments
        IDMS_TO_MS.put("1048677234.0", "MainFirst SICAV Luxembourg"); //MainFirst Bank AG
        IDMS_TO_MS.put("161", "Maintrust KAG"); //MAINTRUST Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("1092229816.2", "Martin Currie Investment Mngmt Ltd"); //Martin Currie Global Funds SICAV
        IDMS_TO_MS.put("228", "MEAG Munich Ergo KAG"); //MEAG MUNICH ERGO Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("1142604634.0", "Mediolanum International Funds Ltd");
        IDMS_TO_MS.put("1027415971.0", ""); //Mellon Global Funds plc
        IDMS_TO_MS.put("1192039827.0", "Merrill Lynch"); //Merrill Lynch Invest SAS
        IDMS_TO_MS.put("169", "Metzler Investment GmbH");
        IDMS_TO_MS.put("385", "Metzler Ireland Ltd");
        IDMS_TO_MS.put("170", "MFS Meridian Funds"); //MFS Meridian Funds SICAV
        IDMS_TO_MS.put("372", "MK Luxinvest SA");
        IDMS_TO_MS.put("323", "MM Warburg-Luxinvest SA");
        IDMS_TO_MS.put("983872860.0", "Monega Kapitalanlagegesellschaft mbH");
        IDMS_TO_MS.put("1184157714.0", "Morgan Stanley Real Estate Investment GmbH");
        IDMS_TO_MS.put("177", "Morgan Stanley"); //Morgan Stanley SICAV
        IDMS_TO_MS.put("1179234648.0", "mperical Asset Management (Ireland) Limited");
        IDMS_TO_MS.put("1182863436.0", "Muzinich & Co. Ltd."); //Muzinich & Co (Ireland) Limited
        IDMS_TO_MS.put("217", ""); //Münchner Kapitalanlage AG
        IDMS_TO_MS.put("314", "NESTOR Investment Management S.A.");
        IDMS_TO_MS.put("1028646243.0", "NORAMCO Asset Management SA");
        IDMS_TO_MS.put("123", "Nordea-1 SICAV"); //Nordea 1, SICAV
        IDMS_TO_MS.put("123", "Nordea Fund of Fund SICAV");
        IDMS_TO_MS.put("980761969.0", ""); //NORDINVEST Norddeutsche Investment-Gesellschaft
        IDMS_TO_MS.put("1144233204.0", "Oppenheim Asset Management Services Sarl");
        IDMS_TO_MS.put("322", "Oppenheim Kapitalanlagegesellschaft mbH");
        IDMS_TO_MS.put("509", "Oyster Asset Management S.A."); //Oyster SICAV
        IDMS_TO_MS.put("1184157687.0", "Pall Mall Investment Management Limited");
        IDMS_TO_MS.put("1111072262.0", ""); //PARGESFONDS
        IDMS_TO_MS.put("187", "PEH Wertpapier AG"); //PEH SICAV
        IDMS_TO_MS.put("188", "Performa Fund SICAV"); //Performa Fund
        IDMS_TO_MS.put("190", "Pictet Funds (Europe) SA");
        IDMS_TO_MS.put("1147871473.0", "Pioneer Asset Management SA");
        IDMS_TO_MS.put("1004", "Pioneer Investments Austria GmbH");
        IDMS_TO_MS.put("375", "Pioneer Investments KAG mbH"); //Pioneer Investments Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("1162303853.0", "Polar Capital Partners Limited"); //Polar Capital Fund Plc
        IDMS_TO_MS.put("1124178855.0", "Prima Management AG");
        IDMS_TO_MS.put("971185466.0", "Principal Global Investors (Ireland) Limited");
        IDMS_TO_MS.put("1092229797.2", ""); //Pro Fonds (Lux)
        IDMS_TO_MS.put("995442487.0", "Putnam Investments Limited");
        IDMS_TO_MS.put("10024", "Raiffeisen Kapitalanlage-Gesellschaft mbH");
        IDMS_TO_MS.put("10010", "Raiffeisen Salzburg Invest Kapitalanlage GmbH");
        IDMS_TO_MS.put("10012", "Ringturm Kapitalanlagegesellschaft mbH");
        IDMS_TO_MS.put("245", "RMF Asset Management AG"); //RMF Fund SICAV
        IDMS_TO_MS.put("243", "Robeco Luxembourg SA");
        IDMS_TO_MS.put("1100718682.0", "RREEF Investment GmbH");
        IDMS_TO_MS.put("249", "Sarasin Investmentfonds AG"); //Sarasin Multi Label SICAV
        IDMS_TO_MS.put("436", "Sauren Fonds-Select SICAV");
        IDMS_TO_MS.put("360", "Schoellerbank Invest AG");
        IDMS_TO_MS.put("20", "Schroder Investment Mgmt (Luxembourg)"); //Schroder Investment Management (Luxembourg) SA
        IDMS_TO_MS.put("332", "SEB Asset Management SA");
        IDMS_TO_MS.put("285", "SEB Fund Services SA");
        IDMS_TO_MS.put("34", "SEB"); //SEB Immobilien-Investment GmbH
        IDMS_TO_MS.put("1059397137.0", "SE"); //SEB Invest GmbH
        IDMS_TO_MS.put("10011", "Security Kapitalanlage Aktiengesellschaft");
        IDMS_TO_MS.put("202", "Seligman Global Horizon Funds");
        IDMS_TO_MS.put("1092229820.3", "SG Russell Assett Management"); //SG/Russell Asset Management Limited
        IDMS_TO_MS.put("256", "SGAM Luxembourg SA");
        IDMS_TO_MS.put("1189447853.0", "SIA Funds AG");
        IDMS_TO_MS.put("204", "Siemens Kapitalanlagegesellschaft mbH");
        IDMS_TO_MS.put("1028711683.0", "Skandia"); //Skandia Global Funds plc
        IDMS_TO_MS.put("1124806238.0", "Sparinvest Luxembourg"); //Sparinvest SA
        IDMS_TO_MS.put("1092229826.5", "StarCapital AG");
        IDMS_TO_MS.put("990186222.0", "State Street Global Advisors France"); //State Street Global Advisors France SA
        IDMS_TO_MS.put("262", "Swiss Life Funds (Lux)");
        IDMS_TO_MS.put("349", ""); //Swiss Re Funds Management (Ireland) Ltd.
        IDMS_TO_MS.put("287", "Swisscanto Fondsleitung AG (Switzerland)");
        IDMS_TO_MS.put("1092229825.3", "T. Rowe Price"); //T Rowe Price Funds SICAV
        IDMS_TO_MS.put("1180685446.0", "TheFund AG");
        IDMS_TO_MS.put("446", "Threadneedle Investment Services Limited");
        IDMS_TO_MS.put("1124964658.0", "TMW Pramerica Property Investment GmbH");
        IDMS_TO_MS.put("1092229800.1", "Tweedy Browne Co Llc"); //Tweedy Browne Value Funds SICAV
        IDMS_TO_MS.put("1117443227.0", ""); //Türkisfund SICAV
        IDMS_TO_MS.put("1026205973.0", "UBS Fund Services (Luxembourg) SA");
        IDMS_TO_MS.put("1134393041.0", "UBS Real Estate Kapitalanlagegesellschaft mbH");
        IDMS_TO_MS.put("282", "Union Investment Luxembourg S.A."); //Union Investment Luxembourg SA
        IDMS_TO_MS.put("223", "Union Investment Privatfonds GmbH");
        IDMS_TO_MS.put("1174486247.0", "Union Investment Real Estate AG");
        IDMS_TO_MS.put("271", "Universal-Investment-Gesellschaft mbH");
        IDMS_TO_MS.put("227", "Veritas Investment Trust GmbH"); //Veritas SG Investment Trust GmbH
        IDMS_TO_MS.put("971957096.0", "Volksbank Invest KAG"); //Volksbanken Invest Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("230", "Vontobel Management SA");
        IDMS_TO_MS.put("1133356228.0", "W&W Asset Management AG"); //W&W Asset Management AG Luxemburg
        IDMS_TO_MS.put("331", "W & W Asset Management Dublin Limited"); //W&W Asset Management Dublin Limited
        IDMS_TO_MS.put("1092229825.4", "Wanger Investment Company Plc");
        IDMS_TO_MS.put("174", "Warburg Invest"); //Warburg Invest Kapitalanlagegesellschaft mbH
        IDMS_TO_MS.put("232", "WestInvest mbH"); //WestInvest Gesellschaft fuer Investmentfonds mbH
        IDMS_TO_MS.put("234", "WestLB Asset Management KAG"); //WestLB Mellon Asset Management (Luxembourg) SA
        IDMS_TO_MS.put("1092229834.1", "WestLB Mellon Asset Management (UK) Ltd"); //WestLB Mellon Asset Management KAG mbH
        IDMS_TO_MS.put("1192471854.0", "William Blair SICAV");
        IDMS_TO_MS.put("67841.5", ""); //Worldwide Investors Portfolio SICAV
        IDMS_TO_MS.put("1008091001.0", "WWK Investment SA");

        IDMS_TO_VF.putAll(IDMS_TO_MS);
        IDMS_TO_VF.put("1015241660.0", "Deutsche Asset & Wealth Management Investment S.A.");
        IDMS_TO_VF.put("1031126919.0", "M&G Securities Limited");
        IDMS_TO_VF.put("1047051683.0", "DJE Investment S.A.");
        IDMS_TO_VF.put("1109157063.0", "FIL Investment Management GmbH");
        IDMS_TO_VF.put("1169579453.0", "db x-trackers Team"); // db x-trackers Team // (nur DWS ETFs)
        IDMS_TO_VF.put("123", "Nordea Investment Funds S.A.");
        IDMS_TO_VF.put("165", "BlackRock Asset Management Deutschland");
        IDMS_TO_VF.put("214", ""); // Franklin Templeton Investment Funds // missing in vf
        IDMS_TO_VF.put("238", "Deutsche Asset & Wealth Management Investment S.A.");
        IDMS_TO_VF.put("280", "Deka International S.A.");
        IDMS_TO_VF.put("282", "Union Investment Privatfonds GmbH");
        IDMS_TO_VF.put("971957096.0", "Volksbank Invest Kapitalanlagegesellschaft m.b.H.");
    }

    public void setResource(Resource resource) {
    }

    protected String getIssuername(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final boolean isVwd = profile != null && profile.isAllowed(Selector.VWD_FUND_DATA);

        // s is an IDMS key
        final String vwdKey;
        if (isVwd) {
            vwdKey = IDMS_TO_VF.get(s);
        } else {
            vwdKey = IDMS_TO_MS.get(s);
        }
        if (!StringUtils.hasText(vwdKey)) {
            this.logger.warn("<getIssuername> no mapping for IDMS kag id " + s);
            return "___nohit___";
        }
        return vwdKey;
    }

    /* hack for matching fund names of IDMS to vwd/MS/Feri */
    /*
    public static void main(String[] args) throws Exception {
        final Map<String, String> name2idms = new HashMap<String, String>();
        final Map<String, String> vwd2idms = new HashMap<String, String>();
        final Scanner scanner = new Scanner(new File("d:/temp/kags.txt"));
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] strings = line.split("\t");
            if (strings.length < 3) {
                continue;
            }
            name2idms.put(strings[0].trim(), strings[2].trim());
            vwd2idms.put(strings[1].trim(), strings[2].trim());
        }
        scanner.close();


        final KagidIDMSHandlerInterceptor interceptor = new KagidIDMSHandlerInterceptor();
        interceptor.setResource(new UrlResource("http://vwddev.market-maker.de/dmxml-1/iview/retrieve.xml?authentication=dzbank-vrbp&authenticationType=resource&atoms=FND_FinderMetadata"));
        interceptor.refresh();

        final Map<String, String> result = new TreeMap<String, String>();

        int found = 0;
        int notFound = 0;
        final Map<Integer, String> map = interceptor.issuer.get();
        for (final Map.Entry<String, String> entry : name2idms.entrySet()) {
            final String oldName = entry.getKey();

            final String s = getFullname(oldName, map);
            if (s != null) {
                print(result, oldName, entry.getValue(), null);
                found++;
            }
            else {
                final String oldNorm = normalize(oldName);

                String s2 = null;
                for (String msName : map.values()) {
                    final String msNorm = normalize(msName);
                    if (oldNorm.equals(msNorm)) {
                        if (s2 != null) {
//                            System.out.println("ALARM " + oldName + " " + msName);
//                            System.exit(0);
                            s2 = null;
                            break;
                        }
                        s2 = oldName;
                    }
                }

                if (s2 != null) {
                    print(result, s2, entry.getValue(), null);
                    found++;
                }
                else {
                    print(result, "", entry.getValue(), oldName);
                    notFound++;
                }
            }
        }

        for (String s : result.values()) {
            System.out.println(s);
        }

        System.out.println("found: " + found + ", not-found: " + notFound);
    }

    private static void print(Map<String, String> result, String name, final String idms,
            String comment) {
        String value = "IDMS_TO_VWD.put(\"" + idms + "\", \"" + name + "\");";
        if (comment != null) {
            value += " //" + comment;
        }

        String key = "".equals(name) ? comment : name;
        result.put(key.toLowerCase(), value);
    }

    private static String getFullname(String oldName, Map<Integer, String> map) {
        for (String s : map.values()) {
            if (s.equals(oldName)) {
                return s;
            }
        }
        return null;
    }

    private static String normalize(String msName) {
        final String s = msName.toLowerCase().replaceAll("\\s", "").replaceAll("\\.", "");
        if (s.length() < 7) {
            return s;
        }
        return s.substring(2, Math.min(s.length() - 4, 15));
    }
    */
}