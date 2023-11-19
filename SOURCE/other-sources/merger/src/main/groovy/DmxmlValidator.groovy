import javax.xml.transform.stream.StreamSource
import javax.xml.XMLConstants
import javax.xml.validation.SchemaFactory
import org.xml.sax.SAXParseException
/*
 * Dmxml.java
 *
 * Created on 10.01.12 13:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

/**
 *
 * @author oflege
 */

public class DmxmlValidator {
    def schemas = [:]
    def factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    
    def baseUrl = "http://lap37.market-maker.de/dmxml-1"

    public static void main(String[] args) {
        def cli = new CliBuilder(usage:'DmxmlValidator [options] [zones]')
        cli.u(longOpt:'baseurl', args:1, argName:'url', 'baseUrl ending in "dmxml-1"')
        def options = cli.parse(args)

        def dmxml = new DmxmlValidator()
        if (options.baseurl) dmxml.baseUrl = options.baseurl;
        for (String zone: options.arguments()) {
            dmxml.validate(zone)
        }
    }

    def validate(zone) {
        println "--------------${zone}------------------"
        def numErrs = 0
        new URL("${baseUrl}/${zone}/test-urls.txt").eachLine {line ->
            line = line.replace("authenticationType=&", "authenticationType=resource&")
            line = line.replace("newsid=157w94smcpz5d", "newsid=1ffhag4lpxaim")
            line = line.replace("Xun=abcd&", "")
            line = line.replace("../", "") // for postbank

            def answer = new URL(line).getText()
            def matcher = answer =~ /xsi:noNamespaceSchemaLocation="([^"]+)/
            def String schemaName = matcher[0][1]

            if (!schemas[schemaName]) {
                def schemaUrl = schemaName.startsWith("http") \
                    ? new URL(schemaName) : new URL("${baseUrl}/${schemaName}")
                println "loading ${schemaName}"
                def schema = factory.newSchema(new StreamSource(new StringReader(schemaUrl.getText())))
                schemas[schemaName] = schema
            }
            def validator = schemas[schemaName].newValidator()
            try {
                validator.validate(new StreamSource(new StringReader(answer)))
                println " OK   ${line}"
            } catch (SAXParseException e) {
                println " FAIL ${line}"
                println e.getMessage()
                println ""
                numErrs++
            }
        }
        println "numErrs = ${numErrs}"
        println "--------------${zone}------------------"
    }
}

