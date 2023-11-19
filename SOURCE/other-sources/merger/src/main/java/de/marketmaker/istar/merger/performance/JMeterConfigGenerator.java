/*
 * JMeterConfigGenerator.java
 *
 * Created on 27.09.2006 21:10:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.performance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Martin Wilke
 */

public class JMeterConfigGenerator {

    /*
    Reads four files pre.jmxx, loop.jmxx and post.jmxx and http://vwddev.market-maker.de/pb1/sample-vwddev-form.html
    loop.jmxx will be iterated with every line of the url containing "request", extracting name and argumentValue
    for a thread in JMeter
    name is the atoms value from the url and argumentValue is the XML request String

    */


    // pre2, loop2 and post2 will ersult in ONE Thread Group per request
    // pre, loop and post will do one Thread group for all requests

    String pre = "d:/tmp/pre.jmxx";

    String loop = "d:/tmp/loop.pb.jmxx";

    String post = "d:/tmp/post.jmxx";

//    String result = "d:/tmp/pb1_vwddev_all.jmx";
//    String result = "d:/tmp/pb1_pb_all2.jmx";
    String result = "d:/tmp/pb1_pb_with_real_qids.jmx";


    String requestFile = "d:/tmp/pb_requests__symbol__.txt";

//    String qidFile = "d:/tmp/qidWknPlatzAnzahl.bis1000.qid.txt";
    String qidFile = "d:/tmp/qidWknPlatzAnzahl.bis2000.qid.txt";

    String varName = "__name__";
    String varArgumentValue = "__argumentValue__";

    String varSymbol = "__symbol__";


    public static void main(String[] args) {
        JMeterConfigGenerator me = new JMeterConfigGenerator();
        me.work();
    }

    private void work() {

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(result));

            String preText = readFile(pre);
            String loopText = readFile(loop);
            String postText = readFile(post);
            String requestLine;
            String qidLine;

            bw.write(preText);
            int numThreads = 0;

            // ueber qid list
            final BufferedReader brq = new BufferedReader(new FileReader(qidFile));
            while ((qidLine = brq.readLine()) != null) {

                // ueber requests
                final BufferedReader br = new BufferedReader(new FileReader(requestFile));
                while ((requestLine = br.readLine()) != null) {
                    if (requestLine.indexOf("request") > -1) {

                        String argumentValue = requestLine;
                        System.out.println("argumentValue = " + argumentValue);
                        String[] split2 = requestLine.split("<");
                        String[] split3 = split2[5].split("\"");
                        String name = split3[1]+" "+qidLine;
                        System.out.println("name = " + name);

                        argumentValue = argumentValue.replaceAll("<", "&lt;");
                        argumentValue = argumentValue.replaceAll(">", "&gt;");
                        argumentValue = argumentValue.replaceAll("\"", "&quot;");
                        String resultString = loopText.replaceAll(varName, name).replaceAll(varArgumentValue, argumentValue).replaceAll(varSymbol, qidLine);
//                        System.out.println("resultString = " + resultString);

                        if (!name.startsWith("WL") && !name.startsWith("PF") && !name.startsWith("LT")) {
                            bw.write(resultString);
                            numThreads++;
                            System.out.println("wrote " + name);
                        }


                    }
                } // while
                br.close();
            }

            brq.close();

            bw.write(postText);
            bw.close();

            System.out.println("Wrote " + numThreads + " threads.");
            System.out.println("Please see " + result + " for the generated JMeter config");

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile(String fileName) throws IOException {
        final BufferedReader brLoop = new BufferedReader(new FileReader(fileName));
        String line;
        final StringBuilder loopBuilder = new StringBuilder();
        while ((line = brLoop.readLine()) != null) {
            loopBuilder.append(line).append("\r\n");
        }
        brLoop.close();
        return loopBuilder.toString();
    }


}
