package de.marketmaker.istar.merger.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseComparator {
    Class<?> sort() default Void.class;
    Class<?> filter() default Void.class;
}
