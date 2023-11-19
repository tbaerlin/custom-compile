package de.marketmaker.istar.common.util;

import java.lang.reflect.Field;

/**
 * Class and Reflection related utiliy classes
 */
public class ClassUtil {

    private ClassUtil() {
    }

    /**
     * Get a static instance or instantiate a new class with the given name in format
     * <code>fullyQualifiedClassName[#STATIC_INSTANCE_NAME]</code>
     * @param name Name of Class or static instance
     * @param <V> Type
     * @return Requested object if available
     */
    public static <V> V getObject(String name) {
        if (name == null || name.chars().allMatch(Character::isWhitespace)) {
            return null;
        }
        final String[] classAndField = name.split("#");
        try {
            final Class<?> aClass = Class.forName(classAndField[0]);
            if (classAndField.length > 1) {
                final Field field = aClass.getField(classAndField[1]);
                //noinspection unchecked
                return (V) field.get(null);
            }
            //noinspection unchecked
            return (V) aClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(name, e);
        }
    }
}
