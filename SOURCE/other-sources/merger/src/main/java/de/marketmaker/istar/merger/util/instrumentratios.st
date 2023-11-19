instrumentratios(fields,enumfields,type,derivative) ::= <<
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.BitSet;

/**
 * Generated via <code>de.marketmaker.istar.merger.util.DataStructureGenerator</code>
 */
public class InstrumentRatios$type$ extends InstrumentRatios $if(derivative)$implements InstrumentRatiosDerivative $endif${
    protected static final long serialVersionUID = 1L;

    public InstrumentRatios$type$(long id) {
        super(id);
    }

    public QuoteRatios createQuoteRatios(long quoteid) {
        return new QuoteRatios$type$(quoteid, this);
    }

    public PropertySupport propertySupport(int fid) {
        return RatiosPropertySupport$type$.forInstrument(fid);
    }

$fields:{field|
$if(field.localized)$
    private $field.type$[] $field.name$ = $field.defaultValue$;

$else$
    private $field.type$ $field.name$ = $field.defaultValue$;

$endif$
}$
$fields:{field|
$if(field.localized)$
    public $field.type$ get$field.methodName$(int localeIndex) {
$if(field.deprecated)$
        ackDeprecatedFieldAccess($field.fieldid$);
$endif$
        if (this.$field.name$ == null) {
            return null;
        $\u007D$
        return this.$field.name$[localeIndex];
    $\u007D$

    public void set$field.methodName$(int localeIndex, $field.type$ value) {
        if (this.$field.name$ == null) {
            this.$field.name$ = new String[RatioFieldDescription.$field.name$.getLocales().length];
        $\u007D$
        this.$field.name$[localeIndex] = value;
    $\u007D$

$else$
    public $field.type$ get$field.methodName$() {
$if(field.deprecated)$
        ackDeprecatedFieldAccess($field.fieldid$);
$endif$
        return this.$field.name$;
    $\u007D$

    public void set$field.methodName$($field.type$ value) {
        this.$field.name$ = value;
    $\u007D$

$endif$
}$
    // Selectable Interface ----------------------------------------

    public String getString(int fieldid) {
        final PropertySupport ps = propertySupport(fieldid);
        return ps != null ? ps.getString(this) : null;
    }

    public String getString(int fieldid, int localeIndex) {
        final PropertySupport ps = propertySupport(fieldid);
        return ps != null ? ps.getString(this, localeIndex) : null;
    }

    public Boolean getBoolean(int fieldid) {
        final PropertySupport ps = propertySupport(fieldid);
        return ps != null && ps.getBoolean(this);
    }

    public Long getLong(int fieldid) {
        final PropertySupport ps = propertySupport(fieldid);
        final long result = ps != null ? ps.getLong(this) : Long.MIN_VALUE;
        return result == Long.MIN_VALUE ? null : result;
    }

    @Override
    public BitSet getBitSet(int fieldid) {
        final PropertySupport ps = propertySupport(fieldid);
        return ps != null ? ps.getBitSet(this) : null;
    }

    public Integer getInt(int fieldid) {
        final PropertySupport ps = propertySupport(fieldid);
        final int result = ps != null ? ps.getInt(this) : Integer.MIN_VALUE;
        return result == Integer.MIN_VALUE ? null : result;
    }

    // methods for access by fieldname (velocity) ----------------------------------------

    public String getString(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        if (field == null) {
            return null;
        }

        if (!field.isLocalized()) {
            return getString(field.id());
        }

        // TODO: use RequestContextHolder for locale (instead of null), problem is ratios package
        final int localeIndex = RatioFieldDescription.getLocaleIndex(field, null);
        return getString(field.id(), localeIndex);
    }

    public Boolean getBoolean(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        return field != null && getBoolean(field.id());
    }

    public Long getLong(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        return field != null ? getLong(field.id()) : null;
    }

    public BitSet getBitSet(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        return field != null ? getBitSet(field.id()) : null;
    }

    public Integer getInt(String fieldname) {
        final RatioFieldDescription.Field field = getFieldByName(fieldname);
        return field != null ? getInt(field.id()) : null;
    }

    // RatioUpdatable Interface ----------------------------------------

    public void set(int fieldid, int localeIndex, String value) {
        final PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            ps.set(this, localeIndex, value);
        }
    }

    public void set(int fieldid, boolean value) {
        final PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            ps.set(this, value);
        }
    }

    public void set(int fieldid, long value) {
        final PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            ps.set(this, value);
        }
    }

    @Override
    public void set(int fieldid, BitSet value) {
        final PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            ps.set(this, value);
        }
    }

    public void set(int fieldid, int value) {
        final PropertySupport ps = propertySupport(fieldid);
        if (ps != null) {
            ps.set(this, value);
        }
    }
}
>>
