/*
 * TreasurySelectorConsolidator.java
 *
 * Created on 22.07.13 17:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

/**
 * rsync -avz webadm@tools:produktion/var/data/sap/selector .
 *
 * @author tkiesgen
 */
class TreasurySelectorConsolidator {
  def static CUSTOMER_PREFIXES = ["sen", "constantia", "bosch"]
  def static selector2desc = [:]
  def static skipped = [] as Set

  public static void main(String[] args) {
    new XmlParser().parse("/Users/tkiesgen/tmp/selectors.xml").Sel.each { selector ->
      selector2desc.put(getSelector(selector.@selector), selector.@description)
    }

    def currentProfiles = [:]
    new File("/Users/tkiesgen/entwicklung/istar/domain/src/main/java/de/marketmaker/istar/domain/resources/resource-profiles.properties").each { line ->
      def String[] tokens = line.split("=")
      if (tokens.length == 2) {
        currentProfiles.put(tokens[0], tokens[1])
      }
    }
    println(currentProfiles)

    def customer2selectors = [:]

    new File("/Users/tkiesgen/tmp/reports/dm-treasury/selector").eachFile { file ->
      def filename = file.name
      String customer = getCustomer(filename)
      def selectors = customer2selectors.get(customer, [] as Set) as Set

      file.each { line ->
        if (isValid(line)) {
          selectors.add(line)
        }
      }
    }

    customer2selectors.each { entry ->
      def String key = "dmt-" + entry.key + ".PRICES_EOD"

      def List values = entry.value as List
      Collections.sort(values)
      def String los = values.toString()
      def String value = los.substring(1, los.length() - 1).replaceAll(" ", "")

      if (!currentProfiles.get(key).equals(value)) {
        println key + "=" + value
      }
    }
  }

  private static boolean isValid(String selector) {
    if (skipped.contains(selector)) {
      return false
    }

    def String description = selector2desc.get(selector)
    if (description == null) {
      throw new IllegalStateException("unknown selector: " + selector)
    }
    if (description.startsWith("Kennzahlen")) {
      println "skip " + selector + ": " + description
      skipped.add(selector)
      return false
    }
    return true
  }

  private static String getSelector(String selector) {
    if (selector.startsWith("0")) {
      return selector.substring(1)
    }
    return selector
  }

  private static String getCustomer(String filename) {
    def customer = filename.substring(0, filename.indexOf("_"))

//    if(true) return customer

    def prefixed = CUSTOMER_PREFIXES.find {
      if (customer.startsWith(it)) {
        return it
      }
    }
    if (prefixed) {
      return prefixed
    }

    if (customer.endsWith("-1") || customer.endsWith("-2")) {
      return customer.substring(0, customer.lastIndexOf("-"))
    }

    return customer
  }
}
