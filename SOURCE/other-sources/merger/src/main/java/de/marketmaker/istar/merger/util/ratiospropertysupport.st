ratiospropertysupport(instrumentfields, quotefields, type) ::= <<
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class RatiosPropertySupport$type$ {
    private static PropertySupport[] qps = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    private static PropertySupport[] ips = new PropertySupport[RatioFieldDescription.getMaxFieldId() + 1];

    public static PropertySupport forQuote(int fieldid) {
        return qps[fieldid];
    }

    public static PropertySupport forInstrument(int fieldid) {
        return ips[fieldid];
    }

    static {

$instrumentfields:{field|
        ips[$field.fieldid$] = new PropertySupport<InstrumentRatios$type$>() {
$if(field.numeric)$
            public void set(InstrumentRatios$type$ ratios, $field.type$ value) {
                ratios.set$field.methodName$(value);
            $\u007D$

            public $field.type$ get$field.typeMethodName$(InstrumentRatios$type$ ratios) {
                return ratios.get$field.methodName$();
            $\u007D$
$else$
            public void set(InstrumentRatios$type$ ratios, int localeIndex, $field.type$ value) {
$if(field.localized)$
                ratios.set$field.methodName$(localeIndex, value);
$else$
                ratios.set$field.methodName$(value);
$endif$
            $\u007D$

            public $field.type$ get$field.typeMethodName$(InstrumentRatios$type$ ratios, int localeIndex) {
$if(field.localized)$
                return ratios.get$field.methodName$(localeIndex);
$else$
                return ratios.get$field.methodName$();
$endif$
            $\u007D$
$endif$
        $\u007D$;
}$
$quotefields:{field|
        qps[$field.fieldid$] = new PropertySupport<QuoteRatios$type$>() {
$if(field.numeric)$
            public void set(QuoteRatios$type$ ratios, $field.type$ value) {
                ratios.set$field.methodName$(value);
            $\u007D$

            public $field.type$ get$field.typeMethodName$(QuoteRatios$type$ ratios) {
                return ratios.get$field.methodName$();
            $\u007D$
$else$
            public void set(QuoteRatios$type$ ratios, int localeIndex, $field.type$ value) {
$if(field.localized)$
                ratios.set$field.methodName$(localeIndex, value);
$else$
                ratios.set$field.methodName$(value);
$endif$
            $\u007D$

            public $field.type$ get$field.typeMethodName$(QuoteRatios$type$ ratios, int localeIndex) {
$if(field.localized)$
                return ratios.get$field.methodName$(localeIndex);
$else$
                return ratios.get$field.methodName$();
$endif$
            $\u007D$
$endif$
        $\u007D$;
}$
    }
}
>>
