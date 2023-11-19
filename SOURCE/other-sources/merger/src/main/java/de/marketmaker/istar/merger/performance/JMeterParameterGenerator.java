/*
* JMeterParameterGenerator.java
*
* Created on 29.09.2006 15:45:49
*
* Copyright (c) market maker Software AG. All Rights Reserved.
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

public class JMeterParameterGenerator {

    String result = "d:/tmp/pb1_request_parameter.txt";

    String requestFile = "d:/tmp/pb_requests__symbol__.txt";

//    String qidFile = "d:/tmp/qidWknPlatzAnzahl.qid.txt";
    String qidFile = "d:/tmp/qidWknPlatzAnzahl.bis1000.qid.txt";
//    String qidFile = "d:/tmp/qidWknPlatzAnzahl.bis2000.qid.txt";

    String varSymbol = "__symbol__";


    public static void main(String[] args) {
        JMeterParameterGenerator me = new JMeterParameterGenerator();
        me.work();
    }

    private void work() {

        try {
            final BufferedWriter bw = new BufferedWriter(new FileWriter(result));

            String requestLine;
            String qidLine;

            int numRequests = 0;

            // ueber qid list
            final BufferedReader brq = new BufferedReader(new FileReader(qidFile));
            while ((qidLine = brq.readLine()) != null) {

                // ueber requests
                final BufferedReader br = new BufferedReader(new FileReader(requestFile));
                while ((requestLine = br.readLine()) != null) {

                    String resultString = requestLine.replaceAll(varSymbol, qidLine);
//                    System.out.println("resultString = " + resultString);

                    bw.write(resultString+"\n");
                    numRequests++;
                    if (numRequests % 1000 == 0) {
                        System.out.println("done " + numRequests);
                    }

                } // while
                br.close();
            }

            brq.close();
            
            bw.close();

            System.out.println("Wrote " + numRequests + " threads.");
            System.out.println("Please see " + result + " for the generated JMeter config");

        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }


    }


}
