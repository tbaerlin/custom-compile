package de.marketmaker.istar.merger.web.easytrade;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;

/**
 * A generic Editor for simple enum types.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EnumEditor<E extends Enum<E>> extends PropertyEditorSupport {
    private final Class<E> elementType;

    private final boolean uppercase;

    /**
     * Registers a custom editor for objects of type E on binder by calling
     * {@link org.springframework.web.bind.ServletRequestDataBinder#registerCustomEditor(Class, java.beans.PropertyEditor)}
     * @param c class of objects this editor can handle
     * @param binder registration target
     * @param <E> an Enum class
     */
    public static <E extends Enum<E>> void register(Class<E> c, ServletRequestDataBinder binder) {
        binder.registerCustomEditor(c, create(c));
    }

    public static <E extends Enum<E>> void register(Class<E> c, boolean uppercase,
            ServletRequestDataBinder binder) {
        binder.registerCustomEditor(c, create(c, uppercase));
    }

    public static <E extends Enum<E>> EnumEditor<E> create(Class<E> c) {
        return new EnumEditor<>(c);
    }

    public static <E extends Enum<E>> EnumEditor<E> create(Class<E> c, boolean uppercase) {
        return new EnumEditor<>(c, uppercase);
    }

    public EnumEditor(Class<E> elementType) {
        this(elementType, true);
    }

    public EnumEditor(Class<E> elementType, boolean uppercase) {
        this.elementType = elementType;
        this.uppercase = uppercase;
    }

    public void setAsText(String text) {
        if (!StringUtils.hasText(text)) {
            return;
        }

        setValue(Enum.valueOf(this.elementType, this.uppercase ? text.toUpperCase() : text));
    }

    public String getAsText() {
        final E value = (E) getValue();
        if (value == null) {
            return null;
        }
        return value.name();
    }
}